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
import com.example.contender2.network.ChartPolaridadeCategoriaTemporalDto
import com.example.contender2.network.MediaMensalDto
import com.example.contender2.network.RetrofitInstance
import java.net.URLDecoder
import kotlin.math.cos
import kotlin.math.sin

/* =================== Cores =================== */

private val Serie1Color = Color(0xFF6BA9FF) // azul
private val Serie2Color = Color(0xFFFF80A6) // rosa

@Composable
internal fun chartTextColor(): Color = MaterialTheme.colorScheme.onSurface

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
    val opcoesDeGrafico = listOf("Histograma", "Rosca", "Radar")

    var comparacoes by remember { mutableStateOf<List<AspectoComparadoDto>>(emptyList()) }
    var erro by remember { mutableStateOf<String?>(null) }

    var polaridadesCategoriaRest1 by remember { mutableStateOf<List<ChartPolaridadeCategoriaDto>>(emptyList()) }
    var polaridadesCategoriaRest2 by remember { mutableStateOf<List<ChartPolaridadeCategoriaDto>>(emptyList()) }
    var mediasMensaisRest1 by remember { mutableStateOf<List<MediaMensalDto>>(emptyList()) }
    var mediasMensaisRest2 by remember { mutableStateOf<List<MediaMensalDto>>(emptyList()) }
    var polaridadesCategoriaTemporalRest1 by remember { mutableStateOf<List<ChartPolaridadeCategoriaTemporalDto>>(emptyList()) }
    var polaridadesCategoriaTemporalRest2 by remember { mutableStateOf<List<ChartPolaridadeCategoriaTemporalDto>>(emptyList()) }
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
            polaridadesCategoriaTemporalRest1 = RetrofitInstance.api.chartPolaridadeCategoriaTemporal(id1)
            polaridadesCategoriaTemporalRest2 = RetrofitInstance.api.chartPolaridadeCategoriaTemporal(id2)
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
            Text(
                "Comparação",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = chartTextColor()
            )
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
                    Text(opcao, color = chartTextColor())
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Aspecto ou categoria", fontWeight = FontWeight.Bold, color = chartTextColor())
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
                placeholder = { Text("comida, atendimento...", color = chartTextColor().copy(alpha = 0.6f)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { /* filtro já é reativo */ }) {
                Text("Pesquisar", color = chartTextColor())
            }
        }

        if (erro != null) {
            Text("Erro: $erro", color = Color.Red)
        } else when (graficoSelecionado) {
            "Rosca" -> {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Aspectos mais comentados dos restaurantes.",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = chartTextColor(),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Spacer(Modifier.height(12.dp))

                val categoriasRest1 = polaridadesCategoriaRest1.filter { it.qt_opinioes > 0 }
                val categoriasRest2 = polaridadesCategoriaRest2.filter { it.qt_opinioes > 0 }

                if (categoriasRest1.isEmpty() && categoriasRest2.isEmpty()) {
                    Text("Nenhum dado de categorias para exibir.", color = chartTextColor())
                } else {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        CategoriaDonutSection(
                            titulo = nome1Dec,
                            dados = categoriasRest1,
                            colorForCategory = categoriaColorProvider
                        )

                        Divider(color = Color(0xFFE0E0E0))

                        CategoriaDonutSection(
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
                    Text("Nenhum dado para exibir com o filtro atual.", color = chartTextColor())
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
                    val dadosTemporaisRest1 = polaridadesCategoriaTemporalRest1.filter {
                        it.qt_opinioes > 0 && (aspecto.isBlank() || nomeCategoriaTemporal(it).contains(aspecto, ignoreCase = true))
                    }
                    val dadosTemporaisRest2 = polaridadesCategoriaTemporalRest2.filter {
                        it.qt_opinioes > 0 && (aspecto.isBlank() || nomeCategoriaTemporal(it).contains(aspecto, ignoreCase = true))
                    }
                    val temMediaMensal = mediasMensaisRest1.isNotEmpty() || mediasMensaisRest2.isNotEmpty()
                    val temDadosTemporais = dadosTemporaisRest1.isNotEmpty() || dadosTemporaisRest2.isNotEmpty()

                    if (!temMediaMensal && !temDadosTemporais) {
                        Text("Nenhum dado temporal para exibir com o filtro atual.", color = chartTextColor())
                    } else {
                        if (temMediaMensal) {
                            MonthlyMediaSection(
                                nomeRestaurante1 = nome1Dec,
                                nomeRestaurante2 = nome2Dec,
                                dadosRest1 = mediasMensaisRest1,
                                dadosRest2 = mediasMensaisRest2
                            )
                        }

                        if (temMediaMensal && temDadosTemporais) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (temDadosTemporais) {
                            CategoryTemporalComparisonSection(
                                nomeRestaurante1 = nome1Dec,
                                nomeRestaurante2 = nome2Dec,
                                dadosRest1 = dadosTemporaisRest1,
                                dadosRest2 = dadosTemporaisRest2
                            )
                        } else if (aspecto.isNotBlank()) {
                            Text("Nenhum dado temporal de categorias para exibir com o filtro atual.", color = chartTextColor())
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
private fun polaridadeEscalonada(valor: Float): Float =
    (valor * 100f).coerceIn(-100f, 100f)

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
    var periodoSelecionado by remember { mutableStateOf(MonthRange.LAST_6) }
    var mesesPersonalizados by remember { mutableStateOf(6) }

    if (mesesOrdenados.isEmpty()) return

    val valoresRest1 = remember(dadosRest1) {
        dadosRest1.associate { it.ano_mes to polaridadeEscalonada(it.media_polaridade) }
    }
    val valoresRest2 = remember(dadosRest2) {
        dadosRest2.associate { it.ano_mes to polaridadeEscalonada(it.media_polaridade) }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F2FF),
        border = BorderStroke(1.dp, Color(0xFFB39DDB).copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Comparação Geral Mensal",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = chartTextColor()
            )

            Spacer(Modifier.height(12.dp))

            LegendComparacao(
                itens = listOf(
                    LegendItemData(label = nomeRestaurante1, color = Serie1Color),
                    LegendItemData(label = nomeRestaurante2, color = Serie2Color)
                )
            )

            Spacer(Modifier.height(16.dp))

            //Indicador de escala (Ruim → Bom)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

            }

            Spacer(Modifier.height(12.dp))

            MonthRangeSelector(
                periodoSelecionado = periodoSelecionado,
                mesesPersonalizados = mesesPersonalizados,
                onPeriodoSelecionado = { periodoSelecionado = it },
                onMesesPersonalizadosChange = { mesesPersonalizados = it }
            )

            MonthlyBarChart(
                mesesOrdenados = filtrarMesesPorPeriodo(
                    mesesOrdenados = mesesOrdenados,
                    periodoSelecionado = periodoSelecionado,
                    mesesPersonalizados = mesesPersonalizados
                ),
                valoresRest1 = valoresRest1,
                valoresRest2 = valoresRest2
            )
        }
    }
}


@Composable
private fun MonthlyBarChart(
    mesesOrdenados: List<String>,
    valoresRest1: Map<String, Float>,
    valoresRest2: Map<String, Float>,
    chartHeight: Dp = 220.dp
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
        .coerceAtLeast(360.dp)

    // NOVO: eixo de -100 até +100
    val tickValues = listOf(-100f, -50f, 0f, 50f, 100f)
    val axisColor = Color(0xFFC8A8F0)
    val gridColor = Color(0xFFEADCF8)

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
            val fullHeight = bottomPx - topPx

            val zeroY = topPx + (fullHeight / 2) // centro

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

            // GRID horizontal
            tickValues.forEach { tick ->
                val y = zeroY - (tick / 100f) * (fullHeight / 2f)
                drawLine(
                    color = if (tick == 0f) Color.Gray else gridColor,
                    start = Offset(leftPx, y),
                    end = Offset(rightPx, y),
                    strokeWidth = 1f
                )
                drawContext.canvas.nativeCanvas.drawText(
                    tick.toInt().toString(),
                    leftPx - 20.dp.toPx(),
                    y + 4.dp.toPx(),
                    axisLabelPaint
                )
            }

            // EIXOS
            drawLine(
                color = axisColor,
                start = Offset(leftPx, topPx),
                end = Offset(leftPx, bottomPx),
                strokeWidth = 2f
            )
            drawLine(
                color = axisColor,
                start = Offset(leftPx, zeroY),
                end = Offset(rightPx, zeroY),
                strokeWidth = 2f
            )

            // DESENHO DAS BARRAS
            mesesOrdenados.forEachIndexed { index, mes ->

                val v1 = valoresRest1[mes] ?: 0f
                val v2 = valoresRest2[mes] ?: 0f
                val label = formatarMesLabel(mes)

                val groupStart = leftPx + index * (groupWidthPx + groupSpacingPx)

                val bar1X = groupStart + barSpacingPx / 2f
                val bar2X = bar1X + barWidthPx + barSpacingPx / 2f

                fun barEndY(valor: Float): Float {
                    return zeroY - (valor / 100f) * (fullHeight / 2f)
                }

                // barra 1
                drawLine(
                    color = Serie1Color,
                    start = Offset(bar1X + barWidthPx / 2, zeroY),
                    end = Offset(bar1X + barWidthPx / 2, barEndY(v1)),
                    strokeWidth = barWidthPx
                )

                // barra 2
                drawLine(
                    color = Serie2Color,
                    start = Offset(bar2X + barWidthPx / 2, zeroY),
                    end = Offset(bar2X + barWidthPx / 2, barEndY(v2)),
                    strokeWidth = barWidthPx
                )

                // label mês
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    groupStart + groupWidthPx / 2,
                    bottomPx + 20.dp.toPx(),
                    labelPaint
                )
            }
        }
    }
}


