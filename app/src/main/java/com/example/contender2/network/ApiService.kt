package com.example.contender2.network

import retrofit2.Call
import retrofit2.http.GET

data class Restaurante(
    val id: Int,
    val nome: String,
    val endereco: String?,
    val categoria: String?,
    val url_platform: String?
)

public interface ApiService {
    @GET("restaurantes/")
    suspend fun getRestaurantes(): List<Restaurante>
}
