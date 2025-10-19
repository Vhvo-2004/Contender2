package com.example.contender2.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/* ===================== DTOs ===================== */
data class ChartPolaridadeCategoriaDto(
    val restaurante_id: Int,
    val categoria_id: Int,
    val categoria_nome: String,
    val qt_opinioes: Int,
    val avg_polaridade: Double,
    val updated_at: String
)
data class ChartPolaridadeAspectoDto(
    val restaurante_id: Int,
    val aspecto: String,
    val avg_polaridade: Double,
    val qt_opinioes: Int,
    val updated_at: String   // use Date se tiver adapter configurado
)

data class ChartGeneroAspectoDto(
    val restaurante_id: Int,
    val categoria_id: Int,
    val masc_count: Int,
    val fem_count: Int,
    val outros_count: Int,
    val total: Int,
    val updated_at: String   // use Date se tiver adapter configurado
)

data class CategoriaOpiniaoDto(
    val id: Int,
    val categoria: String
)

data class RestauranteDto(
    val id: Int,
    val nome: String
)

data class ClienteDto(
    val id: Int,
    val nome: String,
    val email: String? = null,
    val login: String? = null,
    val genero: String? = null   // mantém string; não é usado nos charts
)

data class ComentarioDto(
    val id: Int,
    val data_publicacao: String?,   // deixe como String; parse no app se precisar
    val curtidas: Int? = null,
    val texto: String,
    val titulo: String? = null,
    val url: String? = null,
    val restaurante_id: Int,
    val cliente_id: Int,
    val autor: String? = null
)

data class OpiniaoDto(
    val id: Int,
    val aspecto: String,
    val sentimento: String?,    // adjetivo (não label pos/neg)
    val polaridade: Float?,     // -1..1
    val sentenca: String?,
    val comentario_id: Int,
    val categoria_id: Int?
)

data class AspectoComparadoDto(
    val aspecto: String,
    val notaPredita1: Float,
    val notaPredita2: Float
)

/* ===================== API ===================== */

interface ApiService {

    // ---- Catálogos e dados brutos (legado) ----
    @GET("restaurantes/")
    suspend fun getRestaurantes(): List<RestauranteDto>

    @GET("clientes/")
    suspend fun getClientes(): List<ClienteDto>

    @GET("comentarios/")
    suspend fun getComentarios(): List<ComentarioDto>

    // Comentários de um restaurante já com 'autor'
    @GET("comentarios/restaurante/{restaurante_id}")
    suspend fun getComentariosPorRestaurante(
        @Path("restaurante_id") restauranteId: Int
    ): List<ComentarioDto>

    @GET("opinioes/")
    suspend fun getOpinioes(): List<OpiniaoDto>

    @GET("categorias-opiniao/")
    suspend fun getCategoriasOpiniao(): List<CategoriaOpiniaoDto>

    // ---- Comparação (já existente) ----
    @GET("comparar_aspectos")
    suspend fun compararAspectos(
        @Query("restaurante1_id") restaurante1Id: Int,
        @Query("restaurante2_id") restaurante2Id: Int
    ): List<AspectoComparadoDto>

    // ---- NOVO: Charts (tabelas de resumo) ----
    @GET("charts/polaridade/{restaurante_id}")
    suspend fun chartPolaridade(
        @Path("restaurante_id") restauranteId: Int,
        @Query("aspecto") aspecto: String? = null
    ): List<ChartPolaridadeAspectoDto>
    @GET("charts/polaridade-categoria/{restaurante_id}")
    suspend fun chartPolaridadeCategoria(
        @Path("restaurante_id") restauranteId: Int
    ): List<ChartPolaridadeCategoriaDto>

    @GET("charts/genero/{restaurante_id}")
    suspend fun chartGenero(
        @Path("restaurante_id") restauranteId: Int,
        @Query("categoria_id") categoriaId: Int? = null
    ): List<ChartGeneroAspectoDto>
}
