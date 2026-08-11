package com.pnhd.galaxybridge.budsdiagnostics

class BudsStatus0x61(val payload: ByteArray) {
    // These offsets are estimated based on typical Buds2 Pro protocol patterns (revision 14)
    // and the specific bytes reported by the PM.
    
    val revision: Int = payload.getOrNull(0)?.toInt() ?: -1
    
    val earTypeRaw: Int = payload.getOrNull(1)?.toInt() ?: -1
    
    val batteryLeft: Int = payload.getOrNull(2)?.toInt() ?: -1
    val batteryRight: Int = payload.getOrNull(3)?.toInt() ?: -1
    
    val isCoupled: Boolean = (payload.getOrNull(4)?.toInt() ?: 0) != 0
    
    val mainConnectionRaw: Int = payload.getOrNull(5)?.toInt() ?: -1
    
    val leftPlacementRaw: Int = payload.getOrNull(6)?.toInt() ?: -1
    val rightPlacementRaw: Int = payload.getOrNull(7)?.toInt() ?: -1
    
    val batteryCaseRaw: Int = payload.getOrNull(8)?.toInt() ?: -1
    
    val noiseControlModeRaw: Int = payload.getOrNull(9)?.toInt() ?: -1

    fun getNoiseControlModeName(): String = when (noiseControlModeRaw) {
        0 -> "OFF"
        1 -> "ANC"
        2 -> "AMBIENT"
        3 -> "ADAPTIVE"
        else -> "UNKNOWN ($noiseControlModeRaw)"
    }

    override fun toString(): String = buildString {
        appendLine("0x61 EXTENDED_STATUS")
        appendLine("revision: $revision")
        appendLine("ear_type_raw: $earTypeRaw (UNVERIFIED)")
        appendLine("battery_l: $batteryLeft%")
        appendLine("battery_r: $batteryRight%")
        appendLine("coupled: $isCoupled")
        appendLine("main_conn_raw: $mainConnectionRaw (UNVERIFIED)")
        appendLine("left_placement: $leftPlacementRaw (UNVERIFIED)")
        appendLine("right_placement: $rightPlacementRaw (UNVERIFIED)")
        appendLine("case_battery_raw: $batteryCaseRaw (UNVERIFIED)")
        appendLine("noise_mode: ${getNoiseControlModeName()}")
    }
}
