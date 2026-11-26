package mx.edu.utez.sistemadeinventario

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.edu.utez.sistemadeinventario.data.nfc.NfcManager
import mx.edu.utez.sistemadeinventario.ui.navigation.AppNavGraph
import mx.edu.utez.sistemadeinventario.ui.theme.SistemadeInventarioTheme

class MainActivity : ComponentActivity() {

    private lateinit var nfcManager: NfcManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        nfcManager = NfcManager(this)

        setContent {
            SistemadeInventarioTheme {
                AppNavGraph(nfcManager = nfcManager)
            }
        }

        intent?.let { nfcManager.handleIntent(it) }
    }

    override fun onResume() {
        super.onResume()
        nfcManager.enableReadMode()
    }

    override fun onPause() {
        super.onPause()
        nfcManager.disableReadMode()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        nfcManager.handleIntent(intent)
    }
}
