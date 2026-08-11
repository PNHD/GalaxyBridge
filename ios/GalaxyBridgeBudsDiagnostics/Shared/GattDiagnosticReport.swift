import Foundation

struct GattCharacteristicRecord: Equatable {
    let serviceUUID: String
    let characteristicUUID: String
    let properties: [String]
    let valueHex: String?
    let valueSource: String?
    let maxWriteWithResponse: Int?
    let maxWriteWithoutResponse: Int?
    let notificationsEnabled: Bool
}

enum GattDiagnosticReport {
    static func render(
        generatedAt: Date,
        classification: String,
        connectionState: String,
        records: [GattCharacteristicRecord]
    ) -> String {
        var lines = [
            "GalaxyBridge GB-M0-R4 iOS GATT inventory",
            "generated_at_utc: \(iso8601.string(from: generatedAt))",
            "target: owner-selected peripheral (identifier redacted)",
            "classification: \(classification)",
            "connection_state: \(connectionState)",
            "unknown_writes_performed: false",
            "characteristic_count: \(records.count)",
            ""
        ]

        for record in records.sorted(by: sortRecords) {
            lines.append("service_uuid: \(record.serviceUUID)")
            lines.append("characteristic_uuid: \(record.characteristicUUID)")
            lines.append("properties: \(record.properties.sorted().joined(separator: ","))")
            lines.append("property_read: \(record.properties.contains("read"))")
            lines.append("property_write: \(record.properties.contains("write"))")
            lines.append("property_write_without_response: \(record.properties.contains("writeWithoutResponse"))")
            lines.append("property_notify: \(record.properties.contains("notify"))")
            lines.append("property_indicate: \(record.properties.contains("indicate"))")
            lines.append("notifications_enabled: \(record.notificationsEnabled)")
            lines.append("max_write_with_response: \(optionalInt(record.maxWriteWithResponse))")
            lines.append("max_write_without_response: \(optionalInt(record.maxWriteWithoutResponse))")
            lines.append("current_value_source: \(record.valueSource ?? "unavailable")")
            lines.append("current_value_hex: \(record.valueHex ?? "unavailable")")
            lines.append("")
        }

        return lines.joined(separator: "\n")
    }

    private static let iso8601: ISO8601DateFormatter = {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return formatter
    }()

    private static func optionalInt(_ value: Int?) -> String {
        value.map(String.init) ?? "not-applicable"
    }

    private static func sortRecords(
        _ lhs: GattCharacteristicRecord,
        _ rhs: GattCharacteristicRecord
    ) -> Bool {
        (lhs.serviceUUID, lhs.characteristicUUID) < (rhs.serviceUUID, rhs.characteristicUUID)
    }
}
