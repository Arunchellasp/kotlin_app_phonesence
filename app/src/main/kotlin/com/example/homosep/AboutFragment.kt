package com.example.homosep

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.homosep.databinding.FragmentAboutBinding
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.nio.charset.Charset

class AboutFragment : Fragment(), SerialInputOutputManager.Listener {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    private data class SerialDeviceItem(
        val label: String,
        val port: UsbSerialPort
    ) {
        override fun toString(): String = label
    }

    private lateinit var usbManager: UsbManager
    private var deviceItems = emptyList<SerialDeviceItem>()
    private var serialPort: UsbSerialPort? = null
    private var ioManager: SerialInputOutputManager? = null
    private var pendingPermissionItem: SerialDeviceItem? = null

    private val usbPermissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ACTION_USB_PERMISSION) return

            val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
            val item = pendingPermissionItem
            pendingPermissionItem = null

            if (granted && item != null) {
                openSelectedPort(item)
            } else {
                binding.serialStatusText.text = getString(R.string.serial_permission_denied)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usbManager = requireContext().getSystemService(Context.USB_SERVICE) as UsbManager

        binding.serialScanButton.setOnClickListener { scanSerialDevices() }
        binding.serialConnectButton.setOnClickListener { connectSelectedDevice() }
        binding.serialDisconnectButton.setOnClickListener { disconnectSerial() }
        binding.serialSendButton.setOnClickListener { sendSerialMessage() }

        setupBaudRates()
        scanSerialDevices()
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(usbPermissionReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            requireContext().registerReceiver(usbPermissionReceiver, filter)
        }
    }

    override fun onStop() {
        requireContext().unregisterReceiver(usbPermissionReceiver)
        super.onStop()
    }

    override fun onDestroyView() {
        disconnectSerial()
        super.onDestroyView()
        _binding = null
    }

    override fun onNewData(data: ByteArray) {
        val text = data.toString(Charset.defaultCharset())
        activity?.runOnUiThread {
            if (_binding != null) {
                val current = binding.serialReceiveText.text.toString()
                binding.serialReceiveText.text = if (current == getString(R.string.serial_no_data)) {
                    text
                } else {
                    current + text
                }
            }
        }
    }

    override fun onRunError(e: Exception) {
        activity?.runOnUiThread {
            if (_binding != null) {
                binding.serialStatusText.text = "Serial read error: ${e.message}"
            }
        }
    }

    private fun setupBaudRates() {
        val baudRates = listOf("9600", "19200", "38400", "57600", "115200", "230400")
        binding.serialBaudSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            baudRates
        )
        binding.serialBaudSpinner.setSelection(baudRates.indexOf("115200"))
    }

    private fun scanSerialDevices() {
        val drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        deviceItems = drivers.flatMap { driver ->
            driver.ports.mapIndexed { index, port ->
                val device = driver.device
                val label = buildString {
                    append(device.deviceName)
                    append(" | VID ")
                    append(device.vendorId)
                    append(" PID ")
                    append(device.productId)
                    append(" | Port ")
                    append(index)
                }
                SerialDeviceItem(label, port)
            }
        }

        val labels = if (deviceItems.isEmpty()) {
            listOf(getString(R.string.serial_no_devices))
        } else {
            deviceItems.map { it.label }
        }
        binding.serialDeviceSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            labels
        )
        binding.serialStatusText.text = if (deviceItems.isEmpty()) {
            getString(R.string.serial_no_devices)
        } else {
            "Found ${deviceItems.size} USB serial port(s)"
        }
    }

    private fun connectSelectedDevice() {
        val selectedIndex = binding.serialDeviceSpinner.selectedItemPosition
        val item = deviceItems.getOrNull(selectedIndex)
        if (item == null) {
            binding.serialStatusText.text = getString(R.string.serial_no_devices)
            return
        }

        val device = item.port.driver.device
        if (!usbManager.hasPermission(device)) {
            pendingPermissionItem = item
            usbManager.requestPermission(device, buildPermissionIntent())
            binding.serialStatusText.text = "Requesting USB permission..."
            return
        }

        openSelectedPort(item)
    }

    private fun openSelectedPort(item: SerialDeviceItem) {
        disconnectSerial()

        try {
            val connection = usbManager.openDevice(item.port.driver.device)
                ?: throw IllegalStateException("Unable to open USB device")
            val baudRate = binding.serialBaudSpinner.selectedItem.toString().toInt()
            val port = item.port

            port.open(connection)
            port.setParameters(
                baudRate,
                UsbSerialPort.DATABITS_8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )

            val manager = SerialInputOutputManager(port, this)
            manager.start()

            serialPort = port
            ioManager = manager
            binding.serialStatusText.text = "Connected at $baudRate baud"
        } catch (exception: Exception) {
            binding.serialStatusText.text = "Serial connect failed: ${exception.message}"
            disconnectSerial()
        }
    }

    private fun disconnectSerial() {
        try {
            ioManager?.stop()
            ioManager = null
            serialPort?.close()
            serialPort = null
            binding.serialStatusText.text = getString(R.string.serial_not_connected)
        } catch (exception: Exception) {
            binding.serialStatusText.text = "Serial disconnect failed: ${exception.message}"
        }
    }

    private fun sendSerialMessage() {
        val port = serialPort
        if (port == null) {
            binding.serialStatusText.text = getString(R.string.serial_not_connected)
            return
        }

        val message = binding.serialMessageInput.text.toString()
        if (message.isEmpty()) return

        try {
            port.write(message.toByteArray(Charset.defaultCharset()), WRITE_TIMEOUT_MS)
            binding.serialStatusText.text = "Sent ${message.length} character(s)"
        } catch (exception: Exception) {
            binding.serialStatusText.text = "Serial send failed: ${exception.message}"
        }
    }

    private fun buildPermissionIntent(): PendingIntent {
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val intent = Intent(ACTION_USB_PERMISSION).setPackage(requireContext().packageName)
        return PendingIntent.getBroadcast(requireContext(), 0, intent, flags)
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "com.example.homosep.USB_PERMISSION"
        private const val WRITE_TIMEOUT_MS = 1000
    }
}
