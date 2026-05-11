package com.example.simplebuttonapp

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private lateinit var locationCallback: LocationCallback
    private var locationUpdatesStarted = false
    
    // GPS Data TextViews
    private lateinit var gpsStatusText: TextView
    private lateinit var latitudeText: TextView
    private lateinit var longitudeText: TextView
    private lateinit var accuracyText: TextView
    private lateinit var altitudeText: TextView

    // Accelerometer TextViews
    private lateinit var accelXText: TextView
    private lateinit var accelYText: TextView
    private lateinit var accelZText: TextView

    // Gyroscope TextViews
    private lateinit var gyroXText: TextView
    private lateinit var gyroYText: TextView
    private lateinit var gyroZText: TextView

    // Magnetometer TextViews
    private lateinit var magXText: TextView
    private lateinit var magYText: TextView
    private lateinit var magZText: TextView

    // Sensors
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private var magnetometerSensor: Sensor? = null

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize Sensor Manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Get sensors
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Initialize GPS TextViews
        gpsStatusText = findViewById(R.id.gpsStatusText)
        latitudeText = findViewById(R.id.latitudeText)
        longitudeText = findViewById(R.id.longitudeText)
        accuracyText = findViewById(R.id.accuracyText)
        altitudeText = findViewById(R.id.altitudeText)

        // Initialize Accelerometer TextViews
        accelXText = findViewById(R.id.accelXText)
        accelYText = findViewById(R.id.accelYText)
        accelZText = findViewById(R.id.accelZText)

        // Initialize Gyroscope TextViews
        gyroXText = findViewById(R.id.gyroXText)
        gyroYText = findViewById(R.id.gyroYText)
        gyroZText = findViewById(R.id.gyroZText)

        // Initialize Magnetometer TextViews
        magXText = findViewById(R.id.magXText)
        magYText = findViewById(R.id.magYText)
        magZText = findViewById(R.id.magZText)

        // Initialize Location Callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                if (location != null) {
                    latitudeText.text =
                        getString(R.string.latitude_format, location.latitude)
                    longitudeText.text =
                        getString(R.string.longitude_format, location.longitude)
                    accuracyText.text =
                        getString(R.string.accuracy_format, location.accuracy)
                    altitudeText.text =
                        getString(R.string.altitude_format, location.altitude)
                    gpsStatusText.text = getString(R.string.gps_found)
                }
            }
        }

        // Request GPS location
        requestLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        // Register sensor listeners
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        // Resume location updates
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        // Unregister sensor listeners to save battery
        sensorManager.unregisterListener(this)
        // Stop location updates
        stopLocationUpdates()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelXText.text = getString(R.string.sensor_value_format, "X", event.values[0])
                accelYText.text = getString(R.string.sensor_value_format, "Y", event.values[1])
                accelZText.text = getString(R.string.sensor_value_format, "Z", event.values[2])
            }

            Sensor.TYPE_GYROSCOPE -> {
                gyroXText.text = getString(R.string.sensor_value_format, "X", event.values[0])
                gyroYText.text = getString(R.string.sensor_value_format, "Y", event.values[1])
                gyroZText.text = getString(R.string.sensor_value_format, "Z", event.values[2])
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                magXText.text = getString(R.string.sensor_value_format, "X", event.values[0])
                magYText.text = getString(R.string.sensor_value_format, "Y", event.values[1])
                magZText.text = getString(R.string.sensor_value_format, "Z", event.values[2])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this app
    }

    private fun requestLocationUpdates() {
        // Check if permissions are granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Permissions already granted, start location updates
            startLocationUpdates()
        }
    }

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        gpsStatusText.text = getString(R.string.gps_searching)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        locationUpdatesStarted = true
    }

    private fun stopLocationUpdates() {
        if (locationUpdatesStarted) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            locationUpdatesStarted = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                startLocationUpdates()
            } else {
                // Permission denied
                gpsStatusText.text = getString(R.string.permission_denied)
                latitudeText.text = getString(R.string.no_data)
                longitudeText.text = getString(R.string.no_data)
                accuracyText.text = getString(R.string.no_data)
                altitudeText.text = getString(R.string.no_data)
            }
        }
    }
}