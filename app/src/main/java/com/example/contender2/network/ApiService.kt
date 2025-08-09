package com.example.contender2.network

import retrofit2.http.GET
import retrofit2.http.Query

// === DTOs da API (apenas dados, sem coisas de UI) ===
data class RestauranteDto(
    val id: Int,
    val nome: String
)

data class ClienteDto(
    val id: Int,
    val nome: String,
    val email: String? = null,
    val login: String? = null,
    val genero: String? = null
)

data class ComentarioDto(
    val id: Int,
    val data_publicacao: String?,   // ISO 8601; parseie para Instant/LocalDateTime se quiser
    val curtidas: Int? = null,
    val texto: String,
    val titulo: String? = null,
    val url: String? = null,
    val restaurante_id: Int,
    val cliente_id: Int
)

data class CategoriaOpiniaoDto(
    val id: Int,
    val categoria: String
)

data class OpiniaoDto(
    val id: Int,
    val aspecto: String,
    val sentimento: String?,       // "positivo" | "negativo" | "neutro"
    val polaridade: Float?,        // -1..1
    val sentenca: String?,
    val comentario_id: Int,
    val categoria_id: Int?
)

data class AspectoComparadoDto(
    val aspecto: String,
    val notaPredita1: Float,
    val notaPredita2: Float
)

// === Endpoints ===
interface ApiService {

    // Restaurantes
    @GET("restaurantes/")
    suspend fun getRestaurantes(): List<RestauranteDto>

    // Clientes
    @GET("clientes/")
    suspend fun getClientes(): List<ClienteDto>

    // Comentários (substitui /avaliacoes/)
    @GET("comentarios/")
    suspend fun getComentarios(): List<ComentarioDto>

    // Opiniões (1:N por comentário)
    @GET("opinioes/")
    suspend fun getOpinioes(): List<OpiniaoDto>

    // Categorias (opcional)
    @GET("categorias-opiniao/")
    suspend fun getCategoriasOpiniao(): List<CategoriaOpiniaoDto>

    // Comparação (mesma assinatura de antes)
    @GET("comparar_aspectos")
    suspend fun compararAspectos(
        @Query("restaurante1_id") restaurante1Id: Int,
        @Query("restaurante2_id") restaurante2Id: Int
    ): List<AspectoComparadoDto>
}
