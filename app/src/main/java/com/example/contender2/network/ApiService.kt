package com.example.contender2.network

import androidx.compose.ui.graphics.vector.ImageVector
import retrofit2.http.GET

data class Restaurante(
    val id: Int,
    val nome: String,
    val endereco: String?,
    val categoria: String?,
    val url_platform: String?
)
data class Avaliacao(
    val id: Int,
    val usuario: String,
    val comentario: String,
    val nota: Int,
    val data: String, // ou LocalDateTime se convertido
    val restaurante_id: Int,
    val nomeUsuario: String,
    val imagemUsuario: ImageVector
)

public interface ApiService {
    @GET("restaurantes/")
    suspend fun getRestaurantes(): List<Restaurante>
    @GET("avaliacoes/")
    suspend fun getAvaliacoes(): List<Avaliacao>

}
