package com.example.contender2.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.contender2.network.*
import java.net.URLEncoder
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Comparison(navController: NavHostController, id1: Int, id2: Int) {
    var indiceAbaSelecionada by remember { mutableStateOf(0) }
    var consultaAspecto by remember { mutableStateOf("") }

    var restaurantes by remember { mutableStateOf<List<RestauranteDto>>(emptyList()) }
    var comentarios by remember { mutableStateOf<List<ComentarioDto>>(emptyList()) }
    var clientes by remember { mutableStateOf<Map<Int, ClienteDto>>(emptyMap()) }
    var opinioesPorComentario by remember { mutableStateOf<Map<Int, List<OpiniaoDto>>>(emptyMap()) }

    var erro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id1, id2) {
        try {
            erro = null
            val r = RetrofitInstance.api.getRestaurantes()
            val c = RetrofitInstance.api.getComentarios()
            val cl = RetrofitInstance.api.getClientes().associateBy { it.id }
            val op = RetrofitInstance.api.getOpinioes().groupBy { it.comentario_id }

            restaurantes = r
            comentarios = c
            clientes = cl
            opinioesPorComentario = op

            Log.d("API_DEBUG", "restaurantes=${r.size}, comentarios=${c.size}, clientes=${cl.size}, opinioes=${op.values.sumOf { it.size }}")
        } catch (e: Exception) {
            e.printStackTrace()
            erro = e.message
        }
    }

    // pega os dois restaurantes por ID
    val r1 = restaurantes.firstOrNull { it.id == id1 }
    val r2 = restaurantes.firstOrNull { it.id == id2 }
    val restaurantesComComentarios = listOfNotNull(r1, r2).filter { rest ->
        comentarios.any { it.restaurante_id == rest.id }
    }

    val abas = restaurantesComComentarios.map { it.nome }
    val restauranteIdSelecionado = restaurantesComComentarios
        .getOrNull(indiceAbaSelecionada)
        ?.id

    val comentariosDoRestaurante = comentarios.filter { it.restaurante_id == restauranteIdSelecionado }

    // filtro por texto/“aspecto”
    val comentariosFiltrados = comentariosDoRestaurante.filter { com ->
        val termo = consultaAspecto.trim()
        if (termo.isEmpty()) return@filter true
        val noComentario = com.texto.contains(termo, true) || (com.titulo?.contains(termo, true) == true)
        val ops = opinioesPorComentario[com.id].orEmpty()
        val nasOpinioes = ops.any { it.aspecto.contains(termo, true) || (it.sentenca?.contains(termo, true) == true) }
        noComentario || nasOpinioes
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comparação") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (r1 != null && r2 != null) {
                Button(
                    onClick = {
                        val nome1Encoded = URLEncoder.encode(r1.nome, "UTF-8")
                        val nome2Encoded = URLEncoder.encode(r2.nome, "UTF-8")
                        navController.navigate("Charts/${r1.id}/${r2.id}/${nome1Encoded}/${nome2Encoded}")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) { Text("Ir para Gráficos (Aspectos)") }
            }
        },
        containerColor = Color(0xFFF2F2F7)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            when {
                erro != null -> Text("Erro: $erro", color = Color.Red)
                abas.isEmpty() -> Text("Nenhum restaurante com comentários disponíveis.", color = Color.Gray)
                else -> SegmentedControl(
                    items = abas,
                    selectedIndex = indiceAbaSelecionada,
                    onIndexSelected = { indiceAbaSelecionada = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = consultaAspecto,
                onValueChange = { consultaAspecto = it },
                label = { Text("Aspecto / Texto") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
                trailingIcon = {
                    if (consultaAspecto.isNotEmpty()) {
                        IconButton(onClick = { consultaAspecto = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpar Busca")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(comentariosFiltrados, key = { it.id }) { comentario ->
                    val autor = clientes[comentario.cliente_id]
                    val ops = opinioesPorComentario[comentario.id].orEmpty()
                    ItemComentario(comentario = comentario, autor = autor, opinioes = ops)
                }
            }
        }
    }
}

/* --------- componentes auxiliares (iguais ao que te mandei) ---------- */

@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onIndexSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    unselectedColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
    ) {
        items.forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (index == selectedIndex) selectedColor else unselectedColor)
                    .clickable { onIndexSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    color = if (index == selectedIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ItemComentario(
    comentario: ComentarioDto,
    autor: ClienteDto?,
    opinioes: List<OpiniaoDto>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Autor: ${autor?.nome ?: "Desconhecido"}", fontWeight = FontWeight.Bold)
            comentario.titulo?.let { Text(it) }
            Text("Comentário: ${comentario.texto}")
            comentario.data_publicacao?.let { Text("Data: $it") }
            comentario.curtidas?.let { Text("Curtidas: $it") }

            if (opinioes.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FlowRowChip(opinioes)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRowChip(opinioes: List<OpiniaoDto>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        opinioes.forEach { op ->
            AssistChip(
                onClick = {},
                label = { Text("${op.aspecto} • ${op.sentimento ?: "neutro"}") }
            )
        }
    }
}

