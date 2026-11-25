package mx.edu.utez.sistemadeinventario.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.edu.utez.sistemadeinventario.ui.data.local.dao.CarritoDao
import mx.edu.utez.sistemadeinventario.ui.data.local.dao.ProductoDao
import mx.edu.utez.sistemadeinventario.ui.data.local.entity.CarritoEntity
import mx.edu.utez.sistemadeinventario.ui.data.local.entity.ProductoEntity


@Database(
    entities = [ProductoEntity::class, CarritoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun carritoDao(): CarritoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventario_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}