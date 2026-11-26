package mx.edu.utez.sistemadeinventario.data.remote.dto


data class ProductoDto(
    val id: Long,
    val nombre: String,
    val sku: String? = null,
    val precio: Double,
    val stock: Int,
    val stockMinimo: Int = 0,
    val categoria: String? = null,
    val fotoUrl: String? = null,
    val nfcId: String? = null
)


data class ProductoRequest(
    val nombre: String,
    val sku: String? = null,
    val precio: Double,
    val stock: Int,
    val stockMinimo: Int = 0,
    val categoria: String? = null,
    val nfcId: String? = null,
    val fotoUrl: String? = null
)
