package dev.naijun.dununlocker

import android.Manifest
import android.app.IActivityManager
import android.app.Instrumentation
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.system.Os
import android.telephony.TelephonyManager
import android.util.Log
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import androidx.core.net.toUri
import dev.naijun.dununlocker.util.OneUiUtils
import org.lsposed.hiddenapibypass.HiddenApiBypass
import org.lsposed.lsparanoid.Obfuscate

const val UNLOCKER_TAG = "BrokerInstrumentation"

@Obfuscate
class BrokerInstrumentation : Instrumentation() {

    override fun onCreate(arguments: Bundle?) {
        HiddenApiBypass.setHiddenApiExemptions("")

        super.onCreate(arguments)

        if (arguments == null) {
            Log.e(UNLOCKER_TAG, "Arguments is null")
            finish(-1, Bundle())
            return
        }

        val am = IActivityManager.Stub.asInterface(
            ShizukuBinderWrapper(
                SystemServiceHelper.getSystemService(Context.ACTIVITY_SERVICE)
            )
        )
        try {
            am.startDelegateShellPermissionIdentity(
                Os.getuid(),
                arrayOf(Manifest.permission.WRITE_APN_SETTINGS)
            )

            val name = arguments.getString("name") ?: "DUN"
            val numeric = arguments.getString("numeric") ?: "45005"
            val mcc = arguments.getString("mcc") ?: "450"
            val mnc = arguments.getString("mnc") ?: "05"
            val apn = arguments.getString("apn") ?: ""
            val type = arguments.getString("type") ?: "default,mms,supl,rcs,dun"
            val protocol = arguments.getString("protocol") ?: "IPV4V6"
            val mmsc = arguments.getString("mmsc") ?: ""
            val mmsProxy = arguments.getString("mms_proxy") ?: ""
            val mmsPort = arguments.getString("mms_port") ?: ""
            val roamingProtocol = arguments.getString("roaming_protocol") ?: "IPV4V6"
            val server = arguments.getString("server") ?: "*"
            val authType = arguments.getString("auth_type") ?: "0"
            val user = arguments.getString("user") ?: ""
            val password = arguments.getString("password") ?: ""
            val subId = arguments.getInt("sub_id", -1)

            if (apn.isEmpty()) {
                Log.e(UNLOCKER_TAG, "APN address is empty")
                finish(-2, Bundle())
                return
            }

            val telephonyManager = context.getSystemService(TelephonyManager::class.java)

            val apnTypeSets = type.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()

            val contentResolver = context.contentResolver

            val uri = if (subId > 0) {
                "content://telephony/carriers/subId/$subId".toUri()
            } else {
                "content://telephony/carriers".toUri()
            }

            val values = ContentValues().apply {
                put("name", name)
                if (!OneUiUtils.isOneUi() || (OneUiUtils.getOneUiInfo().versionCode ?: 0) < 80500) {
                    put("numeric", numeric)
                }
                put("mcc", mcc)
                put("mnc", mnc)
                put("apn", apn)
                put("type", apnTypeSets.joinToString(","))
                put("protocol", protocol)
                put("roaming_protocol", roamingProtocol)
                put("authtype", authType.toIntOrNull() ?: 0)
                put("carrier_id", telephonyManager.createForSubscriptionId(subId).simSpecificCarrierId)
                put("user_visible", 1)
                put("current", 1)
                put("always_on", 1)

                if (subId > 0) {
                    put("sub_id", subId)
                }

                if (server.isNotEmpty()) put("server", server)

                if (user.isNotEmpty()) put("user", user)
                if (password.isNotEmpty()) put("password", password)

                if (mmsc.isNotEmpty()) put("mmsc", mmsc)
                if (mmsProxy.isNotEmpty()) put("mmsproxy", mmsProxy)
                if (mmsPort.isNotEmpty()) put("mmsport", mmsPort)
            }

            val insertedUri = contentResolver.insert(uri, values)

            if (insertedUri != null) {
                try {
                    val apnId = insertedUri.lastPathSegment
                    val preferredUri = if (subId > 0) {
                        "content://telephony/carriers/preferapn/subId/$subId".toUri()
                    } else {
                        "content://telephony/carriers/preferapn".toUri()
                    }
                    val preferredValues = ContentValues().apply {
                        put("apn_id", apnId)
                    }
                    contentResolver.update(preferredUri, preferredValues, null, null)
                } catch (e: Exception) {
                    Log.w(UNLOCKER_TAG, "Failed to set preferred APN: ${e.message}")
                }

                finish(0, Bundle().apply {
                    putString("inserted_uri", insertedUri.toString())
                })
            } else {
                // If the APN data is identical, there is a possibility of modification.
                Log.e(UNLOCKER_TAG, "Failed to insert APN")
                finish(-3, Bundle())
            }

        } catch (e: Exception) {
            Log.e(UNLOCKER_TAG, "Error in BrokerInstrumentation", e)
            finish(-100, Bundle().apply {
                putString("error", e.message ?: "Unknown error")
            })
        } finally {
            try {
                am.stopDelegateShellPermissionIdentity()
            } catch (e: Exception) {
                Log.w(UNLOCKER_TAG, "Failed to stop delegate permission: ${e.message}")
            }
        }
    }
}
