package mx.edu.utez.sistemadeinventario.data.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/** Resultado de intentar grabar una etiqueta. */
sealed interface NfcWriteResult {
    data object Exito : NfcWriteResult
    data class Error(val mensaje: String) : NfcWriteResult
}

/**
 * Maneja el NFC de la app. Se crea en MainActivity.
 *
 * MODO LECTURA (normal): cada etiqueta detectada emite su texto en [nfcEvents].
 *   (si la etiqueta no tiene texto, emite "" -> el ViewModel lo trata como "no encontrado")
 *
 * MODO ESCRITURA: después de llamar [startWriting], la SIGUIENTE etiqueta detectada se graba
 *   en vez de leerse, y el resultado sale por [writeEvents].
 */
class NfcManager(private val activity: Activity) {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _nfcEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val nfcEvents: SharedFlow<String> = _nfcEvents.asSharedFlow()

    private val _writeEvents = MutableSharedFlow<NfcWriteResult>(extraBufferCapacity = 1)
    val writeEvents: SharedFlow<NfcWriteResult> = _writeEvents.asSharedFlow()

    // Texto que se va a grabar en la próxima etiqueta (null = modo lectura)
    @Volatile
    private var textoPendiente: String? = null

    val isAvailable: Boolean get() = nfcAdapter != null
    val isEnabled: Boolean get() = nfcAdapter?.isEnabled == true

    /** Llamar en onResume: la app recibe las etiquetas mientras está abierta. */
    fun enableReadMode() {
        val adapter = nfcAdapter ?: return
        val intent = Intent(activity, activity.javaClass)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(
            activity, 0, intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        adapter.enableForegroundDispatch(activity, pendingIntent, null, null)
    }

    /** Llamar en onPause. */
    fun disableReadMode() {
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    /** Activa el modo escritura: la próxima etiqueta que se acerque se graba con este texto. */
    fun startWriting(texto: String) {
        textoPendiente = texto
    }

    /** Cancela el modo escritura (por ejemplo al cerrar el diálogo). */
    fun cancelWriting() {
        textoPendiente = null
    }

    /** Llamar desde onNewIntent de MainActivity. */
    @Suppress("DEPRECATION")
    fun handleIntent(intent: Intent) {
        val accion = intent.action
        val esNfc = accion == NfcAdapter.ACTION_NDEF_DISCOVERED ||
                accion == NfcAdapter.ACTION_TECH_DISCOVERED ||
                accion == NfcAdapter.ACTION_TAG_DISCOVERED
        if (!esNfc) return

        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return

        val texto = textoPendiente
        if (texto != null) {
            // MODO ESCRITURA
            textoPendiente = null
            scope.launch {
                val resultado = NfcWriter.writeText(tag, texto)
                _writeEvents.tryEmit(
                    if (resultado.isSuccess) NfcWriteResult.Exito
                    else NfcWriteResult.Error(resultado.exceptionOrNull()?.message ?: "Error al grabar")
                )
            }
        } else {
            // MODO LECTURA
            scope.launch {
                _nfcEvents.tryEmit(NfcParser.readText(intent) ?: "")
            }
        }
    }
}
