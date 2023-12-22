package com.library.KTLibrary.controller

class PeakController {
    private var xx_ecg_outdata = FloatArray(9)
    private var xx_msl_mm = 0f
    private var xx_outdata_itw = 0f
    private var xx_outdata_S_M = 0f
    private var xx_outdata_M_M = 0f
    private var xx_finaloper = 0f
    private var xx_real_finaloper: Long = 0
    private var xx_msl_pp = 0f
    private var xx_itx_1 = FloatArray(15)
    private var xx_s_max = FloatArray(15)
    private var xx_m_min = FloatArray(15)
    private var xx_itx = FloatArray(15)
    private var xx_ecgarray = FloatArray(15)

    constructor(
        xx_ecg_outdata: FloatArray,
        xx_ecgarray: FloatArray,
        xx_itx: FloatArray,
        xx_m_min: FloatArray,
        xx_itx_1: FloatArray,
        xx_s_max: FloatArray
    ) {
        this.xx_ecg_outdata = xx_ecg_outdata
        this.xx_ecgarray = xx_ecgarray
        this.xx_itx = xx_itx
        this.xx_m_min = xx_m_min
        this.xx_itx_1 = xx_itx_1
        this.xx_s_max = xx_s_max
    }

    constructor()

    fun getPeackData(ecg: Int): Double {
        xx_ecg_outdata[8] = xx_ecg_outdata[7]
        xx_ecg_outdata[7] = xx_ecg_outdata[6]
        xx_ecg_outdata[6] = xx_ecg_outdata[5]
        xx_ecg_outdata[5] = xx_ecg_outdata[4]
        xx_ecg_outdata[4] = xx_ecg_outdata[3]
        xx_ecg_outdata[3] = xx_ecg_outdata[2]
        xx_ecg_outdata[2] = xx_ecg_outdata[1]
        xx_ecg_outdata[1] = xx_ecg_outdata[0]
        xx_ecg_outdata[0] = ecg.toFloat()
        xx_msl_mm = Math.abs(xx_ecg_outdata[0] - xx_ecg_outdata[8])
        xx_msl_pp = xx_maxsmin(xx_msl_mm)
        xx_outdata_itw = xx_iten(xx_msl_pp) / 10
        xx_outdata_S_M = xx_sum_max(xx_outdata_itw)
        xx_outdata_M_M = xx_max_min(xx_outdata_S_M)
        xx_finaloper = xx_outdata_S_M - xx_outdata_M_M
        xx_real_finaloper = (xx_iten_1(xx_finaloper) / 6)
        if (xx_real_finaloper >= 1024) xx_real_finaloper = 1024
        return xx_real_finaloper.toDouble()
    }

    private fun xx_maxsmin(num: Float): Float // ecg p to p 援ы븯뒗 븿닔
    {
        for (z in 0..13) {
            xx_ecgarray[z] = xx_ecgarray[z + 1]
        }
        xx_ecgarray[14] = num
        var maxvalue = xx_ecgarray[0]
        var minvalue = xx_ecgarray[0]
        for (o in 0..14) {
            if (xx_ecgarray[o] >= maxvalue) maxvalue = xx_ecgarray[o]
            if (xx_ecgarray[o] <= minvalue) minvalue = xx_ecgarray[o]
        }
        return maxvalue - minvalue
    }

    private fun xx_iten(data: Float): Float //씠룞빀 35媛 깦뵆
    {
        var tx = 0
        var sumit = 0f
        xx_itx[14] = data
        tx = 0
        while (tx < 15) {
            sumit += xx_itx[tx]
            tx++
        }
        tx = 0
        while (tx < 14) {
            xx_itx[tx] = xx_itx[tx + 1]
            tx++
        }
        return sumit
    }

    private fun xx_sum_max(num: Float): Float // 理쒕媛 援ы븯뒗 븿닔
    {
        for (z in 0..13) {
            xx_s_max[z] = xx_s_max[z + 1]
        }
        xx_s_max[14] = num
        var maxvalue = xx_s_max[0]
        for (o in 0..14) {
            if (xx_s_max[o] >= maxvalue) maxvalue = xx_s_max[o]
        }
        return maxvalue
    }

    private fun xx_max_min(num: Float): Float // 理쒖냼媛 援ы븯뒗 븿닔
    {
        for (z in 0..13) {
            xx_m_min[z] = xx_m_min[z + 1]
        }
        xx_m_min[14] = num
        var minvalue = xx_m_min[0]
        for (o in 0..14) {
            if (xx_m_min[o] <= minvalue) minvalue = xx_m_min[o]
        }
        return minvalue
    }

    fun xx_iten_1(data: Float): Long //씠룞빀 35媛 깦뵆
    {
        var tx1 = 0
        var sumit1 = 0f
        xx_itx_1[14] = data //34
        tx1 = 0
        while (tx1 < 15) {
            sumit1 += xx_itx_1[tx1]
            tx1++
        }
        tx1 = 0
        while (tx1 < 14) {
            xx_itx_1[tx1] = xx_itx_1[tx1 + 1]
            tx1++
        }
        return sumit1.toLong()
    }
}