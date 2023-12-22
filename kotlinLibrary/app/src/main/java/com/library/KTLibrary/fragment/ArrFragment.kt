package com.library.KTLibrary.fragment

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.library.KTLibrary.R
import com.library.KTLibrary.controller.PeakController
import com.library.KTLibrary.viewmodel.SharedViewModel
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Arrays
import java.util.Date

@RequiresApi(api = Build.VERSION_CODES.O)
class ArrFragment : Fragment() {

    /*currentTime*/ //region
    var currentYear: String? = null
    var currentMonth: String? = null
    var currentDay: String? = null
    var currentDate: String? = null
    var currentTime: String? = null

    //endregion
    /*targetTime*/ //region
    var targetYear: String? = null
    var targetMonth: String? = null
    var targetDay: String? = null
    var targetDate: String? = null

    //endregion
    /*SimpleDateFormat*/ //region
    var date = SimpleDateFormat("yyyy-MM-dd")
    var time = SimpleDateFormat("HH:mm:ss")
    var year = SimpleDateFormat("yyyy")
    var month = SimpleDateFormat("MM")
    var day = SimpleDateFormat("dd")

    //endregion
    /*DateTimeFormatter*/ //region
    var yearFormat = DateTimeFormatter.ofPattern("yyyy")
    var monthFormat = DateTimeFormatter.ofPattern("MM")
    var dayFormat = DateTimeFormatter.ofPattern("dd")

    //endregion
    /*imagebutton*/ //region
    var yesterdayButton: ImageButton? = null
    var tomorrowButton: ImageButton? = null

    //endregion
    /*TextView*/ //region
    var dateDisplay: TextView? = null
    private var status: TextView? = null
    private var statusText: TextView? = null
    var arrStatus: TextView? = null
    var arrStatusText: TextView? = null

    //endregion
    /*arrayList*/ //region
    var buttonList = ArrayList<Button>()
    var textList = ArrayList<Button>()
    var arrList = ArrayList<String>()
    var arrFileNameList = ArrayList<String>()

    //endregion
    /*count*/ //region
    var arrCnt = 0
    var updateArrCnt = 0

    //endregion
    /*LinearLayout*/ //region
    var arrButton: LinearLayout? = null
    var arrText: LinearLayout? = null

