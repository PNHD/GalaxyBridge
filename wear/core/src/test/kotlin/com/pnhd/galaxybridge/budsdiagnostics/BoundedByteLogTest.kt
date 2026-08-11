package com.pnhd.galaxybridge.budsdiagnostics

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class BoundedByteLogTest {
    @Test
    fun keepsOnlyNewestBytesWhileTrackingTotal() {
        val log = BoundedByteLog(maxBytes = 5)

        log.append(byteArrayOf(0x01, 0x02, 0x03))
        log.append(byteArrayOf(0x04, 0x05, 0x06, 0x07))

        assertEquals(7L, log.totalBytes)
        assertArrayEquals(byteArrayOf(0x03, 0x04, 0x05, 0x06, 0x07), log.snapshot())
        assertEquals("0304050607", log.hexSnapshot())
    }
}
