package mx.edu.utez.sistemadeinventario.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.sistemadeinventario.data.local.entity.CarritoEntity

@Dao
interface CarritoDao {

    @Query("SELECT * FROM carrito ORDER BY nombre COLLATE NOCASE")
    fun observeAll(): Flow<List<CarritoEntity>>

    @Query("SELECT COALESCE(SUM(precio_unitario * cantidad), 0.0) FROM carrito")
    fun observeTotal(): Flow<Double>

    @Query("SELECT * FROM carrito WHERE producto_id = :productoId")
    suspend fun getByProductoId(productoId: Long): CarritoEntity?

    @Upsert
    suspend fun upsert(item: CarritoEntity)

    @Query("UPDATE carrito SET cantidad = :cantidad WHERE producto_id = :productoId")
    suspend fun updateCantidad(productoId: Long, cantidad: Int)

    @Query("DELETE FROM carrito WHERE producto_id = :productoId")
    suspend fun delete(productoId: Long)

    @Query("DELETE FROM carrito")
    suspend fun clear()
}
