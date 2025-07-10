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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.contender2.network.Avaliacao
import com.example.contender2.network.Restaurante
import com.example.contender2.network.RetrofitInstance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Comparison(navController: NavHostController) {
    var indiceAbaSelecionada by remember { mutableStateOf(0) }

    var consultaAspecto by remember { mutableStateOf("") }
    var dataInicio by remember { mutableStateOf("") }
    var dataFim by remember { mutableStateOf("") }

    var restaurantes by remember { mutableStateOf<List<Restaurante>>(emptyList()) }
    var avaliacoes by remember { mutableStateOf<List<Avaliacao>>(emptyList()) }

    // 🔥 Requisição GET na inicialização
    LaunchedEffect(Unit) {
        try {
            restaurantes = RetrofitInstance.api.getRestaurantes()
            avaliacoes = RetrofitInstance.api.getAvaliacoes()
            Log.d("API_DEBUG", "Restaurantes recebidos: ${restaurantes.size}")
            Log.d("API_DEBUG", "Avaliações recebidas: ${avaliacoes.size}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 🔷 Filtra restaurantes com avaliações
    val restauranteIdsComAvaliacoes = avaliacoes.map { it.restaurante_id }.toSet()
    val restaurantesComAvaliacoes = restaurantes.filter { it.id in restauranteIdsComAvaliacoes }

    // 🔷 Nomes para as abas
    val abas = restaurantesComAvaliacoes.map { it.nome }

    // 🔷 Filtro de aspecto + restaurante
    val restauranteIdSelecionado = restaurantesComAvaliacoes.getOrNull(indiceAbaSelecionada)?.id
    val avaliacoesFiltradas = avaliacoes.filter { avaliacao ->
        val correspondeAspecto = consultaAspecto.isEmpty() || avaliacao.comentario.contains(consultaAspecto, ignoreCase = true)
        val correspondeRestaurante = restauranteIdSelecionado == null || avaliacao.restaurante_id == restauranteIdSelecionado
        correspondeAspecto && correspondeRestaurante
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
            NavigationBar {
                val itensNavegacaoInferior = listOf("Avaliações", "Gráficos")
                val iconesNavegacaoInferior = listOf(Icons.Filled.Star, Icons.Filled.BarChart)
                var itemNavegacaoInferiorSelecionado by remember { mutableStateOf(0) }

                itensNavegacaoInferior.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(iconesNavegacaoInferior[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = itemNavegacaoInferiorSelecionado == index,
                        onClick = {
                            itemNavegacaoInferiorSelecionado = index
                            when (item) {
                                "Avaliações" -> navController.navigate("reviews_screen")
                                "Gráficos" -> navController.navigate("Charts")
                            }
                        }
                    )
                }
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

            // Botão segmentado / Abas
            if (abas.isNotEmpty()) {
                SegmentedControl(
                    items = abas,
                    selectedIndex = indiceAbaSelecionada,
                    onIndexSelected = { indiceAbaSelecionada = it }
                )
            } else {
                Text("Nenhum restaurante com avaliações disponíveis.", color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de busca de aspecto
            OutlinedTextField(
                value = consultaAspecto,
                onValueChange = { consultaAspecto = it },
                label = { Text("Aspecto") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar Aspecto") },
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

            Spacer(modifier = Modifier.height(8.dp))

            // Campos de data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = dataInicio,
                    onValueChange = { dataInicio = it },
                    label = { Text("Data de Início") },
                    placeholder = { Text("dd/mm/aaaa") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = dataFim,
                    onValueChange = { dataFim = it },
                    label = { Text("Data de Fim") },
                    placeholder = { Text("dd/mm/aaaa") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de Avaliações
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(avaliacoesFiltradas) { avaliacao ->
                    ItemAvaliacao(avaliacao = avaliacao)
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { /* TODO: Visualizar todas as avaliações */ },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ver ${avaliacoes.size} Avaliações", color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

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
fun ItemAvaliacao(avaliacao: Avaliacao) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Usuário: ${avaliacao.usuario}", fontWeight = FontWeight.Bold)
            Text("Comentário: ${avaliacao.comentario}")
            Text("Nota: ${avaliacao.nota}")
            Text("Data: ${avaliacao.data}")
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun ComparacaoScreenPreview() {
    Comparison(navController = rememberNavController())
}
