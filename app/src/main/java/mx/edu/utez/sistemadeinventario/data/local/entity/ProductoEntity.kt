package mx.edu.utez.sistemadeinventario.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(

    tableName = "producto",
)
data class ProductoEntity(


    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "nombre")
    val nombre: String,


    @ColumnInfo(name = "sku")
    val sku: String? = null,

    @ColumnInfo(name = "precio")
    val precio: Double,

    @ColumnInfo(name = "stock")
    val stock: Int,

    @ColumnInfo(name = "stock_minimo")
    val stockMinimo: Int = 0,

    @ColumnInfo(name = "categoria")
    val categoria: String? = null,


    @ColumnInfo(name = "foto_url")
    val fotoUrl: String? = null,

    @ColumnInfo(name = "nfc_id")
    val nfcId: String? = null
)
