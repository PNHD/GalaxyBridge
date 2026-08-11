package com.pnhd.galaxybridge.budsdiagnostics

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.time.Instant
import java.util.UUID

class ReadOnlyRfcommProbe(
    private val adapter: BluetoothAdapter,
    private val listener: (RfcommProbeSnapshot) -> Unit
) {
    companion object {
        val SERVICE_UUID: UUID = ProtocolConstants.RFCOMM_SERVICE_UUID
        private const val MAX_CAPTURE_BYTES = 4096
    }

    private val lock = Any()
    private var socket: BluetoothSocket? = null
    private var worker: Thread? = null
    private var stopRequested = false
    private var startedAtUtc: String? = null
    private var established = false
    private var connectionTimeMs: Long? = null
    private var socketState = "idle"
    private var exceptionType: String? = null
    private var exceptionMessage: String? = null
    private var disconnectReason: String? = null
    private var rawBytes = BoundedByteLog(MAX_CAPTURE_BYTES)

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice): Boolean = synchronized(lock) {
        if (worker?.isAlive == true || socketState == "connecting" || socketState == "connected") {
            return false
        }

        stopRequested = false
        startedAtUtc = Instant.now().toString()
        established = false
        connectionTimeMs = null
        socketState = "connecting"
        exceptionType = null
        exceptionMessage = null
        disconnectReason = null
        rawBytes = BoundedByteLog(MAX_CAPTURE_BYTES)
        emitLocked()

        worker = Thread({ runConnection(device) }, "buds-rfcomm-read-only").also { it.start() }
        true
    }

    fun disconnect() {
        val socketToClose = synchronized(lock) {
            stopRequested = true
            disconnectReason = "owner_requested"
            socket
        }
        runCatching { socketToClose?.close() }
    }

    @SuppressLint("MissingPermission")
    private fun runConnection(device: BluetoothDevice) {
        val startedNanos = System.nanoTime()
        try {
            adapter.cancelDiscovery()
            val localSocket = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
            val cancelledBeforeConnect = synchronized(lock) {
                socket = localSocket
                stopRequested
            }
            if (cancelledBeforeConnect) {
                return
            }
            localSocket.connect()

            synchronized(lock) {
                established = true
                connectionTimeMs = (System.nanoTime() - startedNanos) / 1_000_000
                socketState = "connected_read_only"
                emitLocked()
            }

            val buffer = ByteArray(512)
            while (!isStopRequested()) {
                val count = localSocket.inputStream.read(buffer)
                if (count < 0) {
                    synchronized(lock) { disconnectReason = "remote_eof" }
                    break
                }
                if (count > 0) {
                    synchronized(lock) {
                        rawBytes.append(buffer, count)
                        emitLocked()
                    }
                }
            }
        } catch (error: Exception) {
            synchronized(lock) {
                if (!stopRequested) {
                    exceptionType = error.javaClass.name
                    exceptionMessage = error.message
                    disconnectReason = if (established) "read_failed" else "connect_failed"
                }
            }
        } finally {
            val socketToClose = synchronized(lock) { socket }
            runCatching { socketToClose?.close() }
            synchronized(lock) {
                socket = null
                socketState = "disconnected"
                if (disconnectReason == null) {
                    disconnectReason = if (stopRequested) "owner_requested" else "connection_ended"
                }
                emitLocked()
            }
        }
    }

    private fun isStopRequested(): Boolean = synchronized(lock) { stopRequested }

    private fun emitLocked() {
        listener(
            RfcommProbeSnapshot(
                startedAtUtc = startedAtUtc,
                connectionEstablished = established,
                connectionTimeMs = connectionTimeMs,
                socketState = socketState,
                exceptionType = exceptionType,
                exceptionMessage = DiagnosticRedactor.redact(exceptionMessage),
                bytesReceived = rawBytes.totalBytes,
                boundedRawBytesHex = rawBytes.hexSnapshot(),
                disconnectReason = disconnectReason
            )
        )
    }
}
