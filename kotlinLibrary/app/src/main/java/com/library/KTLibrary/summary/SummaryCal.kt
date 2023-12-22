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
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.library.KTLibrary.R
import com.library.KTLibrary.viewmodel.SharedViewModel
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

class SummaryCal : Fragment() {
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
    /*preWeekTargetTime*/ //region
    var preWeekTargetYear: String? = null
    var preWeekTargetMonth: String? = null
    var preWeekTargetDay: String? = null
    var preWeekTargetDate: String? = null

    //endregion
    /*Week*/ //region
    var weekTCalArrayData = ArrayList<Double>()
    var weekECalArrayData = ArrayList<Double>()
    var weekTCalEntries: MutableList<BarEntry> = ArrayList()
    var weekECalEntries: MutableList<BarEntry> = ArrayList()
    var weekCalTimeData = ArrayList<String>()
    var weekDirCheck: Boolean? = null

    //endregion
    /*Month*/ //region
    var monthTCalData = ArrayList<Double>()
    var monthECalData = ArrayList<Double>()
    var monthTCalEntries: MutableList<BarEntry> = ArrayList()
    var monthECalEntries: MutableList<BarEntry> = ArrayList()
    var monthCalTimeData = ArrayList<String>()
    var monthDirCheck: Boolean? = null

    //endregion
    /*Year*/ //region
    var yearTCalData = ArrayList<Double>()
    var yearECalData = ArrayList<Double>()
    var yearTCalEntries: MutableList<BarEntry> = ArrayList()
    var yearECalEntries: MutableList<BarEntry> = ArrayList()
    var yearCalTimeData = ArrayList<String>()

    //endregion
    /*timeCheck*/ //region
    var yearDirCheck: Boolean? = null
    var dayCheck = true
    var weekCheck: Boolean? = null
    var monthCheck: Boolean? = null
    var yearCheck: Boolean? = null

    //endregion
    /*targetCal*/ //region
    var targetTCal = 0
    var targetECal = 0
    var weektargetTCal = 0
    var weektargetECal = 0
    var monthtargetTCal = 0
    var monthtargetECal = 0
    var yeartargetTCal = 0
    var yeartargetECal = 0

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
    /*button*/ //region
    lateinit var dayButton: Button
    lateinit var weekButton: Button
    lateinit var monthButton: Button
    lateinit var yearButton: Button
    lateinit var buttons: Array<Button>

    //endregion
    /*imagebutton*/ //region
    var yesterdayButton: ImageButton? = null
    var tomorrowButton: ImageButton? = null

    //endregion
    /*textview*/ //region
    var dateDisplay: TextView? = null
    var calValue: TextView? = null
    var eCalValue: TextView? = null
    var tvTargetTCal: TextView? = null
    var tvTargetECal: TextView? = null

    //endregion
    /*ProgressBar*/ //region
    var tCalProgressBar: ProgressBar? = null
    var eCalProgressBar: ProgressBar? = null

