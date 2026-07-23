package com.example.homosep

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.net.URI
import java.util.Locale
import java.util.concurrent.Executors

object MqttGpsPublisherManager : SensorEventListener {

    data class State(
        val brokerUri: String = "",
        val topic: String = "devices/<device-name>/gps",
        val status: String = "Not connected",
        val locationSummary: String = "Waiting for GPS data...",
        val payload: String = "Payload:",
        val isConnected: Boolean = false,
        val isPublishing: Boolean = false,
        val receivedEvent: String = "Received Events: None"
    )

    interface Listener {
        fun onStateChanged(state: State)
    }

    private val mqttExecutor = Executors.newSingleThreadExecutor()
    private val listeners = mutableSetOf<Listener>()

    private var appContext: Context? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var mqttClient: MqttClient? = null
    private var latestLocation: Location? = null
    private var locationUpdatesStarted = false

    private var sensorManager: SensorManager? = null
    private var accelData = FloatArray(3)
    private var gyroData = FloatArray(3)
    private var magData = FloatArray(3)

    private var brokerUri: String = ""
    private var topic: String = "vehicles/<vehicle-id>/location"
    private var currentVehicleId: String = ""
    private var currentVehicleName: String = ""
    private var status: String = "Not connected"
    private var locationSummary: String = "Waiting for GPS data..."
    private var payload: String = "Payload:"
    private var receivedEvent: String = "Received Events: None"
    private var isPublishing: Boolean = false
    
    private var currentCleaningId: String = "not assigned"
    private var currentBoundaryId: String = "not assigned"

    fun initialize(context: Context) {
        if (appContext != null) return

        appContext = context.applicationContext
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context.applicationContext)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                latestLocation = location
                locationSummary = buildLocationSummary(location)
                payload = "Payload: ${buildGpsPayload(location)}"
                notifyListeners()

