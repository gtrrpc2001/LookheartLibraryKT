package com.library.KTLibrary.fragment.summary.barChart

import android.graphics.Typeface
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.library.KTLibrary.myEnum.BarChartType
import com.library.KTLibrary.fragment.summary.lineChart.LineChartController
import com.library.KTLibrary.fragment.summary.lineChart.NonZeroValueFormatter

object BarChartController {
    private const val GROUP_SPACE = 0.3f
    private const val BAR_SPACE = 0.05f
    private const val BAR_WIDTH = 0.3f

    fun setBarChart(barChart: BarChart) {
        val typeface = Typeface.create("sans-serif", Typeface.BOLD)

        barChart.apply {
            setNoDataText("")
            description.isEnabled = false

            legend.textSize = 15f
            legend.typeface = typeface

            xAxis.isEnabled = true
            xAxis.granularity = 1f
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)

            axisLeft.isGranularityEnabled = true
            axisLeft.granularity = 1f
            axisLeft.axisMinimum = 0f

            axisRight.isEnabled = false

            isDoubleTapToZoomEnabled = false
            isHighlightPerTapEnabled = false
            isDragEnabled = true

            setDrawMarkers(false)
            setPinchZoom(false)
        }
    }

    fun showBarChart(
        barChart: BarChart,
        barData: BarData,
        timeTable: List<String>,
        type: BarChartType,
    ) {
        val xRangeMaximum = if (timeTable.size <= 13) timeTable.size.toFloat() else 12.5f

        configureBarChartSettings(type, barChart, barData, timeTable.size)

        barChart.data = barData
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(timeTable)
        barChart.xAxis.setLabelCount(timeTable.size, false)
        barChart.setVisibleXRangeMaximum(xRangeMaximum)

        barChart.data.notifyDataChanged()
        barChart.notifyDataSetChanged()
        barChart.moveViewToX(timeTable.size.toFloat())
        barChart.invalidate()
    }

    private fun configureBarChartSettings(
        type: BarChartType,
        barChart: BarChart,
        barData: BarData,
        axisMaximum: Int
    ) {
        barChart.xAxis.setCenterAxisLabels(false) // 중앙 라벨 설정 비활성화
        barChart.xAxis.axisMinimum = 0f // X축 최소값 초기화

        when (type) {
            BarChartType.ARR -> {
                barData.barWidth = 0.85f
                barChart.xAxis.axisMaximum = axisMaximum.toFloat()

                barChart.xAxis.resetAxisMaximum()
                barChart.xAxis.resetAxisMinimum()
            }

            BarChartType.CALORIE, BarChartType.STEP -> {
                barData.barWidth = BAR_WIDTH
                barData.groupBars(0f, GROUP_SPACE, BAR_SPACE)

                barChart.xAxis.setCenterAxisLabels(true)
                barChart.xAxis.axisMaximum = 0f + barData.getGroupWidth(GROUP_SPACE, BAR_SPACE) * axisMaximum

            }
        }
    }

    fun getBarDataSet(
        entries: MutableMap<String, List<BarEntry>>,
        graphColors: Array<Int>
    ) : BarData {
        val barDataSetList = ArrayList<IBarDataSet>()

        entries.keys.forEachIndexed { index, label ->
            val entry = entries[label]
            val barChartDataSet = BarDataSet(entry, label)

            barChartDataSet(barChartDataSet, graphColors[index])

            barDataSetList.add(barChartDataSet)
        }

        return BarData(barDataSetList)
    }

    private fun barChartDataSet(
        barDataSet: BarDataSet,
        color: Int
    ) {
        barDataSet.color = color
        barDataSet.setDrawValues(true)
        barDataSet.valueFormatter = NonZeroValueFormatter()
    }

    fun resetZoom(barChart: BarChart, xAxisSize: Int) {
        if (xAxisSize <= 12) {
            barChart.dragDecelerationFrictionCoef = 0f
            barChart.post {
                barChart.fitScreen()
                barChart.post {
                    barChart.dragDecelerationFrictionCoef = 1f
                }
            }
        }
    }

    fun initChart(barChart: BarChart) {
        barChart.clear()
        barChart.data = null
        barChart.fitScreen()
        barChart.moveViewToX(0f)
        barChart.notifyDataSetChanged()
    }
}