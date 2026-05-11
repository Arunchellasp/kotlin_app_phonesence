package com.example.simplebuttonapp

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class DashboardFragment : Fragment(), SensorEventListener {

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Initialize Sensor Manager
        sensorManager = requireContext().getSystemService(AppCompatActivity.SENSOR_SERVICE) as SensorManager

        // Get sensors
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // Initialize GPS TextViews
        gpsStatusText = view.findViewById(R.id.gpsStatusText)
        latitudeText = view.findViewById(R.id.latitudeText)
        longitudeText = view.findViewById(R.id.longitudeText)
        accuracyText = view.findViewById(R.id.accuracyText)
        altitudeText = view.findViewById(R.id.altitudeText)

        // Initialize Accelerometer TextViews
        accelXText = view.findViewById(R.id.accelXText)
        accelYText = view.findViewById(R.id.accelYText)
        accelZText = view.findViewById(R.id.accelZText)

        // Initialize Gyroscope TextViews
        gyroXText = view.findViewById(R.id.gyroXText)
        gyroYText = view.findViewById(R.id.gyroYText)
        gyroZText = view.findViewById(R.id.gyroZText)

        // Initialize Magnetometer TextViews
        magXText = view.findViewById(R.id.magXText)
        magYText = view.findViewById(R.id.magYText)
        magZText = view.findViewById(R.id.magZText)

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
                requireContext(),
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
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions
            ActivityCompat.requestPermissions(
                requireActivity(),
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
}