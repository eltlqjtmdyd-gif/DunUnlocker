package dev.naijun.dununlocker.data

data class SimInfo(
    val subscriptionId: Int,
    val slotIndex: Int,
    val carrierName: String,
    val displayName: String,
    val mcc: String,
    val mnc: String
) {
    val formattedName: String
        get() = "$displayName ($carrierName)"
}
