package com.example.contender2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

// Classe de dados fictícia para uma avaliação
data class Avaliacao(
    val id: Int,
    val nomeUsuario: String,
    val imagemUsuario: ImageVector,
    val aspecto: String,
    val preco: String,
    val distancia: String,
    val avaliacao: Float,
    val textoComplementar: String,
    val eFavorito: Boolean
)

// Dados de exemplo (substitua pela sua fonte de dados real)
val avaliacoesExemplo = List(5) { index ->
    Avaliacao(
        id = index,
        nomeUsuario = if (index < 2) "Usuário 1" else "Usuário 2",
        imagemUsuario = Icons.Filled.AccountCircle,
        aspecto = "aspecto",
        preco = "$$",
        distancia = "1.2 km de distância",
        avaliacao = 4.5f,
        textoComplementar = "Texto complementar lorem ipsum...",
        eFavorito = index % 2 == 0
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Comparison(navController: NavHostController) {
    var indiceAbaSelecionada by remember { mutableStateOf(0) }
    val abas = listOf("Restaurante 1", "Restaurante 2")

    var consultaAspecto by remember { mutableStateOf("") }
    var dataInicio by remember { mutableStateOf("") }
    var dataFim by remember { mutableStateOf("") }

    // Estado para navegação inferior
    var itemNavegacaoInferiorSelecionado by remember { mutableStateOf(0) }
    val itensNavegacaoInferior = listOf("Avaliações", "Gráficos")
    val iconesNavegacaoInferior = listOf(Icons.Filled.Star, Icons.Filled.BarChart)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comparação") },
                navigationIcon = {
                    IconButton(onClick = { /* Lidar com o botão de voltar */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
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
            SegmentedControl(
                items = abas,
                selectedIndex = indiceAbaSelecionada,
                onIndexSelected = { indiceAbaSelecionada = it }
            )

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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(avaliacoesExemplo) { avaliacao ->
                    ItemAvaliacao(avaliacao = avaliacao)
                }

                // Item para visualizar todas as avaliações
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { /* Lidar com Visualizar todas as Avaliações */ },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ver 231 Avaliações", color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// Componente de Controle Segmentado Personalizado
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

// Componente para um único item de avaliação
@Composable
fun ItemAvaliacao(avaliacao: Avaliacao) {
    var eFavorito by remember { mutableStateOf(avaliacao.eFavorito) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(avaliacao.imagemUsuario, contentDescription = "Avatar do Usuário", modifier = Modifier.size(40.dp), tint = Color.Gray)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(avaliacao.nomeUsuario, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${avaliacao.aspecto} • ${avaliacao.preco} • ${avaliacao.distancia}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < avaliacao.avaliacao) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Estrela de Avaliação",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(avaliacao.textoComplementar, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            }

            IconButton(
                onClick = { eFavorito = !eFavorito },
                modifier = Modifier.align(Alignment.Top)
            ) {
                Icon(
                    imageVector = if (eFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (eFavorito) Color.Red else Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun ComparacaoScreenPreview() {
    Comparison(navController = rememberNavController())
}
