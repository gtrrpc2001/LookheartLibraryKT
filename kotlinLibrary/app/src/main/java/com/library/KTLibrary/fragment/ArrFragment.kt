package com.library.KTLibrary.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.library.KTLibrary.R
import com.library.KTLibrary.controller.PeakController
import com.library.KTLibrary.model.ArrDataModel
import com.library.KTLibrary.model.GetArrDataModel
import com.library.KTLibrary.model.GetArrListModel
import com.library.KTLibrary.server.RetrofitServerManager
import com.library.KTLibrary.server.RetrofitServerManager.ServerTaskCallback
import org.apache.commons.lang3.ArrayUtils
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Arrays
import java.util.Date
import java.util.Locale
import java.util.Objects

@RequiresApi(api = Build.VERSION_CODES.O)
class ArrFragment(email: String?) : Fragment() {

    private val retrofitServerManager: RetrofitServerManager
    private var emergencyMap: MutableMap<String, String> = HashMap() // writeTime : address
    private var currentToast: Toast? = null

    /*Date*/ //region
    private var startDate: String? = null
    private var endDate: String? = null

    //endregion
    /*SimpleDateFormat*/ //region
    var date: SimpleDateFormat? = null

    //endregion
    /*image button*/ //region
    var yesterdayButton: ImageButton? = null
    var tomorrowButton: ImageButton? = null

    //endregion
    /*TextView*/ //region
    var dateDisplay: TextView? = null
    var status: TextView? = null
    var statusText: TextView? = null
    var arrStatus: TextView? = null
    var arrStatusText: TextView? = null

    //endregion
    /*arrayList*/ //region
    var numberButtonList = ArrayList<Button>()
    var writeTimeButtonList = ArrayList<Button>()

    //endregion
    /*LinearLayout*/ //region
    var arrNumberButtonsView: LinearLayout? = null
    var arrWriteTimeButtonsView: LinearLayout? = null

    //endregion
    @get:JvmName("View")
    var view: View? = null

    var arrChart: LineChart? = null
    var scrollView: ScrollView? = null
    var progressBar: ProgressBar? = null

    //endregion
    /*constructor*/ //region
    init {
        retrofitServerManager = RetrofitServerManager.getInstance()!!
        retrofitServerManager.setEmail(email!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_arr, container, false)
        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        setViewID()
        setChart()
        setOnClickListener()
        updateCurrentDate()
        todayArrList()
        return view
    }

    fun updateCurrentDate() {
        // 시간 갱신 메서드
        val mNow = System.currentTimeMillis()
        val mDate = Date(mNow)
        startDate = date!!.format(mDate)
        dateCalculate(0, true)
    }

    fun tomorrowButtonEvent() {
        dateCalculate(1, true)
        todayArrList()
    }

    fun yesterdayButtonEvent() {
        dateCalculate(1, false)
        todayArrList()
    }

