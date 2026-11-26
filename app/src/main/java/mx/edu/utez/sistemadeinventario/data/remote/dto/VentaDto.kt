package mx.edu.utez.sistemadeinventario.data.remote.dto


data class VentaRequest(
    val items: List<ItemVentaRequest>
)

data class ItemVentaRequest(
    val productoId: Long,
    val cantidad: Int
)


data class VentaResponse(
    val id: Long,
    val fecha: String? = null,
    val total: Double = 0.0,
    val vendedor: String? = null,
    val items: List<ItemVentaResponse> = emptyList()
)

data class ItemVentaResponse(
    val producto: String = "",
    val cantidad: Int = 0,
    val precioUnitario: Double = 0.0
)
