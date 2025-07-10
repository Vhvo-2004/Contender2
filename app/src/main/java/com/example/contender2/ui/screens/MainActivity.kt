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
import androidx.navigation.compose.*
import com.example.contender2.R
import com.example.contender2.network.Restaurante
import com.example.contender2.network.RetrofitInstance
import com.example.contender2.ui.theme.Contender2Theme

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
                        composable("Comparison") { Comparison(navController) }
                        composable("Charts") { Charts(navController) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var restaurantes by remember { mutableStateOf<List<Restaurante>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var expanded1 by remember { mutableStateOf(false) }
    var searchQuery2 by remember { mutableStateOf("") }
    var expanded2 by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitInstance.api.getRestaurantes()
            Log.d("API_TEST", "Recebido: $response")
            restaurantes = response
        } catch (e: Exception) {
            Log.e("API_TEST", "Erro: ${e.message}")
        }
    }

    val filteredList1 = restaurantes.filter { it.nome.contains(searchQuery, ignoreCase = true) }
    val filteredList2 = restaurantes.filter { it.nome.contains(searchQuery2, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Barra de Pesquisa superior
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0E9F6), shape = RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pesquisar Restaurante",
                color = Color.Gray,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.clickable { navController.navigate("Comparison") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Restaurantes Carregados:", fontWeight = FontWeight.Bold)
        restaurantes.forEach { restaurante ->
            Text("- ${restaurante.nome}")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

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

        // Dropdown Restaurante 1
        ExposedDropdownMenuBox(
            expanded = expanded1,
            onExpandedChange = { expanded1 = !expanded1 }
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    expanded1 = it.isNotEmpty()
                },
                label = { Text("Restaurante 1") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
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
                            searchQuery = restaurante.nome
                            expanded1 = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown Restaurante 2
        ExposedDropdownMenuBox(
            expanded = expanded2,
            onExpandedChange = { expanded2 = !expanded2 }
        ) {
            OutlinedTextField(
                value = searchQuery2,
                onValueChange = {
                    searchQuery2 = it
                    expanded2 = it.isNotEmpty()
                },
                label = { Text("Restaurante 2") },
                placeholder = { Text("Digite os restaurantes para compará-los") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
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
                            expanded2 = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        IconButton(
            onClick = { navController.navigate("Comparison") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Comparar"
            )
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
