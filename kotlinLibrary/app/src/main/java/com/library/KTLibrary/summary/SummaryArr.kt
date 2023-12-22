package com.library.KTLibrary.summary

import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.library.KTLibrary.R
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Arrays
import java.util.Date

class SummaryArr : Fragment() {
    private var arrChart: BarChart? = null
    private var email: String? = null

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

    /*preWeekTargetYear*/ //region
    var preWeekTargetYear: String? = null
    var preWeekTargetMonth: String? = null
    var preWeekTargetDay: String? = null
    var preWeekTargetDate: String? = null

    //endregion

    /*Week*/ //region
    var weekArrArrayData = ArrayList<Double>()
    var weekArrTimeData = ArrayList<String>()
    var weekEntries: MutableList<BarEntry> = ArrayList()

    //endregion

    /*month*/ //region
    var monthArrData = ArrayList<Double>()
    var monthArrTimeData = ArrayList<String>()
    var monthEntries: MutableList<BarEntry> = ArrayList()

    //endregion

    /*year*/ //region
    var yearArrData = ArrayList<Double>()
    var yearArrTimeData = ArrayList<String>()
    var yearEntries: MutableList<BarEntry> = ArrayList()

    //endregion

    /*count*/ //region
    var dailyArrCnt = 0
    var weekArrCnt = 0
    var monthArrCnt = 0

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

    /*check*/ //region
    var dayCheck = true
    var weekCheck: Boolean? = null
    var monthCheck: Boolean? = null
    var yearCheck: Boolean? = null

    //endregion

    /*ImageButton*/ //region
    var yesterdayButton: ImageButton? = null
    var tomorrowButton: ImageButton? = null

    //endregion

    /*Button*/ //region
    lateinit var dayButton: Button
    lateinit var weekButton: Button
    lateinit var monthButton: Button
    lateinit var yearButton: Button
    var buttons = emptyArray<Button>()

    //endregion

    /*TextView*/ //region
    var dateDisplay: TextView? = null
    var arrCnt: TextView? = null
    var arrText: TextView? = null

    //endregion


