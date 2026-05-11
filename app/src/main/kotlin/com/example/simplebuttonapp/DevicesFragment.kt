package com.example.simplebuttonapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class DevicesFragment : Fragment(), MqttGpsPublisherManager.Listener {

    private lateinit var brokerInput: EditText
    private lateinit var portInput: EditText
    private lateinit var deviceNameInput: EditText
    private lateinit var topicText: TextView
    private lateinit var statusText: TextView
    private lateinit var locationText: TextView
    private lateinit var payloadText: TextView

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (fineGranted || coarseGranted) {
                MqttGpsPublisherManager.ensureLocationUpdates()
            } else {
                statusText.text = getString(R.string.permission_denied)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_devices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MqttGpsPublisherManager.initialize(requireContext())

        brokerInput = view.findViewById(R.id.brokerInput)
        portInput = view.findViewById(R.id.portInput)
        deviceNameInput = view.findViewById(R.id.deviceNameInput)
        topicText = view.findViewById(R.id.topicText)
        statusText = view.findViewById(R.id.statusText)
        locationText = view.findViewById(R.id.locationText)
        payloadText = view.findViewById(R.id.payloadText)

        val connectButton = view.findViewById<Button>(R.id.connectButton)
        val disconnectButton = view.findViewById<Button>(R.id.disconnectButton)
        val startPublishButton = view.findViewById<Button>(R.id.startPublishButton)
        val stopPublishButton = view.findViewById<Button>(R.id.stopPublishButton)

        deviceNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                MqttGpsPublisherManager.updateTopic(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        connectButton.setOnClickListener {
            MqttGpsPublisherManager.connect(
                brokerInput.text.toString(),
                portInput.text.toString()
            )
        }

        disconnectButton.setOnClickListener {
            MqttGpsPublisherManager.disconnect()
        }

        startPublishButton.setOnClickListener {
            requestLocationIfNeeded()
            MqttGpsPublisherManager.startPublishing(deviceNameInput.text.toString())
        }

        stopPublishButton.setOnClickListener {
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

    override fun onStateChanged(state: MqttGpsPublisherManager.State) {
        activity?.runOnUiThread {
            topicText.text = "${getString(R.string.mqtt_topic_label)} ${state.topic}"
            statusText.text = "${getString(R.string.mqtt_status_label)} ${state.status}"
            locationText.text = state.locationSummary
            payloadText.text = state.payload
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
