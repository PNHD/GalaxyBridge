package com.pnhd.galaxybridge.budsdiagnostics

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticReportTest {
    @Test
    fun reportRedactsBluetoothAddressAndStatesZeroCommands() {
        val snapshot = RfcommProbeSnapshot(
            startedAtUtc = "2026-08-11T00:00:00Z",
            connectionEstablished = false,
            connectionTimeMs = null,
            socketState = "disconnected",
            exceptionType = "java.io.IOException",
            exceptionMessage = "read failed for AA:BB:CC:DD:EE:FF",
            bytesReceived = 0,
            boundedRawBytesHex = "",
            disconnectReason = "connect_failed"
        )

        val report = DiagnosticReport.render(snapshot, ProtocolConstants.RFCOMM_SERVICE_UUID)

        assertTrue(report.contains("commands_sent: 0"))
        assertTrue(report.contains("[REDACTED_BT_ADDRESS]"))
        assertFalse(report.contains("AA:BB:CC:DD:EE:FF"))
        assertTrue(report.contains("2e73a4ad-332d-41fc-90e2-16bef06523f2"))
    }
}
