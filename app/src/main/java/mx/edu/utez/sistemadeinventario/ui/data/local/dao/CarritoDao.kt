package mx.edu.utez.sistemadeinventario.ui.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.sistemadeinventario.ui.data.local.entity.CarritoEntity

@Dao
interface CarritoDao {

    @Query("SELECT * FROM carrito ORDER BY nombre COLLATE NOCASE")
    fun observeAll(): Flow<List<CarritoEntity>>

    @Query("SELECT * FROM carrito WHERE producto_id = :productoId")
    suspend fun getByProductoId(productoId: Int): CarritoEntity?

    @Upsert
    suspend fun upsert(item: CarritoEntity)

    @Query("UPDATE carrito SET cantidad = :cantidad WHERE producto_id = :productoId")
    suspend fun updateCantidad(productoId: Int, cantidad: Int)

    @Query("DELETE FROM carrito WHERE producto_id = :productoId")
    suspend fun delete(productoId: Int)

    @Query("DELETE FROM carrito")
    suspend fun clear()
}