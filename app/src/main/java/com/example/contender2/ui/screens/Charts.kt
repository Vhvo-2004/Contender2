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
import androidx.compose.ui.graphics.Brush
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
import com.example.contender2.network.ChartPolaridadeAspectoDto
import com.example.contender2.network.RetrofitInstance
import java.net.URLDecoder
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
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
    val opcoesDeGrafico = listOf("Histograma", "Pizza", "Radar", "Resumo")

    var comparacoes by remember { mutableStateOf<List<AspectoComparadoDto>>(emptyList()) }
    var erro by remember { mutableStateOf<String?>(null) }

    var polaridadesRest1 by remember { mutableStateOf<List<ChartPolaridadeAspectoDto>>(emptyList()) }
    var polaridadesRest2 by remember { mutableStateOf<List<ChartPolaridadeAspectoDto>>(emptyList()) }

    // Decodifica os nomes
    val nome1Dec = remember(nome1) { URLDecoder.decode(nome1, "UTF-8") }
    val nome2Dec = remember(nome2) { URLDecoder.decode(nome2, "UTF-8") }

    LaunchedEffect(id1, id2) {
        try {
            erro = null
            comparacoes = RetrofitInstance.api.compararAspectos(id1, id2)
            polaridadesRest1 = RetrofitInstance.api.chartPolaridade(id1)
            polaridadesRest2 = RetrofitInstance.api.chartPolaridade(id2)
        } catch (e: Exception) {
            e.printStackTrace()
            erro = e.message ?: "Erro ao carregar dados"
        }
    }

    val dadosFiltrados = comparacoes.filter {
        aspecto.isBlank() || it.aspecto.contains(aspecto, ignoreCase = true)
    }

    val aspectoColorProvider = rememberAspectColorProvider()

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
            graficoSelecionado == "Pizza" -> {
                Spacer(Modifier.height(12.dp))

                val chart1 = polaridadesRest1.filter {
                    aspecto.isBlank() || it.aspecto.contains(aspecto, ignoreCase = true)
                }
                val chart2 = polaridadesRest2.filter {
                    aspecto.isBlank() || it.aspecto.contains(aspecto, ignoreCase = true)
                }

                if (chart1.isEmpty() && chart2.isEmpty()) {
                    Text("Nenhum dado de aspectos para exibir.")
                } else {
                    PieChartAspectoComparativo(
                        nome1 = nome1Dec,
                        nome2 = nome2Dec,
                        dados1 = chart1,
                        dados2 = chart2,
                        colorForAspect = aspectoColorProvider
                    )
                }
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
                        key(dado.aspecto) {
                            val serie1 = remember(polaridadesRest1, dado.aspecto) {
                                buildMonthlyPolaritySeries(polaridadesRest1, dado.aspecto)
                            }
                            val serie2 = remember(polaridadesRest2, dado.aspecto) {
                                buildMonthlyPolaritySeries(polaridadesRest2, dado.aspecto)
                            }

                            AspectTemporalComparison(
                                aspecto = dado.aspecto,
                                nome1 = nome1Dec,
                                nome2 = nome2Dec,
                                serie1 = serie1,
                                serie2 = serie2
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                        }
                    }
                }
            }
            graficoSelecionado == "Resumo" -> {
                Spacer(Modifier.height(12.dp))
                ResumoComparativo(
                    dados = dadosFiltrados.ifEmpty { comparacoes },
                    nome1 = nome1Dec,
                    nome2 = nome2Dec
                )
            }
        }
    }
}

/* =================== Util =================== */

/** Normaliza valor de [-1, 1] para [0, 1] só para desenho. */
private fun normalize01(v: Float): Float = ((v + 1f) / 2f).coerceIn(0f, 1f)

/* =================== Histograma =================== */

private val MonthFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM").withLocale(Locale("pt", "BR"))

private data class AspectMonthlyPolarity(
    val month: YearMonth,
    val positive: Float,
    val negative: Float
)

private fun buildMonthlyPolaritySeries(
    dados: List<ChartPolaridadeAspectoDto>,
    aspecto: String
): List<AspectMonthlyPolarity> {
    return dados.asSequence()
        .filter { it.aspecto.equals(aspecto, ignoreCase = true) }
        .mapNotNull { dto ->
            val yearMonth = parseYearMonth(dto.updated_at) ?: return@mapNotNull null
            yearMonth to dto.avg_polaridade.toFloat()
        }
        .groupBy({ it.first }, { it.second })
        .map { (month, valores) ->
            val media = valores.average().toFloat()
            AspectMonthlyPolarity(
                month = month,
                positive = if (media > 0f) media.coerceIn(0f, 1f) * 100f else 0f,
                negative = if (media < 0f) (-media).coerceIn(0f, 1f) * 100f else 0f
            )
        }
        .sortedBy { it.month }
}

