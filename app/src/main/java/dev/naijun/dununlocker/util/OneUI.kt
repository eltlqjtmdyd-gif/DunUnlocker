package dev.naijun.dununlocker.util

import android.os.Build
import java.lang.reflect.Field
import java.lang.reflect.Method

data class OneUiInfo(
    val isOneUi: Boolean,
    val versionCode: Long?,
)

object OneUiUtils {

    fun getOneUiInfo(): OneUiInfo {
        if (!isSamsungDevice()) {
            return OneUiInfo(
                isOneUi = false,
                versionCode = null,
            )
        }

        val rawFromProp = getOneUiCodeFromSystemProperty()
        if (rawFromProp != null && rawFromProp > 0) {
            return OneUiInfo(
                isOneUi = true,
                versionCode = rawFromProp,
            )
        }

        val rawFromSem = getOneUiCodeFromSemPlatformInt()
        if (rawFromSem != null && rawFromSem >= 10000) {
            return OneUiInfo(
                isOneUi = true,
                versionCode = rawFromSem.toLong(),
            )
        }

        return OneUiInfo(
            isOneUi = false,
            versionCode = null,
        )
    }

    fun isOneUi(): Boolean = getOneUiInfo().isOneUi

    private fun isSamsungDevice(): Boolean {
        return Build.MANUFACTURER.equals("samsung", ignoreCase = true) ||
                Build.BRAND.equals("samsung", ignoreCase = true)
    }

    private fun getOneUiCodeFromSystemProperty(): Long? {
        return try {
            // hidden API
            val method: Method = Build::class.java.getDeclaredMethod("getLong", String::class.java)
            method.isAccessible = true
            val value = method.invoke(null, "ro.build.version.oneui") as Long
            if (value > 0) value else null
        } catch (_: Throwable) {
            null
        }
    }

    private fun getOneUiCodeFromSemPlatformInt(): Int? {
        return try {
            val field: Field = Build.VERSION::class.java.getDeclaredField("SEM_PLATFORM_INT")
            val semPlatformInt = field.getInt(null)

            val oneUiCode = semPlatformInt - 90000
            if (oneUiCode > 0) oneUiCode else null
        } catch (_: Throwable) {
            null
        }
    }
}