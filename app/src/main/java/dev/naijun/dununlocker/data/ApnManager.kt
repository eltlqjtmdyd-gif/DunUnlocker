package dev.naijun.dununlocker.data

import android.app.ActivityManager
import android.app.IActivityManager
import android.app.UiAutomationConnection
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionInfo
import android.telephony.TelephonyFrameworkInitializer
import android.util.Log
import com.android.internal.telephony.ISub
import dev.naijun.dununlocker.BrokerInstrumentation
import dev.naijun.dununlocker.R
import dev.naijun.dununlocker.domain.model.ApnContent
import dev.naijun.dununlocker.domain.model.CarrierType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lsposed.lsparanoid.Obfuscate
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import kotlin.collections.emptyList

@Obfuscate
class ApnManager(
    private val context: Context
) {
    companion object {
        private const val TAG = "ApnManager"
    }

    private val sub: ISub
        get() = ISub.Stub.asInterface(
            TelephonyFrameworkInitializer
                .getTelephonyServiceManager()
                .subscriptionServiceRegisterer
                .get()?.let {
                    ShizukuBinderWrapper(
                        it,
                    )
                }
        )

    fun getActiveSubscriptions(): List<SimInfo> {
        return try {
            val activeSubscriptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                sub.getActiveSubscriptionInfoList(null, null, true)
            } else {
                sub::class.java.getMethod(
                    "getActiveSubscriptionInfoList",
                    String::class.java,
                    String::class.java,
                ).invoke(sub, null, null)
                    ?.let { it as? List<*> }
                    ?.filterIsInstance<SubscriptionInfo>()
            } ?: return emptyList()

            activeSubscriptions.map { info ->
                SimInfo(
                    subscriptionId = info.subscriptionId,
                    slotIndex = info.simSlotIndex,
                    carrierName = info.carrierName?.toString()
                        ?: context.getString(R.string.sim_unknown_carrier),
                    displayName = info.displayName?.toString()
                        ?: context.getString(R.string.sim_default_name, info.simSlotIndex + 1),
                    mcc = info.mccString ?: "",
                    mnc = info.mncString ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get subscriptions", e)
            emptyList()
        }
    }

    suspend fun applyApnConfig(
        carrierType: CarrierType,
        customApnContent: ApnContent? = null,
        subscriptionId: Int? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val apnContent = customApnContent ?: ApnContent.getDefaultConfig(carrierType)

            val bundle = Bundle().apply {
                putString("name", apnContent.name)
                putString("numeric", apnContent.numeric)
                putString("mcc", apnContent.mcc)
                putString("mnc", apnContent.mnc)
                putString("apn", apnContent.apn)
                putString("type", apnContent.type)
                putString("protocol", apnContent.protocol)
                putString("mmsc", apnContent.mmsc)
                putString("mms_proxy", apnContent.mmsProxy)
                putString("mms_port", apnContent.mmsPort)
                putString("roaming_protocol", apnContent.roamingProtocol)
                putString("server", apnContent.server)
                putString("auth_type", apnContent.authType)
                putString("user", apnContent.user)
                putString("password", apnContent.password)

                subscriptionId?.let { putInt("sub_id", it) }
            }

            overrideConfigUsingBroker(bundle)

            kotlinx.coroutines.delay(500)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply APN config", e)
            Result.failure(e)
        }
    }

    fun createCustomApnContent(
        carrierType: CarrierType,
        name: String,
        apnAddress: String,
        apnType: String,
        mmsc: String,
        mmsProxy: String,
        mmsPort: String,
        mcc: String,
        mnc: String,
        authType: String,
        protocol: String,
        roamingProtocol: String,
        useMmsSettings: Boolean
    ): ApnContent {
        val defaultConfig = ApnContent.getDefaultConfig(carrierType)

        return ApnContent(
            name = name,
            numeric = "$mcc$mnc",
            mcc = mcc,
            mnc = mnc,
            apn = apnAddress,
            type = apnType,
            protocol = protocol,
            mmsc = if (useMmsSettings) mmsc else "",
            mmsProxy = if (useMmsSettings) mmsProxy else "",
            mmsPort = if (useMmsSettings) mmsPort else "",
            roamingProtocol = roamingProtocol,
            server = defaultConfig.server,
            authType = ApnContent.getAuthTypeCode(authType),
            user = defaultConfig.user,
            password = defaultConfig.password
        )
    }

    private fun overrideConfigUsingBroker(bundle: Bundle) {
        val am = IActivityManager.Stub.asInterface(
            ShizukuBinderWrapper(
                SystemServiceHelper.getSystemService(Context.ACTIVITY_SERVICE)
            )
        )

        am.startInstrumentation(
            ComponentName(context, BrokerInstrumentation::class.java),
            null,
            ActivityManager.INSTR_FLAG_NO_RESTART,
            bundle,
            null,
            UiAutomationConnection(),
            0,
            null
        )
    }
}
