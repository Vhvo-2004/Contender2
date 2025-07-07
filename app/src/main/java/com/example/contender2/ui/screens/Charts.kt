package com.example.contender2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun Charts(navController: NavHostController) {
    var aspecto by remember { mutableStateOf("comida") }
    var graficoSelecionado by remember { mutableStateOf("Histograma") }

    val opcoesDeGrafico = listOf("Histograma", "Pizza", "Radar")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // TopBar com seta de voltar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
            }
            Text("Comparação", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Seletor de tipo de gráfico
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFB39DDB), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            opcoesDeGrafico.forEach { opcao ->
                OutlinedButton(
                    onClick = { graficoSelecionado = opcao },
                    border = BorderStroke(1.dp, Color(0xFFB39DDB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (graficoSelecionado == opcao) Color(0xFFEBDEF0) else Color.Transparent
                    )
                ) {
                    Text(opcao)
                }
            }

        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de busca
        Text("Aspecto", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = aspecto,
                onValueChange = { aspecto = it },
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Ícone de Pesquisa")
                },
                trailingIcon = {
                    IconButton(onClick = { aspecto = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpar")
                    }
                },
                placeholder = { Text("comida") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { /* Lógica de pesquisa */ }) {
                Text("Pesquisar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()

        // Gráfico do Restaurante 1
        Text("Restaurante 1", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        GraficoHistograma() // Substitua pela função real do gráfico

        Spacer(modifier = Modifier.height(16.dp))

        // Gráfico do Restaurante 2
        Text("Restaurante 2", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        GraficoHistograma()

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { /* limpar filtros */ },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1C4E9))
        ) {
            Text("Limpar Rótulos")
        }

        Spacer(modifier = Modifier.weight(1f))

        // Barra de navegação inferior
        BarraDeNavegacao(navController)
    }
}

@Composable
fun BarraDeNavegacao(navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navController.navigate("inicio") }) {
            Icon(Icons.Default.Home, contentDescription = "Início")
        }
        IconButton(onClick = { navController.navigate("perfil") }) {
            Icon(Icons.Default.Person, contentDescription = "Perfil")
        }
        IconButton(onClick = { navController.navigate("configuracoes") }) {
            Icon(Icons.Default.Settings, contentDescription = "Configurações")
        }
    }
}


@Composable
fun GraficoHistograma() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Restaurante 1", style = MaterialTheme.typography.titleLarge)

        // Aqui você pode inserir um gráfico de verdade usando uma lib externa ou Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Gráfico de barras - Restaurante 1 (placeholder)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Restaurante 2", style = MaterialTheme.typography.titleLarge)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text("Gráfico de barras - Restaurante 2 (placeholder)")
        }
    }
}
