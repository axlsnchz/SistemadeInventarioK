package mx.edu.utez.sistemadeinventario

import android.app.Application


class InventarioApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}
