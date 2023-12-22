package com.library.KTLibrary.summary

import android.content.Context
import android.graphics.Color
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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.library.KTLibrary.R
import com.library.KTLibrary.controller.LineChartController
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.sql.Time
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Collections
import java.util.Date

class SummaryBpm : Fragment() {
    /*button*/ //region
    var buttons = emptyArray<Button>()
    lateinit var todayButton: Button
    lateinit var twoDaysButton: Button
    lateinit var threeDaysButton: Button

    //endregion
    /*TextView*/ //region
    var dateDisplay: TextView? = null
    var minBpm: TextView? = null
    var maxBpm: TextView? = null
    var avgBpm: TextView? = null
    var diffMinBpm: TextView? = null
    var diffMaxBpm: TextView? = null

    //endregion
    /*imagebutton*/ //region
    var yesterdayButton: ImageButton? = null
    var tomorrowButton: ImageButton? = null

    //endregion
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
    /*tartget date를 기준으로 -1*/ //region
    var twoDaysBpmYear: String? = null
    var twoDaysBpmMonth: String? = null
    var twoDaysBpmDay: String? = null
    var twoDaysBpmDate: String? = null

    //endregion
    /*tartget date를 기준으로 -2*/ //region
    var threeDaysBpmYear: String? = null
    var threeDaysBpmMonth: String? = null
    var threeDaysBpmDay: String? = null
    var threeDaysBpmDate: String? = null

    //endregion
    /*dayboolean*/ //region
    var today: Boolean? = null
    var twoDays: Boolean? = null
    var threeDays: Boolean? = null