private fun parseYearMonth(value: String?): YearMonth? {
    if (value.isNullOrBlank()) return null
    val trimmed = value.trim()
    val zone = java.time.ZoneId.systemDefault()

    val attempts: List<() -> YearMonth?> = listOf(
        {
            runCatching { Instant.parse(trimmed).atZone(zone) }
                .getOrNull()
                ?.let { YearMonth.of(it.year, it.month) }
        },
        {
            runCatching { LocalDateTime.parse(trimmed) }
                .getOrNull()
                ?.let { YearMonth.of(it.year, it.month) }
        },
        {
            runCatching {
                LocalDateTime.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }.getOrNull()?.let { YearMonth.of(it.year, it.month) }
        },
        {
            runCatching { LocalDate.parse(trimmed) }
                .getOrNull()
                ?.let { YearMonth.of(it.year, it.month) }
        },
        {
            runCatching {
                LocalDate.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            }.getOrNull()?.let { YearMonth.of(it.year, it.month) }
        },
        {
            runCatching { YearMonth.parse(trimmed) }.getOrNull()
        },
        {
            runCatching {
                YearMonth.parse(trimmed, DateTimeFormatter.ofPattern("yyyy-MM"))
            }.getOrNull()
        }
    )

    for (attempt in attempts) {
        val result = attempt()
        if (result != null) return result
    }
    return null
}

@Composable
private fun AspectTemporalComparison(
    aspecto: String,
    nome1: String,
    nome2: String,
    serie1: List<AspectMonthlyPolarity>,
    serie2: List<AspectMonthlyPolarity>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text("Aspecto: $aspecto", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        AspectTrendCard(titulo = nome1, dados = serie1)
        Spacer(modifier = Modifier.height(16.dp))
        AspectTrendCard(titulo = nome2, dados = serie2)
    }
}

@Composable
private fun AspectTrendCard(titulo: String, dados: List<AspectMonthlyPolarity>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFD9C6F0), RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(titulo, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))

        if (dados.isEmpty()) {
            Text(
                text = "Sem dados temporais suficientes para este aspecto.",
                color = Color.Gray,
                fontSize = 12.sp
            )
        } else {
            LegendComparacao(
                itens = listOf(
                    LegendItemData(label = "Positivo", color = Serie1Color),
                    LegendItemData(label = "Negativo", color = Serie2Color)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            AspectPolarityTrendChart(
                dados = dados,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
        }
    }
}

@Composable
private fun AspectPolarityTrendChart(
    dados: List<AspectMonthlyPolarity>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val leftPadding = 56f
        val bottomPadding = 36f
        val topPadding = 16f

        val chartWidth = size.width - leftPadding
        val chartHeight = size.height - bottomPadding - topPadding
        if (chartWidth <= 0f || chartHeight <= 0f) return@Canvas

        val originX = leftPadding
        val originY = size.height - bottomPadding
        val maxValue = 100f
        val groupWidth = chartWidth / dados.size.coerceAtLeast(1)
        val barWidth = groupWidth / 3f
        val barSpacing = barWidth / 2f

        val gridSteps = listOf(0f, 25f, 50f, 75f, 100f)
        val labelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 26f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        val axisPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }

        gridSteps.forEach { step ->
            val y = originY - (step / maxValue) * chartHeight
            drawLine(
                color = Color(0xFFE0E0E0),
                start = Offset(originX, y),
                end = Offset(originX + chartWidth, y),
                strokeWidth = if (step == 0f) 2f else 1f
            )
            if (step > 0f) {
                drawContext.canvas.nativeCanvas.drawText(
                    step.toInt().toString(),
                    originX - 8f,
                    y + 8f,
                    axisPaint
                )
            }
        }

        dados.forEachIndexed { index, entry ->
            val centerX = originX + groupWidth * index + groupWidth / 2f
            val positiveHeight = (entry.positive / maxValue) * chartHeight
            val negativeHeight = (entry.negative / maxValue) * chartHeight

            val positiveX = centerX - barWidth - barSpacing / 2f
            val negativeX = centerX + barSpacing / 2f

            drawRect(
                color = Serie1Color,
                topLeft = Offset(positiveX, originY - positiveHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, positiveHeight)
            )
            drawRect(
                color = Serie2Color,
                topLeft = Offset(negativeX, originY - negativeHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, negativeHeight)
            )

            val label = entry.month.format(MonthFormatter).lowercase()
            drawContext.canvas.nativeCanvas.drawText(
                label,
                centerX,
                originY + 24f,
                labelPaint
            )
        }
    }
}

/* =================== Resumo =================== */

