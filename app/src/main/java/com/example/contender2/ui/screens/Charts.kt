package com.example.contender2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.contender2.network.AspectoComparadoDto
import com.example.contender2.network.RetrofitInstance
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Charts(id1: Int, id2: Int, nome1: String, nome2: String, navController: NavHostController) {
    var aspecto by remember { mutableStateOf("") }
    var graficoSelecionado by remember { mutableStateOf("Histograma") }
    val opcoesDeGrafico = listOf("Histograma", "Pizza", "Radar")

    var comparacoes by remember { mutableStateOf<List<AspectoComparadoDto>>(emptyList()) }
    var erro by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(id1, id2) {
        try {
            erro = null
            comparacoes = RetrofitInstance.api.compararAspectos(id1, id2)
        } catch (e: Exception) {
            e.printStackTrace()
            erro = e.message ?: "Erro ao carregar comparação"
        }
    }

    val dadosFiltrados = comparacoes.filter {
        aspecto.isBlank() || it.aspecto.contains(aspecto, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Text("Comparação", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

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

        Text("Aspecto", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = aspecto,
                onValueChange = { aspecto = it },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pesquisar") },
                trailingIcon = {
                    IconButton(onClick = { aspecto = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpar")
                    }
                },
                placeholder = { Text("comida") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { /* filtro já é reativo */ }) { Text("Pesquisar") }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()

        when {
            erro != null -> {
                Text("Erro: $erro", color = Color.Red)
            }
            dadosFiltrados.isEmpty() -> {
                Spacer(Modifier.height(12.dp))
                Text("Nenhum dado para exibir com o filtro atual.")
            }
            graficoSelecionado == "Radar" -> {
                Spacer(Modifier.height(12.dp))
                RadarChartTodosAspectos(dadosFiltrados, nome1, nome2)
            }
            else -> {
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    dadosFiltrados.forEach { dado ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text("Aspecto: ${dado.aspecto}", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            when (graficoSelecionado) {
                                "Histograma" -> BarChartComparativo(
                                    original1 = dado.notaPredita1,
                                    original2 = dado.notaPredita2,
                                    nome1 = nome1, nome2 = nome2
                                )
                                "Pizza" -> PieChartComparativo(
                                    original1 = dado.notaPredita1,
                                    original2 = dado.notaPredita2,
                                    nome1 = nome1, nome2 = nome2
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

/** Normaliza valor de [-1, 1] para [0, 1] só para desenho. */
private fun normalize01(v: Float): Float = ((v + 1f) / 2f).coerceIn(0f, 1f)

@Composable
fun BarChartComparativo(original1: Float, original2: Float, nome1: String, nome2: String) {
    val valor1 = normalize01(original1)
    val valor2 = normalize01(original2)
    val max = maxOf(valor1, valor2, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("$nome1: ${"%.3f".format(original1)}", fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth(valor1 / max)
                .height(20.dp)
                .background(Color(0xFF90CAF9))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text("$nome2: ${"%.3f".format(original2)}", fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth(valor2 / max)
                .height(20.dp)
                .background(Color(0xFFF48FB1))
        )
    }
}

@Composable
fun PieChartComparativo(original1: Float, original2: Float, nome1: String, nome2: String) {
    val v1 = normalize01(original1)
    val v2 = normalize01(original2)
    val total = (v1 + v2).takeIf { it != 0f } ?: 1f
    val proporcao1 = v1 / total
    val proporcao2 = v2 / total

    Canvas(modifier = Modifier.size(150.dp)) {
        val sweep1 = proporcao1 * 360f
        val sweep2 = proporcao2 * 360f
        drawArc(color = Color(0xFF90CAF9), startAngle = 0f, sweepAngle = sweep1, useCenter = true)
        drawArc(color = Color(0xFFF48FB1), startAngle = sweep1, sweepAngle = sweep2, useCenter = true)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(10.dp).background(Color(0xFF90CAF9)))
        Text("$nome1 (${String.format("%.3f", original1)})")
        Spacer(Modifier.width(8.dp))
        Box(Modifier.size(10.dp).background(Color(0xFFF48FB1)))
        Text("$nome2 (${String.format("%.3f", original2)})")
    }
}

@Composable
fun RadarChartTodosAspectos(
    aspectos: List<AspectoComparadoDto>,
    nomeRestaurante1: String,
    nomeRestaurante2: String
) {
    val orig1 = aspectos.map { it.notaPredita1 }
    val orig2 = aspectos.map { it.notaPredita2 }
    val labels = aspectos.map { it.aspecto }

    // para desenho usamos valores normalizados [0,1]
    val valores1 = orig1.map { normalize01(it) }
    val valores2 = orig2.map { normalize01(it) }

    val raio = 100.dp

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = raio.toPx()
        val count = labels.size.coerceAtLeast(1)
        val angleStep = (2 * Math.PI / count).toFloat()

        // Eixos + rótulos
        for (i in labels.indices) {
            val angle = angleStep * i - Math.PI / 2
            val x = centerX + radius * cos(angle).toFloat()
            val y = centerY + radius * sin(angle).toFloat()

            drawLine(Color.LightGray, Offset(centerX, centerY), Offset(x, y), strokeWidth = 2f)
            // Labels (simples): você pode melhorar com offset/medida de texto se quiser
            drawContext.canvas.nativeCanvas.drawText(
                labels[i],
                x,
                y,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 28f
                }
            )
        }

        fun gerarPontos(valores: List<Float>): List<Offset> =
            valores.mapIndexed { i, valor ->
                val ang = angleStep * i - Math.PI / 2
                val raioLocal = (valor) * radius
                Offset(
                    centerX + raioLocal * cos(ang).toFloat(),
                    centerY + raioLocal * sin(ang).toFloat()
                )
            }

        val pontos1 = gerarPontos(valores1)
        drawPolygon(pontos1, color = Color(0xFF90CAF9))

        val pontos2 = gerarPontos(valores2)
        drawPolygon(pontos2, color = Color(0xFFF48FB1))

        drawCircle(Color.Gray, radius = radius, center = Offset(centerX, centerY), style = Stroke(2f))
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).background(Color(0xFF90CAF9)))
            Spacer(Modifier.width(4.dp))
            Text(nomeRestaurante1)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).background(Color(0xFFF48FB1)))
            Spacer(Modifier.width(4.dp))
            Text(nomeRestaurante2)
        }
    }
}

fun DrawScope.drawPolygon(points: List<Offset>, color: Color) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
        close()
    }
    drawPath(path = path, color = color.copy(alpha = 0.4f))
    drawPath(path = path, color = color, style = Stroke(width = 2f))
}
