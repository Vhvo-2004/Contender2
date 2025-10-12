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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.contender2.network.AspectoComparadoDto
import com.example.contender2.network.RetrofitInstance
import java.net.URLDecoder
import kotlin.math.cos
import kotlin.math.sin

/* =================== Cores =================== */

private val Serie1Color = Color(0xFF6BA9FF) // azul
private val Serie2Color = Color(0xFFFF80A6) // rosa

// cores fixas das fatias
private val FemaleColor = Color(0xFF9C88FF) // 1 - feminino
private val MaleColor   = Color(0xFF55E1C4) // 2 - masculino
private val OtherColor  = Color(0xFFFFC85C) // 3 - outro/indef.

/* =================== Tela =================== */

@Composable
fun Charts(
    id1: Int,
    id2: Int,
    nome1: String,
    nome2: String,
    navController: NavHostController
) {
    var aspecto by remember { mutableStateOf("") }
    var graficoSelecionado by remember { mutableStateOf("Histograma") }
    val opcoesDeGrafico = listOf("Histograma", "Pizza", "Radar")

    var comparacoes by remember { mutableStateOf<List<AspectoComparadoDto>>(emptyList()) }
    var erro by remember { mutableStateOf<String?>(null) }

    // catálogo: aspecto (texto) -> categoria_id (int)
    var categorias by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    // Decodifica os nomes
    val nome1Dec = remember(nome1) { URLDecoder.decode(nome1, "UTF-8") }
    val nome2Dec = remember(nome2) { URLDecoder.decode(nome2, "UTF-8") }

    LaunchedEffect(id1, id2) {
        try {
            erro = null
            comparacoes = RetrofitInstance.api.compararAspectos(id1, id2)
            // carrega catálogo de categorias para mapear "aspecto" -> categoria_id
            val cats = RetrofitInstance.api.getCategoriasOpiniao()
            categorias = cats.associate { it.categoria.lowercase() to it.id }
        } catch (e: Exception) {
            e.printStackTrace()
            erro = e.message ?: "Erro ao carregar dados"
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
                RadarChartTodosAspectos(dadosFiltrados, nome1Dec, nome2Dec)
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
                                    nome1 = nome1Dec, nome2 = nome2Dec
                                )
                                "Pizza" -> {
                                    // Busca agregados prontos da API /charts/genero
                                    var g1 by remember(dado.aspecto, id1, categorias) { mutableStateOf(GenderCounts(0,0,0)) }
                                    var g2 by remember(dado.aspecto, id2, categorias) { mutableStateOf(GenderCounts(0,0,0)) }

                                    LaunchedEffect(dado.aspecto, id1, categorias) {
                                        g1 = loadGeneroCountsFromApi(id1, dado.aspecto, categorias)
                                    }
                                    LaunchedEffect(dado.aspecto, id2, categorias) {
                                        g2 = loadGeneroCountsFromApi(id2, dado.aspecto, categorias)
                                    }

                                    PieChartGeneroComparativo(
                                        aspecto = dado.aspecto,
                                        nome1 = nome1Dec,
                                        nome2 = nome2Dec,
                                        g1 = g1,
                                        g2 = g2
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

/* =================== Util =================== */

/** Normaliza valor de [-1, 1] para [0, 1] só para desenho. */
private fun normalize01(v: Float): Float = ((v + 1f) / 2f).coerceIn(0f, 1f)

/* =================== Histograma =================== */

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
                .background(Serie1Color.copy(alpha = 0.75f))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text("$nome2: ${"%.3f".format(original2)}", fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth(valor2 / max)
                .height(20.dp)
                .background(Serie2Color.copy(alpha = 0.75f))
        )
    }
}

/* =================== Pizza por gênero =================== */

data class GenderCounts(val feminino: Int, val masculino: Int, val outro: Int) {
    val total get() = feminino + masculino + outro
}

/** Carrega do backend a contagem por gênero para um restaurante + aspecto.
 *  Mapeia 'aspecto' -> categoria_id usando o catálogo carregado.
 */
private suspend fun loadGeneroCountsFromApi(
    restauranteId: Int,
    aspecto: String,
    categorias: Map<String, Int>
): GenderCounts {
    val catId = categorias[aspecto.lowercase()] ?: return GenderCounts(0, 0, 0)
    val rows = RetrofitInstance.api.chartGenero(restauranteId, categoriaId = catId)
    val row = rows.firstOrNull() ?: return GenderCounts(0, 0, 0)
    return GenderCounts(
        feminino = row.fem_count,
        masculino = row.masc_count,
        outro = row.outros_count
    )
}

@Composable
fun PieChartGeneroComparativo(
    aspecto: String,
    nome1: String,
    nome2: String,
    g1: GenderCounts,
    g2: GenderCounts
) {
    Column(Modifier.fillMaxWidth()) {
        Text("Distribuição por gênero • $aspecto", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                PieGenero(titulo = nome1, counts = g1)
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                PieGenero(titulo = nome2, counts = g2)
            }
        }
    }
}

@Composable
private fun PieGenero(titulo: String, counts: GenderCounts) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            titulo,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            lineHeight = 18.sp,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(6.dp))

        if (counts.total == 0) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(Color(0xFFECECEC), shape = RoundedCornerShape(75.dp)),
                contentAlignment = Alignment.Center
            ) { Text("Sem opiniões", color = Color.Gray, fontSize = 12.sp) }
            return
        }

        val total = counts.total.toFloat()
        val fatias = listOf(
            counts.feminino to FemaleColor,
            counts.masculino to MaleColor,
            counts.outro to OtherColor
        )

        Canvas(modifier = Modifier.size(150.dp)) {
            var start = -90f
            fatias.forEach { (valor, cor) ->
                if (valor > 0) {
                    val sweep = 360f * (valor / total)
                    drawArc(color = cor, startAngle = start, sweepAngle = sweep, useCenter = true)
                    start += sweep
                }
            }
            // anel central opcional
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = size.minDimension * 0.28f,
                center = center
            )
        }

        Spacer(Modifier.height(6.dp))

        // Legenda + contagens/percentuais (1 casa)
        val pF = if (total > 0f) "%.1f".format(counts.feminino / total * 100f) else "0.0"
        val pM = if (total > 0f) "%.1f".format(counts.masculino / total * 100f) else "0.0"
        val pO = if (total > 0f) "%.1f".format(counts.outro     / total * 100f) else "0.0"

        Column(horizontalAlignment = Alignment.Start) {
            LegendGeneroRow("Feminino: ${counts.feminino} (${pF}%)", FemaleColor)
            LegendGeneroRow("Masculino: ${counts.masculino} (${pM}%)", MaleColor)
            LegendGeneroRow("Outro/Indef.: ${counts.outro} (${pO}%)", OtherColor)
        }
    }
}

