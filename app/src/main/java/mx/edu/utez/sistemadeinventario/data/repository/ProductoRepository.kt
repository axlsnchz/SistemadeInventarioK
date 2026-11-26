package mx.edu.utez.sistemadeinventario.data.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import mx.edu.utez.sistemadeinventario.data.local.AppDatabase
import mx.edu.utez.sistemadeinventario.data.local.entity.ProductoEntity
import mx.edu.utez.sistemadeinventario.data.remote.ApiService
import mx.edu.utez.sistemadeinventario.data.remote.dto.ProductoDto
import mx.edu.utez.sistemadeinventario.data.remote.dto.ProductoRequest
import okhttp3.MultipartBody

data class CrearProductoResultado(
    val producto: ProductoEntity,
    val errorFoto: String? = null
) {
    val completo: Boolean get() = errorFoto == null
}


class ProductoRepository(
    private val api: ApiService,
    private val database: AppDatabase
) {
    private val productoDao = database.productoDao()


    fun observarProductos(query: String = ""): Flow<List<ProductoEntity>> {
        val q = query.trim()
        return if (q.isEmpty()) productoDao.observeAll() else productoDao.search(q)
    }

    fun observarProducto(id: Long): Flow<ProductoEntity?> = productoDao.observeById(id)


    suspend fun refresh(): Result<Unit> =
        safeApiCall { api.getProductos() }.map { lista ->
            database.withTransaction {
                productoDao.clear()
                productoDao.upsertAll(lista.map { it.toEntity() })
            }
        }

    suspend fun obtenerProducto(id: Long): Result<ProductoEntity> =
        safeApiCall { api.getProducto(id) }.fold(
            onSuccess = { dto ->
                val entity = dto.toEntity()
                productoDao.upsert(entity)
                Result.success(entity)
            },
            onFailure = { error ->
                val local = if (error is SinConexionException) productoDao.getById(id) else null
                if (local != null) Result.success(local) else Result.failure(error)
            }
        )

    suspend fun buscarPorNfc(nfcId: String): Result<ProductoEntity> =
        safeApiCall { api.getProductoByNfc(nfcId) }.fold(
            onSuccess = { dto ->
                val entity = dto.toEntity()
                productoDao.upsert(entity)
                Result.success(entity)
            },
            onFailure = { error ->
                when (error) {
                    is NoEncontradoException ->
                        Result.failure(NoEncontradoException("Esta etiqueta no pertenece a ningun producto"))
                    is SinConexionException -> {
                        val local = productoDao.getByNfcId(nfcId)
                        if (local != null) Result.success(local) else Result.failure(error)
                    }
                    else -> Result.failure(error)
                }
            }
        )


    suspend fun crearProducto(
        request: ProductoRequest,
        foto: MultipartBody.Part?
    ): Result<CrearProductoResultado> {
        val creado = safeApiCall { api.crearProducto(request) }
            .getOrElse { return Result.failure(it) }
        return Result.success(guardarYSubirFoto(creado, foto))
    }

    suspend fun editarProducto(
        id: Long,
        request: ProductoRequest,
        foto: MultipartBody.Part?
    ): Result<CrearProductoResultado> {
        val editado = safeApiCall { api.editarProducto(id, request) }
            .getOrElse { return Result.failure(it) }
        return Result.success(guardarYSubirFoto(editado, foto))
    }

    suspend fun subirFoto(id: Long, foto: MultipartBody.Part): Result<ProductoEntity> =
        safeApiCall { api.subirFoto(id, foto) }.map { dto ->
            val entity = dto.toEntity()
            productoDao.upsert(entity)
            entity
        }

    private suspend fun guardarYSubirFoto(
        dto: ProductoDto,
        foto: MultipartBody.Part?
    ): CrearProductoResultado {
        val producto = dto.toEntity()
        productoDao.upsert(producto)
        if (foto == null) return CrearProductoResultado(producto)

        return safeApiCall { api.subirFoto(producto.id, foto) }.fold(
            onSuccess = { actualizado ->
                val conFoto = actualizado.toEntity()
                productoDao.upsert(conFoto)
                CrearProductoResultado(conFoto)
            },
            onFailure = { error ->
                CrearProductoResultado(
                    producto = producto,
                    errorFoto = error.message ?: "No se pudo subir la foto"
                )
            }
        )
    }
}

private fun ProductoDto.toEntity() = ProductoEntity(
    id = id,
    nombre = nombre,
    sku = sku,
    precio = precio,
    stock = stock,
    stockMinimo = stockMinimo,
    categoria = categoria,
    fotoUrl = fotoUrl,
    nfcId = nfcId
)