@Composable
private fun ResumoComparativo(
    dados: List<AspectoComparadoDto>,
    nome1: String,
    nome2: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        OverallSentimentSummary(dados = dados, nome1 = nome1, nome2 = nome2)
        AspectDifferenceHighlights(dados = dados, nome1 = nome1, nome2 = nome2)
    }
}

@Composable
private fun OverallSentimentSummary(
    dados: List<AspectoComparadoDto>,
    nome1: String,
    nome2: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFD9C6F0), RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text("Satisfação média estimada", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        if (dados.isEmpty()) {
            Text("Nenhum aspecto corresponde ao filtro atual.", color = Color.Gray)
            return
        }

        val media1 = dados.map { it.notaPredita1 }.average().toFloat()
        val media2 = dados.map { it.notaPredita2 }.average().toFloat()
        val percent1 = normalize01(media1) * 100f
        val percent2 = normalize01(media2) * 100f

        SentimentProgressRow(
            label = nome1,
            percent = percent1,
            rawScore = media1,
            color = Serie1Color
        )
        Spacer(Modifier.height(12.dp))
        SentimentProgressRow(
            label = nome2,
            percent = percent2,
            rawScore = media2,
            color = Serie2Color
        )
    }
}

@Composable
private fun SentimentProgressRow(
    label: String,
    percent: Float,
    rawScore: Float,
    color: Color
) {
    val coerced = percent.coerceIn(0f, 100f)
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.Medium)
            Text("${"%.2f".format(rawScore)}", color = Color.DarkGray, fontSize = 12.sp)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF1ECFA))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (coerced / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text("${"%.1f".format(coerced)}%", color = Color.DarkGray, fontSize = 11.sp)
    }
}

private data class AspectDifferenceHighlight(
    val aspecto: String,
    val valor1: Float,
    val valor2: Float
)

@Composable
private fun AspectDifferenceHighlights(
    dados: List<AspectoComparadoDto>,
    nome1: String,
    nome2: String
) {
    val highlights = dados
        .map { AspectDifferenceHighlight(it.aspecto, it.notaPredita1, it.notaPredita2) }
        .sortedByDescending { abs(it.valor1 - it.valor2) }
        .take(5)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFD9C6F0), RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text("Maiores diferenças por aspecto", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        if (highlights.isEmpty()) {
            Text("Nenhum aspecto corresponde ao filtro atual.", color = Color.Gray)
            return
        }

        highlights.forEachIndexed { index, highlight ->
            AspectDifferenceRow(
                destaque = highlight,
                nome1 = nome1,
                nome2 = nome2
            )
            if (index != highlights.lastIndex) {
                Spacer(Modifier.height(14.dp))
                Divider(color = Color(0xFFE8E1F5))
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun AspectDifferenceRow(
    destaque: AspectDifferenceHighlight,
    nome1: String,
    nome2: String
) {
    val percent1 = normalize01(destaque.valor1) * 100f
    val percent2 = normalize01(destaque.valor2) * 100f
    val diffPercent = percent1 - percent2
    val vencedor = when {
        diffPercent > 1f -> nome1
        diffPercent < -1f -> nome2
        else -> "Empate"
    }

    Column(Modifier.fillMaxWidth()) {
        Text(destaque.aspecto, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        SentimentProgressRow(nome1, percent1, destaque.valor1, Serie1Color)
        Spacer(Modifier.height(12.dp))
        SentimentProgressRow(nome2, percent2, destaque.valor2, Serie2Color)
        Spacer(Modifier.height(8.dp))
        val diffTexto = when (vencedor) {
            "Empate" -> "Diferença mínima entre os restaurantes"
            else -> "${"%.1f".format(abs(diffPercent))}% a favor de $vencedor"
        }
        Text(diffTexto, color = Color(0xFF6A5AA9), fontSize = 12.sp)
    }
}

/* =================== Pizza por aspecto =================== */

@Composable
private fun rememberAspectColorProvider(): (String) -> Color {
    val map = remember { mutableStateMapOf<String, Color>() }
    return remember {
        { aspecto ->
            val key = aspecto.lowercase()
            map.getOrPut(key) {
                AspectPalette[map.size % AspectPalette.size]
            }
        }
    }
}

@Composable
fun PieChartAspectoComparativo(
    nome1: String,
    nome2: String,
    dados1: List<ChartPolaridadeAspectoDto>,
    dados2: List<ChartPolaridadeAspectoDto>,
    colorForAspect: (String) -> Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        PieChartRestaurante(
            titulo = nome1,
            dados = dados1,
            colorForAspect = colorForAspect
        )
        PieChartRestaurante(
            titulo = nome2,
            dados = dados2,
            colorForAspect = colorForAspect
        )
    }
}

@Composable
private fun PieChartRestaurante(
    titulo: String,
    dados: List<ChartPolaridadeAspectoDto>,
    colorForAspect: (String) -> Color
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            titulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(8.dp))

        if (dados.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF2F2F2))
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Sem dados suficientes", color = Color.Gray)
            }
            return
        }

        val ordenado = dados.filter { it.qt_opinioes > 0 }
            .sortedByDescending { it.qt_opinioes }

        if (ordenado.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF2F2F2))
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Sem dados suficientes", color = Color.Gray)
            }
            return
        }

        val total = ordenado.sumOf { it.qt_opinioes }
        val slices = ordenado.map { dado ->
            val color = colorForAspect(dado.aspecto)
            PieSlice(
                label = dado.aspecto,
                valor = dado.qt_opinioes,
                percentual = if (total > 0) dado.qt_opinioes.toFloat() / total.toFloat() else 0f,
                color = color
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                AspectPieChart(slices = slices)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                slices.forEach { slice ->
                    LegendAspectRow(slice)
                }
            }
        }
    }
}