@Composable
private fun LegendGeneroRow(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 12.sp)
    }
}

/* =================== Radar =================== */

@Composable
fun RadarChartTodosAspectos(
    aspectos: List<AspectoComparadoDto>,
    nomeRestaurante1: String,
    nomeRestaurante2: String
) {
    val MAX_AXES = 17
    val dados = aspectos.sortedBy { it.aspecto.lowercase() }.take(MAX_AXES)

    if (dados.isEmpty()) { Text("Sem dados para o gráfico de radar."); return }
    if (dados.size < 3) { Text("O gráfico de radar precisa de pelo menos 3 categorias."); return }

    val labels = dados.map { it.aspecto }
    val valores1 = dados.map { normalize01(it.notaPredita1) }
    val valores2 = dados.map { normalize01(it.notaPredita2) }

    val raioDp = 120.dp
    val labelPadDp = 16.dp

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .padding(8.dp)
    ) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = raioDp.toPx()
        val labelPad = labelPadDp.toPx()
        val count = labels.size
        val angleStep = (2.0 * Math.PI / count).toFloat()

        // grade
        val rings = 4
        repeat(rings) { i ->
            val r = radius * (i + 1) / rings
            drawCircle(
                color = Color(0xFFE0E0E0),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = 1.5f)
            )
        }

        // eixos + labels
        val labelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 22f
            isAntiAlias = true
        }
        for (i in 0 until count) {
            val ang = angleStep * i - Math.PI.toFloat() / 2f
            val x = cx + radius * cos(ang)
            val y = cy + radius * sin(ang)
            drawLine(
                color = Color(0xFFBDBDBD),
                start = Offset(cx, cy),
                end = Offset(x, y),
                strokeWidth = 1.5f
            )
            val lx = cx + (radius + labelPad) * cos(ang)
            val ly = cy + (radius + labelPad) * sin(ang)
            drawContext.canvas.nativeCanvas.drawText(labels[i], lx, ly, labelPaint)
        }

        fun pontos(vals: List<Float>): List<Offset> =
            vals.mapIndexed { i, v ->
                val ang = angleStep * i - Math.PI.toFloat() / 2f
                val rLocal = (v.coerceIn(0f, 1f)) * radius
                Offset(cx + rLocal * cos(ang), cy + rLocal * sin(ang))
            }

        val p1 = pontos(valores1); drawPolygon(p1, color = Serie1Color)
        val p2 = pontos(valores2); drawPolygon(p2, color = Serie2Color)

        drawCircle(color = Color.Gray, radius = radius, center = Offset(cx, cy), style = Stroke(width = 2f))
    }

    // Estes componentes (LegendComparacao/LegendItemData) ficam no seu arquivo Legend.kt
    LegendComparacao(
        itens = listOf(
            LegendItemData(label = nomeRestaurante1, color = Serie1Color),
            LegendItemData(label = nomeRestaurante2, color = Serie2Color)
        ),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

private fun DrawScope.drawPolygon(
    points: List<Offset>,
    color: Color,
    fillAlpha: Float = 0.40f,
    strokeWidth: Float = 2f
) {
    if (points.size < 3) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
        close()
    }
    drawPath(path = path, color = color.copy(alpha = fillAlpha))
    drawPath(path = path, color = color, style = Stroke(width = strokeWidth))
}

@Composable
private fun LegendItem(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(14.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Clip
        )
    }
}
