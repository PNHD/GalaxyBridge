import Combine
import CoreBluetooth
import Foundation

struct PeripheralCandidate: Identifiable {
    let id: UUID
    let displayName: String
    let isLikelyBuds2Pro: Bool
}

final class BudsGattInventory: NSObject, ObservableObject {
    @Published private(set) var candidates: [PeripheralCandidate] = []
    @Published private(set) var selectedPeripheralID: UUID?
    @Published private(set) var isScanning = false
    @Published private(set) var status = "Waiting for Bluetooth"
    @Published private(set) var report = ""
    @Published var classification = "HARDWARE_REQUIRED" {
        didSet { rebuildReport() }
    }

    private var central: CBCentralManager!
    private var peripherals: [UUID: CBPeripheral] = [:]
    private var selectedPeripheral: CBPeripheral?
    private var records: [String: GattCharacteristicRecord] = [:]
    private var connectionState = "not_connected"

    override init() {
        super.init()
        central = CBCentralManager(delegate: self, queue: .main)
        rebuildReport()
    }

    func startScan() {
        guard central.state == .poweredOn else {
            status = "Bluetooth must be powered on"
            return
        }
        candidates = []
        peripherals = [:]
        central.scanForPeripherals(
            withServices: nil,
            options: [CBCentralManagerScanOptionAllowDuplicatesKey: false]
        )
        isScanning = true
        status = "Scanning for CoreBluetooth-visible peripherals"
    }

    func stopScan() {
        central.stopScan()
        isScanning = false
        status = candidates.isEmpty ? "No peripherals observed" : "Select the physical Buds target"
    }

    func select(_ candidate: PeripheralCandidate) {
        guard let peripheral = peripherals[candidate.id] else { return }
        stopScan()
        if let previous = selectedPeripheral, previous.identifier != peripheral.identifier {
            central.cancelPeripheralConnection(previous)
        }
        selectedPeripheral = peripheral
        selectedPeripheralID = peripheral.identifier
        records = [:]
        connectionState = "connecting"
        status = "Connecting to explicitly selected peripheral"
        peripheral.delegate = self
        central.connect(peripheral, options: nil)
        rebuildReport()
    }

    func disconnect() {
        guard let selectedPeripheral else { return }
        central.cancelPeripheralConnection(selectedPeripheral)
        connectionState = "disconnecting"
        status = "Disconnecting"
        rebuildReport()
    }

    private func rebuildReport() {
        report = GattDiagnosticReport.render(
            generatedAt: Date(),
            classification: classification,
            connectionState: connectionState,
            records: Array(records.values)
        )
    }

    private func upsert(
        _ characteristic: CBCharacteristic,
        valueSource: String? = nil,
        notificationsEnabled: Bool? = nil
    ) {
        guard let service = characteristic.service, let peripheral = selectedPeripheral else { return }
        let key = "\(service.uuid.uuidString)/\(characteristic.uuid.uuidString)"
        let existing = records[key]
        let properties = propertyNames(characteristic.properties)
        let canWrite = characteristic.properties.contains(.write)
        let canWriteWithoutResponse = characteristic.properties.contains(.writeWithoutResponse)

        records[key] = GattCharacteristicRecord(
            serviceUUID: service.uuid.uuidString,
            characteristicUUID: characteristic.uuid.uuidString,
            properties: properties,
            valueHex: characteristic.value.map(boundedHex) ?? existing?.valueHex,
            valueSource: valueSource ?? existing?.valueSource,
            maxWriteWithResponse: canWrite ? peripheral.maximumWriteValueLength(for: .withResponse) : nil,
            maxWriteWithoutResponse: canWriteWithoutResponse
                ? peripheral.maximumWriteValueLength(for: .withoutResponse)
                : nil,
            notificationsEnabled: notificationsEnabled ?? existing?.notificationsEnabled ?? false
        )
        rebuildReport()
    }

    private func propertyNames(_ properties: CBCharacteristicProperties) -> [String] {
        var names: [String] = []
        let values: [(CBCharacteristicProperties, String)] = [
            (.broadcast, "broadcast"),
            (.read, "read"),
            (.writeWithoutResponse, "writeWithoutResponse"),
            (.write, "write"),
            (.notify, "notify"),
            (.indicate, "indicate"),
            (.authenticatedSignedWrites, "authenticatedSignedWrites"),
            (.extendedProperties, "extendedProperties"),
            (.notifyEncryptionRequired, "notifyEncryptionRequired"),
            (.indicateEncryptionRequired, "indicateEncryptionRequired")
        ]
        for (option, name) in values where properties.contains(option) {
            names.append(name)
        }
        return names
    }

