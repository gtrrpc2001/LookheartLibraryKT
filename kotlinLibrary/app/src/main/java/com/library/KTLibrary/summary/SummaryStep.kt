package com.library.KTLibrary.summary

import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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

@RequiresApi(api = Build.VERSION_CODES.O)
class SummaryStep : Fragment() {
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
    /*preTargetTime*/ //region
    var preWeekTargetYear: String? = null
    var preWeekTargetMonth: String? = null
    var preWeekTargetDay: String? = null
    var preWeekTargetDate: String? = null

    //endregion
    /*Week*/ //region
    var weekStepArrayData = ArrayList<Double>()
    var weekDistanceArrayData = ArrayList<Double>()
    var weekStepEntries: MutableList<BarEntry> = ArrayList()
    var weekDistanceEntries: MutableList<BarEntry> = ArrayList()
    var weekTimeData = ArrayList<String>()
    var weektargetStep = 0
    var weektargetDistance = 0
    var weekDirCheck: Boolean? = null

    //endregion
    /*Month*/ //region
    var monthStepData = ArrayList<Double>()
    var monthDistanceData = ArrayList<Double>()
    var monthStepEntries: MutableList<BarEntry> = ArrayList()
    var monthDistanceEntries: MutableList<BarEntry> = ArrayList()
    var monthTimeData = ArrayList<String>()
    var monthtargetStep = 0
    var monthtargetDistance = 0
    var monthDirCheck: Boolean? = null

    //endregion
    /*Year*/ //region
    var yearStepData = ArrayList<Double>()
    var yearDistanceData = ArrayList<Double>()
    var yearStepEntries: MutableList<BarEntry> = ArrayList()
    var yearDistanceEntries: MutableList<BarEntry> = ArrayList()
    var yeartargetStep = 0
    var yeartargetDistance = 0
    var yearTimeData = ArrayList<String>()

    //endregion
    /*target*/ //region
    var targetStep = 0
    var targetDistance = 0
    var targetDistanceKm = 0

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
    /*button*/ //region
    var dayButton: Button? = null
    var weekButton: Button? = null
    var monthButton: Button? = null
    var yearButton: Button? = null
    lateinit var buttons: Array<Button?>

    //endregion
    /*imagebutton*/ //region
    var yesterdayButton: ImageButton? = null
    var tomorrowButton: ImageButton? = null

    //endregion
    /*textView*/ //region
    var dateDisplay: TextView? = null
    var stepValue: TextView? = null
    var distanceValue: TextView? = null
    var tvTargetStep: TextView? = null
    var tvTargetDistance: TextView? = null

    //endregion
    /*ProgressBar*/ //region
    var stepProgressBar: ProgressBar? = null
    var distanceProgressBar: ProgressBar? = null

