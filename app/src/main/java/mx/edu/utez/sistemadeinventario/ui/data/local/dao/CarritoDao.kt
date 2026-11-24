package mx.edu.utez.sistemadeinventario.ui.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.sistemadeinventario.ui.data.local.entity.CarritoItemEntity

@Dao
interface CarritoDao {

    @Query("SELECT * FROM carrito ORDER BY nombre COLLATE NOCASE")
    fun observeAll(): Flow<List<CarritoItemEntity>>

    @Query("SELECT * FROM carrito WHERE producto_id = :productoId")
    suspend fun getByProductoId(productoId: Int): CarritoItemEntity?

    @Upsert
    suspend fun upsert(item: CarritoItemEntity)

    @Query("UPDATE carrito SET cantidad = :cantidad WHERE producto_id = :productoId")
    suspend fun updateCantidad(productoId: Int, cantidad: Int)

    @Query("DELETE FROM carrito WHERE producto_id = :productoId")
    suspend fun delete(productoId: Int)

    @Query("DELETE FROM carrito")
    suspend fun clear()
}