    //endregion
    private var viewModel: SharedViewModel? = null
    private var calChart: BarChart? = null
    var numbersOfHourlyCalorieData = 0
    private lateinit var view: View
    private var email: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_summary_cal, container, false)
        val emailSharedPreferences = activity?.getSharedPreferences("User", Context.MODE_PRIVATE)
        email = emailSharedPreferences?.getString("email", "null")
        calChart = view?.findViewById(R.id.calChart)
        dayButton = view?.findViewById(R.id.summaryCalDayButton)!!
        weekButton = view?.findViewById(R.id.summaryCalWeekButton)!!
        monthButton = view?.findViewById(R.id.summaryCalMonthButton)!!
        yearButton = view?.findViewById(R.id.summaryCalYearButton)!!
        yesterdayButton = view?.findViewById(R.id.yesterdayButton)
        tomorrowButton = view?.findViewById(R.id.tomorrowButton)
        dateDisplay = view?.findViewById(R.id.dateDisplay)
        calValue = view?.findViewById(R.id.summaryCalValue)
        eCalValue = view?.findViewById(R.id.summaryECalValue)
        tvTargetTCal = view?.findViewById(R.id.targetTCal)
        tvTargetECal = view?.findViewById(R.id.targetECal)
        tCalProgressBar = view?.findViewById(R.id.calProgress)
        eCalProgressBar = view?.findViewById(R.id.eCalProgress)
        buttons = arrayOf(dayButton, weekButton, monthButton, yearButton)
        setTargetCal()
        currentTimeCheck()
        todayCalChartGraph()
        dayButton.setOnClickListener(View.OnClickListener {
            setColor(dayButton)
            todayCalChartGraph()
            dayCheck = true
            weekCheck = false
            monthCheck = false
            yearCheck = false
        })
        weekButton.setOnClickListener(View.OnClickListener {
            setColor(weekButton)
            weekCalChartGraph()
            dayCheck = false
            weekCheck = true
            monthCheck = false
            yearCheck = false
        })
        monthButton.setOnClickListener(View.OnClickListener {
            setColor(monthButton)
            monthCalChartGraph()
            dayCheck = false
            weekCheck = false
            monthCheck = true
            yearCheck = false
        })
        yearButton.setOnClickListener(View.OnClickListener {
            setColor(yearButton)
            yearCalChartGraph()
            dayCheck = false
            weekCheck = false
            monthCheck = false
            yearCheck = true
        })
        tomorrowButton?.setOnClickListener(View.OnClickListener { tomorrowButtonEvent() })
        yesterdayButton?.setOnClickListener(View.OnClickListener { yesterdayButtonEvent() })
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        viewModel!!.tCalText.observe(viewLifecycleOwner) { text: CharSequence? ->
            tvTargetTCal?.setText(
                text
            )
        }
        viewModel!!.eCalText.observe(viewLifecycleOwner) { text: CharSequence? ->
            tvTargetECal?.setText(
                text
            )
        }
        return view
    }

    fun tomorrowButtonEvent() {
        var i = 0
        while (20 > i) {
            calChart!!.zoomOut()
            i++
        }
        setDateCheck(true)
    }

    fun yesterdayButtonEvent() {
        setDateCheck(false)
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
        date = LocalDate.parse(targetDate, formatter)
        targetYear = date.format(yearFormat)
        targetMonth = date.format(monthFormat)
        targetDay = date.format(dayFormat)
    }

    fun setTimeCalculate(check: Boolean) {
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

    fun todayCalChartGraph() {
        calChart!!.clear()
        dateDisplay!!.text = targetDate
        var sumTCal = 0
        var sumECal = 0
        var resultTCal = 0
        var resultECal = 0
        numbersOfHourlyCalorieData = 0

        // 경로
        val directory = getCalDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

        // 파일 경로와 이름
        val file = File(directory, "CalAndDistanceData.csv")
        if (file.exists()) {
            // 파일이 있는 경우

            // arr data가 저장되는 배열 리스트
            val tCalArrayData = ArrayList<Double>()
            val eCalArrayData = ArrayList<Double>()
            // arr time data가 저장되는 배열 리스트
            val timeData = ArrayList<String>()
            // 그래프의 x축(시간) y축(데이터)이 저장되는 Entry 리스트
            val tCalEntries: MutableList<BarEntry> = ArrayList()
            val eCalEntries: MutableList<BarEntry> = ArrayList()
            try {
                // file read
                val br = BufferedReader(FileReader(file))
                var line: String
                while (br.readLine().also { line = it } != null) {
                    val columns = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray() // 데이터 구분
                    val tCal = columns[4].toDouble()
                    val eCal = columns[5].toDouble()
                    sumTCal = 0
                    sumECal = 0
                    numbersOfHourlyCalorieData++
                    val myArrTimeRow = columns[0]
                    sumTCal += tCal.toInt()
                    sumECal += eCal.toInt()
                    resultTCal += tCal.toInt()
                    resultECal += eCal.toInt()

                    // 데이터 저장
                    timeData.add(myArrTimeRow)
                    tCalArrayData.add(tCal)
                    eCalArrayData.add(eCal)
                }

                // 그래프에 들어갈 데이터 저장
                for (i in tCalArrayData.indices) {
                    tCalEntries.add(BarEntry(i.toFloat(), tCalArrayData[i].toFloat()))
                    eCalEntries.add(BarEntry(i.toFloat(), eCalArrayData[i].toFloat()))
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            // 그래프 Set
            val tCaldataSet =
                getBarDataSet(tCalEntries, resources.getString(R.string.summaryTCal), Color.RED)

            // 그래프 Set
            val eCaldataSet =
                getBarDataSet(eCalEntries, resources.getString(R.string.summaryECal), Color.BLUE)
            val groupSpace = 0.3f
            val barSpace = 0.05f
            val barWidth = 0.3f
            val todayCalChartData = BarData(tCaldataSet, eCaldataSet)
            todayCalChartData.barWidth = barWidth
            setBarChartOption(todayCalChartData, timeData, groupSpace, barSpace)
        } else {
            // 파일이 없는 경우
        }
        setValueText(resultTCal, resultECal)
        var tCalProgress = (resultTCal.toDouble() / targetTCal * 100).toInt()
        var eCalProgress = (resultECal.toDouble() / targetECal * 100).toInt()
        tCalProgress = Math.min(tCalProgress, 100)
        eCalProgress = Math.min(eCalProgress, 100)
        tCalProgressBar!!.progress = tCalProgress
        eCalProgressBar!!.progress = eCalProgress
    }

    fun weekCalChartGraph() {
        numbersOfHourlyCalorieData = 0
        calChart!!.clear()
        weekTCalArrayData.clear()
        weekECalArrayData.clear()
        weekTCalEntries.clear()
        weekECalEntries.clear()
        calcWeek()
        if (weekDirCheck!!) {
            // 파일 있음

            // 그래프에 들어갈 데이터 저장
            for (i in weekTCalArrayData.indices) {
                weekTCalEntries.add(BarEntry(i.toFloat(), weekTCalArrayData[i].toFloat()))
                weekECalEntries.add(BarEntry(i.toFloat(), weekECalArrayData[i].toFloat()))
            }

            // 그래프 Set
            val tCaldataSet =
                getBarDataSet(weekTCalEntries, resources.getString(R.string.summaryTCal), Color.RED)

            // 그래프 Set
            val eCaldataSet = getBarDataSet(
                weekECalEntries,
                resources.getString(R.string.summaryECal),
                Color.BLUE
            )
            val groupSpace = 0.3f
            val barSpace = 0.05f
            val barWidth = 0.3f
            val todayCalChartData = BarData(tCaldataSet, eCaldataSet)
            todayCalChartData.barWidth = barWidth
            setBarChartOption(todayCalChartData, weekCalTimeData, groupSpace, barSpace)
        } else {
            // 파일 없음
        }
    }

    fun calcWeek() {

        // 화면에 보여주는 날짜 값
        val displayMonth: String?
        val displayDay: String?
        var sumTCal = 0
        var sumECal = 0
        var resultTCal = 0
        var resultECal = 0
        var dataCheck = 0
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
            numbersOfHourlyCalorieData++
            sumTCal = 0
            sumECal = 0
            val directory = getCalDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

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
                        val tCal = columns[4].toDouble()
                        val eCal = columns[5].toDouble()
                        sumTCal += tCal.toInt()
                        sumECal += eCal.toInt()
                        resultTCal += tCal.toInt()
                        resultECal += eCal.toInt()
                    }
                    // 데이터 저장
                    weekTCalArrayData.add(sumTCal.toDouble())
                    weekECalArrayData.add(sumECal.toDouble())
                    weekCalTimeData.add(weekDays[i])
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                // 파일이 없는 경우

                // 데이터 저장
                weekTCalArrayData.add(0.0)
                weekECalArrayData.add(0.0)
                weekCalTimeData.add(weekDays[i])
                dataCheck++
            }
            i++
        }

        // 파일이 없음
        weekDirCheck = if (dataCheck == 7) {
            false
        } else {
            true
        }
        dateDisplay!!.text = "$displayMonth.$displayDay ~ $targetMonth.$targetDay"
        setValueText(resultTCal, resultECal)
        var tCalProgress = (resultTCal.toDouble() / weektargetTCal * 100).toInt()
        var eCalProgress = (resultECal.toDouble() / weektargetECal * 100).toInt()
        tCalProgress = Math.min(tCalProgress, 100)
        eCalProgress = Math.min(eCalProgress, 100)
        tCalProgressBar!!.progress = tCalProgress
        eCalProgressBar!!.progress = eCalProgress

        // 기존 날짜로 변경
        setTargetDate()
    }

    fun monthCalChartGraph() {
        calChart!!.clear()
        numbersOfHourlyCalorieData = 0
        calChart!!.clear()
        monthTCalData.clear()
        monthECalData.clear()
        monthTCalEntries.clear()
        monthECalEntries.clear()
        calcMonth()
        if (monthDirCheck!!) {
            // 디렉토리 있음
            // 그래프에 들어갈 데이터 저장
            for (i in monthTCalData.indices) {
                monthTCalEntries.add(BarEntry(i.toFloat(), monthTCalData[i].toFloat()))
                monthECalEntries.add(BarEntry(i.toFloat(), monthECalData[i].toFloat()))
            }

            // 그래프 Set
            val tCaldataSet = getBarDataSet(
                monthTCalEntries,
                resources.getString(R.string.summaryTCal),
                Color.RED
            )

            // 그래프 Set
            val eCaldataSet = getBarDataSet(
                monthECalEntries,
                resources.getString(R.string.summaryECal),
                Color.BLUE
            )
            val groupSpace = 0.3f
            val barSpace = 0.05f
            val barWidth = 0.3f
            val todayCalChartData = BarData(tCaldataSet, eCaldataSet)
            todayCalChartData.barWidth = barWidth
            setBarChartOption(todayCalChartData, monthCalTimeData, groupSpace, barSpace)
        } else {
            // 디렉토리 없음
        }
    }

    fun calcMonth() {
        val yearMonth = YearMonth.of(targetYear!!.toInt(), targetMonth!!.toInt())
        val daysInMonth = yearMonth.lengthOfMonth()
        var sumTCal = 0
        var sumECal = 0
        var resultTCal = 0
        var resultECal = 0
        var timeData = 0
        val days =
            lastModifiedDirectory("LOOKHEART/$email/$targetYear/$targetMonth") // 마지막으로 수정된 파일 넘버 찾기

        // 기존 Date
        preWeekTargetDate = targetDate
        preWeekTargetYear = targetYear
        preWeekTargetMonth = targetMonth
        preWeekTargetDay = targetDay
        var directory = getCalDirectory("LOOKHEART/$email/$targetYear/$targetMonth")
        if (directory.exists()) {
            // 디렉토리가 있는 경우
            monthDirCheck = true
            // 1일까지 날짜 이동
            dateCalculate(days - 1, false)
            var i = 0
            while (days > i) {
                sumTCal = 0
                sumECal = 0
                directory = getCalDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

                // 파일 경로와 이름
                val file = File(directory, "CalAndDistanceData.csv")
                dateCalculate(1, true)
                timeData = i + 1
                numbersOfHourlyCalorieData++
                if (file.exists()) {
                    // 파일이 있는 경우
                    try {
                        // file read
                        val br = BufferedReader(FileReader(file))
                        var line: String
                        while (br.readLine().also { line = it } != null) {
                            val columns = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray() // 데이터 구분
                            val tCal = columns[4].toDouble()
                            val eCal = columns[5].toDouble()
                            sumTCal += tCal.toInt()
                            sumECal += eCal.toInt()
                            resultTCal += tCal.toInt()
                            resultECal += eCal.toInt()
                        }
                        // 데이터 저장
                        monthTCalData.add(sumTCal.toDouble())
                        monthECalData.add(sumECal.toDouble())
                        monthCalTimeData.add(timeData.toString())
                        br.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    // 파일이 없는 경우
                    // 데이터 저장
                    monthTCalData.add(0.0)
                    monthECalData.add(0.0)
                    monthCalTimeData.add(timeData.toString())
                }
                i++
            }
        } else {
            // 디렉토리가 없는 경우
            monthDirCheck = false
        }
        dateDisplay!!.text = "$preWeekTargetYear.$preWeekTargetMonth"
        monthtargetTCal = targetTCal * daysInMonth
        monthtargetECal = targetECal * daysInMonth
        setValueText(resultTCal, resultECal)
        var tCalProgress = (resultTCal.toDouble() / monthtargetTCal * 100).toInt()
        var eCalProgress = (resultECal.toDouble() / monthtargetECal * 100).toInt()
        tCalProgress = Math.min(tCalProgress, 100)
        eCalProgress = Math.min(eCalProgress, 100)
        tCalProgressBar!!.progress = tCalProgress
        eCalProgressBar!!.progress = eCalProgress

        // 기존 날짜로 변경
        setTargetDate()
    }

    fun lastModifiedDirectory(Name: String?): Int {
        val directory = getCalDirectory(Name)
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

    fun yearCalChartGraph() {
        calChart!!.clear()
        numbersOfHourlyCalorieData = 0
        calChart!!.clear()
        yearTCalData.clear()
        yearECalData.clear()
        yearTCalEntries.clear()
        yearECalEntries.clear()
        calcYear()
        if (yearDirCheck!!) {
            // 디렉토리 있음
            // 그래프에 들어갈 데이터 저장
            for (i in yearTCalData.indices) {
                yearTCalEntries.add(BarEntry(i.toFloat(), yearTCalData[i].toFloat()))
                yearECalEntries.add(BarEntry(i.toFloat(), yearECalData[i].toFloat()))
            }

            // 그래프 Set
            val tCaldataSet =
                getBarDataSet(yearTCalEntries, resources.getString(R.string.summaryTCal), Color.RED)

            // 그래프 Set
            val eCaldataSet = getBarDataSet(
                yearECalEntries,
                resources.getString(R.string.summaryECal),
                Color.BLUE
            )
            val groupSpace = 0.3f
            val barSpace = 0.05f
            val barWidth = 0.3f
            val todayCalChartData = BarData(tCaldataSet, eCaldataSet)
            todayCalChartData.barWidth = barWidth
            setBarChartOption(todayCalChartData, yearCalTimeData, groupSpace, barSpace)
        } else {
            // 디렉토리 없음
        }
    }

    fun calcYear() {
        var sumTCal = 0
        var sumECal = 0
        var resultTCal = 0
        var resultECal = 0
        var timeData = 0

        // 기존 Date
        setPrevDate()
        val month = lastModifiedDirectory("LOOKHEART/$email/$targetYear")
        targetDate = "$targetYear-01-01"
        targetMonth = "01"
        targetDay = "01"
        var directory = getCalDirectory("LOOKHEART/$email/$targetYear")
        if (directory.exists()) {
            // 디렉토리가 있는 경우
            yearDirCheck = true

            // 1월부터 지정 월까지 반복
            // month
            var i = 0
            while (month > i) {
                val yearMonth = YearMonth.of(targetYear!!.toInt(), targetMonth!!.toInt())
                val daysInMonth = yearMonth.lengthOfMonth()
                numbersOfHourlyCalorieData++
                sumTCal = 0
                sumECal = 0

                // day
                var j = 0
                while (daysInMonth > j) {
                    directory =
                        getCalDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

                    // 파일 경로와 이름
                    val file = File(directory, "CalAndDistanceData.csv")
                    dateCalculate(1, true)
                    timeData = i + 1
                    if (file.exists()) {
                        // 파일이 있는 경우
                        try {
                            // file read
                            val br = BufferedReader(FileReader(file))
                            var line: String
                            while (br.readLine().also { line = it } != null) {
                                val columns =
                                    line.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                        .toTypedArray() // 데이터 구분
                                val tCal = columns[4].toDouble()
                                val eCal = columns[5].toDouble()
                                sumTCal += tCal.toInt()
                                sumECal += eCal.toInt()
                                resultTCal += tCal.toInt()
                                resultECal += eCal.toInt()
                            }
                            br.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        // 파일이 없는 경우
                    }
                    j++
                }
                // 데이터 저장
                yearTCalData.add(sumTCal.toDouble())
                yearECalData.add(sumECal.toDouble())
                yearCalTimeData.add(timeData.toString())
                i++
            }
        } else {
            // 디렉토리가 없는 경우
            yearDirCheck = false
        }
        dateDisplay!!.text = targetYear
        setValueText(resultTCal, resultECal)
        var tCalProgress = (resultTCal.toDouble() / yeartargetTCal * 100).toInt()
        var eCalProgress = (resultECal.toDouble() / yeartargetECal * 100).toInt()
        tCalProgress = Math.min(tCalProgress, 100)
        eCalProgress = Math.min(eCalProgress, 100)
        tCalProgressBar!!.progress = tCalProgress
        eCalProgressBar!!.progress = eCalProgress

        // 기존 날짜로 변경
        setTargetDate()
    }

    fun setTargetDate() {
        // 기존 날짜로 변경
        targetYear = preWeekTargetYear
        targetMonth = preWeekTargetMonth
        targetDay = preWeekTargetDay
        targetDate = preWeekTargetDate
    }

    fun setPrevDate() {
        // 기존 Date
        preWeekTargetDate = targetDate
        preWeekTargetYear = targetYear
        preWeekTargetMonth = targetMonth
        preWeekTargetDay = targetDay
    }

    fun setValueText(resultTCal: Int, resultECal: Int) {
        calValue!!.text = resultTCal.toString() + " " + resources.getString(R.string.eCalValue2)
        eCalValue!!.text = resultECal.toString() + " " + resources.getString(R.string.eCalValue2)
        tvTargetTCal!!.text = targetTCal.toString() + " " + resources.getString(R.string.eCalValue2)
        tvTargetECal!!.text = targetECal.toString() + " " + resources.getString(R.string.eCalValue2)
    }

    fun setBarChartOption(
        CalChartData: BarData,
        data: ArrayList<String>,
        groupSpace: Float,
        barSpace: Float
    ) {
        calChart!!.xAxis.axisMinimum = 0f
        calChart!!.xAxis.axisMaximum =
            0f + CalChartData.getGroupWidth(
                groupSpace,
                barSpace
            ) * numbersOfHourlyCalorieData // group count : 2
        CalChartData.groupBars(0f, groupSpace, barSpace)
        val legend = calChart!!.legend
        legend.formSize = 12f // Font size
        legend.typeface = Typeface.DEFAULT_BOLD
        calChart!!.setNoDataText("")
        calChart!!.data = CalChartData
        calChart!!.xAxis.isEnabled = true
        calChart!!.xAxis.setCenterAxisLabels(true)
        calChart!!.xAxis.valueFormatter = IndexAxisValueFormatter(data)
        calChart!!.xAxis.granularity = 1f
        calChart!!.xAxis.setLabelCount(data.size, false)
        calChart!!.xAxis.position = XAxis.XAxisPosition.BOTTOM
        calChart!!.xAxis.setDrawGridLines(false)
        calChart!!.description.isEnabled = false
        calChart!!.axisLeft.isGranularityEnabled = true
        calChart!!.axisLeft.granularity = 1.0f
        calChart!!.axisLeft.axisMinimum = 0f
        calChart!!.axisRight.isEnabled = false
        calChart!!.setDrawMarkers(false)
        calChart!!.isDragEnabled = true
        calChart!!.setPinchZoom(false)
        calChart!!.isDoubleTapToZoomEnabled = false
        calChart!!.isHighlightPerTapEnabled = false
        calChart!!.moveViewToX(0f)

        // 차트를 그릴 때 호출해야 합니다.
        calChart!!.fitScreen()
        calChart!!.resetZoom()
        calChart!!.zoomOut()
        calChart!!.notifyDataSetChanged()
        calChart!!.viewPortHandler.refresh(Matrix(), calChart, true)
        calChart!!.invalidate()
    }

    fun getBarDataSet(data: List<BarEntry>?, label: String?, Color: Int): BarDataSet {
        val dataSet = BarDataSet(data, label)
        dataSet.color = Color
        dataSet.setDrawValues(false)
        return dataSet
    }

    fun getCalDirectory(name: String?): File {
        return File(activity?.filesDir, name)
    }

    fun setDateCheck(check: Boolean) {
        if (dayCheck) {
            dateCalculate(1, check)
            todayCalChartGraph()
        } else if (weekCheck!!) {
            dateCalculate(7, check)
            weekCalChartGraph()
        } else if (monthCheck!!) {
            setTimeCalculate(check)
            monthCalChartGraph()
        } else {
            // year
            setTimeCalculate(check)
            yearCalChartGraph()
        }
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

    fun setTargetCal() {
        // o_cal 일일 목표 소비 총 칼로리
        // o_ecal 일일 목표 소비 활동 칼로리
        val sharedPref = activity?.getSharedPreferences(email, Context.MODE_PRIVATE)
        targetTCal = sharedPref?.getString("o_cal", "3000")!!.toInt() // 총 칼로리
        targetECal = sharedPref?.getString("o_ecal", "500")!!.toInt() // 활동 칼로리
        weektargetTCal = targetTCal * 7
        weektargetECal = targetECal * 7
        yeartargetTCal = targetTCal * 365
        yeartargetECal = targetECal * 365
    }
}