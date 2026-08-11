package com.pnhd.galaxybridge.budsdiagnostics

class BoundedByteLog(private val maxBytes: Int) {
    private val bytes = ArrayDeque<Byte>()
    var totalBytes: Long = 0
        private set

    init {
        require(maxBytes > 0) { "maxBytes must be positive" }
    }

    @Synchronized
    fun append(source: ByteArray, count: Int = source.size) {
        require(count in 0..source.size) { "count is outside source bounds" }
        repeat(count) { index ->
            if (bytes.size == maxBytes) {
                bytes.removeFirst()
            }
            bytes.addLast(source[index])
            totalBytes += 1
        }
    }

    @Synchronized
    fun snapshot(): ByteArray = ByteArray(bytes.size) { index -> bytes.elementAt(index) }

    @Synchronized
    fun hexSnapshot(): String = bytes.joinToString(separator = "") { byte ->
        "%02X".format(byte.toInt() and 0xFF)
    }
}
