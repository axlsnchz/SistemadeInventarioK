package mx.edu.utez.sistemadeinventario.data.remote

import mx.edu.utez.sistemadeinventario.data.remote.dto.LoginRequest
import mx.edu.utez.sistemadeinventario.data.remote.dto.LoginResponse
import mx.edu.utez.sistemadeinventario.data.remote.dto.ProductoDto
import mx.edu.utez.sistemadeinventario.data.remote.dto.ProductoRequest
import mx.edu.utez.sistemadeinventario.data.remote.dto.VentaRequest
import mx.edu.utez.sistemadeinventario.data.remote.dto.VentaResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/productos")
    suspend fun getProductos(@Query("q") query: String? = null): Response<List<ProductoDto>>

    @GET("api/productos/{id}")
    suspend fun getProducto(@Path("id") id: Long): Response<ProductoDto>

    @GET("api/productos/nfc/{nfcId}")
    suspend fun getProductoByNfc(@Path("nfcId") nfcId: String): Response<ProductoDto>

    @POST("api/ventas")
    suspend fun crearVenta(@Body request: VentaRequest): Response<VentaResponse>

    @POST("api/productos")
    suspend fun crearProducto(@Body request: ProductoRequest): Response<ProductoDto>

    @PUT("api/productos/{id}")
    suspend fun editarProducto(
        @Path("id") id: Long,
        @Body request: ProductoRequest
    ): Response<ProductoDto>

    @Multipart
    @POST("api/productos/{id}/foto")
    suspend fun subirFoto(
        @Path("id") id: Long,
        @Part foto: MultipartBody.Part
    ): Response<ProductoDto>
}
