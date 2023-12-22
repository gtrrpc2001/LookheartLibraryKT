package com.library.KTLibrary.controller

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

object LineChartController {
    fun setChartOption(chart: LineChart, lineData: LineData, timeArr: ArrayList<String>) {
        chart.data = lineData
        chart.setNoDataText("") // 데이터가 없는 경우 차트에 표시되는 텍스트 설정
        chart.xAxis.isEnabled = true // x축 활성화(true)
        chart.legend.textSize = 15f // 범례 텍스트 크기 설정("BPM" size)
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(timeArr) // x축의 값 설정
        chart.setVisibleXRangeMaximum(500f) // 한 번에 보여지는 x축 최대 값
        chart.xAxis.granularity = 1f // 축의 최소 간격
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM // x축 위치
        chart.xAxis.setDrawGridLines(false) // 축의 그리드 선
        chart.description.isEnabled = false // 차트 설명
        chart.axisLeft.axisMaximum = 200f // y 축 최대값
        chart.axisLeft.axisMinimum = 40f // y 축 최소값
        chart.axisRight.isEnabled = false // 참조 반환
        chart.setDrawMarkers(false) // 값 마커
        chart.isDragEnabled = true // 드래그 기능
        chart.setPinchZoom(false) // 줌 기능
        chart.isDoubleTapToZoomEnabled = false // 더블 탭 줌 기능
        chart.isHighlightPerDragEnabled = false // 드래그 시 하이라이트
        chart.data.notifyDataChanged() // 차트에게 데이터가 변경되었음을 알림
        chart.notifyDataSetChanged() // 차트에게 데이터가 변경되었음을 알림
        chart.moveViewToX(0f) // 주어진 x값의 위치로 뷰 이동
        chart.invalidate()
    }

    fun setChartData(data: MutableList<Entry>, ArrayData: ArrayList<Double>) {
        for (i in ArrayData.indices) {
            data.add(Entry(i.toFloat(), ArrayData[i].toFloat()))
        }
    }

    fun getLineData(data: List<Entry>, label: String, color: Int): LineDataSet {
        val targetDataSet = LineDataSet(data, label)
        targetDataSet.setDrawCircles(false)
        targetDataSet.color = color
        targetDataSet.lineWidth = 0.5f
        targetDataSet.setDrawValues(true)
        return targetDataSet
    }

    fun setZoom(chart: LineChart) {
        var i = 0
        while (20 > i) {
            chart.zoomOut()
            i++
        }
    }
}