private data class PieSlice(
    val label: String,
    val valor: Int,
    val percentual: Float,
    val color: Color
)

@Composable
private fun AspectPieChart(slices: List<PieSlice>) {
    val total = slices.sumOf { it.valor }
    if (total <= 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(75.dp))
                .background(Color(0xFFF2F2F2)),
            contentAlignment = Alignment.Center
        ) {
            Text("Sem dados", color = Color.Gray)
        }
        return
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        var start = -90f
        slices.forEach { slice ->
            if (slice.valor > 0) {
                val sweep = 360f * (slice.valor.toFloat() / total.toFloat())
                drawArc(
                    color = slice.color,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = true
                )
                start += sweep
            }
        }

        drawCircle(
            color = Color.White.copy(alpha = 0.6f),
            radius = size.minDimension * 0.28f,
            center = center
        )
    }
}

@Composable
private fun LegendAspectRow(slice: PieSlice) {
    val percentText = "%.2f".format(slice.percentual * 100f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(slice.color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "${slice.label}: ${slice.valor} (${percentText}%)",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
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

        // fundo com leve gradiente radial
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFF5F2FF),
                    Color(0xFFE8F1FF)
                ),
                center = Offset(cx, cy),
                radius = radius * 1.35f
            ),
            size = size
        )

        // grade
        val rings = 4
        repeat(rings) { i ->
            val r = radius * (i + 1) / rings
            drawCircle(
                color = Color(0xFFDDD6F3),
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
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val labelBackgroundPadding = 12f
        for (i in 0 until count) {
            val ang = angleStep * i - Math.PI.toFloat() / 2f
            val x = cx + radius * cos(ang)
            val y = cy + radius * sin(ang)
            drawLine(
                color = Color(0xFFC7BCD6),
                start = Offset(cx, cy),
                end = Offset(x, y),
                strokeWidth = 1.5f
            )
            val lx = cx + (radius + labelPad) * cos(ang)
            val ly = cy + (radius + labelPad) * sin(ang)
            val labelText = labels[i]
            val textWidth = labelPaint.measureText(labelText)
            val fontMetrics = labelPaint.fontMetrics
            val textHeight = fontMetrics.bottom - fontMetrics.top
            val rectLeft = lx - textWidth / 2f - labelBackgroundPadding / 2f
            val rectTop = ly + fontMetrics.top - labelBackgroundPadding / 2f
            drawRoundRect(
                color = Color.White.copy(alpha = 0.85f),
                topLeft = Offset(rectLeft, rectTop),
                size = androidx.compose.ui.geometry.Size(
                    textWidth + labelBackgroundPadding,
                    textHeight + labelBackgroundPadding
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
            drawContext.canvas.nativeCanvas.drawText(labelText, lx, ly, labelPaint)
        }

        fun pontos(vals: List<Float>): List<Offset> =
            vals.mapIndexed { i, v ->
                val ang = angleStep * i - Math.PI.toFloat() / 2f
                val rLocal = (v.coerceIn(0f, 1f)) * radius
                Offset(cx + rLocal * cos(ang), cy + rLocal * sin(ang))
            }

        val p1 = pontos(valores1); drawPolygon(p1, color = Serie1Color)
        val p2 = pontos(valores2); drawPolygon(p2, color = Serie2Color)

        p1.forEach { ponto ->
            drawCircle(color = Serie1Color, center = ponto, radius = 6f)
            drawCircle(color = Color.White, center = ponto, radius = 2.5f)
        }
        p2.forEach { ponto ->
            drawCircle(color = Serie2Color, center = ponto, radius = 6f)
            drawCircle(color = Color.White, center = ponto, radius = 2.5f)
        }

        drawCircle(color = Color(0xFFB49DD7), radius = radius, center = Offset(cx, cy), style = Stroke(width = 2f))
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

