package mx.edu.utez.sistemadeinventario.ui.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "inventario_session"
)
data class UserSession(
    val token: String? = null,
    val username: String? = null,
    val rol: String? = null,
    val isLoggedIn: Boolean = false
) {
    val isAdmin: Boolean get() = rol.equals("ADMIN", ignoreCase = true)
}

class SessionPreferencesRepository(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val USERNAME = stringPreferencesKey("username")
        val ROL = stringPreferencesKey("rol")
    }

    val sessionFlow: Flow<UserSession> = context.sessionDataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            val token = prefs[Keys.TOKEN]
            UserSession(
                token = token,
                username = prefs[Keys.USERNAME],
                rol = prefs[Keys.ROL],
                isLoggedIn = !token.isNullOrBlank()
            )
        }

    suspend fun saveSession(token: String, username: String, rol: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
            prefs[Keys.USERNAME] = username
            prefs[Keys.ROL] = rol
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { it.clear() }
    }

    fun getTokenSync(): String? = runBlocking { sessionFlow.first().token }
}