/* =================== Polaridade por categoria no tempo =================== */

private data class CategoriaTemporalComparada(
    val categoriaId: Int,
    val categoriaNome: String,
    val dadosRest1: List<ChartPolaridadeCategoriaTemporalDto>,
    val dadosRest2: List<ChartPolaridadeCategoriaTemporalDto>
)

private fun nomeCategoriaTemporal(item: ChartPolaridadeCategoriaTemporalDto): String =
    item.categoria_nome?.takeIf { it.isNotBlank() } ?: "Categoria ${item.categoria_id}"

@Composable
private fun CategoryTemporalComparisonSection(
    nomeRestaurante1: String,
    nomeRestaurante2: String,
    dadosRest1: List<ChartPolaridadeCategoriaTemporalDto>,
    dadosRest2: List<ChartPolaridadeCategoriaTemporalDto>
) {
    val categorias = remember(dadosRest1, dadosRest2) {
        val agrupadoRest1 = dadosRest1.groupBy { it.categoria_id }
        val agrupadoRest2 = dadosRest2.groupBy { it.categoria_id }

        (agrupadoRest1.keys + agrupadoRest2.keys)
            .distinct()
            .map { categoriaId ->
                val grupoRest1 = agrupadoRest1[categoriaId].orEmpty().sortedBy { it.periodo }
                val grupoRest2 = agrupadoRest2[categoriaId].orEmpty().sortedBy { it.periodo }
                val nome = (grupoRest1.firstOrNull()?.let(::nomeCategoriaTemporal)
                    ?: grupoRest2.firstOrNull()?.let(::nomeCategoriaTemporal)
                    ?: "Categoria $categoriaId")

                CategoriaTemporalComparada(
                    categoriaId = categoriaId,
                    categoriaNome = nome,
                    dadosRest1 = grupoRest1,
                    dadosRest2 = grupoRest2
                )
            }
            .sortedBy { it.categoriaNome.lowercase() }
    }

    Text(
        text = "Comparação temporal por categoria",
        fontSize = 18.sp,
        color = chartTextColor().copy(alpha = 0.85f),
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Agora o histograma usa a polaridade média por período, evitando comparar apenas a média geral da categoria.",
        fontSize = 13.sp,
        color = chartTextColor().copy(alpha = 0.75f),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
    Spacer(modifier = Modifier.height(12.dp))

    categorias.forEach { categoria ->
        CategoryTemporalCard(
            categoria = categoria,
            nomeRestaurante1 = nomeRestaurante1,
            nomeRestaurante2 = nomeRestaurante2
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CategoryTemporalCard(
    categoria: CategoriaTemporalComparada,
    nomeRestaurante1: String,
    nomeRestaurante2: String
) {
    val periodosOrdenados = remember(categoria) {
        (categoria.dadosRest1.map { it.periodo } + categoria.dadosRest2.map { it.periodo })
            .distinct()
            .sorted()
    }
    var periodoSelecionado by remember(categoria.categoriaId) { mutableStateOf(MonthRange.LAST_6) }
    var mesesPersonalizados by remember(categoria.categoriaId) { mutableStateOf(6) }

    val valoresRest1 = remember(categoria.dadosRest1) {
        categoria.dadosRest1.associate { it.periodo to polaridadeEscalonada(it.avg_polaridade.toFloat()) }
    }
    val valoresRest2 = remember(categoria.dadosRest2) {
        categoria.dadosRest2.associate { it.periodo to polaridadeEscalonada(it.avg_polaridade.toFloat()) }
    }
    val opinioesRest1 = remember(categoria.dadosRest1) {
        categoria.dadosRest1.associate { it.periodo to it.qt_opinioes }
    }
    val opinioesRest2 = remember(categoria.dadosRest2) {
        categoria.dadosRest2.associate { it.periodo to it.qt_opinioes }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F2FF),
        border = BorderStroke(1.dp, Color(0xFFB39DDB).copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = categoria.categoriaNome,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = chartTextColor()
            )
            Spacer(Modifier.height(12.dp))

            LegendComparacao(
                itens = listOf(
                    LegendItemData(label = nomeRestaurante1, color = Serie1Color),
                    LegendItemData(label = nomeRestaurante2, color = Serie2Color)
                )
            )

            Spacer(Modifier.height(12.dp))

            MonthRangeSelector(
                periodoSelecionado = periodoSelecionado,
                mesesPersonalizados = mesesPersonalizados,
                onPeriodoSelecionado = { periodoSelecionado = it },
                onMesesPersonalizadosChange = { mesesPersonalizados = it }
            )

            TemporalCategoryBarChart(
                periodosOrdenados = filtrarMesesPorPeriodo(
                    mesesOrdenados = periodosOrdenados,
                    periodoSelecionado = periodoSelecionado,
                    mesesPersonalizados = mesesPersonalizados
                ),
                valoresRest1 = valoresRest1,
                valoresRest2 = valoresRest2,
                opinioesRest1 = opinioesRest1,
                opinioesRest2 = opinioesRest2
            )
        }
    }
}

@Composable
private fun TemporalCategoryBarChart(
    periodosOrdenados: List<String>,
    valoresRest1: Map<String, Float>,
    valoresRest2: Map<String, Float>,
    opinioesRest1: Map<String, Int>,
    opinioesRest2: Map<String, Int>,
    chartHeight: Dp = 240.dp
) {
    if (periodosOrdenados.isEmpty()) return

    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val leftPadding = 48.dp
    val rightPadding = 24.dp
    val topPadding = 24.dp
    val bottomPadding = 64.dp
    val groupWidth = 60.dp
    val groupSpacing = 18.dp
    val barSpacing = 8.dp

    val totalGroupsWidth = groupWidth * periodosOrdenados.size
    val totalSpacing = groupSpacing * (periodosOrdenados.size - 1).coerceAtLeast(0)
    val chartWidth = (leftPadding + rightPadding + totalGroupsWidth + totalSpacing)
        .coerceAtLeast(360.dp)

    val tickValues = listOf(-100f, -50f, 0f, 50f, 100f)
    val axisColor = Color(0xFFC8A8F0)
    val gridColor = Color(0xFFEADCF8)

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
            val fullHeight = bottomPx - topPx
            val zeroY = topPx + (fullHeight / 2f)

            val groupWidthPx = groupWidth.toPx()
            val groupSpacingPx = groupSpacing.toPx()
            val barSpacingPx = barSpacing.toPx()
            val barWidthPx = (groupWidthPx - barSpacingPx) / 2f

            val labelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#5A4B81")
                textSize = with(density) { 12.sp.toPx() }
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            val axisLabelPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#7A6AA6")
                textSize = with(density) { 12.sp.toPx() }
                isAntiAlias = true
            }
            val countPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#7A6AA6")
                textSize = with(density) { 10.sp.toPx() }
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            tickValues.forEach { tick ->
                val y = zeroY - (tick / 100f) * (fullHeight / 2f)
                drawLine(
                    color = if (tick == 0f) Color.Gray else gridColor,
                    start = Offset(leftPx, y),
                    end = Offset(rightPx, y),
                    strokeWidth = 1f
                )
                drawContext.canvas.nativeCanvas.drawText(
                    tick.toInt().toString(),
                    leftPx - 20.dp.toPx(),
                    y + 4.dp.toPx(),
                    axisLabelPaint
                )
            }

            drawLine(
                color = axisColor,
                start = Offset(leftPx, topPx),
                end = Offset(leftPx, bottomPx),
                strokeWidth = 2f
            )
            drawLine(
                color = axisColor,
                start = Offset(leftPx, zeroY),
                end = Offset(rightPx, zeroY),
                strokeWidth = 2f
            )

            fun barEndY(valor: Float): Float = zeroY - (valor / 100f) * (fullHeight / 2f)

            fun DrawScope.drawBar(valor: Float, centerX: Float, color: Color) {
                drawLine(
                    color = color,
                    start = Offset(centerX, zeroY),
                    end = Offset(centerX, barEndY(valor)),
                    strokeWidth = barWidthPx
                )
            }

            periodosOrdenados.forEachIndexed { index, periodo ->
                val v1 = valoresRest1[periodo] ?: 0f
                val v2 = valoresRest2[periodo] ?: 0f
                val groupStart = leftPx + index * (groupWidthPx + groupSpacingPx)
                val bar1X = groupStart + barSpacingPx / 2f
                val bar2X = bar1X + barWidthPx + barSpacingPx / 2f
                val bar1Center = bar1X + barWidthPx / 2f
                val bar2Center = bar2X + barWidthPx / 2f

                drawBar(v1, bar1Center, Serie1Color)
                drawBar(v2, bar2Center, Serie2Color)

                drawContext.canvas.nativeCanvas.drawText(
                    formatarPeriodoLabel(periodo),
                    groupStart + groupWidthPx / 2f,
                    bottomPx + 20.dp.toPx(),
                    labelPaint
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "${opinioesRest1[periodo] ?: 0}/${opinioesRest2[periodo] ?: 0} op.",
                    groupStart + groupWidthPx / 2f,
                    bottomPx + 38.dp.toPx(),
                    countPaint
                )
            }
        }
    }
}


