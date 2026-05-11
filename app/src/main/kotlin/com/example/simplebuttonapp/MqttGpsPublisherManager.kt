package com.example.simplebuttonapp

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
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.net.URI
import java.util.Locale
import java.util.concurrent.Executors

object MqttGpsPublisherManager {

    data class State(
        val brokerUri: String = "",
        val topic: String = "devices/<device-name>/gps",
        val status: String = "Not connected",
        val locationSummary: String = "Waiting for GPS data...",
        val payload: String = "Payload:",
        val isConnected: Boolean = false,
        val isPublishing: Boolean = false
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

    private var brokerUri: String = ""
    private var topic: String = "devices/<device-name>/gps"
    private var status: String = "Not connected"
    private var locationSummary: String = "Waiting for GPS data..."
    private var payload: String = "Payload:"
    private var isPublishing: Boolean = false

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
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
        listener.onStateChanged(currentState())
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun updateTopic(deviceName: String) {
        topic = if (deviceName.trim().isEmpty()) {
            "devices/<device-name>/gps"
        } else {
            "devices/${deviceName.trim()}/gps"
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

    fun connect(brokerInput: String, portInput: String) {
        val uri = try {
            buildBrokerUri(brokerInput, portInput)
        } catch (exception: IllegalArgumentException) {
            status = exception.message ?: "Invalid MQTT broker"
            notifyListeners()
            return
        }

        brokerUri = uri
        status = "Connecting to $uri"
        notifyListeners()

        mqttExecutor.execute {
            try {
                val existingClient = mqttClient
                if (existingClient?.isConnected == true) {
                    existingClient.disconnect()
                }

                val clientId = "SimpleButtonApp-${System.currentTimeMillis()}"
                val client = MqttClient(uri, clientId, MemoryPersistence())
                val options = MqttConnectOptions().apply {
                    isCleanSession = true
                    isAutomaticReconnect = true
                    connectionTimeout = 10
                    keepAliveInterval = 30
                }

                client.connect(options)
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

    fun startPublishing(deviceName: String) {
        updateTopic(deviceName)

        if (topic == "devices/<device-name>/gps") {
            status = "Enter a device name"
            notifyListeners()
            return
        }

        if (mqttClient?.isConnected != true) {
            status = "Connect to an MQTT broker before publishing"
            notifyListeners()
            return
        }

        val location = latestLocation
        if (location == null) {
            status = "Waiting for GPS data..."
            notifyListeners()
            return
        }

        isPublishing = true
        status = "Started publishing GPS"
        notifyListeners()
        publishLocation(location)
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
                    isPublishing = false
                    status = "Connect to an MQTT broker before publishing"
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
                isPublishing = false
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
            isPublishing = isPublishing
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
        return JSONObject()
            .put("lat", location.latitude)
            .put("lng", location.longitude)
            .put("speed", if (location.hasSpeed()) location.speed else 0.0f)
            .put("accuracy", if (location.hasAccuracy()) location.accuracy else 0.0f)
            .put("heading", if (location.hasBearing()) location.bearing else 0.0f)
            .put("altitude", if (location.hasAltitude()) location.altitude else 0.0)
            .toString()
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
}
