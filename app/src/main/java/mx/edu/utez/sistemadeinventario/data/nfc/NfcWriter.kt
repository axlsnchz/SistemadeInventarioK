package mx.edu.utez.sistemadeinventario.data.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException


object NfcWriter {

    suspend fun writeText(tag: Tag, texto: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val mensaje = NdefMessage(arrayOf(NdefRecord.createTextRecord("es", texto)))
            val ndef = Ndef.get(tag)

            if (ndef != null) {
                escribirEnNdef(ndef, mensaje)
            } else {
                escribirEnEtiquetaVirgen(tag, mensaje)
            }
        }
    }

    private fun escribirEnNdef(ndef: Ndef, mensaje: NdefMessage) {
        ndef.connect()
        try {
            if (!ndef.isWritable) throw IOException("La etiqueta es de solo lectura")
            if (ndef.maxSize < mensaje.toByteArray().size) throw IOException("El texto no cabe en la etiqueta")
            ndef.writeNdefMessage(mensaje)
        } finally {
            ndef.close()
        }
    }

    private fun escribirEnEtiquetaVirgen(tag: Tag, mensaje: NdefMessage) {
        val formateable = NdefFormatable.get(tag)
            ?: throw IOException("Esta etiqueta no es compatible")
        formateable.connect()
        try {
            formateable.format(mensaje)
        } finally {
            formateable.close()
        }
    }
}
