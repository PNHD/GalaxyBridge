package com.pnhd.galaxybridge.budsdiagnostics

import java.util.UUID

object ProtocolConstants {
    val RFCOMM_SERVICE_UUID: UUID = UUID.fromString("2e73a4ad-332d-41fc-90e2-16bef06523f2")
}

data class RfcommProbeSnapshot(
    val startedAtUtc: String?,
    val targetName: String? = null,
    val targetAddressSuffix: String? = null,
    val targetClassification: String? = null,
    val connectionEstablished: Boolean,
    val connectionTimeMs: Long?,
    val socketState: String,
    val exceptionType: String?,
    val exceptionMessage: String?,
    val bytesReceived: Long,
    val boundedRawBytesHex: String,
    val disconnectReason: String?,
    val totalFrames: Int = 0,
    val validCrcFrames: Int = 0,
    val invalidCrcFrames: Int = 0,
    val framesById: Map<Int, Int> = emptyMap(),
    val latestStatus0x61: String? = null,
    val captureFilePath: String? = null,
    val captureSha256: String? = null
)

object DiagnosticRedactor {
    private val bluetoothAddress = Regex("(?i)([0-9a-f]{2}:){5}[0-9a-f]{2}")

    fun redact(text: String?): String? = text?.replace(bluetoothAddress, "[REDACTED_BT_ADDRESS]")
}

object DiagnosticReport {
    fun render(snapshot: RfcommProbeSnapshot, serviceUuid: UUID): String = buildString {
        appendLine("GalaxyBridge GB-M0-R4 Watch6 RFCOMM probe")
        appendLine("target_name: ${snapshot.targetName ?: "unknown"}")
        appendLine("target_classification: ${snapshot.targetClassification ?: "unknown"}")
        appendLine("target_address_suffix: ${snapshot.targetAddressSuffix ?: "unknown"}")
        appendLine("rfcomm_service_uuid: $serviceUuid")
        appendLine("rfcomm_channel: SDP-resolved; never hard-coded")
        appendLine("commands_sent: 0")
        appendLine("status_query_implemented: false")
        appendLine("connection_started_at_utc: ${snapshot.startedAtUtc ?: "not_started"}")
        appendLine("connection_established: ${snapshot.connectionEstablished}")
        appendLine("connection_time_ms: ${snapshot.connectionTimeMs ?: "unavailable"}")
        appendLine("socket_state: ${snapshot.socketState}")
        appendLine("exception_type: ${snapshot.exceptionType ?: "none"}")
        appendLine("exception_message: ${DiagnosticRedactor.redact(snapshot.exceptionMessage) ?: "none"}")
        appendLine("bytes_received_total: ${snapshot.bytesReceived}")
        appendLine("disconnect_reason: ${snapshot.disconnectReason ?: "none"}")
        appendLine("")
        appendLine("--- DECODER SUMMARY ---")
        appendLine("total_frames: ${snapshot.totalFrames}")
        appendLine("valid_crc_frames: ${snapshot.validCrcFrames}")
        appendLine("invalid_crc_frames: ${snapshot.invalidCrcFrames}")
        appendLine("frames_by_id:")
        snapshot.framesById.forEach { (id, count) ->
            appendLine("  ${"0x%02X".format(id)} (${BudsSppMsgIds.getName(id)}): $count")
        }
        appendLine("")
        appendLine("--- LATEST 0x61 DECODE ---")
        appendLine(snapshot.latestStatus0x61 ?: "none received")
        appendLine("")
        appendLine("--- PRIVATE CAPTURE ---")
        appendLine("local_path: ${snapshot.captureFilePath ?: "none"}")
        appendLine("sha256: ${snapshot.captureSha256 ?: "none"}")
    }
}
