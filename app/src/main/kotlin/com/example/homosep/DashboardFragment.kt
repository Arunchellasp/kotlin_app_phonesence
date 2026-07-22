package com.example.homosep

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.homosep.databinding.FragmentDashboardBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class DashboardFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sensorManager: SensorManager
    private lateinit var locationCallback: LocationCallback
    private var locationUpdatesStarted = false

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
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
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

        // Initialize Location Callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                if (location != null) {
                    binding.latitudeText.text =
                        getString(R.string.latitude_format, location.latitude)
                    binding.longitudeText.text =
                        getString(R.string.longitude_format, location.longitude)
                    binding.accuracyText.text =
                        getString(R.string.accuracy_format, location.accuracy)
                    binding.altitudeText.text =
                        getString(R.string.altitude_format, location.altitude)
                    binding.gpsStatusText.text = getString(R.string.gps_found)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || _binding == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                binding.accelXText.text = getString(R.string.sensor_value_format, "X", event.values[0])
                binding.accelYText.text = getString(R.string.sensor_value_format, "Y", event.values[1])
                binding.accelZText.text = getString(R.string.sensor_value_format, "Z", event.values[2])
            }

            Sensor.TYPE_GYROSCOPE -> {
                binding.gyroXText.text = getString(R.string.sensor_value_format, "X", event.values[0])
                binding.gyroYText.text = getString(R.string.sensor_value_format, "Y", event.values[1])
                binding.gyroZText.text = getString(R.string.sensor_value_format, "Z", event.values[2])
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                binding.magXText.text = getString(R.string.sensor_value_format, "X", event.values[0])
                binding.magYText.text = getString(R.string.sensor_value_format, "Y", event.values[1])
                binding.magZText.text = getString(R.string.sensor_value_format, "Z", event.values[2])
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
        binding.gpsStatusText.text = getString(R.string.gps_searching)

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