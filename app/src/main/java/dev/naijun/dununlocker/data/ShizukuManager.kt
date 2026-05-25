package dev.naijun.dununlocker.data

import android.content.pm.PackageManager
import androidx.annotation.StringRes
import dev.naijun.dununlocker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

data class ShizukuErrorMessage(
    @param:StringRes val resId: Int,
    val args: List<String> = emptyList()
)

object ShizukuManager {
    private val _isGranted = MutableStateFlow(false)
    val isGranted: StateFlow<Boolean> = _isGranted.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _errorMessage = MutableStateFlow<ShizukuErrorMessage?>(null)
    val errorMessage: StateFlow<ShizukuErrorMessage?> = _errorMessage.asStateFlow()

    private val REQUEST_CODE = 1001

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        _isRunning.value = true
        checkPermission()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _isRunning.value = false
        _isGranted.value = false
    }

    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQUEST_CODE) {
                val granted = grantResult == PackageManager.PERMISSION_GRANTED
                _isGranted.value = granted
                if (!granted) {
                    setError(R.string.shizuku_permission_denied)
                } else {
                    _errorMessage.value = null
                }
            }
        }

    fun initialize() {
        Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)

        checkStatus()
    }

    fun cleanup() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
    }

    private fun checkStatus() {
        try {
            _isRunning.value = Shizuku.pingBinder()
            if (_isRunning.value) {
                checkPermission()
            }
        } catch (e: Exception) {
            _isRunning.value = false
            _isGranted.value = false
            setError(R.string.shizuku_permission_not_running)
        }
    }

    private fun checkPermission() {
        try {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                _isGranted.value = true
                _errorMessage.value = null
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                _isGranted.value = false
                setError(R.string.shizuku_permission_required)
            } else {
                _isGranted.value = false
            }
        } catch (e: Exception) {
            _isGranted.value = false
            setError(R.string.shizuku_permission_check_error, e.message.orEmpty())
        }
    }

    fun requestPermission() {
        try {
            if (!_isRunning.value) {
                setError(R.string.shizuku_permission_not_running)
                return
            }

            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                _isGranted.value = true
                _errorMessage.value = null
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                setError(R.string.shizuku_permission_allow_prompt)
                Shizuku.requestPermission(REQUEST_CODE)
            } else {
                Shizuku.requestPermission(REQUEST_CODE)
            }
        } catch (e: Exception) {
            setError(R.string.shizuku_permission_request_error, e.message.orEmpty())
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun setError(@StringRes resId: Int, vararg args: String) {
        _errorMessage.value = ShizukuErrorMessage(resId, args.toList())
    }
}
