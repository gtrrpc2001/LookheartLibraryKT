package com.library.KTLibrary.model

import org.apache.commons.lang3.ArrayUtils
import java.util.Arrays

class ArrDataModel(data: String) {
    // Getter
    val time: String
    val timeZone: String
    val bodyState: String
    val arrType: String
    val ecgData: Array<Double>

    init {
        val parts = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        time = parts[0]
        timeZone = parts[1]
        bodyState = parts[2]
        arrType = parts[3]
        ecgData = parts.asDoubles()
    }

    fun Array<String>.asDoubles() = this.slice( 4.. this.size).map {  s: String -> s.toDouble() }.toTypedArray()


    fun setResultPeakData(prefixEcgData: Array<Double>?): Array<Double> {
        return ArrayUtils.addAll(prefixEcgData, *ecgData)
    }
}