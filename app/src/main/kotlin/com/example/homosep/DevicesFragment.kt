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

        binding.deviceNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                MqttGpsPublisherManager.updateTopic(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        binding.connectButton.setOnClickListener {
            MqttGpsPublisherManager.connect(
                binding.brokerInput.text.toString(),
                binding.portInput.text.toString(),
                binding.usernameInput.text.toString(),
                binding.passwordInput.text.toString()
            )
        }

        binding.disconnectButton.setOnClickListener {
            MqttGpsPublisherManager.disconnect()
        }

        binding.startPublishButton.setOnClickListener {
            requestLocationIfNeeded()
            MqttGpsPublisherManager.startPublishing(binding.deviceNameInput.text.toString())
        }

        binding.stopPublishButton.setOnClickListener {
            MqttGpsPublisherManager.stopPublishing()
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
