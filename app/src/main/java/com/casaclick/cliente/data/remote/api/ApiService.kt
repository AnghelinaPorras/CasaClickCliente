package com.casaclick.cliente.data.remote.api
import com.casaclick.cliente.data.remote.dto.*
import retrofit2.http.*
import com.google.gson.JsonObject
interface ApiService {
    @POST("api/auth/login") suspend fun login(@Body body: LoginRequest): LoginResponse
    @GET("api/propiedades?incluirEliminados=1") suspend fun propiedades(): List<PropiedadDto>
    @GET("api/visitas") suspend fun visitas(): List<VisitaDto>
    @GET("api/propiedades/{id}") suspend fun propiedad(@Path("id") id: Long): PropiedadDto
    @GET("api/visitas/{id}") suspend fun visita(@Path("id") id: Long): VisitaDto
    @POST("api/{entidad}") suspend fun crear(@Path("entidad") entidad: String, @Header("X-Operacion-UUID") uuid: String, @Body payload: JsonObject): retrofit2.Response<JsonObject>
    @PUT("api/{entidad}/{id}") suspend fun editar(@Path("entidad") entidad: String, @Path("id") id: Long, @Header("X-Operacion-UUID") uuid: String, @Body payload: JsonObject): retrofit2.Response<JsonObject>
    @HTTP(method = "DELETE", path = "api/{entidad}/{id}", hasBody = true) suspend fun eliminar(@Path("entidad") entidad: String, @Path("id") id: Long, @Header("X-Operacion-UUID") uuid: String, @Body payload: JsonObject): retrofit2.Response<JsonObject>
    @GET("api/dev/falla") suspend fun simularFalla(@Query("activar") activar: Int): JsonObject
}
