package com.example.homosep

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.homosep.databinding.FragmentDevicesBinding

class DevicesFragment : Fragment(), MqttGpsPublisherManager.Listener {

    private var _binding: FragmentDevicesBinding? = null
    private val binding get() = _binding!!

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (fineGranted || coarseGranted) {
                MqttGpsPublisherManager.ensureLocationUpdates()
            } else {
                binding.statusText.text = getString(R.string.permission_denied)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MqttGpsPublisherManager.initialize(requireContext())

        val masterKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPrefs = EncryptedSharedPreferences.create(
            requireContext(),
            "vehicle_config_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val savedName = sharedPrefs.getString("vehicle_name", "") ?: ""
        val savedId = sharedPrefs.getString("vehicle_id", "") ?: ""

        if (savedName.isNotEmpty() || savedId.isNotEmpty()) {
            binding.deviceNameInput.setText(savedName)
            binding.vehicleIdInput.setText(savedId)
            MqttGpsPublisherManager.updateConfig(savedName, savedId)
            binding.deviceNameInput.isEnabled = false
            binding.vehicleIdInput.isEnabled = false
        }

        binding.saveConfigButton.setOnClickListener {
            val name = binding.deviceNameInput.text.toString()
            val id = binding.vehicleIdInput.text.toString()
            sharedPrefs.edit()
                .putString("vehicle_name", name)
                .putString("vehicle_id", id)
                .apply()

            binding.deviceNameInput.isEnabled = false
            binding.vehicleIdInput.isEnabled = false
            MqttGpsPublisherManager.updateConfig(name, id)
            MqttGpsPublisherManager.connect()
            MqttGpsPublisherManager.startPublishing()
        }

        binding.editConfigButton.setOnClickListener {
            binding.deviceNameInput.isEnabled = true
            binding.vehicleIdInput.isEnabled = true
        }



        requestLocationIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        MqttGpsPublisherManager.addListener(this)
    }

    override fun onPause() {
        MqttGpsPublisherManager.removeListener(this)
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStateChanged(state: MqttGpsPublisherManager.State) {
        activity?.runOnUiThread {
            if (_binding != null) {
                binding.topicText.text = "${getString(R.string.mqtt_topic_label)} ${state.topic}"
                binding.statusText.text = "${getString(R.string.mqtt_status_label)} ${state.status}"
                binding.locationText.text = state.locationSummary
                binding.payloadText.text = state.payload
                binding.eventsText.text = state.receivedEvent
            }
        }
    }

    private fun requestLocationIfNeeded() {
        val fineGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            requireContext(),
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
}
