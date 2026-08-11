package com.pnhd.galaxybridge.budsdiagnostics

import org.junit.Assert.*
import org.junit.Test

class BudsSppDecoderTest {

    private fun createFrame(msgId: Int, payload: ByteArray): ByteArray {
        val body = byteArrayOf(msgId.toByte()) + payload
        val crc = Crc16.calculate(body, 0, body.size)
        val bodyWithCrc = body + byteArrayOf((crc and 0xFF).toByte(), (crc shr 8 and 0xFF).toByte())
        val size = bodyWithCrc.size
        val header = (size and 0x03FF).toShort() // Flags 0
        
        val frame = mutableListOf<Byte>()
        frame.add(0xFD.toByte()) // SOM
        frame.add((header.toInt() and 0xFF).toByte())
        frame.add((header.toInt() shr 8 and 0xFF).toByte())
        bodyWithCrc.forEach { frame.add(it) }
        frame.add(0xDD.toByte()) // EOM
        return frame.toByteArray()
    }

    @Test
    fun testValidFrame() {
        val payload = byteArrayOf(0xAA.toByte(), 0xBB.toByte())
        val data = createFrame(0x61, payload)
        
        val decoder = BudsSppDecoder()
        val frames = decoder.decode(data, data.size)
        
        assertEquals(1, frames.size)
        assertEquals(0x61, frames[0].messageId)
        assertArrayEquals(payload, frames[0].payload)
        assertTrue("CRC should be valid", frames[0].crcValid)
        assertEquals(data.size, frames[0].rawFrameLength)
    }

    @Test
    fun testSplitFrame() {
        val data = createFrame(0x61, byteArrayOf(0xAA.toByte(), 0xBB.toByte()))
        val part1 = data.sliceArray(0 until 4)
        val part2 = data.sliceArray(4 until data.size)
        
        val decoder = BudsSppDecoder()
        val frames1 = decoder.decode(part1, part1.size)
        assertTrue(frames1.isEmpty())
        
        val frames2 = decoder.decode(part2, part2.size)
        assertEquals(1, frames2.size)
        assertEquals(0x61, frames2[0].messageId)
        assertTrue(frames2[0].crcValid)
    }

    @Test
    fun testMultipleFrames() {
        val frame1 = createFrame(0xCD, byteArrayOf(0x01))
        val frame2 = createFrame(0x61, byteArrayOf(0x02))
        
        val data = frame1 + frame2
        val decoder = BudsSppDecoder()
        val frames = decoder.decode(data, data.size)
        assertEquals(2, frames.size)
        assertEquals(0xCD, frames[0].messageId)
        assertEquals(0x61, frames[1].messageId)
    }

    @Test
    fun testInvalidCrc() {
        val data = createFrame(0x61, byteArrayOf(0xAA.toByte()))
        data[data.size - 2] = (data[data.size - 2] + 1).toByte() // Corrupt CRC
        
        val decoder = BudsSppDecoder()
        val frames = decoder.decode(data, data.size)
        assertEquals(1, frames.size)
        assertFalse("CRC should be invalid", frames[0].crcValid)
    }

    @Test
    fun testInvalidEom() {
        val data = byteArrayOf(
            0xFD.toByte(), 0x06, 0x00, 0x61, 0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0x40, 0xEE.toByte()
        )
        val decoder = BudsSppDecoder()
        val frames = decoder.decode(data, data.size)
        assertTrue("Frame with invalid EOM should be skipped/ignored until resync", frames.isEmpty())
    }

    @Test
    fun test0x61Decoder() {
        // Mock payload for 0x61
        // revision=14, earType=1, battL=90, battR=90, coupled=1, mainConn=1, placeL=1, placeR=1, battCase=100, noiseMode=1
        val payload = byteArrayOf(14, 1, 90, 90, 1, 1, 1, 1, 100, 1)
        val status = BudsStatus0x61(payload)
        
        assertEquals(14, status.revision)
        assertEquals(90, status.batteryLeft)
        assertEquals(90, status.batteryRight)
        assertTrue(status.isCoupled)
        assertEquals("ANC", status.getNoiseControlModeName())
    }

    @Test
    fun test0xCDRedaction() {
        // Redaction is handled in DiagnosticReport/Report rendering, 
        // BudsSppMsgIds just provides names.
        assertEquals("CRADLE_SERIAL_NUMBER", BudsSppMsgIds.getName(0xCD))
    }
}
