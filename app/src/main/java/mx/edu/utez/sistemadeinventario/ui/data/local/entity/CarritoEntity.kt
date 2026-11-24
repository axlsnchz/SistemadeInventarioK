package mx.edu.utez.sistemadeinventario.ui.data.local.entity;

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(

    tableName = "producto",
)
data class CarritoEntity (

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "nombre")
    val nombre: String,
    @ColumnInfo(name = "sku")
    val sku: String,

    @ColumnInfo(name = "precio_unitario")
    val precioUnitario: Double,

    @ColumnInfo(name = "stock")
    val stock: Int,

    @ColumnInfo(name = "stock_minimo")
    val stockMinimo: Int,


    @ColumnInfo(name = "categoria")
    val cateogoria: String,


    @ColumnInfo(name = "local_image_path")
    val fotoUri: String? = null,

    @ColumnInfo(name = "nfc_id")
    val nfcId: Int,


    )