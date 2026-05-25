package dev.naijun.dununlocker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.naijun.dununlocker.data.ShizukuManager
import dev.naijun.dununlocker.ui.screen.HomeScreen
import dev.naijun.dununlocker.ui.theme.DunUnlockerTheme
import org.lsposed.hiddenapibypass.HiddenApiBypass
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        HiddenApiBypass.setHiddenApiExemptions("")

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ShizukuManager.initialize()

        setContent {
            DunUnlockerTheme {
                HomeScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ShizukuManager.cleanup()
    }
}