    private func boundedHex(_ data: Data) -> String {
        let limit = 256
        let prefix = data.prefix(limit).map { String(format: "%02X", $0) }.joined()
        return data.count > limit ? "\(prefix) [truncated total_bytes=\(data.count)]" : prefix
    }

    private func isLikelyTarget(name: String) -> Bool {
        let normalized = name.lowercased()
        return normalized.contains("buds2 pro")
            || normalized.contains("buds2pro")
            || normalized.contains("sm-r510")
    }
}

extension BudsGattInventory: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOn:
            status = "Bluetooth ready; tap Scan"
        case .poweredOff:
            status = "Bluetooth is powered off"
        case .unauthorized:
            status = "Bluetooth permission denied"
        case .unsupported:
            status = "CoreBluetooth unsupported on this device"
        default:
            status = "Bluetooth unavailable: \(central.state.rawValue)"
        }
    }

    func centralManager(
        _ central: CBCentralManager,
        didDiscover peripheral: CBPeripheral,
        advertisementData: [String: Any],
        rssi RSSI: NSNumber
    ) {
        let advertisedName = advertisementData[CBAdvertisementDataLocalNameKey] as? String
        let name = advertisedName ?? peripheral.name ?? "Unnamed peripheral"
        peripherals[peripheral.identifier] = peripheral
        let candidate = PeripheralCandidate(
            id: peripheral.identifier,
            displayName: name,
            isLikelyBuds2Pro: isLikelyTarget(name: name)
        )
        candidates.removeAll { $0.id == candidate.id }
        candidates.append(candidate)
        candidates.sort {
            if $0.isLikelyBuds2Pro != $1.isLikelyBuds2Pro { return $0.isLikelyBuds2Pro }
            return $0.displayName.localizedCaseInsensitiveCompare($1.displayName) == .orderedAscending
        }
        status = "Observed \(candidates.count) peripheral(s); select the physical target"
    }

    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        connectionState = "connected"
        status = "Connected; discovering every visible service"
        peripheral.discoverServices(nil)
        rebuildReport()
    }

    func centralManager(
        _ central: CBCentralManager,
        didFailToConnect peripheral: CBPeripheral,
        error: Error?
    ) {
        connectionState = "connect_failed"
        status = "Connection failed: \(error?.localizedDescription ?? "unknown error")"
        rebuildReport()
    }

    func centralManager(
        _ central: CBCentralManager,
        didDisconnectPeripheral peripheral: CBPeripheral,
        error: Error?
    ) {
        connectionState = "disconnected"
        status = error.map { "Disconnected: \($0.localizedDescription)" } ?? "Disconnected"
        rebuildReport()
    }
}

extension BudsGattInventory: CBPeripheralDelegate {
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if let error {
            status = "Service discovery failed: \(error.localizedDescription)"
            return
        }
        let services = peripheral.services ?? []
        status = "Discovered \(services.count) service(s); enumerating characteristics"
        for service in services {
            peripheral.discoverCharacteristics(nil, for: service)
        }
        rebuildReport()
    }

    func peripheral(
        _ peripheral: CBPeripheral,
        didDiscoverCharacteristicsFor service: CBService,
        error: Error?
    ) {
        if let error {
            status = "Characteristic discovery error: \(error.localizedDescription)"
            return
        }
        for characteristic in service.characteristics ?? [] {
            upsert(characteristic)
            if characteristic.properties.contains(.read) {
                peripheral.readValue(for: characteristic)
            }
            if characteristic.properties.contains(.notify) || characteristic.properties.contains(.indicate) {
                peripheral.setNotifyValue(true, for: characteristic)
            }
        }
        status = "Inventory contains \(records.count) characteristic(s)"
        rebuildReport()
    }

    func peripheral(
        _ peripheral: CBPeripheral,
        didUpdateValueFor characteristic: CBCharacteristic,
        error: Error?
    ) {
        if let error {
            status = "Value read/notification error: \(error.localizedDescription)"
            return
        }
        let source = characteristic.isNotifying ? "read_or_notification" : "read"
        upsert(characteristic, valueSource: source)
    }

    func peripheral(
        _ peripheral: CBPeripheral,
        didUpdateNotificationStateFor characteristic: CBCharacteristic,
        error: Error?
    ) {
        if let error {
            status = "Notification subscription rejected: \(error.localizedDescription)"
        }
        upsert(characteristic, notificationsEnabled: error == nil && characteristic.isNotifying)
    }
}
