package com.example.contender2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.contender2.network.AspectoComparadoDto
import com.example.contender2.network.ChartPolaridadeCategoriaDto
import com.example.contender2.network.MediaMensalDto
import com.example.contender2.network.OpiniaoTemporalDto
import com.example.contender2.network.RetrofitInstance
import java.net.URLDecoder
import kotlin.math.cos
import kotlin.math.sin

/* =================== Cores =================== */

private val Serie1Color = Color(0xFF6BA9FF) // azul
private val Serie2Color = Color(0xFFFF80A6) // rosa

private val AspectPalette = listOf(
    Color(0xFF6BA9FF), // azul
    Color(0xFFFF80A6), // rosa
    Color(0xFF9C88FF), // roxo
    Color(0xFFFFC85C), // amarelo
    Color(0xFF55E1C4), // verde água
    Color(0xFFF28F79), // coral
    Color(0xFF6EDAA6), // verde
    Color(0xFFA6E3E9)  // azul claro
)

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

    var polaridadesCategoriaRest1 by remember { mutableStateOf<List<ChartPolaridadeCategoriaDto>>(emptyList()) }
    var polaridadesCategoriaRest2 by remember { mutableStateOf<List<ChartPolaridadeCategoriaDto>>(emptyList()) }
    var mediasMensaisRest1 by remember { mutableStateOf<List<MediaMensalDto>>(emptyList()) }
    var mediasMensaisRest2 by remember { mutableStateOf<List<MediaMensalDto>>(emptyList()) }
    var opinioesTempoRest1 by remember { mutableStateOf<List<OpiniaoTemporalDto>>(emptyList()) }
    var opinioesTempoRest2 by remember { mutableStateOf<List<OpiniaoTemporalDto>>(emptyList()) }

    // Decodifica os nomes
    val nome1Dec = remember(nome1) { URLDecoder.decode(nome1, "UTF-8") }
    val nome2Dec = remember(nome2) { URLDecoder.decode(nome2, "UTF-8") }

    LaunchedEffect(id1, id2) {
        try {
            erro = null
            comparacoes = RetrofitInstance.api.compararAspectos(id1, id2)
            polaridadesCategoriaRest1 = RetrofitInstance.api.chartPolaridadeCategoria(id1)
            polaridadesCategoriaRest2 = RetrofitInstance.api.chartPolaridadeCategoria(id2)
            mediasMensaisRest1 = RetrofitInstance.api.getMediaMensal(id1)
            mediasMensaisRest2 = RetrofitInstance.api.getMediaMensal(id2)
            opinioesTempoRest1 = RetrofitInstance.api.getGraficoTemporal(id1)
            opinioesTempoRest2 = RetrofitInstance.api.getGraficoTemporal(id2)
        } catch (e: Exception) {
            e.printStackTrace()
            erro = e.message ?: "Erro ao carregar dados"
        }
    }

    val dadosFiltrados = comparacoes.filter {
        aspecto.isBlank() || it.aspecto.contains(aspecto, ignoreCase = true)
    }

    val categoriaColorProvider = rememberCategoryColorProvider()

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

        if (erro != null) {
            Text("Erro: $erro", color = Color.Red)
        } else when (graficoSelecionado) {
            "Pizza" -> {
                Spacer(Modifier.height(12.dp))

                val categoriasRest1 = polaridadesCategoriaRest1.filter { it.qt_opinioes > 0 }
                val categoriasRest2 = polaridadesCategoriaRest2.filter { it.qt_opinioes > 0 }

                if (categoriasRest1.isEmpty() && categoriasRest2.isEmpty()) {
                    Text("Nenhum dado de categorias para exibir.")
                } else {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        CategoriaPizzaSection(
                            titulo = nome1Dec,
                            dados = categoriasRest1,
                            colorForCategory = categoriaColorProvider
                        )

                        Divider(color = Color(0xFFE0E0E0))

                        CategoriaPizzaSection(
                            titulo = nome2Dec,
                            dados = categoriasRest2,
                            colorForCategory = categoriaColorProvider
                        )
                    }
                }
            }
            "Radar" -> {
                Spacer(Modifier.height(12.dp))
                if (dadosFiltrados.isEmpty()) {
                    Text("Nenhum dado para exibir com o filtro atual.")
                } else {
                    RadarChartTodosAspectos(dadosFiltrados, nome1Dec, nome2Dec)
                }
            }
            else -> {
                Spacer(Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    val temTemporal = opinioesTempoRest1.isNotEmpty() || opinioesTempoRest2.isNotEmpty()
                    val temMediaMensal = mediasMensaisRest1.isNotEmpty() || mediasMensaisRest2.isNotEmpty()
                    val temComparacoes = dadosFiltrados.isNotEmpty()

                    if (!temTemporal && !temMediaMensal && !temComparacoes) {
                        Text("Nenhum dado para exibir com o filtro atual.")
                    } else {
                        if (temTemporal) {
                            TemporalComparativoChart(
                                nome1 = nome1Dec,
                                nome2 = nome2Dec,
                                opinioes1 = opinioesTempoRest1,
                                opinioes2 = opinioesTempoRest2
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (temMediaMensal) {
                            MonthlyMediaSection(
                                nomeRestaurante1 = nome1Dec,
                                nomeRestaurante2 = nome2Dec,
                                dadosRest1 = mediasMensaisRest1,
                                dadosRest2 = mediasMensaisRest2
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (temComparacoes) {
                            dadosFiltrados.forEach { dado ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ) {
                                    Text("Aspecto: ${dado.aspecto}", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    BarChartComparativo(
                                        original1 = dado.notaPredita1,
                                        original2 = dado.notaPredita2,
                                        nome1 = nome1Dec, nome2 = nome2Dec
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* =================== Gráfico Temporal Comparativo =================== */

@Composable
private fun TemporalComparativoChart(
    nome1: String,
    nome2: String,
    opinioes1: List<OpiniaoTemporalDto>,
    opinioes2: List<OpiniaoTemporalDto>
) {
    val meses = remember(opinioes1, opinioes2) {
        (opinioes1.map { it.ano_mes } + opinioes2.map { it.ano_mes })
            .distinct()
            .sorted()
    }

    if (meses.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "Opiniões ao longo do tempo",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        LegendComparacao(
            itens = listOf(
                LegendItemData(label = nome1, color = Serie1Color),
                LegendItemData(label = nome2, color = Serie2Color)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Positivo", fontWeight = FontWeight.Bold)
        TemporalBarSection(
            meses = meses,
            opinioes1 = opinioes1,
            opinioes2 = opinioes2,
            tipo = TemporalOpiniaoTipo.POSITIVA
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text("Negativo", fontWeight = FontWeight.Bold)
        TemporalBarSection(
            meses = meses,
            opinioes1 = opinioes1,
            opinioes2 = opinioes2,
            tipo = TemporalOpiniaoTipo.NEGATIVA
        )
    }
}

private enum class TemporalOpiniaoTipo { POSITIVA, NEGATIVA }

@Composable
private fun TemporalBarSection(
    meses: List<String>,
    opinioes1: List<OpiniaoTemporalDto>,
    opinioes2: List<OpiniaoTemporalDto>,
    tipo: TemporalOpiniaoTipo,
    graficoAltura: Dp = 160.dp
) {
    val maxValor = remember(meses, opinioes1, opinioes2, tipo) {
        val valores = buildList {
            opinioes1.forEach {
                add(if (tipo == TemporalOpiniaoTipo.POSITIVA) it.positivas else it.negativas)
            }
            opinioes2.forEach {
                add(if (tipo == TemporalOpiniaoTipo.POSITIVA) it.positivas else it.negativas)
            }
        }
        valores.maxOrNull()?.coerceAtLeast(1) ?: 1
    }

    val scrollState = rememberScrollState()

    val maxBarHeight = (graficoAltura - 32.dp).coerceAtLeast(0.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        meses.forEach { mes ->
            val v1 = opinioes1.find { it.ano_mes == mes }
                ?.let { if (tipo == TemporalOpiniaoTipo.POSITIVA) it.positivas else it.negativas }
                ?: 0
            val v2 = opinioes2.find { it.ano_mes == mes }
                ?.let { if (tipo == TemporalOpiniaoTipo.POSITIVA) it.positivas else it.negativas }
                ?: 0

            val alturaBarra1 = if (maxValor == 0) 0.dp else maxBarHeight * (v1.toFloat() / maxValor.toFloat())
            val alturaBarra2 = if (maxValor == 0) 0.dp else maxBarHeight * (v2.toFloat() / maxValor.toFloat())

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.height(graficoAltura)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(alturaBarra1)
                            .background(Serie1Color.copy(alpha = 0.75f))
                    )
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(alturaBarra2)
                            .background(Serie2Color.copy(alpha = 0.75f))
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatarMesLabel(mes),
                    fontSize = 11.sp
                )
            }
        }
    }
}
/* =================== Util =================== */

/** Normaliza valor de [-1, 1] para [0, 1] só para desenho. */
private fun normalize01(v: Float): Float = ((v + 1f) / 2f).coerceIn(0f, 1f)

/* =================== Média Mensal =================== */

@Composable
private fun MonthlyMediaSection(
    nomeRestaurante1: String,
    nomeRestaurante2: String,
    dadosRest1: List<MediaMensalDto>,
    dadosRest2: List<MediaMensalDto>
) {
    val mesesOrdenados = remember(dadosRest1, dadosRest2) {
        (dadosRest1.map { it.ano_mes } + dadosRest2.map { it.ano_mes })
            .distinct()
            .sorted()
    }

    if (mesesOrdenados.isEmpty()) return

    val positivosRest1 = remember(dadosRest1) { dadosRest1.associate { it.ano_mes to polaridadePositiva(it.media_polaridade) } }
    val positivosRest2 = remember(dadosRest2) { dadosRest2.associate { it.ano_mes to polaridadePositiva(it.media_polaridade) } }
    val negativosRest1 = remember(dadosRest1) { dadosRest1.associate { it.ano_mes to polaridadeNegativa(it.media_polaridade) } }
    val negativosRest2 = remember(dadosRest2) { dadosRest2.associate { it.ano_mes to polaridadeNegativa(it.media_polaridade) } }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F2FF),
        border = BorderStroke(1.dp, Color(0xFFB39DDB).copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Média de polaridade mensal",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(12.dp))

            LegendComparacao(
                itens = listOf(
                    LegendItemData(label = nomeRestaurante1, color = Serie1Color),
                    LegendItemData(label = nomeRestaurante2, color = Serie2Color)
                )
            )

            Spacer(Modifier.height(16.dp))

            Text("Positivo", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            MonthlyBarChart(
                mesesOrdenados = mesesOrdenados,
                valoresRest1 = positivosRest1,
                valoresRest2 = positivosRest2
            )

            Spacer(Modifier.height(16.dp))

            Text("Negativo", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            MonthlyBarChart(
                mesesOrdenados = mesesOrdenados,
                valoresRest1 = negativosRest1,
                valoresRest2 = negativosRest2
            )
        }
    }
}

@Composable
private fun MonthlyBarChart(
    mesesOrdenados: List<String>,
    valoresRest1: Map<String, Float>,
    valoresRest2: Map<String, Float>,
    chartHeight: Dp = 180.dp
) {
    if (mesesOrdenados.isEmpty()) return

    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val leftPadding = 48.dp
    val rightPadding = 24.dp
    val topPadding = 16.dp
    val bottomPadding = 48.dp
    val groupWidth = 52.dp
    val groupSpacing = 16.dp
    val barSpacing = 8.dp

    val totalGroupsWidth = groupWidth * mesesOrdenados.size
    val totalSpacing = groupSpacing * (mesesOrdenados.size - 1).coerceAtLeast(0)
    val chartWidth = (leftPadding + rightPadding + totalGroupsWidth + totalSpacing)
        .coerceAtLeast(320.dp)

    val tickValues = listOf(0f, 20f, 40f, 60f, 80f, 100f)
    val axisColor = Color(0xFFC8A8F0)
    val gridColor = Color(0xFFEADCF8)
    val axisStrokeWidth = with(density) { 1.5.dp.toPx() }
    val gridStrokeWidth = with(density) { 1.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        Canvas(
            modifier = Modifier
                .height(chartHeight + topPadding + bottomPadding)
                .width(chartWidth)
        ) {
            val leftPx = leftPadding.toPx()
            val rightPx = size.width - rightPadding.toPx()
            val topPx = topPadding.toPx()
            val bottomPx = size.height - bottomPadding.toPx()
            val chartHeightPx = bottomPx - topPx
            val groupWidthPx = groupWidth.toPx()
            val groupSpacingPx = groupSpacing.toPx()
            val barSpacingPx = barSpacing.toPx()
            val barWidthPx = (groupWidthPx - barSpacingPx) / 2f

            val labelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#5A4B81")
                textSize = with(density) { 13.sp.toPx() }
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            val axisLabelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#7A6AA6")
                textSize = with(density) { 12.sp.toPx() }
                isAntiAlias = true
            }

            // Grid horizontal e labels do eixo Y
            tickValues.forEach { tick ->
                val y = bottomPx - (tick / 100f) * chartHeightPx
                drawLine(
                    color = gridColor,
                    start = Offset(leftPx, y),
                    end = Offset(rightPx, y),
                    strokeWidth = gridStrokeWidth
                )
                drawContext.canvas.nativeCanvas.drawText(
                    tick.toInt().toString(),
                    leftPx - with(density) { 12.dp.toPx() },
                    y + with(density) { 4.dp.toPx() },
                    axisLabelPaint
                )
            }

            // Eixos
            drawLine(
                color = axisColor,
                start = Offset(leftPx, topPx),
                end = Offset(leftPx, bottomPx),
                strokeWidth = axisStrokeWidth
            )
            drawLine(
                color = axisColor,
                start = Offset(leftPx, bottomPx),
                end = Offset(rightPx, bottomPx),
                strokeWidth = axisStrokeWidth
            )

            drawContext.canvas.nativeCanvas.drawText(
                "y",
                leftPx - with(density) { 20.dp.toPx() },
                topPx - with(density) { 4.dp.toPx() },
                axisLabelPaint
            )

            val baseYLabel = bottomPx + with(density) { 20.dp.toPx() }

            mesesOrdenados.forEachIndexed { index, mes ->
                val valor1 = valoresRest1[mes]?.coerceIn(0f, 100f) ?: 0f
                val valor2 = valoresRest2[mes]?.coerceIn(0f, 100f) ?: 0f
                val label = formatarMesLabel(mes)

                val groupStart = leftPx + index * (groupWidthPx + groupSpacingPx)

                val bar1Start = groupStart + barSpacingPx / 2f
                val bar2Start = bar1Start + barWidthPx + barSpacingPx / 2f

                val bar1Height = (valor1 / 100f) * chartHeightPx
                val bar2Height = (valor2 / 100f) * chartHeightPx

                drawRect(
                    color = Serie1Color.copy(alpha = 0.85f),
                    topLeft = Offset(bar1Start, bottomPx - bar1Height),
                    size = Size(barWidthPx, bar1Height)
                )

                drawRect(
                    color = Serie2Color.copy(alpha = 0.85f),
                    topLeft = Offset(bar2Start, bottomPx - bar2Height),
                    size = Size(barWidthPx, bar2Height)
                )

                val labelX = groupStart + groupWidthPx / 2f
                drawContext.canvas.nativeCanvas.drawText(label, labelX, baseYLabel, labelPaint)
            }
        }
    }
}

private fun polaridadePositiva(valor: Float): Float = (valor.coerceAtLeast(0f) * 100f).coerceAtMost(100f)

private fun polaridadeNegativa(valor: Float): Float = (-valor.coerceAtMost(0f) * 100f).coerceAtMost(100f)

private fun formatarMesLabel(anoMes: String): String {
    val partes = anoMes.split("-")
    val nomes = listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")
    val mes = partes.getOrNull(1)?.toIntOrNull()
    return if (mes != null && mes in 1..12) nomes[mes - 1] else anoMes
}

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

/* =================== Pizza por categoria =================== */

@Composable
private fun rememberCategoryColorProvider(): (String) -> Color {
    val map = remember { mutableStateMapOf<String, Color>() }
    return remember {
        { categoria ->
            val key = categoria.lowercase()
            map.getOrPut(key) {
                AspectPalette[map.size % AspectPalette.size]
            }
        }
    }
}

@Composable
private fun CategoriaPizzaSection(
    titulo: String,
    dados: List<ChartPolaridadeCategoriaDto>,
    colorForCategory: (String) -> Color
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = titulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp
        )
        Spacer(Modifier.height(12.dp))

        val ordenado = dados
            .filter { it.qt_opinioes > 0 }
            .sortedByDescending { it.qt_opinioes }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF8F2FF),
            border = BorderStroke(1.dp, Color(0xFFB39DDB).copy(alpha = 0.4f))
        ) {
            if (ordenado.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sem dados suficientes", color = Color.Gray)
                }
            } else {
                val total = ordenado.sumOf { it.qt_opinioes }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val isCompact = false

                    if (isCompact) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CategoriaPieChart(
                                dados = ordenado,
                                total = total,
                                colorForCategory = colorForCategory,
                                modifier = Modifier.size(160.dp)
                            )
                            CategoriaLegend(
                                dados = ordenado,
                                total = total,
                                colorForCategory = colorForCategory,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoriaPieChart(
                                dados = ordenado,
                                total = total,
                                colorForCategory = colorForCategory,
                                modifier = Modifier.size(160.dp)
                            )
                            CategoriaLegend(
                                dados = ordenado,
                                total = total,
                                colorForCategory = colorForCategory,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriaPieChart(
    dados: List<ChartPolaridadeCategoriaDto>,
    total: Int,
    colorForCategory: (String) -> Color,
    modifier: Modifier = Modifier
) {
    if (total <= 0) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("Sem dados", color = Color.Gray)
        }
        return
    }

    Canvas(modifier = modifier) {
        var startAngle = -90f
        dados.forEach { item ->
            val valor = item.qt_opinioes
            if (valor <= 0) return@forEach

            val sweep = 360f * (valor.toFloat() / total.toFloat())
            drawArc(
                color = colorForCategory(item.categoria_nome),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true
            )
            startAngle += sweep
        }

        drawCircle(
            color = Color.White.copy(alpha = 0.08f),
            radius = size.minDimension * 0.5f,
            style = Stroke(width = size.minDimension * 0.02f)
        )
    }
}

@Composable
private fun CategoriaLegend(
    dados: List<ChartPolaridadeCategoriaDto>,
    total: Int,
    colorForCategory: (String) -> Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        dados.forEach { item ->
            val percentual = if (total > 0) item.qt_opinioes.toFloat() / total.toFloat() else 0f
            CategoriaLegendItem(
                nome = item.categoria_nome,
                quantidade = item.qt_opinioes,
                percentual = percentual,
                color = colorForCategory(item.categoria_nome)
            )
        }
    }
}

@Composable
private fun CategoriaLegendItem(
    nome: String,
    quantidade: Int,
    percentual: Float,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = nome,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "$quantidade (${"%.2f".format(percentual * 100f)}%)",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B6B6B)
        )
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
