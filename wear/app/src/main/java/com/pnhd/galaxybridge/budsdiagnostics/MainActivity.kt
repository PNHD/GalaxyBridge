package com.pnhd.galaxybridge.budsdiagnostics

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    companion object {
        private const val BLUETOOTH_PERMISSION_REQUEST = 510
    }

    private val bluetoothAdapter by lazy {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    private val probe by lazy { ReadOnlyRfcommProbe(bluetoothAdapter, ::onProbeSnapshot) }

    private lateinit var statusView: TextView
    private lateinit var targetView: TextView
    private lateinit var deviceGroup: RadioGroup
    private lateinit var connectButton: Button
    private lateinit var disconnectButton: Button
    private lateinit var reportView: TextView
    private var selectedDevice: BluetoothDevice? = null
    private var latestReport = DiagnosticReport.render(
        RfcommProbeSnapshot(null, null, null, null, false, null, "idle", null, null, 0, "", null),
        ReadOnlyRfcommProbe.SERVICE_UUID
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createContentView())
        ensureBluetoothPermissionAndLoad()
    }

    override fun onDestroy() {
        probe.disconnect()
        super.onDestroy()
    }

    private fun createContentView(): ScrollView {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        content.addView(text("GB-M0-R4 Buds RFCOMM Probe", 18f))
        
        targetView = text("TARGET: None selected", 14f).apply { 
            setTextColor(0xFF00FF00.toInt()) // Green to stand out
        }
        content.addView(targetView)

        val connectionButtons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            connectButton = button("Connect") { connectSelectedDevice() }.also {
                it.isEnabled = false
                addView(it)
            }
            disconnectButton = button("Disconnect") { probe.disconnect() }.also {
                it.isEnabled = false
                addView(it)
            }
        }
        content.addView(connectionButtons)

        statusView = text("Checking Bluetooth permission")
        content.addView(statusView)

        content.addView(button("Refresh bonded devices") { ensureBluetoothPermissionAndLoad() })

        deviceGroup = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        content.addView(deviceGroup)

        val exportButtons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(button("Copy report") { copyReport() })
            addView(button("Share report") { shareReport() })
        }
        content.addView(exportButtons)

        reportView = text(latestReport, 10f).apply { setTextIsSelectable(true) }
        content.addView(reportView)

        return ScrollView(this).apply { addView(content) }
    }

    private fun text(value: String, sizeSp: Float = 12f) = TextView(this).apply {
        text = value
        textSize = sizeSp
        setPadding(4, 6, 4, 6)
    }

    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun ensureBluetoothPermissionAndLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connectGranted = checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val scanGranted = checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

            if (!connectGranted || !scanGranted) {
                requestPermissions(
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                    BLUETOOTH_PERMISSION_REQUEST
                )
                return
            }
        }
        loadBondedDevices()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            val allGranted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                loadBondedDevices()
            } else {
                statusView.text = "Bluetooth permissions denied (Connect/Scan required)"
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadBondedDevices() {
        selectedDevice = null
        connectButton.isEnabled = false
        deviceGroup.removeAllViews()

        if (!bluetoothAdapter.isEnabled) {
            statusView.text = "Bluetooth is disabled"
            return
        }

        val allBonded = bluetoothAdapter.bondedDevices
        val targetDevices = allBonded.filter { isLikelyTarget(it.name) }
            .sortedBy { it.name ?: "" }
        
        val otherDevices = allBonded.filter { !isLikelyTarget(it.name) }
            .sortedBy { it.name ?: "" }

        statusView.text = "${allBonded.size} bonded device(s). ${targetDevices.size} potential SM-R510."

        targetDevices.forEach { device ->
            val radio = RadioButton(this).apply {
                text = deviceLabel(device)
                setOnCheckedChangeListener { _, checked ->
                    if (checked) {
                        selectTarget(device)
                    }
                }
            }
            deviceGroup.addView(radio)
        }

        if (targetDevices.size == 1) {
            (deviceGroup.getChildAt(0) as RadioButton).isChecked = true
        }

        // Add others as read-only labels to avoid accidental selection
        if (otherDevices.isNotEmpty()) {
            val contentLayout = deviceGroup.parent as LinearLayout
            contentLayout.addView(text("--- OTHER BONDED (NOT TARGETS) ---", 10f))
            otherDevices.forEach { device ->
                contentLayout.addView(text(deviceLabel(device), 8f))
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun selectTarget(device: BluetoothDevice) {
        selectedDevice = device
        connectButton.isEnabled = true
        targetView.text = "TARGET: ${device.name}\n${maskedAddress(device.address)}"
        statusView.text = "Ready to connect to SM-R510 target"
    }

    private fun connectSelectedDevice() {
        val device = selectedDevice ?: return
        
        if (!isLikelyTarget(device.name)) {
            statusView.text = "TARGET_REJECTED: ${device.name} is not SM-R510"
            return
        }

        if (probe.connect(device)) {
            statusView.text = "RFCOMM connection started (15s timeout)"
            connectButton.isEnabled = false
            disconnectButton.isEnabled = true
        } else {
            statusView.text = "A probe is already active"
        }
    }

    private fun onProbeSnapshot(snapshot: RfcommProbeSnapshot) {
        runOnUiThread {
            latestReport = DiagnosticReport.render(snapshot, ReadOnlyRfcommProbe.SERVICE_UUID)
            reportView.text = latestReport
            val active = snapshot.socketState == "connecting" || snapshot.socketState == "connected_read_only"
            
            if (active) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

            connectButton.isEnabled = !active && selectedDevice != null
            disconnectButton.isEnabled = active
            
            targetView.text = "TARGET: ${snapshot.targetName ?: selectedDevice?.name ?: "None"}\n${snapshot.targetAddressSuffix ?: "??:??"}"

            statusView.text = when (snapshot.socketState) {
                "connecting" -> "Connection started..."
                "connected_read_only" -> "CONNECTED; received ${snapshot.bytesReceived} byte(s)"
                "connect_timeout" -> "TIMEOUT: Failed to connect in 15s"
                "disconnected" -> "Disconnected: ${snapshot.disconnectReason ?: "unknown"}"
                else -> snapshot.socketState
            }
        }
    }

    private fun copyReport() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("GB-M0-R4 RFCOMM report", latestReport))
        statusView.text = "Redacted report copied"
    }

    private fun shareReport() {
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, latestReport)
                },
                "Export GB-M0-R4 report"
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun deviceLabel(device: BluetoothDevice): String {
        val name = device.name ?: "Unnamed bonded device"
        val modelHint = if (isLikelyTarget(name)) "LIKELY SM-R510" else "UNVERIFIED"
        val type = when (device.type) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
            BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual"
            BluetoothDevice.DEVICE_TYPE_LE -> "LE"
            else -> "Unknown type"
        }
        val cachedUuids = device.uuids?.joinToString { it.uuid.toString() } ?: "none cached"
        return "$modelHint\n$name\n$type; bond=${device.bondState}; address=${maskedAddress(device.address)}\nUUIDs: $cachedUuids"
    }

    private fun isLikelyTarget(name: String?): Boolean {
        val normalized = name?.lowercase() ?: return false
        return normalized.contains("buds2 pro")
                || normalized.contains("buds2pro")
                || normalized.contains("sm-r510")
    }

    private fun maskedAddress(address: String): String {
        val suffix = address.split(":").takeLast(2).joinToString(":")
        return "**:**:**:**:$suffix"
    }
}