private fun polaridadePositiva(valor: Float): Float = (valor.coerceAtLeast(0f) * 100f).coerceAtMost(100f)

private fun formatarMesLabel(anoMes: String): String {
    val partes = anoMes.split("-")
    val nomes = listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")
    val mes = partes.getOrNull(1)?.toIntOrNull()
    return if (mes != null && mes in 1..12) nomes[mes - 1] else anoMes
}

private fun formatarPeriodoLabel(periodo: String): String {
    val partes = periodo.split("-")
    val nomes = listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")
    val mes = partes.getOrNull(1)?.toIntOrNull()
    val ano = partes.getOrNull(0)?.takeLast(2)
    return if (mes != null && mes in 1..12 && ano != null) {
        "${nomes[mes - 1]}/$ano"
    } else {
        periodo
    }
}

@Composable
private fun MonthRangeSelector(
    periodoSelecionado: MonthRange,
    mesesPersonalizados: Int,
    onPeriodoSelecionado: (MonthRange) -> Unit,
    onMesesPersonalizadosChange: (Int) -> Unit
) {
    Text(
        text = "Período",
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = chartTextColor()
    )
    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MonthRange.entries.filter { it.exibirComoBotao }.forEach { periodo ->
            OutlinedButton(
                onClick = { onPeriodoSelecionado(periodo) },
                border = BorderStroke(1.dp, Color(0xFFB39DDB)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (periodoSelecionado == periodo) {
                        Color(0xFFEBDEF0)
                    } else {
                        Color.Transparent
                    }
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(periodo.label, color = chartTextColor())
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    Text(
        text = "Meses: $mesesPersonalizados",
        fontSize = 13.sp,
        color = chartTextColor().copy(alpha = 0.8f)
    )

    Slider(
        value = mesesPersonalizados.toFloat(),
        onValueChange = {
            onPeriodoSelecionado(MonthRange.CUSTOM)
            onMesesPersonalizadosChange(it.toInt().coerceIn(1, 12))
        },
        valueRange = 1f..12f,
        steps = 10,
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFFB39DDB),
            activeTrackColor = Color(0xFFB39DDB)
        )
    )

    Spacer(Modifier.height(12.dp))
}

private enum class MonthRange(
    val label: String,
    val meses: Int?,
    val exibirComoBotao: Boolean
) {
    LAST_3("3 meses", 3, true),
    LAST_6("6 meses", 6, true),
    ALL("Todos", null, true),
    CUSTOM("Personalizado", null, false);
}

private fun filtrarMesesPorPeriodo(
    mesesOrdenados: List<String>,
    periodoSelecionado: MonthRange,
    mesesPersonalizados: Int
): List<String> {
    return when (periodoSelecionado) {
        MonthRange.ALL -> mesesOrdenados
        MonthRange.CUSTOM -> mesesOrdenados.takeLast(mesesPersonalizados.coerceIn(1, 12))
        else -> mesesOrdenados.takeLast(periodoSelecionado.meses ?: mesesOrdenados.size)
    }
}

/* =================== Histograma =================== */

@Composable
fun BarChartComparativo(original1: Float, original2: Float, nome1: String, nome2: String) {
    val valor1 = normalize01(original1)
    val valor2 = normalize01(original2)
    val max = maxOf(valor1, valor2, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "$nome1: ${"%.3f".format(original1)}",
            fontSize = 12.sp,
            color = chartTextColor()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(valor1 / max)
                .height(20.dp)
                .background(Serie1Color.copy(alpha = 0.75f))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "$nome2: ${"%.3f".format(original2)}",
            fontSize = 12.sp,
            color = chartTextColor()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(valor2 / max)
                .height(20.dp)
                .background(Serie2Color.copy(alpha = 0.75f))
        )
    }
}

