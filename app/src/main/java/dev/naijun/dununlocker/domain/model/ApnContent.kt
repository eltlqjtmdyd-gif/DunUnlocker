package dev.naijun.dununlocker.domain.model

data class ApnContent(
    val name: String,
    val numeric: String,
    val mcc: String,
    val mnc: String,
    val apn: String,
    val type: String = "default,mms,supl,rcs,dun",
    val protocol: String = "IPV4V6",
    val mmsc: String,
    val mmsProxy: String,
    val mmsPort: String,
    val roamingProtocol: String = "IPV4V6",
    val server: String = "*",
    val authType: String = "0", // 0: None, 1: PAP, 2: CHAP, 3: PAP or CHAP
    val user: String = "",
    val password: String = ""
) {
    companion object {
        fun getDefaultConfig(carrierType: CarrierType): ApnContent {
            return when (carrierType) {
                CarrierType.CUSTOM -> ApnContent(
                    name = "",
                    numeric = "",
                    mcc = "",
                    mnc = "",
                    apn = "",
                    type = "default,supl,mms,dun",
                    protocol = "IPV4V6",
                    mmsc = "",
                    mmsProxy = "",
                    mmsPort = "",
                    roamingProtocol = "IPV4V6",
                    server = "*"
                )

                CarrierType.SKT_5G -> ApnContent(
                    name = "SKT_5G_DUN",
                    numeric = "45005",
                    mcc = "450",
                    mnc = "05",
                    apn = "5g.sktelecom.com",
                    type = "default,supl,rcs,mms,dun",
                    protocol = "IPV4V6",
                    mmsc = "",
                    mmsProxy = "",
                    mmsPort = "",
                    roamingProtocol = "IPV4",
                    server = ""
                )

                CarrierType.SKT_LTE -> ApnContent(
                    name = "SKT_LTE_DUN",
                    numeric = "45005",
                    mcc = "450",
                    mnc = "05",
                    apn = "lte.sktelecom.com",
                    type = "default,supl,rcs,mms,dun",
                    protocol = "IPV4V6",
                    mmsc = "http://omms.nate.com:9082/oma_mms",
                    mmsProxy = "smart.nate.com",
                    mmsPort = "9093",
                    roamingProtocol = "IPV4",
                    server = ""
                )

                CarrierType.KT_5G -> ApnContent(
                    name = "KT_5G_DUN",
                    numeric = "45008",
                    mcc = "450",
                    mnc = "08",
                    apn = "internet",
                    type = "default,supl,rcs,mms,dun",
                    protocol = "IPV4V6",
                    mmsc = "",
                    mmsProxy = "",
                    mmsPort = "",
                    roamingProtocol = "IPV4",
                    server = ""
                )

                CarrierType.KT_LTE -> ApnContent(
                    name = "KT_LTE_DUN",
                    numeric = "45008",
                    mcc = "450",
                    mnc = "08",
                    apn = "lte.ktfwing.com",
                    type = "default,supl,rcs,mms,dun",
                    protocol = "IPV4V6",
                    mmsc = "http://mmsc.ktfwing.com:9082",
                    mmsProxy = "",
                    mmsPort = "9093",
                    roamingProtocol = "IPV4",
                    server = ""
                )

                CarrierType.LGU_PLUS_5G -> ApnContent(
                    name = "LGU_5G_DUN",
                    numeric = "45006",
                    mcc = "450",
                    mnc = "06",
                    apn = "internet.lguplus.co.kr",
                    type = "default,supl,rcs,mms,dun",
                    protocol = "IPV4V6",
                    mmsc = "",
                    mmsProxy = "",
                    mmsPort = "",
                    roamingProtocol = "IPV4",
                    server = ""
                )

                CarrierType.LGU_PLUS_LTE -> ApnContent(
                    name = "LGU_LTE_DUN",
                    numeric = "45006",
                    mcc = "450",
                    mnc = "06",
                    apn = "internet.lguplus.co.kr",
                    type = "default,supl,rcs,mms,dun",
                    protocol = "IPV4V6",
                    mmsc = "http://omammsc.uplus.co.kr:9084",
                    mmsProxy = "",
                    mmsPort = "9084",
                    roamingProtocol = "IPV4",
                    server = ""
                )
            }
        }

        fun getAuthTypeCode(authType: String): String {
            return when (authType) {
                "없음", "None" -> "0"
                "PAP" -> "1"
                "CHAP" -> "2"
                "PAP 또는 CHAP", "PAP or CHAP" -> "3"
                else -> "0"
            }
        }
    }
}