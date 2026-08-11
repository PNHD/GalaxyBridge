import XCTest
@testable import GalaxyBridgeBudsDiagnostics

final class GattDiagnosticReportTests: XCTestCase {
    func testReportIsSortedAndDoesNotContainPeripheralIdentifier() {
        let records = [
            GattCharacteristicRecord(
                serviceUUID: "FFFF",
                characteristicUUID: "0002",
                properties: ["notify", "read"],
                valueHex: "A1B2",
                valueSource: "read",
                maxWriteWithResponse: nil,
                maxWriteWithoutResponse: nil,
                notificationsEnabled: true
            ),
            GattCharacteristicRecord(
                serviceUUID: "180F",
                characteristicUUID: "2A19",
                properties: ["read"],
                valueHex: "64",
                valueSource: "read",
                maxWriteWithResponse: nil,
                maxWriteWithoutResponse: nil,
                notificationsEnabled: false
            )
        ]

        let report = GattDiagnosticReport.render(
            generatedAt: Date(timeIntervalSince1970: 0),
            classification: "HARDWARE_REQUIRED",
            connectionState: "connected",
            records: records
        )

        XCTAssertLessThan(report.range(of: "180F")!.lowerBound, report.range(of: "FFFF")!.lowerBound)
        XCTAssertTrue(report.contains("unknown_writes_performed: false"))
        XCTAssertTrue(report.contains("target: owner-selected peripheral (identifier redacted)"))
    }
}
