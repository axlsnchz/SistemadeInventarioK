package mx.edu.utez.sistemadeinventario.ui.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.sistemadeinventario.ui.data.local.entity.ProductoEntity


@Dao
interface ProductoDao {

    @Query("SELECT * FROM producto ORDER BY nombre COLLATE NOCASE")
    fun observeAll(): Flow<List<ProductoEntity>>

    @Query(
        """
        SELECT * FROM producto
        WHERE nombre LIKE '%' || :query || '%'
           OR sku LIKE '%' || :query || '%'
        ORDER BY nombre COLLATE NOCASE
        """
    )
    fun search(query: String): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM producto WHERE id = :id")
    suspend fun getById(id: Int): ProductoEntity?

    @Query("SELECT * FROM producto WHERE nfc_id = :nfcId LIMIT 1")
    suspend fun getByNfcId(nfcId: String): ProductoEntity?

    @Upsert
    suspend fun upsertAll(productos: List<ProductoEntity>)

    @Query("DELETE FROM producto")
    suspend fun clear()
}