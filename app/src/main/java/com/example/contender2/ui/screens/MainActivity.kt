package com.example.contender2.ui.screens

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.contender2.R
import com.example.contender2.network.RestauranteDto
import com.example.contender2.network.RetrofitInstance
import com.example.contender2.ui.theme.Contender2Theme
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Contender2Theme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(navController) }

                        // (Opcional) ainda existe sua rota Comparison; mantendo por compatibilidade
                        composable("Comparison/{id1}/{id2}") { backStackEntry ->
                            val id1 = backStackEntry.arguments?.getString("id1")?.toIntOrNull() ?: 0
                            val id2 = backStackEntry.arguments?.getString("id2")?.toIntOrNull() ?: 0
                            Comparison(navController, id1, id2)
                        }


                        composable("Charts/{id1}/{id2}/{nome1}/{nome2}") { backStackEntry ->
                            val id1 = backStackEntry.arguments?.getString("id1")?.toIntOrNull() ?: 0
                            val id2 = backStackEntry.arguments?.getString("id2")?.toIntOrNull() ?: 0
                            val nome1 = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("nome1") ?: "", "UTF-8")
                            val nome2 = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("nome2") ?: "", "UTF-8")
                            Charts(id1, id2, nome1, nome2, navController)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var restaurantes by remember { mutableStateOf<List<RestauranteDto>>(emptyList()) }

    var searchQuery1 by remember { mutableStateOf("") }
    var searchQuery2 by remember { mutableStateOf("") }

    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }

    var selecionado1 by remember { mutableStateOf<RestauranteDto?>(null) }
    var selecionado2 by remember { mutableStateOf<RestauranteDto?>(null) }

    LaunchedEffect(Unit) {
        try {
            restaurantes = RetrofitInstance.api.getRestaurantes()
            Log.d("API_TEST", "Recebido: ${restaurantes.size} restaurantes")
        } catch (e: Exception) {
            Log.e("API_TEST", "Erro: ${e.message}")
        }
    }

    // helpers: se não há seleção, tenta resolver pelo texto digitado (primeiro match)
    fun resolveR1(): RestauranteDto? =
        selecionado1 ?: restaurantes.firstOrNull { it.nome.equals(searchQuery1, ignoreCase = true) }
        ?: restaurantes.firstOrNull { it.nome.contains(searchQuery1, ignoreCase = true) }

    fun resolveR2(): RestauranteDto? =
        selecionado2 ?: restaurantes.firstOrNull { it.nome.equals(searchQuery2, ignoreCase = true) }
        ?: restaurantes.firstOrNull { it.nome.contains(searchQuery2, ignoreCase = true) }

    val filteredList1 = restaurantes.filter { it.nome.contains(searchQuery1, ignoreCase = true) }
    val filteredList2 = restaurantes.filter { it.nome.contains(searchQuery2, ignoreCase = true) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra "Pesquisar Restaurante" (atalho para Comparison)
        

        Spacer(modifier = Modifier.height(24.dp))

        Text("Restaurantes Carregados:", fontWeight = FontWeight.Bold)
        restaurantes.forEach { restaurante -> Text("- ${restaurante.nome}") }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(painter = painterResource(id = R.drawable.arrowscompareicicon),
                contentDescription = "Compare Icon",
                modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Comparar", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dropdown Restaurante 1
        ExposedDropdownMenuBox(expanded = expanded1, onExpandedChange = { expanded1 = !expanded1 }) {
            OutlinedTextField(
                value = searchQuery1,
                onValueChange = {
                    searchQuery1 = it
                    selecionado1 = null
                    expanded1 = it.isNotEmpty()
                },
                label = { Text("Restaurante 1") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = expanded1 && filteredList1.isNotEmpty(),
                onDismissRequest = { expanded1 = false }
            ) {
                filteredList1.forEach { restaurante ->
                    DropdownMenuItem(
                        text = { Text(restaurante.nome) },
                        onClick = {
                            searchQuery1 = restaurante.nome
                            selecionado1 = restaurante
                            expanded1 = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown Restaurante 2
        ExposedDropdownMenuBox(expanded = expanded2, onExpandedChange = { expanded2 = !expanded2 }) {
            OutlinedTextField(
                value = searchQuery2,
                onValueChange = {
                    searchQuery2 = it
                    selecionado2 = null
                    expanded2 = it.isNotEmpty()
                },
                label = { Text("Restaurante 2") },
                placeholder = { Text("Digite para filtrar") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = expanded2 && filteredList2.isNotEmpty(),
                onDismissRequest = { expanded2 = false }
            ) {
                filteredList2.forEach { restaurante ->
                    DropdownMenuItem(
                        text = { Text(restaurante.nome) },
                        onClick = {
                            searchQuery2 = restaurante.nome
                            selecionado2 = restaurante
                            expanded2 = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão para VER COMENTÁRIOS (Comparison)
        Button(
            onClick = {
                val r1 = resolveR1()
                val r2 = resolveR2()
                if (r1 != null && r2 != null) {
                    navController.navigate("Comparison/${r1.id}/${r2.id}")
                }
            },
            enabled = (resolveR1() != null && resolveR2() != null),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Ver comentários")
            Spacer(Modifier.width(8.dp))
            Text("Ver comentários (texto)")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão para GRÁFICOS (Charts) — mantém seu fluxo atual
        Button(
            onClick = {
                val r1 = resolveR1()
                val r2 = resolveR2()
                if (r1 != null && r2 != null) {
                    val n1 = URLEncoder.encode(r1.nome, "UTF-8")
                    val n2 = URLEncoder.encode(r2.nome, "UTF-8")
                    navController.navigate("Charts/${r1.id}/${r2.id}/${n1}/${n2}")
                }
            },
            enabled = (resolveR1() != null && resolveR2() != null),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Comparar")
            Spacer(Modifier.width(8.dp))
            Text("Comparar aspectos (gráficos)")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    Contender2Theme {
        HomeScreen(navController = rememberNavController())
    }
}