    //endregion
    var viewModel: SharedViewModel? = null
    private lateinit var view: View
    var arrChart: LineChart? = null
    var scrollView: ScrollView? = null
    var startFlag = false
    private var email: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.fragment_arr!!, container, false)
        val emailSharedPreferences = activity?.getSharedPreferences("User", Context.MODE_PRIVATE)
        email = emailSharedPreferences?.getString("email", "null")
        setViewID()
        currentTimeCheck()
        targetDateCheck()
        todayArrList()
        Log.e("test",email!!)
        yesterdayButton!!.setOnClickListener { yesterdayButtonEvent() }
        tomorrowButton!!.setOnClickListener { tomorrowButtonEvent() }
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        viewModel!!.removeAllArrList() // 첫 시작 시 리스트가 있다면 제거
        viewModel!!.arrList.observe(viewLifecycleOwner, object : Observer<ArrayList<String>> {
            override fun onChanged(strings: ArrayList<String>) {
                arrList = strings
                updateArrCnt = arrList.size
                if (startFlag && updateArrCnt != 0) {
                    Log.e("arrList", arrList.toString())
                    refreshTodayArrList()

                    // 최하단 포커스
                    scrollView!!.fullScroll(ScrollView.FOCUS_DOWN)
                }
                startFlag = true
            }
        })
        return view
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
        var date = LocalDate.parse(targetDate, formatter)
        date = if (check) {
            // tomorrow
            date.plusDays(myDay.toLong())
        } else {
            // yesterday
            date.minusDays(myDay.toLong())
        }
        targetDate = date.format(formatter)
        println(targetDate)

        /*
            java.util.Date와 java.time.LocalDate는 Java의
            서로 다른 날짜/시간 API를 나타내는 클래스로, 서로 호환되지 않음
            */date = LocalDate.parse(targetDate, formatter)
        targetYear = date.format(yearFormat)
        targetMonth = date.format(monthFormat)
        targetDay = date.format(dayFormat)
    }

    fun todayArrList() {
        dateDisplay!!.text = targetDate
        // 자식 뷰 제거
        arrButton!!.removeAllViews()
        arrText!!.removeAllViews()
        // 최하단 포커스
        scrollView!!.fullScroll(ScrollView.FOCUS_DOWN)

        // 경로
        val directory =
            getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay/arrEcgData")
        if (directory.exists()) {
            var number = 1
            var fileName: String? = null

            // 디렉토리 존재
            val files = directory.listFiles()
            var i = 0
            while (files.size > i) {
                val button = Button(activity)
                val text = Button(activity)
                button.text = "" + number
                button.id = number + 1000
                text.id = number
                try {
                    val arrTime = searchArrDate(number.toString())
                    fileName = "$targetYear-$targetMonth-$targetDay $arrTime"
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // LayoutParams 설정
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,  // 버튼의 너비
                    LinearLayout.LayoutParams.WRAP_CONTENT // 버튼의 높이
                )

                // 마진 설정
                val margin_in_dp = 5 // 3dp
                val scale = resources.displayMetrics.density
                val margin_in_px = (margin_in_dp * scale + 0.5f).toInt() // dp를 px로 변환
                params.setMargins(margin_in_px, margin_in_px, margin_in_px, margin_in_px)
                button.layoutParams = params
                text.layoutParams = params


                // 컬러 설정
                setButton(button, text, fileName)
                setButtonOnClickListener(button)
                setButtonTextOnClickListener(text)
                buttonList.add(button)
                textList.add(text)
                arrButton!!.addView(button)
                arrText!!.addView(text)
                number++
                if (!startFlag) arrCnt++
                i++
            }
        } else {
            // 디렉토리 없음
        }
    }

    fun refreshTodayArrList() {

        // 경로
        val directory =
            getFileDirectory("LOOKHEART/$email/$currentYear/$currentMonth/$currentDay/arrEcgData")
        if (directory.exists()) {
            val number = 1
            var fileName: String? = null

            // 디렉토리 존재
            val files = directory.listFiles()

            // 파일 이름에서 마지막 숫자를 추출하여 정렬
            Arrays.sort(files, object : Comparator<File> {
                override fun compare(o1: File, o2: File): Int {
                    val name1 = o1.name
                    val name2 = o2.name
                    val split1 =
                        name1.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val split2 =
                        name2.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val num1 = split1[split1.size - 1].replace(".csv", "").toInt()
                    val num2 = split2[split2.size - 1].replace(".csv", "").toInt()
                    return Integer.compare(num1, num2)
                }
            })
            val button = Button(activity)
            val text = Button(activity)
            button.text = (arrCnt + 1).toString()
            button.id = arrCnt + 1 + 1000
            text.id = arrCnt + 1

            // fileName
            fileName = targetDate + " " + arrList[0]
            Log.e("fileName", fileName)
            // LayoutParams 설정
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,  // 버튼의 너비
                LinearLayout.LayoutParams.WRAP_CONTENT // 버튼의 높이
            )


            // 마진 설정
            val margin_in_dp = 5 // 3dp
            val scale = resources.displayMetrics.density
            val margin_in_px = (margin_in_dp * scale + 0.5f).toInt() // dp를 px로 변환
            params.setMargins(margin_in_px, margin_in_px, margin_in_px, margin_in_px)
            button.layoutParams = params
            text.layoutParams = params

            // 컬러 설정
            setButton(button, text, fileName)
            setButtonOnClickListener(button)
            setButtonTextOnClickListener(text)
            arrCnt++

            // 버튼 추가 시 기존 리스트 삭제
            viewModel!!.removeArrList(0)

            // 같은 시간인 경우만 버튼 추가
            if (currentDate == targetDate) {
                buttonList.add(button)
                textList.add(text)
                arrButton!!.addView(button)
                arrText!!.addView(text)
            }
        } else {
            // 디렉토리 없음
        }
    }

    fun searchArrChart(buttonNumber: String) {
        arrChart!!.clear()
        statusText!!.text = ""
        status!!.text = ""
        arrStatusText!!.text = ""
        arrStatus!!.text = ""

        // 경로
        val directory =
            getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay/arrEcgData")

        // 파일 경로와 이름
        val file = File(directory, "arrEcgData_$buttonNumber.csv")
        if (file.exists()) {
            // 파일 있음
            val arrArrayData = ArrayList<Double>()
            val entries: MutableList<Entry> = ArrayList()
            try {
                // file read
                val br = BufferedReader(FileReader(file))
                val line = br.readLine()
                var columns =
                    line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val status = columns[2]
                val arrStatus = columns[3]
                setStatus(status, arrStatus)

                //peak mode setting
                val peakCtrl = PeakController()
                run {
                    var i = 60
                    while (500 > i) {
                        columns = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray() // 데이터 구분
                        val ecg = columns[i].toDouble()

                        //peak mode 함수 설정하기
                        val peak = peakCtrl.getPeackData(ecg.toInt())
                        arrArrayData.add(peak)
                        i++
                    }
                }

                // 그래프에 들어갈 데이터 저장
                for (i in arrArrayData.indices) {
                    entries.add(Entry(i.toFloat(), arrArrayData[i].toFloat()))
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            setSearchArrChartOption(entries)
        } else {
            // 파일 없음
        }
    }

    fun setSearchArrChartOption(entries: List<Entry>?) {
        // 1
        val arrChartDataSet = getArrChartDataSet(entries)
        // 2
        val arrChartData = LineData(arrChartDataSet)
        arrChart!!.data = arrChartData
        arrChart!!.xAxis.isEnabled = false
        arrChart!!.setNoDataText("")
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
        if (arrChart!!.description != null) {
            arrChart!!.description.isEnabled = true
            arrChart!!.description.textSize =
                20f // Note: MPAndroidChart doesn't directly support setting font. Only text size and typeface can be set.
        }
        arrChart!!.description.isEnabled = false // 차트 설명
        arrChart!!.data.notifyDataChanged()
        arrChart!!.notifyDataSetChanged()
        arrChart!!.moveViewToX(0f)
    }

    fun getArrChartDataSet(entries: List<Entry>?): LineDataSet {
        val arrChartDataSet = LineDataSet(entries, null)
        arrChartDataSet.setDrawCircles(false)
        arrChartDataSet.color = Color.BLUE
        arrChartDataSet.mode = LineDataSet.Mode.LINEAR
        arrChartDataSet.setDrawValues(false)
        return arrChartDataSet
    }

    fun setButtonTextOnClickListener(text: Button) {
        text.setOnClickListener { v ->
            for (otherButton in textList) {
                otherButton.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.home_bottom_button)
            }
            val clickedButton = v as Button
            clickedButton.background = ContextCompat.getDrawable(requireActivity(), R.drawable.bpm_border)
            val button = activity?.findViewById<Button>(clickedButton.id.toString().toInt() + 1000)
            for (otherButton in buttonList) {
                otherButton.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.arr_button_normal)
            }
            button?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.arr_botton_press)
            val buttonText = button?.text.toString()
            searchArrChart(buttonText)
        }
    }

    fun setButtonOnClickListener(button: Button) {
        button.setOnClickListener { v ->
            for (otherButton in buttonList) {
                otherButton.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.arr_button_normal)
            }
            val clickedButton = v as Button
            val buttonText = clickedButton.text.toString()
            searchArrChart(buttonText)
            clickedButton.background =
                ContextCompat.getDrawable(requireActivity(), R.drawable.arr_botton_press)
            val textButton = activity?.findViewById<Button>((clickedButton.text as String).toInt())
            for (otherButton in textList) {
                otherButton.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.home_bottom_button)
            }
            textButton?.background = ContextCompat.getDrawable(requireActivity(), R.drawable.bpm_border)
        }
    }

    fun setButton(button: Button, text: Button, fileName: String?) {
        // 컬러 설정
        button.background = ContextCompat.getDrawable(requireActivity(), R.drawable.arr_button_normal)
        button.setTextColor(Color.WHITE)
        button.textSize = 14f
        button.setTypeface(null, Typeface.BOLD)
        text.background = ContextCompat.getDrawable(requireActivity(), R.drawable.home_bottom_button)
        text.text = fileName
        text.isClickable = false // 터치 비활성화
    }

    fun currentTimeCheck() {
        val mDate: Date
        val mTime: Time

        // 시간 갱신 메서드
        val mNow = System.currentTimeMillis()
        mDate = Date(mNow)
        mTime = Time(mNow)
        currentYear = year.format(mDate)
        currentMonth = month.format(mDate)
        currentDay = day.format(mDate)
        currentDate = date.format(mDate)
        currentTime = time.format(mTime)
    }

    fun targetDateCheck() {
        targetYear = currentYear
        targetMonth = currentMonth
        targetDay = currentDay
        targetDate = currentDate
    }

    fun setStatus(myStatus: String?, myArrStatus: String?) {
        statusText!!.text = resources.getString(R.string.arrState)
        arrStatusText!!.text = resources.getString(R.string.arrType)
        when (myStatus) {
            "R" -> status!!.text = resources.getString(R.string.rest)
            "E" -> status!!.text = resources.getString(R.string.exercise)
            "S" -> status!!.text = resources.getString(R.string.sleep)
            else -> status!!.text = resources.getString(R.string.rest)
        }
        when (myArrStatus) {
            "arr" -> arrStatus!!.text = resources.getString(R.string.typeArr)
            "fast" -> arrStatus!!.text = resources.getString(R.string.typeFastArr)
            "slow" -> arrStatus!!.text = resources.getString(R.string.typeSlowArr)
            "irregular" -> arrStatus!!.text = resources.getString(R.string.typeHeavyArr)
            else -> arrStatus!!.text = resources.getString(R.string.typeArr)
        }
    }

    fun searchArrDate(arrNumber: String): String? {
        var arrDate: String? = null

        // 경로
        val directory =
            getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay/arrEcgData")

        // 파일 경로와 이름
        val file = File(directory, "arrEcgData_$arrNumber.csv")
        if (file.exists()) {
            // 파일 있음
            try {
                // file read
                val br = BufferedReader(FileReader(file))
                val line = br.readLine()
                val columns =
                    line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                arrDate = columns[0]
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            // 파일 없음
        }
        return arrDate
    }

    fun getFileDirectory(name: String): File {
        return File(activity?.filesDir, name)
    }

    fun setViewID() {
        arrButton = view?.findViewById(R.id.arrButton)
        arrText = view?.findViewById(R.id.arrText)
        statusText = view?.findViewById(R.id.status)
        status = view?.findViewById(R.id.statusValue)
        arrStatusText = view?.findViewById(R.id.arrStatus)
        arrStatus = view?.findViewById(R.id.arrStatusValue)
        arrChart = view?.findViewById(R.id.fragment_arrChart)
        yesterdayButton = view?.findViewById(R.id.yesterdayButton)
        tomorrowButton = view?.findViewById(R.id.tomorrowButton)
        dateDisplay = view?.findViewById(R.id.dateDisplay)
        scrollView = view?.findViewById(R.id.scrollView)
    }
}