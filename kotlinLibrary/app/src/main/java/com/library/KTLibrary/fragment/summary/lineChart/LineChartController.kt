package com.library.KTLibrary.fragment.summary.lineChart

import android.graphics.Typeface
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.library.KTLibrary.myEnum.LineChartType

object LineChartController {
    private const val MAXIMUM = 1000f
    private const val AXIS_MAXIMUM = 200f
    private const val ECG_AXIS_MAXIMUM = 1024f

    fun setLineChart(lineChart: LineChart, legendEnable: Boolean = true) {
        val typeface = Typeface.create("sans-serif", Typeface.BOLD)

        lineChart.apply {
            setNoDataText("")
            description.isEnabled = false

            legend.textSize = 15f
            legend.typeface = typeface
            legend.isEnabled = legendEnable

            xAxis.isEnabled = true
            xAxis.granularity = 1f
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)

            axisRight.isEnabled = false

            isDoubleTapToZoomEnabled = false
            isHighlightPerDragEnabled = false
            isDragEnabled = true

            setDrawMarkers(false)
            setPinchZoom(false)
        }
    }

    fun showLineChart(
        lineChart: LineChart,
        lineData: LineData,
        timeTable: List<String>?,
        type: LineChartType
    ) {
        var axisMaximum = AXIS_MAXIMUM

        timeTable?.let {
            lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(it)
        } ?: run {
            lineChart.xAxis.isEnabled = false
            axisMaximum = ECG_AXIS_MAXIMUM
        }

        lineChart.data = lineData
        lineChart.setVisibleXRangeMaximum(MAXIMUM)
        lineChart.axisLeft.axisMaximum = axisMaximum
        lineChart.axisLeft.axisMinimum = getAxisMinimum(type)
        lineChart.data.notifyDataChanged()
        lineChart.notifyDataSetChanged()
        lineChart.moveViewToX(0f)
    }

    fun getLineDataSet(
        entries: MutableMap<String, ArrayList<Entry>>,
        graphColors: Array<Int>,
        labelEnable: Boolean = false
        ) : LineData {
        val lineDataSetList = ArrayList<ILineDataSet>()

        entries.keys.forEachIndexed { index, key ->
            val entry = entries[key]
            val label =
                if (labelEnable) null
                else key.substring(5) // remove year

            val lineChartDataSet = LineDataSet(entry, label)

            lineChartDataSet(lineChartDataSet, graphColors[index])

            lineDataSetList.add(lineChartDataSet)
        }

        return LineData(lineDataSetList)
    }

    private fun lineChartDataSet(
        lineDataSet: LineDataSet,
        color: Int,
    ) {
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawValues(true)
        lineDataSet.color = color
        lineDataSet.lineWidth = 0.7f
        lineDataSet.mode = LineDataSet.Mode.LINEAR
    }

    private fun getAxisMinimum(type: LineChartType): Float {
        return when(type) {
            LineChartType.BPM -> 40f
            LineChartType.HRV -> 0f
        }
    }

    fun resetZoom(lineChart: LineChart) {
        lineChart.dragDecelerationFrictionCoef = 0f
        lineChart.post {
            lineChart.setVisibleXRangeMaximum(MAXIMUM)
        }
    }

    fun initChart(lineChart: LineChart) {
        lineChart.clear()
        lineChart.data = null
        lineChart.fitScreen()
        lineChart.moveViewToX(0f)
        lineChart.notifyDataSetChanged()
    }
}