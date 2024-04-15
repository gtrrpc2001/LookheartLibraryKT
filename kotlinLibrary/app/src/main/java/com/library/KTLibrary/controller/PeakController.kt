package com.library.KTLibrary.controller

import kotlin.math.abs

object PeakController {

    private var ecgPeakFlag = true

    private var changeEcgData = FloatArray(2)

    init {
        changeEcgData[0] = 512.0F
    }

    fun conversionEcgData(ecg: Float): Float {
        if (ecgPeakFlag) return ecg

        changeEcgData[1] = changeEcgData[0]
        changeEcgData[0] = ecg

        var calcEcgData = changeEcgData[1] - changeEcgData[0]

        if (abs(calcEcgData) <= 50)
            calcEcgData = 0.0F


        if (changeEcgData[1] == changeEcgData[0]) {
            calcEcgData = when {
                changeEcgData[0] <= 10 -> 3.0F
                changeEcgData[0] >= 1000 -> 1000.0F
                else -> calcEcgData + 512
            }
        } else calcEcgData += 512

        return calcEcgData
    }

    fun setEcgFlag(flag: Boolean) {
        ecgPeakFlag = flag
    }

    fun getEcgFlag(): Boolean {
        return ecgPeakFlag
    }
}