    fun dateCalculate(myDay: Int, check: Boolean) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var date = LocalDate.parse(startDate, formatter)
        date = if (check) date.plusDays(myDay.toLong()) // tomorrow
        else date.minusDays(myDay.toLong()) // yesterday
        startDate = date.format(formatter)
        date = LocalDate.parse(startDate, formatter)
        date = date.plusDays(1)
        endDate = date.format(formatter)
    }

    private fun init() {
        dateDisplay!!.text = startDate
        arrChart!!.clear()
        setTextViewVisible(false)
        arrNumberButtonsView!!.removeAllViews()
        arrWriteTimeButtonsView!!.removeAllViews()
        numberButtonList = ArrayList()
        writeTimeButtonList = ArrayList()
    }

    fun todayArrList() {
        init()
        cancelToast()
        setLoadingBar(true)
        retrofitServerManager.getArrList(startDate, endDate, object :
            RetrofitServerManager.ServerTaskCallback {
            override fun onSuccess(result: String?) {
                if (!result!!.contains("result")) {
                    val gson = Gson()
                    val arrList: List<GetArrListModel> = gson.fromJson<List<GetArrListModel>>(
                        result,
                        object : TypeToken<List<GetArrListModel?>?>() {}.type
                    )
                    setupArrButtonList(arrList)
                } else {
                    // no data
                    toast(resources.getString(R.string.noArrData))
                }
                setLoadingBar(false)
            }

            override fun onFailure(e: Exception?) {
                // server Err
                toast(resources.getString(R.string.serverError))
                setLoadingBar(false)
            }
        })
    }

    fun setupArrButtonList(arrList: List<GetArrListModel>) {
        var arrNumber = 1
        for (arrTime in arrList) {
            // Button
            val numberButton = Button(activity)
            val writeTimeButton = Button(activity)

            // Button Layout Params
            numberButton.layoutParams = setupButtonParams()
            writeTimeButton.layoutParams = setupButtonParams()
            if (arrTime.address == null) {
                // Arr
                // Button Properties
                setButtonProperties(
                    numberButton,
                    writeTimeButton,
                    arrNumber.toString(),
                    arrTime.writetime!!,
                    arrNumber
                )

                // Button OnClickListener
                setButtonOnClickListener(numberButton, writeTimeButton, ARR_FLAG)
            } else {
                // Emergency
                // Button Properties
                setButtonProperties(
                    numberButton,
                    writeTimeButton,
                    "E",
                    arrTime.writetime!!,
                    arrNumber
                )

                // Button OnClickListener
                setButtonOnClickListener(numberButton, writeTimeButton, EMERGENCY_FLAG)
                emergencyMap[arrTime.writetime!!] = arrTime.address!!
            }
            numberButtonList.add(numberButton)
            writeTimeButtonList.add(writeTimeButton)
            arrNumberButtonsView!!.addView(numberButton)
            arrWriteTimeButtonsView!!.addView(writeTimeButton)
            arrNumber++
        }
    }

    private fun setupButtonParams(): LinearLayout.LayoutParams {
        // LayoutParams 설정
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,  // 버튼의 너비
            LinearLayout.LayoutParams.WRAP_CONTENT // 버튼의 높이
        )

        // 마진 설정
        val margin_in_dp = 5
        val scale = resources.displayMetrics.density
        val margin_in_px = (margin_in_dp * scale + 0.5f).toInt() // dp를 px로 변환
        params.setMargins(margin_in_px, margin_in_px, margin_in_px, margin_in_px)
        return params
    }

    private fun setButtonProperties(
        numberButton: Button,
        textButton: Button,
        numberText: String,
        buttonText: String,
        buttonID: Int
    ) {
        // 버튼 속성 설정
        numberButton.text = numberText
        numberButton.setTextColor(Color.WHITE)
        numberButton.textSize = 14f
        numberButton.setTypeface(null, Typeface.BOLD)
        numberButton.background =
            ContextCompat.getDrawable(requireActivity(), R.drawable.arr_button_normal)
        numberButton.id = buttonID
        textButton.text = buttonText
        textButton.setTextColor(Color.BLACK)
        textButton.textSize = 14f
        textButton.setTypeface(null, Typeface.BOLD)
        textButton.background =
            ContextCompat.getDrawable(requireActivity(), R.drawable.home_bottom_button)
        textButton.id = buttonID
    }

    private fun setButtonColor(button: Button, colorList: IntArray) {
        for (otherButton in numberButtonList) {
            if (otherButton.id == button.id) otherButton.background = ContextCompat.getDrawable(
                requireActivity(),
                colorList[NUMBER_BUTTON_PRESS_COLOR]
            ) else otherButton.background =
                ContextCompat.getDrawable(requireActivity(), colorList[NUMBER_BUTTON_NORMAL_COLOR])
        }
        for (otherButton in writeTimeButtonList) {
            if (otherButton.id == button.id) otherButton.background = ContextCompat.getDrawable(
                requireActivity(),
                colorList[TEXT_BUTTON_PRESS_COLOR]
            ) else otherButton.background =
                ContextCompat.getDrawable(requireActivity(), colorList[TEXT_BUTTON_NORMAL_COLOR])
        }
    }

    private fun setButtonOnClickListener(
        numberButton: Button,
        writeTimeButton: Button,
        arrFlag: Boolean
    ) {
        val colorList: IntArray
        colorList = if (arrFlag) arrButtonColorList // ARR COLOR
        else emergencyButtonColorList // EMERGENCY COLOR
        numberButton.setOnClickListener { v: View? ->
            getArrData(writeTimeButton.text.toString(), arrFlag)
            setButtonColor(numberButton, colorList)
        }
        writeTimeButton.setOnClickListener { v: View? ->
            getArrData(writeTimeButton.text.toString(), arrFlag)
            setButtonColor(writeTimeButton, colorList)
        }
    }

    private fun getArrData(writeTime: String, arrFlag: Boolean) {
        setLoadingBar(true)
        retrofitServerManager.getArrData(writeTime, object :
            RetrofitServerManager.ServerTaskCallback {
            override fun onSuccess(result: String?) {
                setTextViewVisible(true)
                val gson = Gson()
                val arrData: List<GetArrDataModel> = gson.fromJson<List<GetArrDataModel>>(
                    result,
                    object : TypeToken<List<GetArrDataModel?>?>() {}.type
                )
                if (arrFlag) setupChartData(arrData) else setupEmergencyChart(arrData, writeTime)
                setLoadingBar(false)
            }

            override fun onFailure(e: Exception?) {
                // server Err
                toast(resources.getString(R.string.serverError))
                setLoadingBar(false)
            }
        })
    }

    private fun setupChartData(arrDataList: List<GetArrDataModel>) {
        val arrData = ArrDataModel(arrDataList[0].getArr()!!)
        val peakController = PeakController()
        val resultList: MutableList<Double> = ArrayList()
        val entries: MutableList<Entry> = ArrayList()
        setStatus(arrData.bodyState, arrData.arrType)
        if (peakController.ecgToPeakDataFlag) {
            // PEAK MODE
            val doubleEcgList: MutableList<Array<Double>> = ArrayList()
            var preEcgData = arrayOf<Double>()
            val resultEcgData: Array<Double>

            // Ecg Data String to Double Array
            for (arrEcgData in arrDataList) doubleEcgList.add(arrEcgData.parseToSingleDoubleArray())

            // Sum Ecg Data Array
            for (data in doubleEcgList) preEcgData = ArrayUtils.addAll(preEcgData, *data)
            val startIndex = findStartIndex(preEcgData, arrData.ecgData)

            // Ecg to Peak 초기값 최소 5개 필요

            // Ecg to Peak 초기값 최소 5개 필요
            if (startIndex >= 5) {
                resultEcgData =  arrData.setResultPeakData(preEcgData.copyOfRange(0, startIndex))
            } else {
                resultEcgData = arrData.setResultPeakData(preEcgData)
            }

            // Double ECG -> Double PEAK
            for (ecgData in resultEcgData) resultList.add(peakController.getPeackData(ecgData.toInt()))

            // Double PEAK -> Chart Entries
            for (i in resultList.size - ARR_DATA_SIZE until resultList.size) entries.add(
                Entry(
                    i.toFloat(),
                    resultList[i].toFloat()
                )
            )
        } else {
            // ECG MODE
            resultList.addAll(Arrays.asList(*arrData.ecgData))
            for (ecgData in resultList) entries.add(Entry(0f, ecgData.toFloat()))
        }
        setSearchArrChartOption(entries)
    }

    private fun setupEmergencyChart(arrDataList: List<GetArrDataModel>, writeTime: String) {
        setStatus(
            resources.getString(R.string.emergency), Objects.requireNonNull(
                emergencyMap[writeTime]
            )!!
        )
        val nullCheck = arrDataList[0].getArr()?.length == 0
        var setArrDataString = "null, null, null, null, "
        if (!nullCheck) setArrDataString += arrDataList[0].getArr() else setArrDataString += "0.0"
        val arrDataModel = ArrDataModel(setArrDataString)
        val peakController = PeakController()
        val resultList: MutableList<Double> = ArrayList()
        val entries: MutableList<Entry> = ArrayList()
        if (peakController.ecgToPeakDataFlag) {
            // PEAK MODE
            if (!nullCheck) {
                // Peak Data 초기값 설정을 위해 같은 배열을 합침 ( 500 + 500 )
                val arrData =
                    ArrayUtils.addAll<Double>(arrDataModel.ecgData, *arrDataModel.ecgData)
                for (ecgData in arrData) resultList.add(peakController.getPeackData(ecgData.toInt()))
                for (i in resultList.size - ARR_DATA_SIZE until resultList.size) entries.add(
                    Entry(
                        i.toFloat(),
                        resultList[i].toFloat()
                    )
                )
            } else {
                // no Data
                for (i in 0..499) entries.add(Entry(i.toFloat(), 0.0f))
            }
        } else {
            // ECG MODE
            if (!nullCheck) {
                resultList.addAll(Arrays.asList(*arrDataModel.ecgData))
                for (ecgData in resultList) entries.add(Entry(0f, ecgData.toFloat()))
            } else {
                for (i in 0..499) entries.add(Entry(i.toFloat(), 500.0f))
            }
        }
        setSearchArrChartOption(entries)
    }

    private fun findStartIndex(preEcgData: Array<Double>, arrData: Array<Double>): Int {
        var arrIndex = 0
        val subArrayToFind = Arrays.copyOf(arrData, FIND_ARRAY_INDEX)
        for (i in 0..preEcgData.size - FIND_ARRAY_INDEX) {
            if (isSubArrayMatch(preEcgData[i], subArrayToFind[arrIndex])) {
                if (++arrIndex == FIND_ARRAY_INDEX) return i - FIND_ARRAY_INDEX + 1
            } else {
                arrIndex = 0
            }
        }
        return -1 // not found
    }

    private fun isSubArrayMatch(ecgArray: Double, arrArray: Double): Boolean {
        return ecgArray == arrArray
    }

    fun setSearchArrChartOption(entries: List<Entry>?) {
        // 1
        val arrChartDataSet = getArrChartDataSet(entries)
        // 2
        val arrChartData = LineData(arrChartDataSet)
        arrChart!!.data = arrChartData
        arrChart!!.xAxis.isEnabled = false
        arrChart!!.description.isEnabled = false // 차트 설명
        arrChart!!.data.notifyDataChanged()
        arrChart!!.notifyDataSetChanged()
        arrChart!!.moveViewToX(0f)
    }

    private fun setChart() {
        arrChart!!.xAxis.isEnabled = false
        arrChart!!.setNoDataText("")
        arrChart!!.legend.isEnabled = false // 라벨 제거
        arrChart!!.axisLeft.axisMaximum = 1024f
        arrChart!!.axisLeft.axisMinimum = 0f
        arrChart!!.axisRight.isEnabled = false
        arrChart!!.setDrawMarkers(false)
        arrChart!!.isDragEnabled = false
        arrChart!!.setPinchZoom(false)
        arrChart!!.isDoubleTapToZoomEnabled = false
        arrChart!!.isHighlightPerTapEnabled = false
    }

    fun getArrChartDataSet(entries: List<Entry>?): LineDataSet {
        val arrChartDataSet = LineDataSet(entries, null)
        arrChartDataSet.setDrawCircles(false)
        arrChartDataSet.color = Color.BLUE
        arrChartDataSet.mode = LineDataSet.Mode.LINEAR
        arrChartDataSet.setDrawValues(false)
        return arrChartDataSet
    }

    fun setStatus(myStatus: String, myType: String) {
        statusText!!.text = resources.getString(R.string.arrState)
        arrStatusText!!.text = resources.getString(R.string.arrType)
        status!!.text = setStatus(myStatus)
        arrStatus!!.text = setType(myType.replace("\n", ""))
    }

    private fun setStatus(status: String): String {
        return when (status) {
            "R" -> resources.getString(R.string.rest)
            "E" -> resources.getString(R.string.exercise)
            "S" -> resources.getString(R.string.sleep)
            else -> {
                setEmergencyText()
                ""
            }
        }
    }

    private fun setType(type: String): String {
        return when (type) {
            "arr" -> resources.getString(R.string.typeArr)
            "fast" -> resources.getString(R.string.typeFastArr)
            "slow" -> resources.getString(R.string.typeSlowArr)
            "irregular" -> resources.getString(R.string.typeHeavyArr)
            else -> type // EMERGENCY ADDRESS
        }
    }

    private fun setEmergencyText() {
        statusText!!.text = ""
        arrStatusText!!.text = resources.getString(R.string.emergencyType)
    }

    fun setViewID() {
        arrNumberButtonsView = view?.findViewById(R.id.arrButton)
        arrWriteTimeButtonsView = view?.findViewById(R.id.arrText)
        statusText = view?.findViewById(R.id.status)
        status = view?.findViewById(R.id.statusValue)
        arrStatusText = view?.findViewById(R.id.arrStatus)
        arrStatus = view?.findViewById(R.id.arrStatusValue)
        arrChart = view?.findViewById(R.id.fragment_arrChart)
        yesterdayButton = view?.findViewById(R.id.yesterdayButton)
        tomorrowButton = view?.findViewById(R.id.tomorrowButton)
        dateDisplay = view?.findViewById(R.id.dateDisplay)
        scrollView = view?.findViewById(R.id.scrollView)
        progressBar = view?.findViewById(R.id.progressBar)
    }

    fun setOnClickListener() {
        yesterdayButton!!.setOnClickListener { view: View? -> yesterdayButtonEvent() }
        tomorrowButton!!.setOnClickListener { view: View? -> tomorrowButtonEvent() }
    }

    private fun setTextViewVisible(flag: Boolean) {
        val visible: Int
        visible = if (flag) View.VISIBLE else View.GONE
        statusText!!.visibility = visible
        status!!.visibility = visible
        arrStatusText!!.visibility = visible
        arrStatus!!.visibility = visible
    }

    private fun setLoadingBar(flag: Boolean) {
        if (flag) progressBar!!.visibility = View.VISIBLE else progressBar!!.visibility = View.GONE
    }

    private fun toast(text: String) {
        if (activity != null) {
            if (currentToast != null) currentToast!!.cancel()
            currentToast = Toast.makeText(activity, text, Toast.LENGTH_SHORT)
            currentToast?.show()
        }
    }

    private fun cancelToast() {
        if (currentToast != null) currentToast!!.cancel()
    }

    companion object {
        /*constant*/ //region
        private const val FIND_ARRAY_INDEX = 14
        private const val ARR_DATA_SIZE = 500
        private const val NUMBER_BUTTON_PRESS_COLOR = 0
        private const val NUMBER_BUTTON_NORMAL_COLOR = 1
        private const val TEXT_BUTTON_PRESS_COLOR = 2
        private const val TEXT_BUTTON_NORMAL_COLOR = 3
        private const val ARR_FLAG = true
        private const val EMERGENCY_FLAG = false
        private val arrButtonColorList = intArrayOf(
            R.drawable.arr_botton_press,
            R.drawable.arr_button_normal,
            R.drawable.bpm_border,
            R.drawable.home_bottom_button
        )
        private val emergencyButtonColorList = intArrayOf(
            R.drawable.emergency_press,
            R.drawable.arr_button_normal,
            R.drawable.emergency_border,
            R.drawable.home_bottom_button
        )
    }
}