/* =================== Rosca por categoria =================== */

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
private fun CategoriaDonutSection(
    titulo: String,
    dados: List<ChartPolaridadeCategoriaDto>,
    colorForCategory: (String) -> Color
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = titulo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = chartTextColor()
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
                    Text("Sem dados suficientes", color = chartTextColor())
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
                            CategoriaDonutChart(
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
                            CategoriaDonutChart(
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
private fun CategoriaDonutChart(
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
            Text("Sem dados", color = chartTextColor())
        }
        return
    }

    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.22f
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val arcTopLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        var startAngle = -90f

        dados.forEach { item ->
            val valor = item.qt_opinioes
            if (valor <= 0) return@forEach

            val sweep = 360f * (valor.toFloat() / total.toFloat())
            drawArc(
                color = colorForCategory(item.categoria_nome),
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweep
        }

        drawCircle(
            color = Color(0xFFF8F2FF),
            radius = size.minDimension * 0.26f
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
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF2B2B2B) // forçar texto escuro na legenda
            )

        }
        Text(
            text = "$quantidade (${"%.2f".format(percentual * 100f)}%)",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF2B2B2B) // texto mais escuro para contraste
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

    if (dados.isEmpty()) {
        Text("Sem dados para o gráfico de radar.", color = chartTextColor())
        return
    }
    if (dados.size < 3) {
        Text("O gráfico de radar precisa de pelo menos 3 categorias.", color = chartTextColor())
        return
    }

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
            overflow = TextOverflow.Clip,
            color = chartTextColor()
        )
    }
}