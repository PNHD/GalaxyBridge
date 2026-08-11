import SwiftUI
import UIKit

struct ContentView: View {
    @StateObject private var inventory = BudsGattInventory()

    var body: some View {
        NavigationStack {
            Form {
                Section("Safety") {
                    Text("Read-only diagnostic. It never writes characteristic values or protocol commands.")
                    Text("Notification subscriptions are enabled only on characteristics that advertise notify or indicate.")
                }

                Section("Discovery") {
                    Text(inventory.status)
                    HStack {
                        Button(inventory.isScanning ? "Stop scan" : "Scan") {
                            if inventory.isScanning {
                                inventory.stopScan()
                            } else {
                                inventory.startScan()
                            }
                        }
                        .buttonStyle(.borderedProminent)

                        if inventory.selectedPeripheralID != nil {
                            Button("Disconnect", role: .destructive) {
                                inventory.disconnect()
                            }
                        }
                    }

                    ForEach(inventory.candidates) { candidate in
                        Button {
                            inventory.select(candidate)
                        } label: {
                            HStack {
                                VStack(alignment: .leading) {
                                    Text(candidate.displayName)
                                    Text(candidate.isLikelyBuds2Pro ? "Likely Buds2 Pro / SM-R510" : "Unverified peripheral")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                                Spacer()
                                if inventory.selectedPeripheralID == candidate.id {
                                    Image(systemName: "checkmark.circle.fill")
                                }
                            }
                        }
                    }
                }

                Section("Evidence classification") {
                    Picker("Classification", selection: $inventory.classification) {
                        Text("Hardware required").tag("HARDWARE_REQUIRED")
                        Text("Direct GATT candidate").tag("DIRECT_IOS_GATT_CANDIDATE")
                        Text("No usable GATT observed").tag("NO_USABLE_GATT_OBSERVED")
                    }
                    .pickerStyle(.menu)

                    Text("Choose only after reviewing a physical scan. CoreBluetooth discovery does not prove RFCOMM access.")
                        .font(.caption)
                }

                Section("Redacted report") {
                    HStack {
                        Button("Copy") {
                            UIPasteboard.general.string = inventory.report
                        }
                        ShareLink(item: inventory.report) {
                            Label("Export", systemImage: "square.and.arrow.up")
                        }
                    }
                    Text(inventory.report)
                        .font(.system(.caption, design: .monospaced))
                        .textSelection(.enabled)
                }
            }
            .navigationTitle("Buds GATT Inventory")
        }
    }
}
