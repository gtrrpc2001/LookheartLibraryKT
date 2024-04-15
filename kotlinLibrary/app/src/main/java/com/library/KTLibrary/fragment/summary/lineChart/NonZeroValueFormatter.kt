package com.library.KTLibrary.fragment.summary.lineChart

import com.github.mikephil.charting.formatter.ValueFormatter

class NonZeroValueFormatter: ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        // 값이 0이면 빈 문자열 반환
        if (value == 0f) {
            return ""
        }
        // 값이 0이 아니면 소수점 제거
        return value.toInt().toString()
    }
}