package com.pnhd.galaxybridge.budsdiagnostics

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class BudsSppFrame(
    val messageId: Int,
    val payload: ByteArray,
    val headerFlags: Int,
    val crcValid: Boolean,
    val rawFrameLength: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BudsSppFrame) return false
        if (messageId != other.messageId) return false
        if (!payload.contentEquals(other.payload)) return false
        if (headerFlags != other.headerFlags) return false
        if (crcValid != other.crcValid) return false
        if (rawFrameLength != other.rawFrameLength) return false
        return true
    }

    override fun hashCode(): Int {
        var result = messageId
        result = 31 * result + payload.contentHashCode()
        result = 31 * result + headerFlags
        result = 31 * result + crcValid.hashCode()
        result = 31 * result + rawFrameLength
        return result
    }
}

object BudsSppMsgIds {
    const val STATUS_UPDATED = 0x60
    const val EXTENDED_STATUS_UPDATED = 0x61
    const val CONNECTION_UPDATED = 0x62
    const val VERSION_INFO = 0x63
    const val METERING_REPORT = 0x41
    const val USAGE_REPORT_V2 = 0x47
    const val SPATIAL_AUDIO_DATA = 0xC2
    const val CRADLE_SERIAL_NUMBER = 0xCD
    const val DEBUG_EVENT = 0xF2

    fun getName(id: Int): String = when (id) {
        STATUS_UPDATED -> "STATUS_UPDATED"
        EXTENDED_STATUS_UPDATED -> "EXTENDED_STATUS_UPDATED"
        CONNECTION_UPDATED -> "CONNECTION_UPDATED"
        VERSION_INFO -> "VERSION_INFO"
        METERING_REPORT -> "METERING_REPORT"
        USAGE_REPORT_V2 -> "USAGE_REPORT_V2"
        SPATIAL_AUDIO_DATA -> "SPATIAL_AUDIO_DATA"
        CRADLE_SERIAL_NUMBER -> "CRADLE_SERIAL_NUMBER"
        DEBUG_EVENT -> "DEBUG_EVENT"
        else -> "UNKNOWN_0x${"%02X".format(id)}"
    }
}

class BudsSppDecoder {
    companion object {
        private const val SOM = 0xFD.toByte()
        private const val EOM = 0xDD.toByte()
        private const val MIN_FRAME_SIZE = 7 // SOM(1) + HDR(2) + ID(1) + CRC(2) + EOM(1)
        private const val MAX_FRAME_SIZE = 1024
    }

    private var buffer = ByteArray(MAX_FRAME_SIZE * 2)
    private var bufferSize = 0

    fun decode(data: ByteArray, count: Int): List<BudsSppFrame> {
        if (bufferSize + count > buffer.size) {
            // Safety: Resynchronize/Clear if we exceed buffer logic (should not happen with proper consumption)
            bufferSize = 0
        }
        System.arraycopy(data, 0, buffer, bufferSize, count)
        bufferSize += count

        val frames = mutableListOf<BudsSppFrame>()
        var offset = 0

        while (offset < bufferSize) {
            // Find SOM
            if (buffer[offset] != SOM) {
                offset++
                continue
            }

            // Need at least SOM + HEADER
            if (offset + 3 > bufferSize) break

            val header = ByteBuffer.wrap(buffer, offset + 1, 2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .short.toInt() and 0xFFFF
            
            val payloadLength = header and 0x03FF
            val headerFlags = (header shr 10) and 0x3F

            // Total frame size: SOM(1) + HDR(2) + ID(1) + PAYLOAD(N) + CRC(2) + EOM(1)
            // Wait, the prompt says "BODY: message ID, payload, CRC16". 
            // Usually, "SIZE" in header refers to the "Body".
            // Let's assume SIZE = MsgID(1) + Payload(N) + CRC16(2)
            val totalSize = 1 + 2 + payloadLength + 1

            if (totalSize > MAX_FRAME_SIZE) {
                // Malformed header size, skip SOM and continue
                offset++
                continue
            }

            if (offset + totalSize > bufferSize) break

            val msgId = buffer[offset + 3].toInt() and 0xFF
            val actualPayloadLength = payloadLength - 1 - 2 // Size includes MsgID and CRC?
            
            // Re-read prompt: "BODY: message ID, payload, CRC16". 
            // If SIZE is lower 10 bits of header, and it represents the BODY:
            // Frame: SOM | HDR | [ ID | PAYLOAD | CRC ] | EOM
            // Length = 1 (ID) + PayloadLen + 2 (CRC)
            
            if (payloadLength < 3) { // Must have at least ID and CRC
                offset++
                continue
            }

            if (buffer[offset + totalSize - 1] != EOM) {
                // Invalid EOM, not a valid frame here
                offset++
                continue
            }

            val payload = ByteArray(payloadLength - 3)
            System.arraycopy(buffer, offset + 4, payload, 0, payload.size)

            val receivedCrc = ByteBuffer.wrap(buffer, offset + 3 + payloadLength - 2, 2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .short.toInt() and 0xFFFF
            
            val calculatedCrc = Crc16.calculate(buffer, offset + 3, payloadLength - 2)
            val crcValid = (receivedCrc == calculatedCrc)

            frames.add(
                BudsSppFrame(
                    messageId = msgId,
                    payload = payload,
                    headerFlags = headerFlags,
                    crcValid = crcValid,
                    rawFrameLength = totalSize
                )
            )

            offset += totalSize
        }

        // Shift remaining bytes to start of buffer
        if (offset > 0) {
            val remaining = bufferSize - offset
            if (remaining > 0) {
                System.arraycopy(buffer, offset, buffer, 0, remaining)
            }
            bufferSize = remaining
        }

        return frames
    }
}

object Crc16 {
    // CRC16-CCITT (0x1021)
    fun calculate(data: ByteArray, offset: Int, length: Int): Int {
        var crc = 0xFFFF
        for (i in 0 until length) {
            val b = data[offset + i].toInt() and 0xFF
            for (j in 0 until 8) {
                val bit = (b shr (7 - j) and 1) == 1
                val c15 = (crc shr 15 and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) crc = crc xor 0x1021
            }
        }
        return crc and 0xFFFF
    }
}
