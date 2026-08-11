from __future__ import annotations

import plistlib
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
RFCOMM_UUID = "2e73a4ad-332d-41fc-90e2-16bef06523f2"


def require(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def main() -> None:
    required = [
        "ios/GalaxyBridgeBudsDiagnostics/GalaxyBridgeBudsDiagnostics.xcodeproj/project.pbxproj",
        "ios/GalaxyBridgeBudsDiagnostics/App/GalaxyBridgeBudsDiagnosticsApp.swift",
        "ios/GalaxyBridgeBudsDiagnostics/App/ContentView.swift",
        "ios/GalaxyBridgeBudsDiagnostics/Bluetooth/BudsGattInventory.swift",
        "ios/GalaxyBridgeBudsDiagnostics/Shared/GattDiagnosticReport.swift",
        "ios/GalaxyBridgeBudsDiagnostics/Resources/Info.plist",
        "wear/app/src/main/AndroidManifest.xml",
        "wear/app/src/main/java/com/pnhd/galaxybridge/budsdiagnostics/MainActivity.kt",
        "wear/app/src/main/java/com/pnhd/galaxybridge/budsdiagnostics/ReadOnlyRfcommProbe.kt",
        "wear/core/src/main/kotlin/com/pnhd/galaxybridge/budsdiagnostics/BoundedByteLog.kt",
        "wear/core/src/main/kotlin/com/pnhd/galaxybridge/budsdiagnostics/DiagnosticReport.kt",
    ]
    for relative_path in required:
        require((ROOT / relative_path).is_file(), f"missing required file: {relative_path}")

    info_plist = ROOT / "ios/GalaxyBridgeBudsDiagnostics/Resources/Info.plist"
    with info_plist.open("rb") as stream:
        plist = plistlib.load(stream)
    require("NSBluetoothAlwaysUsageDescription" in plist, "iOS Bluetooth purpose string missing")

    pbxproj = (ROOT / required[0]).read_text(encoding="utf-8")
    for filename in (
        "GalaxyBridgeBudsDiagnosticsApp.swift",
        "ContentView.swift",
        "BudsGattInventory.swift",
        "GattDiagnosticReport.swift",
        "GattDiagnosticReportTests.swift",
    ):
        require(filename in pbxproj, f"Xcode project does not reference {filename}")

    ios_source = "\n".join(
        path.read_text(encoding="utf-8")
        for path in (ROOT / "ios/GalaxyBridgeBudsDiagnostics").rglob("*.swift")
    )
    require(".writeValue(" not in ios_source, "iOS diagnostic contains a characteristic write")
    require("discoverServices(nil)" in ios_source, "iOS diagnostic does not inventory all services")
    require("discoverCharacteristics(nil" in ios_source, "iOS diagnostic does not inventory all characteristics")

    wear_source = "\n".join(
        path.read_text(encoding="utf-8")
        for path in (ROOT / "wear").rglob("*.kt")
    )
    require(RFCOMM_UUID in wear_source, "Wear diagnostic RFCOMM UUID mismatch")
    require(
        "createRfcommSocketToServiceRecord(SERVICE_UUID)" in wear_source,
        "Wear diagnostic is not using public SDP UUID resolution",
    )
    require(".outputStream" not in wear_source, "Wear diagnostic exposes an RFCOMM output stream")
    require("MAX_CAPTURE_BYTES = 4096" in wear_source, "Wear raw-byte capture is not bounded at 4096 bytes")
    require("status_query_implemented: false" in wear_source, "status-query safety declaration missing")

    print("R4 static validation PASS")
    print(f"required_files={len(required)}")
    print("ios_unknown_write_api_calls=0")
    print("wear_rfcomm_output_stream_accesses=0")
    print(f"rfcomm_uuid={RFCOMM_UUID}")


if __name__ == "__main__":
    main()
