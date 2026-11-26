package mx.edu.utez.sistemadeinventario.data.nfc

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef


object NfcParser {

    @Suppress("DEPRECATION")
    fun readText(intent: Intent): String? {
        val mensajes = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        val mensaje = mensajes?.firstOrNull() as? NdefMessage
        if (mensaje != null) return textFromMessage(mensaje)

        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return null
        return readText(tag)
    }

    fun readText(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            val mensaje = ndef.ndefMessage
            if (mensaje == null) null else textFromMessage(mensaje)
        } catch (e: Exception) {
            null
        } finally {
            try { ndef.close() } catch (_: Exception) { }
        }
    }

    private fun textFromMessage(mensaje: NdefMessage): String? {
        val registro = mensaje.records.firstOrNull {
            it.tnf == NdefRecord.TNF_WELL_KNOWN && it.type.contentEquals(NdefRecord.RTD_TEXT)
        } ?: return null

        val payload = registro.payload
        if (payload.isEmpty()) return null

        // El primer byte dice la codificación (bit 7) y cuántos bytes mide el código de idioma ("es", "en"...)
        val estado = payload[0].toInt()
        val charset = if ((estado and 0x80) == 0) Charsets.UTF_8 else Charsets.UTF_16
        val largoIdioma = estado and 0x3F

        return String(payload, 1 + largoIdioma, payload.size - 1 - largoIdioma, charset).trim()
    }
}
