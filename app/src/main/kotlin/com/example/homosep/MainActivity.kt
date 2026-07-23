package com.example.homosep

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.homosep.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (fineGranted || coarseGranted) {
                MqttGpsPublisherManager.ensureLocationUpdates()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the custom MaterialToolbar as the ActionBar
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        MqttGpsPublisherManager.initialize(this)

        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPrefs = EncryptedSharedPreferences.create(
            this,
            "vehicle_config_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val savedName = sharedPrefs.getString("vehicle_name", "") ?: ""
        val savedId = sharedPrefs.getString("vehicle_id", "") ?: ""

        if (savedName.isEmpty() || savedId.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Configuration Required")
                .setMessage("Please enter your Vehicle Number and Vehicle ID to continue.")
                .setCancelable(false)
                .setPositiveButton("Configure Vehicle") { _, _ ->
                    navigateTo(DevicesFragment())
                }
                .show()
        } else {
            MqttGpsPublisherManager.updateConfig(savedName, savedId)
            MqttGpsPublisherManager.connect()
            MqttGpsPublisherManager.startPublishing()
        }

        requestLocationIfNeeded()

        // Set Tracking (Web) as the default home fragment
        if (savedInstanceState == null) {
            loadFragment(TrackingFragment())
        }

        // Setup Floating Action Button for Navigation
        binding.navFab.setOnClickListener {
            val isVisible = binding.fabMenuContainer.visibility == View.VISIBLE
            binding.fabMenuContainer.visibility = if (isVisible) View.GONE else View.VISIBLE
        }

        binding.fabDashboard.setOnClickListener { navigateTo(DashboardFragment()) }
        binding.fabRtsp.setOnClickListener { navigateTo(RtspStreamsFragment()) }
        binding.fabHome.setOnClickListener { navigateTo(TrackingFragment()) }

        binding.accountButton.setOnClickListener { navigateTo(SettingsFragment()) }
    }

    private fun requestLocationIfNeeded() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            MqttGpsPublisherManager.ensureLocationUpdates()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun navigateTo(fragment: Fragment) {
        binding.fabMenuContainer.visibility = View.GONE
        loadFragment(fragment)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}