                if (isPublishing) {
                    publishLocation(location)
                }
            }
        }

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        sensorManager?.let { sm ->
            sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.also {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
        listener.onStateChanged(currentState())
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun updateConfig(vehicleNumber: String, vehicleId: String) {
        currentVehicleName = vehicleNumber.trim()
        currentVehicleId = vehicleId.trim()
        topic = if (currentVehicleId.isEmpty()) {
            "vehicles/<vehicle-id>/location"
        } else {
            "vehicles/${currentVehicleId}/location"
        }
        notifyListeners()
    }

    fun ensureLocationUpdates() {
        val context = appContext ?: return
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            startLocationUpdates()
        } else {
            status = "Location permission denied"
            notifyListeners()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (locationUpdatesStarted) return

        val client = fusedLocationClient ?: return
        val callback = locationCallback ?: return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        locationUpdatesStarted = true
    }

    private const val DEFAULT_BROKER_URI = "tcp://4.213.37.248:1883"
    private const val DEFAULT_USERNAME = "solinas"
    private const val DEFAULT_PASSWORD = "Solinas@123!@#"

    fun connect() {
        val uri = DEFAULT_BROKER_URI

        brokerUri = uri
        status = "Connecting to $uri"
        notifyListeners()

        mqttExecutor.execute {
            try {
                val existingClient = mqttClient
                if (existingClient?.isConnected == true) {
                    existingClient.disconnect()
                }

                val clientId = "homosep-${System.currentTimeMillis()}"
                val client = MqttClient(uri, clientId, MemoryPersistence())
                val options = MqttConnectOptions().apply {
                    isCleanSession = true
                    isAutomaticReconnect = true
                    connectionTimeout = 10
                    keepAliveInterval = 30
                    userName = DEFAULT_USERNAME
                    password = DEFAULT_PASSWORD.toCharArray()
                }

                client.setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable?) {
                        status = "Connection lost: ${cause?.message}"
                        notifyListeners()
                    }

                    override fun messageArrived(t: String?, message: MqttMessage?) {
                        val messageStr = message?.toString() ?: ""
                        if (t != null && t.endsWith("/assignment")) {
                            try {
                                val json = JSONObject(messageStr)
                                if (json.has("cleaningId")) {
                                    currentCleaningId = json.getString("cleaningId")
                                }
                                if (json.has("boundaryId")) {
                                    currentBoundaryId = json.getString("boundaryId")
                                }
                            } catch (e: Exception) {
                                // Ignore non-JSON parsing errors
                            }
                        } else {
                            receivedEvent = "Received Events:\n${messageStr.ifEmpty { "Empty" }}"
                        }
                        notifyListeners()
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                })
                client.connect(options)
                
                if (currentVehicleId.isNotEmpty()) {
                    client.subscribe("vehicles/$currentVehicleId/events")
                    client.subscribe("vehicles/$currentVehicleId/assignment")
                }
                
                mqttClient = client
                status = "Connected to $uri"
                notifyListeners()
            } catch (exception: Exception) {
                status = "MQTT connect failed: ${exception.message}"
                notifyListeners()
            }
        }
    }

    fun disconnect() {
        isPublishing = false
        mqttExecutor.execute {
            try {
                mqttClient?.let { client ->
                    if (client.isConnected) {
                        client.disconnect()
                    }
                    client.close()
                }
                mqttClient = null
                status = "Not connected"
                notifyListeners()
            } catch (exception: Exception) {
                status = "MQTT disconnect failed: ${exception.message}"
                notifyListeners()
            }
        }
    }

    fun startPublishing() {
        if (currentVehicleId.isEmpty()) {
            status = "Enter a vehicle ID"
            notifyListeners()
            return
        }

        isPublishing = true
        status = "Started publishing GPS"
        notifyListeners()

        val location = latestLocation
        if (location != null && mqttClient?.isConnected == true) {
            publishLocation(location)
        }
    }

    fun stopPublishing() {
        if (!isPublishing) return

        isPublishing = false
        status = "Stopped publishing GPS"
        notifyListeners()
    }

    private fun publishLocation(location: Location) {
        val publishTopic = topic
        val publishPayload = buildGpsPayload(location)
        payload = "Payload: $publishPayload"
        notifyListeners()

        mqttExecutor.execute {
            try {
                val client = mqttClient
                if (client?.isConnected != true) {
                    status = "Waiting for MQTT connection to publish"
                    notifyListeners()
                    return@execute
                }

                val message = MqttMessage(publishPayload.toByteArray(Charsets.UTF_8)).apply {
                    qos = 1
                    isRetained = false
                }
                client.publish(publishTopic, message)
                status = "Published to $publishTopic"
                notifyListeners()
            } catch (exception: Exception) {
                status = "MQTT publish failed: ${exception.message}"
                notifyListeners()
            }
        }
    }

    private fun currentState(): State {
        return State(
            brokerUri = brokerUri,
            topic = topic,
            status = status,
            locationSummary = locationSummary,
            payload = payload,
            isConnected = mqttClient?.isConnected == true,
            isPublishing = isPublishing,
            receivedEvent = receivedEvent
        )
    }

    private fun notifyListeners() {
        val state = currentState()
        listeners.toList().forEach { listener ->
            listener.onStateChanged(state)
        }
    }

    private fun buildBrokerUri(brokerInput: String, portInput: String): String {
        val broker = brokerInput.trim()
        val port = portInput.trim().toIntOrNull()
            ?: throw IllegalArgumentException("Enter a valid MQTT port")

        if (broker.isEmpty()) {
            throw IllegalArgumentException("Enter a broker IP or link")
        }

        val withScheme = if (broker.contains("://")) broker else "tcp://$broker"
        val uri = URI(withScheme)
        return if (uri.port == -1) {
            "${withScheme.trimEnd('/')}:$port"
        } else {
            withScheme
        }
    }

    private fun buildGpsPayload(location: Location): String {
        return JSONObject().apply {
            put("vehicleId", currentVehicleId)
            put("vehiclename", currentVehicleName)
            put("center", JSONObject().apply {
                put("longitude", location.longitude)
                put("latitude", location.latitude)
            })
            put("bearing", if (location.hasBearing()) location.bearing else 0.0f)
            put("speed", if (location.hasSpeed()) location.speed else 0.0f)
            put("wasteLevel", 75)
            put("assignedBoundaryId", currentBoundaryId)
            put("cleaningId", currentCleaningId)
            put("accelerometer", JSONObject().apply {
                put("x", accelData[0])
                put("y", accelData[1])
                put("z", accelData[2])
            })
            put("gyroscope", JSONObject().apply {
                put("x", gyroData[0])
                put("y", gyroData[1])
                put("z", gyroData[2])
            })
            put("magnetometer", JSONObject().apply {
                put("x", magData[0])
                put("y", magData[1])
                put("z", magData[2])
            })
        }.toString()
    }

    private fun buildLocationSummary(location: Location): String {
        return String.format(
            Locale.US,
            "Latest GPS: %.6f, %.6f | speed %.2f | accuracy %.1f | heading %.1f | altitude %.1f",
            location.latitude,
            location.longitude,
            if (location.hasSpeed()) location.speed else 0.0f,
            if (location.hasAccuracy()) location.accuracy else 0.0f,
            if (location.hasBearing()) location.bearing else 0.0f,
            if (location.hasAltitude()) location.altitude else 0.0
        )
    }
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelData[0] = event.values[0]
                accelData[1] = event.values[1]
                accelData[2] = event.values[2]
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroData[0] = event.values[0]
                gyroData[1] = event.values[1]
                gyroData[2] = event.values[2]
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magData[0] = event.values[0]
                magData[1] = event.values[1]
                magData[2] = event.values[2]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}