    //endregion
    /*data-max_avg_min_cnt*/ //region
    var avg = 0
    var avgSum = 0
    var avgCnt = 0
    var max = 0
    var min = 70

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
    private lateinit var bpmChart: LineChart
    private lateinit var view: View
    private var email: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_summary_bpm, container, false)
        val emailSharedPreferences = requireActivity().getSharedPreferences("User", Context.MODE_PRIVATE)
        email = emailSharedPreferences.getString("email", "null")

        avgBpm = view?.findViewById(R.id.summaryBpmAvg)
        maxBpm = view?.findViewById(R.id.summaryBpmMax)
        minBpm = view?.findViewById(R.id.summaryBpmMin)
        diffMaxBpm = view?.findViewById(R.id.summaryBpmDiffMax)
        diffMinBpm = view?.findViewById(R.id.summaryBpmDiffMin)
        bpmChart = view?.findViewById(R.id.BpmChart)!!
        dateDisplay = view?.findViewById(R.id.dateDisplay)
        yesterdayButton = view?.findViewById(R.id.yesterdayButton)
        tomorrowButton = view?.findViewById(R.id.tomorrowButton)
        todayButton = view?.findViewById(R.id.summaryBpmTodayButton)!!
        twoDaysButton = view?.findViewById(R.id.summaryBpmTwoDaysButton)!!
        threeDaysButton = view?.findViewById(R.id.summaryBpmThreeDaysButton)!!
        buttons = arrayOf(todayButton, twoDaysButton, threeDaysButton)
        min = 70
        currentTimeCheck()
        todayBpmChartGraph()
        today = true
        todayButton.setOnClickListener(View.OnClickListener {
            setColor(todayButton)
            todayBpmChartGraph()
            today = true
            twoDays = false
            threeDays = false
        })
        twoDaysButton.setOnClickListener(View.OnClickListener {
            setColor(twoDaysButton)
            twoDaysBpmChartGraph()
            today = false
            twoDays = true
            threeDays = false
        })
        threeDaysButton.setOnClickListener(View.OnClickListener {
            setColor(threeDaysButton)
            threeDaysBpmChartGraph()
            today = false
            twoDays = false
            threeDays = true
        })
        yesterdayButton?.setOnClickListener(View.OnClickListener { yesterdayButtonEvent() })
        tomorrowButton?.setOnClickListener(View.OnClickListener { tomorrowButtonEvent() })
        return view
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

    fun tomorrowButtonEvent() {
        dateCalculate(1, true)
        if (today!!) {
            todayBpmChartGraph()
        } else if (twoDays!!) {
            twoDaysBpmChartGraph()
        } else {
            threeDaysBpmChartGraph()
        }
    }

    fun yesterdayButtonEvent() {
        dateCalculate(1, false)
        if (today!!) {
            todayBpmChartGraph()
        } else if (twoDays!!) {
            twoDaysBpmChartGraph()
        } else {
            threeDaysBpmChartGraph()
        }
    }

    fun calcMinMax(bpm: Int) {
        if (bpm != 0) {
            if (min > bpm) {
                min = bpm
            }
            if (max < bpm) {
                max = bpm
            }
            avgSum += bpm
            avgCnt++
            avg = avgSum / avgCnt
        }
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

        /*
            java.util.Date와 java.time.LocalDate는 Java의
            서로 다른 날짜/시간 API를 나타내는 클래스로, 서로 호환되지 않음
            */date = LocalDate.parse(targetDate, formatter)
        targetYear = date.format(yearFormat)
        targetMonth = date.format(monthFormat)
        targetDay = date.format(dayFormat)
        calcDate()
    }

    fun todayBpmChartGraph() {
        Clear(targetDate, true)

        // 경로
        val directory = getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

        // 파일 경로와 이름
        val file = File(directory, "BpmData.csv")
        if (file.exists()) {
            // 파일이 있는 경우

            // bpm data가 저장되는 배열 리스트
            val bpmArrayData = ArrayList<Double>()
            // bpm time data가 저장되는 배열 리스트
            val bpmTimeData = ArrayList<String>()
            // 그래프의 x축(시간) y축(데이터)이 저장되는 Entry 리스트
            val entries: MutableList<Entry> = ArrayList()
            try {
                // file read
                setBpmLoop(file, bpmTimeData, bpmArrayData)

                // 그래프에 들어갈 데이터 저장
                LineChartController.setChartData(entries, bpmArrayData)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            // 그래프 Set
            val dataSet = LineChartController.getLineData(entries, "BPM", Color.BLUE)
            val bpmChartData = LineData(dataSet)
            LineChartController.setChartOption(bpmChart, bpmChartData, bpmTimeData)
            bpmChart!!.legend.typeface = Typeface.DEFAULT_BOLD
        } else {
            // 파일이 없는 경우
        }

        // 줌 인 상태에서 다른 그래프 봤을 경우 대비 줌 아웃
        LineChartController.setZoom(bpmChart)
        setBpmText()
    }

    fun twoDaysBpmChartGraph() {
        Clear("$twoDaysBpmMonth-$twoDaysBpmDay ~ $targetMonth-$targetDay", true)

        // 경로
        var directory = getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

        // 파일 경로와 이름
        val targetFile = File(directory, "BpmData.csv")

        // 경로
        directory =
            getFileDirectory("LOOKHEART/$email/$twoDaysBpmYear/$twoDaysBpmMonth/$twoDaysBpmDay")

        // 파일 경로와 이름
        val twoDaysBpmFile = File(directory, "BpmData.csv")

//        Log.d("targetFile", String.valueOf(targetFile));
//        Log.d("twoDaysBpmFile", String.valueOf(twoDaysBpmFile));
        if (targetFile.exists() && twoDaysBpmFile.exists()) {
            // 파일이 있는 경우

            /*
            target : 기준일
            twoDays : 기준일 -1
             */

            // bpm data가 저장되는 배열 리스트
            val targetBpmArrayData = ArrayList<Double>()
            val twoDaysBpmArrayData = ArrayList<Double>()

            // bpm time data가 저장되는 배열 리스트
            val targetBpmTimeData = ArrayList<String>()
            val twoDaysBpmTimeData = ArrayList<String>()

            // 그래프의 x축(시간) y축(데이터)이 저장되는 Entry 리스트
            val targetEntries: MutableList<Entry> = ArrayList()
            val twoDaysEntries: MutableList<Entry> = ArrayList()

            // target(기준일) 데이터 저장
            try {
                // file read
                setBpmLoop(targetFile, targetBpmTimeData, targetBpmArrayData)
                setBpmLoop(twoDaysBpmFile, twoDaysBpmTimeData, twoDaysBpmArrayData)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            /*
            X 축 타임 테이블을 위해 시작 시간과 종료 시간을 구함
            */
            val firstIndex = targetBpmTimeData.size - 1
            val secondIndex = twoDaysBpmTimeData.size - 1
            val totalIndex = firstIndex + secondIndex
            val totalAXis = ArrayList<String>()
            var j = 0
            for (i in 0..totalIndex) {
                var Xlabel: String
                if (i <= firstIndex) {
                    Xlabel = targetBpmTimeData[i]
                } else {
                    Xlabel = twoDaysBpmTimeData[j]
                    j++
                }
                totalAXis.add(Xlabel)
            }

            //totalAXis.stream().map(d-> d.split(" ")[1]); //날짜 년월일 제외
            Collections.sort(totalAXis) //시간 정렬
            var k = 0
            var z = 0
            try {
                for (i in 0..totalIndex) {
                    val time = totalAXis[i]
                    if (k <= firstIndex) {
                        if (time === targetBpmTimeData[k]) {
                            val bpmDataEntry = Entry(i.toFloat(), targetBpmArrayData[k].toFloat())
                            targetEntries.add(bpmDataEntry)
                            k++
                        }
                    }
                    if (z <= secondIndex) {
                        if (time === twoDaysBpmTimeData[z]) {
                            val bpmDataEntry = Entry(i.toFloat(), twoDaysBpmArrayData[z].toFloat())
                            twoDaysEntries.add(bpmDataEntry)
                            z++
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //Collections.sort(totalAXis); //시간 정렬

            // totalAXis.stream().map(d-> d.split(" ")[1]); //날짜 년월일 제외
            val targetDataSet =
                LineChartController.getLineData(targetEntries, "$targetMonth-$targetDay", Color.RED)
            val twoDaysDataSet = LineChartController.getLineData(
                twoDaysEntries,
                "$twoDaysBpmMonth-$twoDaysBpmDay",
                Color.BLUE
            )

            // 그래프 Set
            val twoDaysBpmChartdataSets = ArrayList<ILineDataSet>()
            twoDaysBpmChartdataSets.add(twoDaysDataSet)
            twoDaysBpmChartdataSets.add(targetDataSet)
            val twoDaysBpmChartData = LineData(twoDaysBpmChartdataSets)
            LineChartController.setChartOption(bpmChart, twoDaysBpmChartData, totalAXis)

            // 줌 인 상태에서 다른 그래프 봤을 경우 대비 줌 아웃
            LineChartController.setZoom(bpmChart)
            setBpmText()
        } else {
            // 파일이 없는 경우
        }
    }

    fun threeDaysBpmChartGraph() {
        bpmChart!!.clear()
        dateDisplay!!.text = "$threeDaysBpmMonth-$threeDaysBpmDay ~ $targetMonth-$targetDay"

        // 경로
        var directory = getFileDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

        // 파일 경로와 이름
        val targetFile = File(directory, "BpmData.csv")

        // 경로
        directory =
            getFileDirectory("LOOKHEART/$email/$twoDaysBpmYear/$twoDaysBpmMonth/$twoDaysBpmDay")

        // 파일 경로와 이름
        val twoDaysBpmFile = File(directory, "BpmData.csv")

        // 경로
        directory =
            getFileDirectory("LOOKHEART/$email/$threeDaysBpmYear/$threeDaysBpmMonth/$threeDaysBpmDay")

        // 파일 경로와 이름
        val threeDaysBpmFile = File(directory, "BpmData.csv")
        if (targetFile.exists() && twoDaysBpmFile.exists() && threeDaysBpmFile.exists()) {
            // 파일이 있는 경우

            /*
            target : 기준일
            twoDays : 기준일 -2
             */

            // bpm data가 저장되는 배열 리스트
            val targetBpmArrayData = ArrayList<Double>()
            val twoDaysBpmArrayData = ArrayList<Double>()
            val threeDaysBpmArrayData = ArrayList<Double>()

            // bpm time data가 저장되는 배열 리스트
            val targetBpmTimeData = ArrayList<String>()
            val twoDaysBpmTimeData = ArrayList<String>()
            val threeDaysBpmTimeData = ArrayList<String>()

            // 그래프의 x축(시간) y축(데이터)이 저장되는 Entry 리스트
            val targetEntries: MutableList<Entry> = ArrayList()
            val twoDaysEntries: MutableList<Entry> = ArrayList()
            val threeDaysEntries: MutableList<Entry> = ArrayList()

            // target(기준일) 데이터 저장
            try {
                // file read
                setBpmLoop(targetFile, targetBpmTimeData, targetBpmArrayData)
                setBpmLoop(twoDaysBpmFile, twoDaysBpmTimeData, twoDaysBpmArrayData)
                setBpmLoop(threeDaysBpmFile, threeDaysBpmTimeData, threeDaysBpmArrayData)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val firstIndex = targetBpmTimeData.size
            val secondIndex = twoDaysBpmTimeData.size
            val thirdIndex = threeDaysBpmTimeData.size
            val totalIndex = firstIndex + secondIndex + thirdIndex
            val totalAXis = ArrayList<String>()
            var j = 0
            var k = 0
            for (i in 0 until totalIndex) {
                var Xlabel: String
                if (i < firstIndex) {
                    Xlabel = targetBpmTimeData[i]
                    totalAXis.add(Xlabel)
                } else if (j < secondIndex) {
                    Xlabel = twoDaysBpmTimeData[j]
                    totalAXis.add(Xlabel)
                    j++
                } else if (k < thirdIndex) {
                    Xlabel = threeDaysBpmTimeData[k]
                    totalAXis.add(Xlabel)
                    k++
                }
            }
            Collections.sort(totalAXis) { o1, o2 -> o1.compareTo(o2) } //시간 정렬
            var a = 0
            var b = 0
            var z = 0
            try {
                // file read
                for (i in 0 until totalIndex) {
                    var bpmDataEntry: Entry
                    val time = totalAXis[i]
                    if (b < secondIndex) {
                        if (twoDaysBpmTimeData.contains(time)) {
                            bpmDataEntry = Entry(i.toFloat(), twoDaysBpmArrayData[b].toFloat())
                            twoDaysEntries.add(bpmDataEntry)
                            b++
                        }
                    }
                    if (a < firstIndex) {
                        if (targetBpmTimeData.contains(time)) {
                            bpmDataEntry = Entry(i.toFloat(), targetBpmArrayData[a].toFloat())
                            targetEntries.add(bpmDataEntry)
                            a++
                        }
                    }
                    if (z < thirdIndex) {
                        if (threeDaysBpmTimeData.contains(time)) {
                            bpmDataEntry = Entry(i.toFloat(), threeDaysBpmArrayData[z].toFloat())
                            threeDaysEntries.add(bpmDataEntry)
                            z++
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 그래프 Set
            val targetDataSet =
                LineChartController.getLineData(targetEntries, "$targetMonth-$targetDay", Color.RED)

            // 그래프 Set
            val twoDaysDataSet = LineChartController.getLineData(
                twoDaysEntries,
                "$twoDaysBpmMonth-$twoDaysBpmDay",
                Color.BLUE
            )

            // 그래프 Set
            val threeDaysDataSet = LineChartController.getLineData(
                threeDaysEntries,
                "$threeDaysBpmMonth-$threeDaysBpmDay",
                Color.parseColor("#138A1E")
            )
            val threeDaysBpmChartdataSets = ArrayList<ILineDataSet>()
            threeDaysBpmChartdataSets.add(threeDaysDataSet)
            threeDaysBpmChartdataSets.add(twoDaysDataSet)
            threeDaysBpmChartdataSets.add(targetDataSet)
            val BpmChartData = LineData(threeDaysBpmChartdataSets)
            LineChartController.setChartOption(bpmChart, BpmChartData, totalAXis)

            // 줌 인 상태에서 다른 그래프 봤을 경우 대비 줌 아웃
            LineChartController.setZoom(bpmChart)
            setBpmText()
        } else {
            // 파일이 없는 경우
        }
    }

    @Throws(IOException::class)
    fun setBpmLoop(file: File?, bpmTimeData: ArrayList<String>, bpmArrayData: ArrayList<Double>) {
        val br = BufferedReader(FileReader(file))
        var line: String
        while (br.readLine().also { line = it } != null) {
            val columns =
                line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() // 데이터 구분
            val bpmDataRow = columns[2].toDouble() // bpm data
            val bpm = columns[2].toInt() // minMaxAvg 찾는 변수
            val bpmTimeCheck = columns[0].split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray() // 시간 구분
            val myBpmTimeRow = bpmTimeCheck[0] + ":" + bpmTimeCheck[1] + ":" + bpmTimeCheck[2]
            calcMinMax(bpm)
            // 데이터 저장
            bpmTimeData.add(myBpmTimeRow)
            bpmArrayData.add(bpmDataRow)
        }
        br.close()
    }

    fun Clear(displayText: String?, clearInt: Boolean) {
        dateDisplay!!.text = displayText
        bpmChart!!.clear()
        if (clearInt) {
            avg = 0
            avgSum = 0
            avgCnt = 0
            max = 0
            min = 70
        }
    }

    fun setBpmText() {
        maxBpm!!.text = "" + max
        minBpm!!.text = "" + min
        avgBpm!!.text = "" + avg
        diffMinBpm!!.text = "-" + (avg - min)
        diffMaxBpm!!.text = "+" + (max - avg)
    }

    fun getFileDirectory(Name: String): File {
        return File(requireActivity().filesDir, Name)
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
        calcDate()
    }

    fun calcDate() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var date: LocalDate

        // target을 기준으로 -1
        date = LocalDate.parse(targetDate, formatter)
        date = date.minusDays(1)
        twoDaysBpmDate = date.format(formatter)
        twoDaysBpmYear = date.format(yearFormat)
        twoDaysBpmMonth = date.format(monthFormat)
        twoDaysBpmDay = date.format(dayFormat)
        date = LocalDate.parse(targetDate, formatter)
        date = date.minusDays(2)

        // target을 기준으로 -2
        threeDaysBpmDate = date.format(formatter)
        threeDaysBpmYear = date.format(yearFormat)
        threeDaysBpmMonth = date.format(monthFormat)
        threeDaysBpmDay = date.format(dayFormat)
    }
}