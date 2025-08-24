package com.example.contender2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import com.example.contender2.network.ClienteDto
import com.example.contender2.network.ComentarioDto
import com.example.contender2.network.OpiniaoDto
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

    // NOVOS estados para pizza por gênero
    var clientes by remember { mutableStateOf<Map<Int, ClienteDto>>(emptyMap()) }
    var comentarios by remember { mutableStateOf<List<ComentarioDto>>(emptyList()) }
    var opinioes by remember { mutableStateOf<List<OpiniaoDto>>(emptyList()) }

    LaunchedEffect(id1, id2) {
        try {
            erro = null
            // Mantém a comparação média por aspecto (para a lista e outros gráficos)
            comparacoes = RetrofitInstance.api.compararAspectos(id1, id2)

            // NOVO: dados crus p/ contar opiniões por gênero na pizza
            clientes = RetrofitInstance.api.getClientes().associateBy { it.id }
            comentarios = RetrofitInstance.api.getComentarios()
            opinioes = RetrofitInstance.api.getOpinioes()
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
                                "Pizza" -> {
                                    // NOVO: pizza por gênero (homens x mulheres) para cada restaurante
                                    val (h1, m1) = contarOpinioesPorGenero(
                                        restauranteId = id1,
                                        aspecto = dado.aspecto,
                                        comentarios = comentarios,
                                        opinioes = opinioes,
                                        clientes = clientes
                                    )
                                    val (h2, m2) = contarOpinioesPorGenero(
                                        restauranteId = id2,
                                        aspecto = dado.aspecto,
                                        comentarios = comentarios,
                                        opinioes = opinioes,
                                        clientes = clientes
                                    )

                                    PieChartGeneroComparativo(
                                        aspecto = dado.aspecto,
                                        nome1 = nome1, nome2 = nome2,
                                        homens1 = h1, mulheres1 = m1,
                                        homens2 = h2, mulheres2 = m2
                                    )
                                }
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

/* --------- NOVO: Pizza por gênero (duas pizzas lado a lado) --------- */

private fun contarOpinioesPorGenero(
    restauranteId: Int,
    aspecto: String,
    comentarios: List<ComentarioDto>,
    opinioes: List<OpiniaoDto>,
    clientes: Map<Int, ClienteDto>
): Pair<Int, Int> {
    // comentários do restaurante
    val comentariosPorRest = comentarios.asSequence()
        .filter { it.restaurante_id == restauranteId }
        .associateBy { it.id }

    // opiniões do aspecto nesses comentários
    var homens = 0
    var mulheres = 0
    opinioes.asSequence()
        .filter { it.aspecto.equals(aspecto, ignoreCase = true) }
        .forEach { op ->
            val com = comentariosPorRest[op.comentario_id] ?: return@forEach
            val genero = clientes[com.cliente_id]?.genero?.lowercase()?.trim()
            when (genero) {
                "masculino" -> homens++
                "feminino" -> mulheres++
                // outros/indefinidos: ignorar para esta pizza
            }
        }

    return homens to mulheres
}

@Composable
fun PieChartGeneroComparativo(
    aspecto: String,
    nome1: String,
    nome2: String,
    homens1: Int, mulheres1: Int,
    homens2: Int, mulheres2: Int
) {
    Column(Modifier.fillMaxWidth()) {
        Text("Distribuição por gênero • $aspecto", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PieGenero(titulo = nome1, homens = homens1, mulheres = mulheres1)
            PieGenero(titulo = nome2, homens = homens2, mulheres = mulheres2)
        }
    }
}

@Composable
private fun PieGenero(titulo: String, homens: Int, mulheres: Int) {
    val total = homens + mulheres
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(titulo, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))

        if (total == 0) {
            // Sem opiniões
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(Color(0xFFECECEC), shape = RoundedCornerShape(75.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Sem opiniões", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            val pHomens = homens.toFloat() / total
            val pMulheres = mulheres.toFloat() / total
            val sweepH = pHomens * 360f
            val sweepM = pMulheres * 360f

            Canvas(modifier = Modifier.size(150.dp)) {
                drawArc(
                    color = Color(0xFF64B5F6), // homens
                    startAngle = 0f,
                    sweepAngle = sweepH,
                    useCenter = true
                )
                drawArc(
                    color = Color(0xFFF06292), // mulheres
                    startAngle = sweepH,
                    sweepAngle = sweepM,
                    useCenter = true
                )
            }

            Spacer(Modifier.height(6.dp))

            // Legenda com quantidades + %
            Column(horizontalAlignment = Alignment.Start) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(Color(0xFF64B5F6)))
                    Spacer(Modifier.width(6.dp))
                    Text("Homens: $homens (${String.format("%.0f", pHomens * 100)}%)", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(Color(0xFFF06292)))
                    Spacer(Modifier.width(6.dp))
                    Text("Mulheres: $mulheres (${String.format("%.0f", pMulheres * 100)}%)", fontSize = 12.sp)
                }
            }
        }
    }
}

/* ------------------ Radar (mantido) ------------------ */

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
            // Labels (simples)
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

/* --------- (Opcional) Pizza antiga por proporção numérica — não usada agora ---------
@Composable
fun PieChartComparativo(original1: Float, original2: Float, nome1: String, nome2: String) { ... }
------------------------------------------------------------------------------------- */
