package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {

                val vm: HabitViewModel = viewModel(
                    factory = HabitViewModel.factory(application)
                )

                Scaffold { padding ->
                    StatsScreen(
                        viewModel = vm,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}


@Composable
fun StatsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("本週完成率", "本月打卡", "使用趨勢")

    val completions by viewModel.completions.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> WeeklyScreen(completions)
            1 -> MonthlyScreen(completions)
            2 -> TrendScreen(completions)
        }
    }
}


@Composable
fun WeeklyScreen(
    completions: List<HabitCompletionEntity>
) {

    val weeklyMap = completions
        .groupBy { it.completedOn }
        .mapValues { it.value.size }

    val done = weeklyMap.values.sum().toFloat()
    val notDone = (21 - done).coerceAtLeast(0f)

    Card(modifier = Modifier.padding(16.dp)) {
        Column(Modifier.padding(16.dp)) {

            Text("本週完成率", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(8.dp))

            PieChartView(done, notDone)

            Spacer(Modifier.height(16.dp))

            Text("每日完成分布")

            WeeklyBarChart(weeklyMap)
        }
    }
}


@Composable
fun PieChartView(done: Float, notDone: Float) {

    AndroidView(factory = { context ->
        PieChart(context).apply {

            val entries = listOf(
                PieEntry(done, "完成"),
                PieEntry(notDone, "未完成")
            )

            val dataSet = PieDataSet(entries, "完成率")
            val pieData = PieData(dataSet)

            this.setData(pieData)   // ✅ FIX
            invalidate()
        }
    }, modifier = Modifier.fillMaxWidth().height(250.dp))
}

@Composable
fun WeeklyBarChart(data: Map<String, Int>) {

    AndroidView(factory = { context ->
        BarChart(context).apply {

            val entries = data.entries.mapIndexed { index, it ->
                BarEntry(index.toFloat(), it.value.toFloat())
            }

            val dataSet = BarDataSet(entries, "週內完成")
            val barData = BarData(dataSet)

            this.setData(barData)   // ✅ FIX

            axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float) =
                    value.toInt().toString()
            }

            axisRight.isEnabled = false
            invalidate()
        }
    }, modifier = Modifier.fillMaxWidth().height(200.dp))
}


@Composable
fun MonthlyScreen(
    completions: List<HabitCompletionEntity>
) {

    val thisMonth = completions.count {
        it.completedOn.startsWith("2026-05")
    }

    val lastMonth = completions.count {
        it.completedOn.startsWith("2026-04")
    }

    AndroidView(factory = { context ->
        BarChart(context).apply {

            val entries = listOf(
                BarEntry(1f, lastMonth.toFloat()),
                BarEntry(2f, thisMonth.toFloat())
            )

            val dataSet = BarDataSet(entries, "打卡次數")

            this.setData(BarData(dataSet))   // ✅ FIX
            invalidate()
        }
    }, modifier = Modifier.fillMaxWidth().height(300.dp))
}


@Composable
fun TrendScreen(
    completions: List<HabitCompletionEntity>
) {

    val sorted = completions.sortedBy { it.completedOn }

    val cumulative = sorted
        .groupBy { it.completedOn }
        .toSortedMap()
        .values
        .runningFold(0) { acc, list -> acc + list.size }
        .drop(1)

    val entries = cumulative.mapIndexed { index, value ->
        Entry(index.toFloat(), value.toFloat())
    }

    AndroidView(factory = { context ->
        LineChart(context).apply {

            val dataSet = LineDataSet(entries, "累積使用趨勢")

            this.setData(LineData(dataSet))   // ✅ FIX
            invalidate()
        }
    }, modifier = Modifier.fillMaxWidth().height(300.dp))
}


@Composable
@Preview
fun PreviewStats() {
    MyApplicationTheme {

    }
}