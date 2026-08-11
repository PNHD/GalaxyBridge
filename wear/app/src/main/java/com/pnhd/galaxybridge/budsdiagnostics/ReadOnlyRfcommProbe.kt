package com.pnhd.galaxybridge.budsdiagnostics

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

class ReadOnlyRfcommProbe(
    private val adapter: BluetoothAdapter,
    private val privateDir: File,
    private val listener: (RfcommProbeSnapshot) -> Unit
) {
    companion object {
        val SERVICE_UUID: UUID = ProtocolConstants.RFCOMM_SERVICE_UUID
        private const val MAX_LOG_HEX_BYTES = 1024
        private const val MAX_CAPTURE_BYTES = 64 * 1024
        private const val CONNECT_TIMEOUT_MS = 15000L
    }

    private val lock = Any()
    private var socket: BluetoothSocket? = null
    private var worker: Thread? = null
    private var targetDevice: BluetoothDevice? = null
    private var stopRequested = false
    private var startedAtUtc: String? = null
    private var established = false
    private var connectionTimeMs: Long? = null
    private var socketState = "idle"
    private var exceptionType: String? = null
    private var exceptionMessage: String? = null
    private var disconnectReason: String? = null
    
    private var bytesReceived: Long = 0
    private var logBytes = BoundedByteLog(MAX_LOG_HEX_BYTES)
    
    private var decoder = BudsSppDecoder()
    private var totalFrames = 0
    private var validCrcFrames = 0
    private var invalidCrcFrames = 0
    private val framesById = mutableMapOf<Int, Int>()
    private var latestStatus0x61: BudsStatus0x61? = null
    
    private var captureFile: File? = null
    private var captureSha256: String? = null

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice): Boolean = synchronized(lock) {
        if (worker?.isAlive == true || socketState == "connecting" || socketState == "connected_read_only") {
            return false
        }

        targetDevice = device
        stopRequested = false
        startedAtUtc = Instant.now().toString()
        established = false
        connectionTimeMs = null
        socketState = "connecting"
        exceptionType = null
        exceptionMessage = null
        disconnectReason = null
        bytesReceived = 0
        logBytes = BoundedByteLog(MAX_LOG_HEX_BYTES)
        decoder = BudsSppDecoder()
        totalFrames = 0
        validCrcFrames = 0
        invalidCrcFrames = 0
        framesById.clear()
        latestStatus0x61 = null
        
        val sessionId = Instant.now().toEpochMilli()
        captureFile = File(privateDir, "capture_$sessionId.bin")
        captureSha256 = null
        
        emitLocked()

        worker = Thread({ runConnection(device) }, "buds-rfcomm-read-only").also { it.start() }
        true
    }

    fun disconnect() {
        val socketToClose = synchronized(lock) {
            stopRequested = true
            if (disconnectReason == null) {
                disconnectReason = "owner_requested"
            }
            socket
        }
        runCatching { socketToClose?.close() }
    }

    @SuppressLint("MissingPermission")
    private fun runConnection(device: BluetoothDevice) {
        val startedNanos = System.nanoTime()
        
        val watchdog = Thread({
            try {
                Thread.sleep(CONNECT_TIMEOUT_MS)
                synchronized(lock) {
                    if (!established && socketState == "connecting") {
                        disconnectReason = "connect_timeout"
                        socketState = "connect_timeout"
                        socket?.close()
                    }
                }
            } catch (e: InterruptedException) {}
        }, "rfcomm-connect-watchdog")
        watchdog.start()

        val digest = MessageDigest.getInstance("SHA-256")
        var captureStream: FileOutputStream? = null

        try {
            captureStream = FileOutputStream(captureFile)
            adapter.cancelDiscovery()
            
            val localSocket = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
            synchronized(lock) {
                socket = localSocket
            }
            
            if (isStopRequested()) return
            
            localSocket.connect()
            watchdog.interrupt()

            synchronized(lock) {
                established = true
                connectionTimeMs = (System.nanoTime() - startedNanos) / 1_000_000
                socketState = "connected_read_only"
                emitLocked()
            }

            val buffer = ByteArray(1024)
            while (!isStopRequested()) {
                val count = localSocket.inputStream.read(buffer)
                if (count < 0) {
                    synchronized(lock) { disconnectReason = "remote_eof" }
                    break
                }
                if (count > 0) {
                    synchronized(lock) {
                        val remaining = (MAX_CAPTURE_BYTES - bytesReceived).toInt()
                        if (remaining > 0) {
                            val toWrite = if (count > remaining) remaining else count
                            captureStream.write(buffer, 0, toWrite)
                            digest.update(buffer, 0, toWrite)
                            bytesReceived += toWrite
                        }
                        
                        logBytes.append(buffer, count)
                        
                        val frames = decoder.decode(buffer, count)
                        frames.forEach { frame ->
                            totalFrames++
                            if (frame.crcValid) {
                                validCrcFrames++
                                framesById[frame.messageId] = (framesById[frame.messageId] ?: 0) + 1
                                if (frame.messageId == BudsSppMsgIds.EXTENDED_STATUS_UPDATED) {
                                    latestStatus0x61 = BudsStatus0x61(frame.payload)
                                }
                            } else {
                                invalidCrcFrames++
                            }
                        }
                        
                        emitLocked()
                    }
                }
            }
        } catch (error: Exception) {
            synchronized(lock) {
                if (!stopRequested && disconnectReason != "connect_timeout") {
                    exceptionType = error.javaClass.name
                    exceptionMessage = error.message
                    disconnectReason = if (established) "read_failed" else "connect_failed"
                }
            }
        } finally {
            watchdog.interrupt()
            runCatching { captureStream?.close() }
            
            val socketToClose = synchronized(lock) { socket }
            runCatching { socketToClose?.close() }
            
            synchronized(lock) {
                socket = null
                if (socketState != "connect_timeout") {
                    socketState = "disconnected"
                }
                if (disconnectReason == null) {
                    disconnectReason = if (stopRequested) "owner_requested" else "connection_ended"
                }
                
                captureSha256 = digest.digest().joinToString("") { "%02x".format(it) }
                emitLocked()
            }
        }
    }

    private fun isStopRequested(): Boolean = synchronized(lock) { stopRequested }

    @SuppressLint("MissingPermission")
    private fun emitLocked() {
        val device = targetDevice
        listener(
            RfcommProbeSnapshot(
                startedAtUtc = startedAtUtc,
                targetName = device?.name,
                targetAddressSuffix = device?.address?.split(":")?.takeLast(2)?.joinToString(":"),
                targetClassification = if (device?.name?.let { isLikelyTarget(it) } == true) "LIKELY_SM_R510" else "UNVERIFIED",
                connectionEstablished = established,
                connectionTimeMs = connectionTimeMs,
                socketState = socketState,
                exceptionType = exceptionType,
                exceptionMessage = DiagnosticRedactor.redact(exceptionMessage),
                bytesReceived = bytesReceived,
                boundedRawBytesHex = logBytes.hexSnapshot(),
                disconnectReason = disconnectReason,
                totalFrames = totalFrames,
                validCrcFrames = validCrcFrames,
                invalidCrcFrames = invalidCrcFrames,
                framesById = framesById.toMap(),
                latestStatus0x61 = latestStatus0x61?.toString(),
                captureFilePath = captureFile?.absolutePath,
                captureSha256 = captureSha256
            )
        )
    }

    private fun isLikelyTarget(name: String?): Boolean {
        val normalized = name?.lowercase() ?: return false
        return normalized.contains("buds2 pro")
                || normalized.contains("buds2pro")
                || normalized.contains("sm-r510")
    }
}
