package mx.edu.utez.sistemadeinventario.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "carrito")
data class CarritoEntity(


    @PrimaryKey
    @ColumnInfo(name = "producto_id")
    val productoId: Long,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "precio_unitario")
    val precioUnitario: Double,
    @ColumnInfo(name = "cantidad")
    val cantidad: Int = 1,
    @ColumnInfo(name = "foto_url")
    val fotoUrl: String? = null,
    @ColumnInfo(name = "stock_disponible")
    val stockDisponible: Int
)