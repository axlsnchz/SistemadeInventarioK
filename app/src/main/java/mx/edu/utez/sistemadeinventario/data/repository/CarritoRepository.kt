package mx.edu.utez.sistemadeinventario.data.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.utez.sistemadeinventario.data.local.dao.CarritoDao
import mx.edu.utez.sistemadeinventario.data.local.entity.CarritoEntity
import mx.edu.utez.sistemadeinventario.data.local.entity.ProductoEntity

class CarritoRepository(
    private val carritoDao: CarritoDao
) {
    val items: Flow<List<CarritoEntity>> = carritoDao.observeAll()
    val total: Flow<Double> = carritoDao.observeTotal()

    suspend fun agregar(producto: ProductoEntity): Result<Unit> {
        if (producto.stock <= 0) {
            return Result.failure(Exception("No hay existencias de ${producto.nombre}."))
        }

        val existente = carritoDao.getByProductoId(producto.id)
        if (existente == null) {
            carritoDao.upsert(producto.toCarritoEntity())
            return Result.success(Unit)
        }

        val nuevaCantidad = existente.cantidad + 1
        if (nuevaCantidad > producto.stock) {
            return Result.failure(Exception("Solo hay ${producto.stock} en existencia de ${producto.nombre}."))
        }
        carritoDao.upsert(
            existente.copy(
                nombre = producto.nombre,
                precioUnitario = producto.precio,
                cantidad = nuevaCantidad,
                fotoUrl = producto.fotoUrl,
                stockDisponible = producto.stock
            )
        )
        return Result.success(Unit)
    }

    suspend fun cambiarCantidad(productoId: Long, cantidad: Int): Result<Unit> {
        if (cantidad <= 0) {
            carritoDao.delete(productoId)
            return Result.success(Unit)
        }
        val item = carritoDao.getByProductoId(productoId)
            ?: return Result.failure(Exception("El producto ya no esta en el carrito"))
        if (cantidad > item.stockDisponible) {
            return Result.failure(Exception("Solo hay ${item.stockDisponible} en existencia"))
        }
        carritoDao.updateCantidad(productoId, cantidad)
        return Result.success(Unit)
    }

    suspend fun quitar(productoId: Long) = carritoDao.delete(productoId)

    suspend fun vaciar() = carritoDao.clear()
}

private fun ProductoEntity.toCarritoEntity() = CarritoEntity(
    productoId = id,
    nombre = nombre,
    precioUnitario = precio,
    cantidad = 1,
    fotoUrl = fotoUrl,
    stockDisponible = stock
)