    private lateinit var view: View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_summary_arr, container, false)
        val emailSharedPreferences = activity?.getSharedPreferences("User", Context.MODE_PRIVATE)
        email = emailSharedPreferences?.getString("email", "null")

        setFindView()
        buttons = arrayOf(dayButton,weekButton,monthButton,yearButton)

        currentTimeCheck()

        todayArrChartGraph()

        dayButton!!.setOnClickListener {
            setColor(dayButton)
            todayArrChartGraph()
            dayCheck = true
            weekCheck = false
            monthCheck = false
            yearCheck = false
        }
        weekButton!!.setOnClickListener {
            setColor(weekButton)
            weekArrChartGraph()
            dayCheck = false
            weekCheck = true
            monthCheck = false
            yearCheck = false
        }
        monthButton!!.setOnClickListener {
            setColor(monthButton)
            monthArrChartGraph()
            dayCheck = false
            weekCheck = false
            monthCheck = true
            yearCheck = false
        }
        yearButton!!.setOnClickListener {
            setColor(yearButton)
            yearArrChartGraph()
            dayCheck = false
            weekCheck = false
            monthCheck = false
            yearCheck = true
        }
        tomorrowButton!!.setOnClickListener { tomorrowButtonEvent() }
        yesterdayButton!!.setOnClickListener { yesterdayButtonEvent() }
        return view
    }

    fun tomorrowButtonEvent() {
        var i = 0
        while (20 > i) {
            arrChart!!.zoomOut()
            i++
        }
        setDayButtonEvent(true)
    }

    fun yesterdayButtonEvent() {
        setDayButtonEvent(false)
    }

    fun dateCalculate(myDay: Int, check: Boolean) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var date: LocalDate
        if (check) {
            // tomorrow
            date = LocalDate.parse(targetDate, formatter)
            date = date.plusDays(myDay.toLong())
            targetDate = date.format(formatter)
            //            Log.d("targetDate", targetDate);
        } else {
            // yesterday
            date = LocalDate.parse(targetDate, formatter)
            date = date.minusDays(myDay.toLong())
            targetDate = date.format(formatter)
            //            Log.d("targetDate", targetDate);
        }
        date = LocalDate.parse(targetDate, formatter)
        targetYear = date.format(yearFormat)
        targetMonth = date.format(monthFormat)
        targetDay = date.format(dayFormat)
    }

    fun monthDateCalculate(check: Boolean) {
        val today = LocalDate.of(
            targetYear!!.toInt(),
            targetMonth!!.toInt(),
            targetDay!!.toInt()
        ) // Here you can specify the date
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val resultDate: LocalDate
        resultDate = if (check) {
            today.plusMonths(1)
        } else {
            today.minusMonths(1)
        }
        setMonthYear(resultDate)
    }

    fun yearDateCalculate(check: Boolean) {
        val today = LocalDate.of(
            targetYear!!.toInt(),
            targetMonth!!.toInt(),
            targetDay!!.toInt()
        ) // Here you can specify the date
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val resultDate: LocalDate
        resultDate = if (check) {
            today.plusYears(1)
        } else {
            today.minusYears(1)
        }
        setMonthYear(resultDate)
        targetDate = resultDate.toString()
        targetYear = resultDate.year.toString()
    }

    fun todayArrChartGraph() {
        arrChart!!.clear()
        dateDisplay!!.text = targetDate
        dailyArrCnt = 0

        // 경로
        val directory = getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

        // 파일 경로와 이름
        val file = File(directory, "CalAndDistanceData.csv")
        if (file.exists()) {
            // 파일이 있는 경우

            // arr data가 저장되는 배열 리스트
            val arrArrayData = ArrayList<Double>()
            // arr time data가 저장되는 배열 리스트
            val arrTimeData = ArrayList<String>()
            // 그래프의 x축(시간) y축(데이터)이 저장되는 Entry 리스트
            var entries: List<BarEntry> = ArrayList()
            try {
                // file read
                val br = BufferedReader(FileReader(file))
                var line: String
                while (br.readLine().also { line = it } != null) {
                    val columns = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray() // 데이터 구분
                    val arrDataRow = columns[6].toDouble() // arr data
                    val myArrTimeRow = columns[0]
                    dailyArrCnt += columns[6].toInt()

                    // 데이터 저장
                    arrTimeData.add(myArrTimeRow)
                    arrArrayData.add(arrDataRow)
                }

                // 그래프에 들어갈 데이터 저장
                entries = getChartData(arrArrayData)
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            // 그래프 Set
            setChartOption(entries, arrTimeData, true, 0f, false)
        } else {
            // 파일이 없는 경우
        }
        arrText!!.text = resources.getString(R.string.arrTimes)
        arrCnt!!.text = "" + dailyArrCnt
    }

    fun weekArrChartGraph() {
        arrChart!!.clear()
        weekArrTimeData.clear()
        weekArrArrayData.clear()
        weekEntries.clear()
        weekArrCnt = 0
        calcWeek()

        // 그래프에 들어갈 데이터 저장
        weekEntries = getChartData(weekArrArrayData)

        // 그래프 Set
        setChartOption(weekEntries, weekArrTimeData, true, 0f, false)
    }

    fun calcWeek() {

        // 화면에 보여주는 날짜 값
        val displayMonth: String?
        val displayDay: String?
        var weekArrSum = 0
        val specificDate = LocalDate.of(
            targetYear!!.toInt(),
            targetMonth!!.toInt(),
            targetDay!!.toInt()
        ) // Here you can specify the date
        val dayOfWeek = specificDate.dayOfWeek
        val weekDays = arrayOf(
            resources.getString(R.string.Monday),
            resources.getString(R.string.Tuesday),
            resources.getString(R.string.Wednesday),
            resources.getString(R.string.Thursday),
            resources.getString(R.string.Friday),
            resources.getString(R.string.Saturday),
            resources.getString(R.string.Sunday)
        )

//        String today = weekDays[dayOfWeek.getValue() - 1];
        var searchMonday = 0 // 월요일 찾기
        searchMonday = when (dayOfWeek) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> 6
        }

        // 기존 Date
        setPrevDate()
        dateCalculate(searchMonday, false)

        // 화면에 보여줄 Date
        displayMonth = targetMonth
        displayDay = targetDay

        // 월 ~ 일
        var i = 0
        while (7 > i) {

            // 경로
            weekArrCnt = 0
            val directory = getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

            // 파일 경로와 이름
            val file = File(directory, "CalAndDistanceData.csv")
            dateCalculate(1, true)
            //            Log.d("file", String.valueOf(file));
            if (file.exists()) {
                // 파일이 있는 경우
                try {
                    // file read
                    val br = BufferedReader(FileReader(file))
                    var line: String
                    while (br.readLine().also { line = it } != null) {
                        val columns = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray() // 데이터 구분
                        val arrDataRow = columns[6].toDouble() // arr data
                        val myArrTimeRow = columns[0]
                        weekArrCnt += columns[6].toInt()
                        weekArrSum += columns[6].toInt()
                    }
                    // 데이터 저장
                    weekArrArrayData.add(weekArrCnt.toDouble())
                    weekArrTimeData.add(weekDays[i])
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                // 파일이 없는 경우

                // 데이터 저장
                weekArrArrayData.add(0.0)
                weekArrTimeData.add(weekDays[i])
            }
            i++
        }
        dateDisplay!!.text = "$displayMonth.$displayDay ~ $targetMonth.$targetDay"
        arrText!!.text = resources.getString(R.string.arrTimes)
        arrCnt!!.text = "" + weekArrSum

        // 기존 날짜로 변경
        setOriginalTime()
    }

    fun monthArrChartGraph() {
        arrChart!!.clear()
        monthArrData.clear()
        monthArrTimeData.clear()
        monthEntries.clear()
        calcMonth()

        // 그래프에 들어갈 데이터 저장
        monthEntries = getChartData(monthArrData)
        setChartOption(monthEntries, monthArrTimeData, false, 15f, true)
    }

    fun calcMonth() {
        val yearMonth = YearMonth.of(targetYear!!.toInt(), targetMonth!!.toInt())
        val daysInMonth = yearMonth.lengthOfMonth()
        val monthArrSum = 0
        var timeData = 0
        val days =
            lastModifiedDirectory("LOOKHEART/$email/$targetYear/$targetMonth") // 마지막으로 수정된 파일 넘버 찾기

        // 기존 Date
        setPrevDate()

        // 1일까지 날짜 이동
        dateCalculate(days - 1, false)
        var i = 0
        while (days > i) {
            monthArrCnt = 0
            val directory = getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

            // 파일 경로와 이름
            val file = File(directory, "CalAndDistanceData.csv")
            dateCalculate(1, true)
            timeData = i + 1
            if (file.exists()) {
                // 파일이 있는 경우
                try {
                    // file read
                    setCalTimeLoop(file)

                    // 데이터 저장
                    monthArrData.add(monthArrCnt.toDouble())
                    monthArrTimeData.add(timeData.toString())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                // 파일이 없는 경우
                // 데이터 저장
                monthArrData.add(0.0)
                monthArrTimeData.add(timeData.toString())
            }
            i++
        }
        dateDisplay!!.text = "$preWeekTargetYear.$preWeekTargetMonth"
        arrText!!.text = resources.getString(R.string.arrTimes)
        arrCnt!!.text = "" + monthArrSum

        // 기존 날짜로 변경
        setOriginalTime()
    }

    fun lastModifiedDirectory(fileName: String?): Int {
        val directory = getFileDirectory(fileName)
        // 현재 디렉토리를 지정

        // 현재 디렉토리의 모든 파일과 디렉토리를 배열로 받아옴
        val files = directory.listFiles()
        if (files != null && files.size > 0) {
            Arrays.sort(files) { f1: File, f2: File ->
                java.lang.Long.compare(
                    f2.lastModified(),
                    f1.lastModified()
                )
            }

            // 디렉토리만 필터링
            for (file in files) {
                if (file.isDirectory) {
                    println("The last modified directory is: " + file.name)
                    return file.name.toInt()
                }
            }
        } else {
            println("The directory is empty or doesn't exist.")
            return 0
        }
        return 0
    }

    fun yearArrChartGraph() {
        arrChart!!.clear()
        yearArrData.clear()
        yearArrTimeData.clear()
        yearEntries.clear()
        calcYear()

        // 그래프에 들어갈 데이터 저장
        yearEntries = getChartData(yearArrData)
        setChartOption(yearEntries, yearArrTimeData, false, 15f, true)
    }

    fun calcYear() {

        // 기존 Date
        setPrevDate()
        val month = lastModifiedDirectory("LOOKHEART/$email/$targetYear")
        val yearArrSum = 0
        var timeData = 0
        targetDate = "$targetYear-01-01"
        targetMonth = "01"
        targetDay = "01"

        // 1월부터 지정 월까지 반복
        // month
        var i = 0
        while (month > i) {
            val yearMonth = YearMonth.of(targetYear!!.toInt(), targetMonth!!.toInt())
            val daysInMonth = yearMonth.lengthOfMonth()
            monthArrCnt = 0

            // day
            var j = 0
            while (daysInMonth > j) {
                val directory =
                    getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

                // 파일 경로와 이름
                val file = File(directory, "CalAndDistanceData.csv")
                dateCalculate(1, true)
                timeData = i + 1
                if (file.exists()) {
                    // 파일이 있는 경우
                    try {
                        // file read
                        setCalTimeLoop(file)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    // 파일이 없는 경우
                }
                j++
            }
            // 데이터 저장
            yearArrData.add(monthArrCnt.toDouble())
            yearArrTimeData.add(timeData.toString())
            i++
        }
        dateDisplay!!.text = targetYear
        arrText!!.text = resources.getString(R.string.arrTimes)
        arrCnt!!.text = "" + yearArrSum

        // 기존 날짜로 변경
        setOriginalTime()
    }

    @Throws(IOException::class)
    fun setCalTimeLoop(file: File?) {
        val br = BufferedReader(FileReader(file))
        var line: String
        while (br.readLine().also { line = it } != null) {
            val columns =
                line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() // 데이터 구분
            monthArrCnt += columns[6].toInt()
        }
        br.close()
    }

    fun setPrevDate() {
        preWeekTargetDate = targetDate
        preWeekTargetYear = targetYear
        preWeekTargetMonth = targetMonth
        preWeekTargetDay = targetDay
    }

    fun getChartData(arrArray: List<Double>): MutableList<BarEntry> {
        val data: MutableList<BarEntry> = ArrayList()
        for (i in arrArray.indices) {
            data.add(BarEntry(i.toFloat(), arrArray[i].toFloat()))
        }
        return data
    }

    fun setChartOption(
        data: List<BarEntry>?,
        arrTimeData: ArrayList<String>,
        fit: Boolean,
        XRangeMax: Float,
        drag: Boolean
    ) {
        val dataSet = BarDataSet(data, "I.H.R.")
        dataSet.color = Color.RED
        dataSet.setDrawValues(true)
        dataSet.valueFormatter = CustomValueFormatter()
        val ArrChartData = BarData(dataSet)
        arrChart!!.data = ArrChartData
        arrChart!!.xAxis.position = XAxis.XAxisPosition.BOTTOM
        arrChart!!.xAxis.setDrawGridLines(false)
        arrChart!!.xAxis.granularity = 1f
        arrChart!!.xAxis.valueFormatter =
            IndexAxisValueFormatter(arrTimeData) // hourlyArrTimeData는 String 배열로 준비해야 합니다.
        arrChart!!.xAxis.setLabelCount(
            arrTimeData.size,
            false
        ) // numbersOfHourlyArrData는 int형 변수여야 합니다.
        arrChart!!.axisRight.isEnabled = false
        arrChart!!.isDragEnabled = drag // 드래그 기능
        arrChart!!.setPinchZoom(false) // 줌 기능
        arrChart!!.setScaleEnabled(false) // 터치 비활성화
        arrChart!!.axisLeft.isGranularityEnabled = true
        arrChart!!.axisLeft.axisMinimum = 0f
        val legend = arrChart!!.legend
        if (XRangeMax != 0f) arrChart!!.setVisibleXRangeMaximum(XRangeMax)
        legend.textSize = 15f
        legend.typeface = Typeface.DEFAULT_BOLD
        arrChart!!.description.isEnabled = false
        arrChart!!.isDoubleTapToZoomEnabled = false
        arrChart!!.isHighlightPerTapEnabled = false
        arrChart!!.moveViewToX(0f)

        // 차트를 그릴 때 호출해야 합니다.
        if (fit) arrChart!!.fitScreen()
        arrChart!!.resetZoom()
        arrChart!!.zoomOut()
        arrChart!!.notifyDataSetChanged()
        arrChart!!.viewPortHandler.refresh(Matrix(), arrChart, true)
        arrChart!!.invalidate()
    }

    fun setMonthYear(resultDate: LocalDate) {
        targetDate = resultDate.toString()
        targetYear = resultDate.year.toString()
        targetMonth = if (resultDate.monthValue < 10) {
            "0" + resultDate.monthValue.toString()
        } else {
            resultDate.monthValue.toString()
        }
        targetDay = if (resultDate.dayOfMonth < 10) {
            "0" + resultDate.dayOfMonth.toString()
        } else {
            resultDate.dayOfMonth.toString()
        }
    }

    fun setDayButtonEvent(check: Boolean) {
        if (dayCheck) {
            dateCalculate(1, check)
            todayArrChartGraph()
        } else if (weekCheck!!) {
            dateCalculate(7, check)
            weekArrChartGraph()
        } else if (monthCheck!!) {
            monthDateCalculate(check)
            monthArrChartGraph()
        } else {
            // year
            yearDateCalculate(check)
            yearArrChartGraph()
        }
    }

    fun setOriginalTime() {
        targetYear = preWeekTargetYear
        targetMonth = preWeekTargetMonth
        targetDay = preWeekTargetDay
        targetDate = preWeekTargetDate
    }

    fun getFileDirectory(name: String?): File {
        return File(activity?.filesDir, name)
    }

    private fun setFindView() {
        arrChart = view?.findViewById(R.id.arrChart)
        dayButton = view?.findViewById(R.id.summaryArrDayButton)!!
        weekButton = view?.findViewById(R.id.summaryArrWeekButton)!!
        monthButton = view?.findViewById(R.id.summaryArrMonthButton)!!
        yearButton = view?.findViewById(R.id.summaryArrYearButton)!!
        yesterdayButton = view?.findViewById(R.id.yesterdayButton)
        tomorrowButton = view?.findViewById(R.id.tomorrowButton)
        dateDisplay = view?.findViewById(R.id.dateDisplay)
        arrCnt = view?.findViewById(R.id.summaryArrCnt)
        arrText = view?.findViewById(R.id.myArrText)
    }

    fun setColor(button: Button?) {
        // 클릭 버튼 색상 변경
        button!!.background = ContextCompat.getDrawable(requireActivity(), R.drawable.summary_button_press)
        button.setTextColor(Color.WHITE)

        // 그 외 버튼 색상 변경
        for (otherButton in buttons) {
            if (otherButton !== button) {
                otherButton!!.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.summary_button_noraml2)
                otherButton.setTextColor(ContextCompat.getColor(requireActivity(), R.color.lightGray))
            }
        }
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
        targetYear = currentYear
        targetMonth = currentMonth
        targetDay = currentDay
        targetDate = currentDate
    }

    inner class CustomValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return if (value == 0f) {
                "" // 값이 0일 때 빈 문자열 반환
            } else {
                Integer.valueOf(value.toInt()).toString() // 그렇지 않으면 기본 값을 반환
            }
        }
    }
}