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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.contender2.R
import com.example.contender2.network.Restaurante
import com.example.contender2.network.RetrofitInstance
import com.example.contender2.ui.theme.Contender2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Contender2Theme {
                // Setup de navegação
                val navController = rememberNavController()

                // Scaffold com a navegação
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home", // Tela inicial
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") { HomeScreen(navController) } // Tela inicial
                        composable("Comparison") { Comparison(navController) } // Tela de comparação
                        composable("Charts") { Charts(navController) } // Tela de grafico(default histogram)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    var restaurantes by remember { mutableStateOf<List<Restaurante>>(emptyList()) }

    // States Restaurante 1
    var searchQuery by remember { mutableStateOf("") }
    var isDropdownExpanded1 by remember { mutableStateOf(false) }

    // States Restaurante 2
    var searchQuery2 by remember { mutableStateOf("") }
    var isDropdownExpanded2 by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api.getRestaurantes()
            Log.d("API_TEST", "Recebido: $response")
            restaurantes = response
        } catch (e: Exception) {
            Log.e("API_TEST", "Erro: ${e.message}")
        }
    }

    // Filtragens
    val filteredList1 = restaurantes.filter {
        it.nome.contains(searchQuery, ignoreCase = true)
    }
    val filteredList2 = restaurantes.filter {
        it.nome.contains(searchQuery2, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Barra de Pesquisa
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0E9F6), shape = RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pesquisar Restaurante",
                color = Color.Gray,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.clickable {
                    navController.navigate("Comparison")
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Exibição dos restaurantes carregados
        Text("Restaurantes Carregados:", fontWeight = FontWeight.Bold)
        restaurantes.forEach { restaurante ->
            Text("- ${restaurante.nome}")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        // Título e Ícone
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrowscompareicicon),
                contentDescription = "Compare Icon",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Comparar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Restaurante 1 com Dropdown de sugestões
        Box {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    searchQuery = newValue
                    isDropdownExpanded1 = newValue.isNotEmpty()
                },
                label = { Text("Restaurante 1") },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = isDropdownExpanded1 && filteredList1.isNotEmpty(),
                onDismissRequest = { isDropdownExpanded1 = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                filteredList1.forEach { restaurante ->
                    DropdownMenuItem(
                        text = { Text(restaurante.nome) },
                        onClick = {
                            searchQuery = restaurante.nome
                            isDropdownExpanded1 = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Restaurante 2 com Dropdown de sugestões
        Box {
            OutlinedTextField(
                value = searchQuery2,
                onValueChange = { newValue ->
                    searchQuery2 = newValue
                    isDropdownExpanded2 = newValue.isNotEmpty()
                },
                label = { Text("Restaurante 2") },
                placeholder = { Text("Digite os restaurantes para compara-los") },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenu(
                expanded = isDropdownExpanded2 && filteredList2.isNotEmpty(),
                onDismissRequest = { isDropdownExpanded2 = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                filteredList2.forEach { restaurante ->
                    DropdownMenuItem(
                        text = { Text(restaurante.nome) },
                        onClick = {
                            searchQuery2 = restaurante.nome
                            isDropdownExpanded2 = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botão de comparação (ícone de busca)
        IconButton(
            onClick = {
                navController.navigate("Comparison")
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Comparar"
            )
        }
    }
}





