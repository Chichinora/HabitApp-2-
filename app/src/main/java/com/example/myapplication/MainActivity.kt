package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*

import androidx.compose.ui.tooling.preview.Preview

import com.example.myapplication.ui.theme.MyApplicationTheme

import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter

/* =========================================================
   Data Model
========================================================= */
data class HabitRecord(
    val day: String,
    val completed: Int
)

/* =========================================================
   Fake Data
========================================================= */
fun getFakeWeeklyData(): List<HabitRecord> {
    return listOf(
        HabitRecord("Mon", 2),
        HabitRecord("Tue", 4),
        HabitRecord("Wed", 3),
        HabitRecord("Thu", 5),
        HabitRecord("Fri", 1),
        HabitRecord("Sat", 4),
        HabitRecord("Sun", 3)
    )
}

/* 本月 vs 上月打卡 */
fun getMonthlyData(): Pair<Int, Int> {
    val lastMonth = listOf(18, 20, 16, 22, 19).random()
    val thisMonth = listOf(20, 25, 23, 28, 30).random()
    return Pair(lastMonth, thisMonth)
}

/* 使用趨勢（累積成長） */
fun getTrendData(): List<Int> {
    val weekly = getFakeWeeklyData()

    val result = mutableListOf<Int>()
    var sum = 0

    for (item in weekly) {
        sum += item.completed
        result.add(sum)
    }

    return result
}

/* 本週完成率（圓餅圖） */
fun getCompletionRate(): Pair<Float, Float> {
    val weekly = getFakeWeeklyData()

    val total = weekly.sumOf { it.completed }
    val max = 35f

    val done = (total / max) * 100
    val notDone = 100 - done

    return Pair(done, notDone)
}

/* =========================================================
   Main Activity
========================================================= */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold { innerPadding ->
                    StatsScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/* =========================================================
   Main UI (三頁 Tab)
   - 本週完成率
   - 本月比較
   - 使用趨勢
========================================================= */
@Composable
fun StatsScreen(modifier: Modifier = Modifier) {

    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("本週完成率", "本月打卡", "使用趨勢")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {

        /* -------------------------
           Tab 切換列
        ------------------------- */
        TabRow(selectedTabIndex = selectedTab) {

            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        /* -------------------------
           頁面切換
        ------------------------- */
        when (selectedTab) {
            0 -> WeeklyScreen()
            1 -> MonthlyScreen()
            2 -> TrendScreen()
        }
    }
}

/* =========================================================
   Page 1：本週完成率 + 週分布
========================================================= */
@Composable
fun WeeklyScreen() {

    val (done, notDone) = getCompletionRate()
    val weekly = getFakeWeeklyData()

    Card(modifier = Modifier.padding(16.dp)) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text("本週完成率", style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(8.dp))

            PieChartView(done, notDone)

            Spacer(Modifier.height(16.dp))

            Text("每日完成分布", style = MaterialTheme.typography.titleSmall)

            WeeklyBarChart(weekly)
        }
    }
}

/* =========================================================
   Pie Chart (完成率)
========================================================= */
@Composable
fun PieChartView(done: Float, notDone: Float) {

    AndroidView(
        factory = { context ->

            PieChart(context).apply {

                val entries = listOf(
                    PieEntry(done, "完成"),
                    PieEntry(notDone, "未完成")
                )

                val dataSet = PieDataSet(entries, "完成率")
                val data = PieData(dataSet)

                this.data = data
                this.invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}

/* =========================================================
   Weekly Bar Chart（每日完成）
========================================================= */
@Composable
fun WeeklyBarChart(weekly: List<HabitRecord>) {

    AndroidView(
        factory = { context ->

            BarChart(context).apply {

                val entries = weekly.mapIndexed { index, item ->
                    BarEntry((index + 1).toFloat(), item.completed.toFloat())
                }

                val dataSet = BarDataSet(entries, "週內完成")
                val data = BarData(dataSet)

                this.data = data
                /*y軸整數*/
                axisLeft.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString() // 強制變整數
                    }
                }

                axisRight.isEnabled = false
                this.invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

/* =========================================================
   Page 2：本月比較
========================================================= */
@Composable
fun MonthlyScreen() {

    val (lastMonth, thisMonth) = getMonthlyData()

    AndroidView(
        factory = { context ->

            BarChart(context).apply {

                val entries = listOf(
                    BarEntry(1f, lastMonth.toFloat()),
                    BarEntry(2f, thisMonth.toFloat())
                )

                val dataSet = BarDataSet(entries, "打卡次數")
                val data = BarData(dataSet)

                this.data = data
                this.invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}

/* =========================================================
   Page 3：使用趨勢（累積成長）
========================================================= */
@Composable
fun TrendScreen() {

    val weekly = getFakeWeeklyData()

    val cumulative = weekly.runningFold(0) { acc, item ->
        acc + item.completed
    }.drop(1)

    val entries = cumulative.mapIndexed { index, value ->
        Entry((index + 1).toFloat(), value.toFloat())
    }

    AndroidView(
        factory = { context ->

            LineChart(context).apply {

                val dataSet = LineDataSet(entries, "累積使用趨勢")
                val data = LineData(dataSet)

                this.data = data
                this.invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}

/* =========================================================
   Preview
========================================================= */
@Preview(showBackground = true)
@Composable
fun PreviewStats() {
    MyApplicationTheme {
        StatsScreen()
    }
}