    //endregion
    private var viewModel: SharedViewModel? = null
    private var stepChart: BarChart? = null
    var yearDirCheck: Boolean? = null
    var numbersOfStepAndDistanceData = 0
    private lateinit var view: View
    private var email: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_summary_step, container, false)
        val emailSharedPreferences = activity?.getSharedPreferences("User", Context.MODE_PRIVATE)
        email = emailSharedPreferences?.getString("email", "null")
        stepChart = view?.findViewById(R.id.stepChart)
        dayButton = view?.findViewById(R.id.summaryStepDayButton)
        weekButton = view?.findViewById(R.id.summaryStepWeekButton)
        monthButton = view?.findViewById(R.id.summaryStepMonthButton)
        yearButton = view?.findViewById(R.id.summaryStepYearButton)
        yesterdayButton = view?.findViewById(R.id.yesterdayButton)
        tomorrowButton = view?.findViewById(R.id.tomorrowButton)
        dateDisplay = view?.findViewById(R.id.dateDisplay)
        stepValue = view?.findViewById(R.id.summaryStepValue)
        distanceValue = view?.findViewById(R.id.summaryDistanceValue)
        tvTargetStep = view?.findViewById(R.id.targetStep)
        tvTargetDistance = view?.findViewById(R.id.targetDistance)
        stepProgressBar = view?.findViewById(R.id.stepProgress)
        distanceProgressBar = view?.findViewById(R.id.distanceProgress)
        buttons = arrayOf(dayButton, weekButton, monthButton, yearButton)
        setTargetStep()
        currentTimeCheck()
        todayStepChartGraph()
        dayButton?.setOnClickListener(View.OnClickListener {
            setColor(dayButton)
            todayStepChartGraph()
            dayCheck = true
            weekCheck = false
            monthCheck = false
            yearCheck = false
        })
        weekButton?.setOnClickListener(View.OnClickListener {
            setColor(weekButton)
            weekStepChartGraph()
            dayCheck = false
            weekCheck = true
            monthCheck = false
            yearCheck = false
        })
        monthButton?.setOnClickListener(View.OnClickListener {
            setColor(monthButton)
            monthStepChartGraph()
            dayCheck = false
            weekCheck = false
            monthCheck = true
            yearCheck = false
        })
        yearButton?.setOnClickListener(View.OnClickListener {
            setColor(yearButton)
            yearStepChartGraph()
            dayCheck = false
            weekCheck = false
            monthCheck = false
            yearCheck = true
        })
        tomorrowButton?.setOnClickListener(View.OnClickListener { tomorrowButtonEvent() })
        yesterdayButton?.setOnClickListener(View.OnClickListener { yesterdayButtonEvent() })
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        viewModel!!.stepText.observe(viewLifecycleOwner, object : Observer<String?> {
            override fun onChanged(value: String?) {
                targetStep = Integer.parseInt(value)
                tvTargetStep?.setText(targetStep.toString() + resources.getString(R.string.stepValue2))
            }
        })
        viewModel!!.distanceText.observe(viewLifecycleOwner, object : Observer<String?> {
            override fun onChanged(value: String?) {
                targetDistance = Integer.parseInt(value)
                tvTargetDistance?.setText(targetDistance.toString() + resources.getString(R.string.distanceValue2))
            }
        })
        return view
    }

    fun tomorrowButtonEvent() {
        var i = 0
        while (20 > i) {
            stepChart!!.zoomOut()
            i++
        }
        setDateButtonEvent(true)
    }

    fun setDateButtonEvent(check: Boolean) {
        if (dayCheck) {
            dateCalculate(1, check)
            todayStepChartGraph()
        } else if (weekCheck!!) {
            dateCalculate(7, check)
            weekStepChartGraph()
        } else if (monthCheck!!) {
            setTimeCalculate(check)
            monthStepChartGraph()
        } else {
            // year
            setTimeCalculate(check)
            yearStepChartGraph()
        }
    }

    fun yesterdayButtonEvent() {
        setDateButtonEvent(false)
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

    fun todayStepChartGraph() {
        stepChart!!.clear()
        dateDisplay!!.text = targetDate
        var sumStep = 0
        var sumDistance = 0
        var resultStep = 0
        var resultDistance = 0
        numbersOfStepAndDistanceData = 0

        // 경로
        val directory = getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

        // 파일 경로와 이름
        val file = File(directory, "CalAndDistanceData.csv")
        if (file.exists()) {
            // 파일이 있는 경우

            // arr data가 저장되는 배열 리스트
            val stepArrayData = ArrayList<Double>()
            val distanceArrayData = ArrayList<Double>()

            // 그래프의 x축(시간) y축(데이터)이 저장되는 Entry 리스트
            val stepEntries: MutableList<BarEntry> = ArrayList()
            val distanceEntries: MutableList<BarEntry> = ArrayList()

            // arr time data가 저장되는 배열 리스트
            val timeData = ArrayList<String>()
            try {
                // file read
                val br = BufferedReader(FileReader(file))
                var line: String
                while (br.readLine().also { line = it } != null) {
                    val columns = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray() // 데이터 구분
                    val step = columns[2].toDouble()
                    val distance = columns[3].toDouble()
                    sumStep = 0
                    sumDistance = 0
                    numbersOfStepAndDistanceData++
                    val myArrTimeRow = columns[0]
                    sumStep += step.toInt()
                    sumDistance += distance.toInt()
                    resultStep += step.toInt()
                    resultDistance += distance.toInt()

                    // 데이터 저장
                    timeData.add(myArrTimeRow)
                    stepArrayData.add(step)
                    distanceArrayData.add(distance)
                }

                // 그래프에 들어갈 데이터 저장
                for (i in stepArrayData.indices) {
                    stepEntries.add(BarEntry(i.toFloat(), stepArrayData[i].toFloat()))
                    distanceEntries.add(BarEntry(i.toFloat(), distanceArrayData[i].toFloat()))
                }
                br.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            // 그래프 Set
            val tCaldataSet =
                getBarDataSet(stepEntries, resources.getString(R.string.step), Color.RED)

            // 그래프 Set
            val eCaldataSet =
                getBarDataSet(distanceEntries, resources.getString(R.string.distanceM), Color.BLUE)
            setStepChartOption(tCaldataSet, eCaldataSet, timeData)
        } else {
            // 파일이 없는 경우
        }
        stepValue!!.text = resultStep.toString() + " " + resources.getString(R.string.stepValue2)
        distanceValue!!.text =
            resultDistance.toString() + " " + resources.getString(R.string.distanceM2)
        tvTargetStep!!.text = targetStep.toString() + " " + resources.getString(R.string.stepValue2)
        tvTargetDistance!!.text =
            targetDistance.toString() + " " + resources.getString(R.string.distanceValue2)
        var tCalProgress = (resultStep.toDouble() / targetStep * 100).toInt()
        var eCalProgress = (resultDistance.toDouble() / targetDistanceKm * 100).toInt()
        tCalProgress = Math.min(tCalProgress, 100)
        eCalProgress = Math.min(eCalProgress, 100)
        stepProgressBar!!.progress = tCalProgress
        distanceProgressBar!!.progress = eCalProgress
    }

    fun weekStepChartGraph() {
        numbersOfStepAndDistanceData = 0
        stepChart!!.clear()
        weekStepArrayData.clear()
        weekDistanceArrayData.clear()
        weekStepEntries.clear()
        weekDistanceEntries.clear()
        calcWeek()
        if (weekDirCheck!!) {
            // 파일 있음

            // 그래프에 들어갈 데이터 저장
            for (i in weekStepArrayData.indices) {
                weekStepEntries.add(BarEntry(i.toFloat(), weekStepArrayData[i].toFloat()))
                weekDistanceEntries.add(BarEntry(i.toFloat(), weekDistanceArrayData[i].toFloat()))
            }

            // 그래프 Set
            val tCaldataSet =
                getBarDataSet(weekStepEntries, resources.getString(R.string.step), Color.RED)

            // 그래프 Set
            val eCaldataSet = getBarDataSet(
                weekDistanceEntries,
                resources.getString(R.string.distanceM),
                Color.BLUE
            )
            setStepChartOption(tCaldataSet, eCaldataSet, weekTimeData)
        } else {
            // 파일 없음
        }
    }

    fun calcWeek() {

        // 화면에 보여주는 날짜 값
        val displayMonth: String?
        val displayDay: String?
        var sumStep = 0
        var sumDistance = 0
        var resultStep = 0
        var resultDistance = 0
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
        setPreTime()
        dateCalculate(searchMonday, false)

        // 화면에 보여줄 Date
        displayMonth = targetMonth
        displayDay = targetDay

        // 월 ~ 일
        var i = 0
        while (7 > i) {

            // 경로
            numbersOfStepAndDistanceData++
            sumStep = 0
            sumDistance = 0
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
                        val step = columns[2].toDouble()
                        val distance = columns[3].toDouble()
                        sumStep += step.toInt()
                        sumDistance += distance.toInt()
                        resultStep += step.toInt()
                        resultDistance += distance.toInt()
                    }
                    // 데이터 저장
                    weekStepArrayData.add(sumStep.toDouble())
                    weekDistanceArrayData.add(sumDistance.toDouble())
                    weekTimeData.add(weekDays[i])
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                // 파일이 없는 경우

                // 데이터 저장
                weekStepArrayData.add(0.0)
                weekDistanceArrayData.add(0.0)
                weekTimeData.add(weekDays[i])
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
        stepValue!!.text = resultStep.toString() + " " + resources.getString(R.string.stepValue2)
        distanceValue!!.text =
            resultDistance.toString() + " " + resources.getString(R.string.distanceM2)
        tvTargetStep!!.text = targetStep.toString() + " " + resources.getString(R.string.stepValue2)
        tvTargetDistance!!.text =
            targetDistance.toString() + " " + resources.getString(R.string.distanceValue2)
        var stepProgress = (resultStep.toDouble() / weektargetStep * 100).toInt()
        var distanceProgress = (resultDistance.toDouble() / weektargetDistance * 100).toInt()
        stepProgress = Math.min(stepProgress, 100)
        distanceProgress = Math.min(distanceProgress, 100)
        stepProgressBar!!.progress = stepProgress
        distanceProgressBar!!.progress = distanceProgress

        // 기존 날짜로 변경
        setTargetTime()
    }

    fun monthStepChartGraph() {
        stepChart!!.clear()
        numbersOfStepAndDistanceData = 0
        stepChart!!.clear()
        monthStepData.clear()
        monthDistanceData.clear()
        monthStepEntries.clear()
        monthDistanceEntries.clear()
        calcMonth()
        if (monthDirCheck!!) {
            // 디렉토리 있음
            // 그래프에 들어갈 데이터 저장
            for (i in monthStepData.indices) {
                monthStepEntries.add(BarEntry(i.toFloat(), monthStepData[i].toFloat()))
                monthDistanceEntries.add(BarEntry(i.toFloat(), monthDistanceData[i].toFloat()))
            }

            // 그래프 Set
            val tCaldataSet =
                getBarDataSet(monthStepEntries, resources.getString(R.string.step), Color.RED)
            val eCaldataSet = getBarDataSet(
                monthDistanceEntries,
                resources.getString(R.string.distanceM),
                Color.BLUE
            )
            setStepChartOption(tCaldataSet, eCaldataSet, monthTimeData)
        } else {
            // 디렉토리 없음
        }
    }

    fun calcMonth() {
        val yearMonth = YearMonth.of(targetYear!!.toInt(), targetMonth!!.toInt())
        val daysInMonth = yearMonth.lengthOfMonth()
        var sumStep = 0
        var sumDistance = 0
        var resultStep = 0
        var resultDistance = 0
        var timeData = 0
        val days =
            lastModifiedDirectory("LOOKHEART/$email/$targetYear/$targetMonth") // 마지막으로 수정된 파일 넘버 찾기

        // 기존 Date
        setPreTime()
        var directory = getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth")
        if (directory.exists()) {
            // 디렉토리가 있는 경우
            monthDirCheck = true
            // 1일까지 날짜 이동
            dateCalculate(days - 1, false)
            var i = 0
            while (days > i) {
                sumStep = 0
                sumDistance = 0
                directory = getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

                // 파일 경로와 이름
                val file = File(directory, "CalAndDistanceData.csv")
                dateCalculate(1, true)
                timeData = i + 1
                numbersOfStepAndDistanceData++
                if (file.exists()) {
                    // 파일이 있는 경우
                    try {
                        // file read
                        val br = BufferedReader(FileReader(file))
                        var line: String
                        while (br.readLine().also { line = it } != null) {
                            val columns = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray() // 데이터 구분
                            val step = columns[2].toDouble()
                            val distance = columns[3].toDouble()
                            sumStep += step.toInt()
                            sumDistance += distance.toInt()
                            resultStep += step.toInt()
                            resultDistance += distance.toInt()
                        }

                        // 데이터 저장
                        monthStepData.add(sumStep.toDouble())
                        monthDistanceData.add(sumDistance.toDouble())
                        monthTimeData.add(timeData.toString())
                        br.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    // 파일이 없는 경우
                    // 데이터 저장
                    monthStepData.add(0.0)
                    monthDistanceData.add(0.0)
                    monthTimeData.add(timeData.toString())
                }
                i++
            }
        } else {
            // 디렉토리가 없는 경우
            monthDirCheck = false
        }
        dateDisplay!!.text = "$preWeekTargetYear.$preWeekTargetMonth"
        stepValue!!.text = resultStep.toString() + " " + resources.getString(R.string.stepValue2)
        distanceValue!!.text =
            resultDistance.toString() + " " + resources.getString(R.string.distanceM2)
        monthtargetStep = targetStep * daysInMonth
        monthtargetDistance = targetDistanceKm * daysInMonth
        tvTargetStep!!.text = targetStep.toString() + " " + resources.getString(R.string.stepValue2)
        tvTargetDistance!!.text =
            targetDistance.toString() + " " + resources.getString(R.string.distanceValue2)
        var stepProgress = (resultStep.toDouble() / monthtargetStep * 100).toInt()
        var distanceProgress = (resultDistance.toDouble() / monthtargetDistance * 100).toInt()
        stepProgress = Math.min(stepProgress, 100)
        distanceProgress = Math.min(distanceProgress, 100)
        stepProgressBar!!.progress = stepProgress
        distanceProgressBar!!.progress = distanceProgress

        // 기존 날짜로 변경
        setTargetTime()
    }

    fun lastModifiedDirectory(name: String?): Int {
        val directory = File(activity?.filesDir, name)
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

    fun yearStepChartGraph() {
        stepChart!!.clear()
        numbersOfStepAndDistanceData = 0
        stepChart!!.clear()
        yearStepData.clear()
        yearDistanceData.clear()
        yearStepEntries.clear()
        yearDistanceEntries.clear()
        calcYear()
        if (yearDirCheck!!) {
            // 디렉토리 있음
            // 그래프에 들어갈 데이터 저장
            for (i in yearStepData.indices) {
                yearStepEntries.add(BarEntry(i.toFloat(), yearStepData[i].toFloat()))
                yearDistanceEntries.add(BarEntry(i.toFloat(), yearDistanceData[i].toFloat()))
            }

            // 그래프 Set
            val tCaldataSet =
                getBarDataSet(yearStepEntries, resources.getString(R.string.step), Color.RED)

            // 그래프 Set
            val eCaldataSet = getBarDataSet(
                yearDistanceEntries,
                resources.getString(R.string.distanceM),
                Color.BLUE
            )
            setStepChartOption(tCaldataSet, eCaldataSet, yearTimeData)
        } else {
            // 디렉토리 없음
        }
    }

    fun calcYear() {
        var sumStep = 0
        var sumDistance = 0
        var resultStep = 0
        var resultDistance = 0
        var timeData = 0

        // 기존 Date
        preWeekTargetDate = targetDate
        preWeekTargetYear = targetYear
        preWeekTargetMonth = targetMonth
        preWeekTargetDay = targetDay
        val month = lastModifiedDirectory("LOOKHEART/$email/$targetYear")
        targetDate = "$targetYear-01-01"
        targetMonth = "01"
        targetDay = "01"
        var directory = getFileDirectory("LOOKHEART/$email/$targetYear")
        if (directory.exists()) {
            // 디렉토리가 있는 경우
            yearDirCheck = true

            // 1월부터 지정 월까지 반복
            // month
            var i = 0
            while (month > i) {
                val yearMonth = YearMonth.of(targetYear!!.toInt(), targetMonth!!.toInt())
                val daysInMonth = yearMonth.lengthOfMonth()
                numbersOfStepAndDistanceData++
                sumStep = 0
                sumDistance = 0

                // day
                var j = 0
                while (daysInMonth > j) {
                    directory =
                        getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

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
                                val step = columns[2].toDouble()
                                val distance = columns[3].toDouble()
                                sumStep += step.toInt()
                                sumDistance += distance.toInt()
                                resultStep += step.toInt()
                                resultDistance += distance.toInt()
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
                yearStepData.add(sumStep.toDouble())
                yearDistanceData.add(sumDistance.toDouble())
                yearTimeData.add(timeData.toString())
                i++
            }
        } else {
            // 디렉토리가 없는 경우
            yearDirCheck = false
        }
        dateDisplay!!.text = targetYear
        stepValue!!.text = resultStep.toString() + " " + resources.getString(R.string.stepValue2)
        distanceValue!!.text =
            resultDistance.toString() + " " + resources.getString(R.string.distanceM2)
        tvTargetStep!!.text = targetStep.toString() + " " + resources.getString(R.string.stepValue2)
        tvTargetDistance!!.text =
            targetDistance.toString() + " " + resources.getString(R.string.distanceValue2)
        var stepProgress = (resultStep.toDouble() / yeartargetStep * 100).toInt()
        var distanceProgress = (resultDistance.toDouble() / yeartargetDistance * 100).toInt()
        stepProgress = Math.min(stepProgress, 100)
        distanceProgress = Math.min(distanceProgress, 100)
        stepProgressBar!!.progress = stepProgress
        distanceProgressBar!!.progress = distanceProgress

        // 기존 날짜로 변경
        setTargetTime()
    }

    fun setPreTime() {
        preWeekTargetDate = targetDate
        preWeekTargetYear = targetYear
        preWeekTargetMonth = targetMonth
        preWeekTargetDay = targetDay
    }

    fun setTargetTime() {
        targetYear = preWeekTargetYear
        targetMonth = preWeekTargetMonth
        targetDay = preWeekTargetDay
        targetDate = preWeekTargetDate
    }

    fun setStepChartOption(
        tCaldataSet: BarDataSet?,
        eCaldataSet: BarDataSet?,
        timeData: ArrayList<String>
    ) {
        val groupSpace = 0.3f
        val barSpace = 0.05f
        val barWidth = 0.3f
        val todaystepChartData = BarData(tCaldataSet, eCaldataSet)
        todaystepChartData.barWidth = barWidth
        stepChart!!.xAxis.axisMinimum = 0f
        stepChart!!.xAxis.axisMaximum =
            0f + todaystepChartData.getGroupWidth(
                groupSpace,
                barSpace
            ) * numbersOfStepAndDistanceData // group count : 2
        todaystepChartData.groupBars(0f, groupSpace, barSpace)
        val legend = stepChart!!.legend
        legend.formSize = 12f // Font size
        legend.typeface = Typeface.DEFAULT_BOLD
        stepChart!!.setNoDataText("")
        stepChart!!.data = todaystepChartData
        stepChart!!.xAxis.isEnabled = true
        stepChart!!.xAxis.setCenterAxisLabels(true)
        stepChart!!.xAxis.valueFormatter = IndexAxisValueFormatter(timeData)
        stepChart!!.xAxis.granularity = 1f
        stepChart!!.xAxis.setLabelCount(timeData.size, false)
        stepChart!!.xAxis.position = XAxis.XAxisPosition.BOTTOM
        stepChart!!.xAxis.setDrawGridLines(false)
        stepChart!!.description.isEnabled = false
        stepChart!!.axisLeft.isGranularityEnabled = true
        stepChart!!.axisLeft.granularity = 1.0f
        stepChart!!.axisLeft.axisMinimum = 0f
        stepChart!!.axisRight.isEnabled = false
        stepChart!!.setDrawMarkers(false)
        stepChart!!.isDragEnabled = true
        stepChart!!.setPinchZoom(false)
        stepChart!!.isDoubleTapToZoomEnabled = false
        stepChart!!.isHighlightPerTapEnabled = false
        stepChart!!.moveViewToX(0f)

        // 차트를 그릴 때 호출해야 합니다.
        stepChart!!.fitScreen()
        stepChart!!.resetZoom()
        stepChart!!.zoomOut()
        stepChart!!.notifyDataSetChanged()
        stepChart!!.viewPortHandler.refresh(Matrix(), stepChart, true)
        stepChart!!.invalidate()
    }

    fun getBarDataSet(data: List<BarEntry>?, label: String?, Color: Int): BarDataSet {
        val dataSet = BarDataSet(data, label)
        dataSet.color = Color
        dataSet.setDrawValues(false)
        return dataSet
    }

    fun getFileDirectory(name: String): File {
        return File(activity?.filesDir, name)
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

    fun setTargetStep() {
        // o_cal 일일 목표 소비 총 칼로리
        // o_ecal 일일 목표 소비 활동 칼로리
        val sharedPref = activity?.getSharedPreferences(email, Context.MODE_PRIVATE)
        targetStep = sharedPref?.getString("o_step", "2000")!!.toInt()
        targetDistance = sharedPref?.getString("o_distance", "5")!!.toInt()
        targetDistanceKm = targetDistance * 1000
        weektargetStep = targetStep * 7
        weektargetDistance = targetDistanceKm * 7
        yeartargetStep = targetStep * 365
        yeartargetDistance = targetDistanceKm * 365
    }
}