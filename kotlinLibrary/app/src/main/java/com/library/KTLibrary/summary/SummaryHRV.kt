package com.library.KTLibrary.summary

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
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

@RequiresApi(api = Build.VERSION_CODES.O)
class SummaryHRV : Fragment() {
    /*imagebutton*/ //region
    var yesterdayButton: ImageButton? = null
    var tomorrowButton: ImageButton? = null

    //endregion
    /*Button*/ //region
    lateinit var buttons: Array<Button?>
    var todayButton: Button? = null
    var twoDaysButton: Button? = null
    var threeDaysButton: Button? = null

    //endregion
    /*TextView*/ //region
    var dateDisplay: TextView? = null
    var minHrv: TextView? = null
    var maxHrv: TextView? = null
    var avgHrv: TextView? = null
    var diffMinHrv: TextView? = null
    var diffMaxHrv: TextView? = null

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
    var twoDaysHrvYear: String? = null
    var twoDaysHrvMonth: String? = null
    var twoDaysHrvDay: String? = null
    var twoDaysHrvDate: String? = null

    //endregion
    /*tartget date를 기준으로 -2*/ //region
    var threeDaysHrvYear: String? = null
    var threeDaysHrvMonth: String? = null
    var threeDaysHrvDay: String? = null
    var threeDaysHrvDate: String? = null

    //endregion
    /*booleanDays*/ //region
    var today = true
    var twoDays: Boolean? = null
    var threeDays: Boolean? = null

    //endregion
    /*max_min_avg_cnt*/ //region
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
    private lateinit var view: View
    private var email: String? = null
    private lateinit var hrvChart: LineChart
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_summary_hrv, container, false)
        val emailSharedPreferences = activity?.getSharedPreferences("User", Context.MODE_PRIVATE)
        email = emailSharedPreferences?.getString("email", "null")
        avgHrv = view?.findViewById(R.id.summaryHrvAvg)
        maxHrv = view?.findViewById(R.id.summaryHrvMax)
        minHrv = view?.findViewById(R.id.summaryHrvMin)
        diffMaxHrv = view?.findViewById(R.id.summaryHrvDiffMax)
        diffMinHrv = view?.findViewById(R.id.summaryHrvDiffMin)
        hrvChart = view?.findViewById(R.id.HrvChart)!!
        dateDisplay = view?.findViewById(R.id.dateDisplay)
        yesterdayButton = view?.findViewById(R.id.yesterdayButton)
        tomorrowButton = view?.findViewById(R.id.tomorrowButton)
        todayButton = view?.findViewById(R.id.summaryHrvTodayButton)
        twoDaysButton = view?.findViewById(R.id.summaryHrvTwoDaysButton)
        threeDaysButton = view?.findViewById(R.id.summaryHrvThreeDaysButton)
        buttons = arrayOf(todayButton, twoDaysButton, threeDaysButton)
        currentTimeCheck()
        todayHrvChartGraph()
        todayButton?.setOnClickListener(View.OnClickListener {
            setColor(todayButton)
            todayHrvChartGraph()
            today = true
            twoDays = false
            threeDays = false
        })
        twoDaysButton?.setOnClickListener(View.OnClickListener {
            setColor(twoDaysButton)
            twoDaysHrvChartGraph()
            today = false
            twoDays = true
            threeDays = false
        })
        threeDaysButton?.setOnClickListener(View.OnClickListener {
            setColor(threeDaysButton)
            threeDaysHrvChartGraph()
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
        setButtonEvent(true)
    }

    fun yesterdayButtonEvent() {
        setButtonEvent(false)
    }

    fun setButtonEvent(check: Boolean) {
        dateCalculate(1, check)
        if (today) {
            todayHrvChartGraph()
        } else if (twoDays!!) {
            twoDaysHrvChartGraph()
        } else {
            threeDaysHrvChartGraph()
        }
    }

    fun calcMinMax(hrv: Int) {
        if (hrv != 0) {
            if (min > hrv) {
                min = hrv
            }
            if (max < hrv) {
                max = hrv
            }
            avgSum += hrv
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

    fun todayHrvChartGraph() {
        dateDisplay!!.text = targetDate
        hrvChart!!.clear()
        Clear()

        // 경로
        val directory = getHrvDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

        // 파일 경로와 이름
        val file = File(directory, "BpmData.csv")
        if (file.exists()) {
            // 파일이 있는 경우

            // hrv data가 저장되는 배열 리스트
            val hrvArrayData = ArrayList<Double>()
            // hrv time data가 저장되는 배열 리스트
            val hrvTimeData = ArrayList<String>()
            // 그래프의 x축(시간) y축(데이터)이 저장되는 Entry 리스트
            val entries: MutableList<Entry> = ArrayList()
            try {
                // file read
                setHrvLoop(file, hrvTimeData, hrvArrayData)


                // 그래프에 들어갈 데이터 저장
                LineChartController.setChartData(entries, hrvArrayData)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            // 그래프 Set
            val dataSet = LineChartController.getLineData(entries, "HRV", Color.BLUE)
            val hrvChartData = LineData(dataSet)
            LineChartController.setChartOption(hrvChart, hrvChartData, hrvTimeData)

            // 줌 인 상태에서 다른 그래프 봤을 경우 대비 줌 아웃
            LineChartController.setZoom(hrvChart)
            setHrvText()
        } else {
            // 파일이 없는 경우
        }
    }

    fun twoDaysHrvChartGraph() {
        hrvChart!!.clear()
        dateDisplay!!.text = "$twoDaysHrvMonth-$twoDaysHrvDay ~ $targetMonth-$targetDay"
        Clear()

        // 경로
        var directory = getHrvDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

        // 파일 경로와 이름
        val targetFile = File(directory, "BpmData.csv")

        // 경로
        directory =
            getHrvDirectory("LOOKHEART/$email/$twoDaysHrvYear/$twoDaysHrvMonth/$twoDaysHrvDay")

        // 파일 경로와 이름
        val twoDaysHrvFile = File(directory, "BpmData.csv")
        if (targetFile.exists() && twoDaysHrvFile.exists()) {
            // 파일이 있는 경우

            /*
            target : 기준일
            twoDays : 기준일 -1
             */

            // hrv data가 저장되는 배열 리스트
            val targetHrvArrayData = ArrayList<Double>()
            val twoDaysHrvArrayData = ArrayList<Double>()

            // hrv time data가 저장되는 배열 리스트
            val targetHrvTimeData = ArrayList<String>()
            val twoDaysHrvTimeData = ArrayList<String>()

            // 그래프의 x축(시간) y축(데이터)이 저장되는 Entry 리스트
            val targetEntries: MutableList<Entry> = ArrayList()
            val twoDaysEntries: MutableList<Entry> = ArrayList()

            // target(기준일) 데이터 저장
            try {
                // file read
                setHrvLoop(targetFile, targetHrvTimeData, targetHrvArrayData)
                setHrvLoop(twoDaysHrvFile, twoDaysHrvTimeData, twoDaysHrvArrayData)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val firstIndex = targetHrvTimeData.size - 1
            val secondIndex = twoDaysHrvTimeData.size - 1
            val totalIndex = firstIndex + secondIndex
            val totalAXis = ArrayList<String>()
            var j = 0
            for (i in 0..totalIndex) {
                var Xlabel: String
                if (i <= firstIndex) {
                    Xlabel = targetHrvTimeData[i]
                } else {
                    Xlabel = twoDaysHrvTimeData[j]
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
                        if (time === targetHrvTimeData[k]) {
                            val HrvDataEntry = Entry(i.toFloat(), targetHrvArrayData[k].toFloat())
                            targetEntries.add(HrvDataEntry)
                            k++
                        }
                    }
                    if (z <= secondIndex) {
                        if (time === twoDaysHrvTimeData[z]) {
                            val HrvDataEntry = Entry(i.toFloat(), twoDaysHrvArrayData[z].toFloat())
                            twoDaysEntries.add(HrvDataEntry)
                            z++
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //Collections.sort(totalAXis); //시간 정렬

            // totalAXis.stream().map(d-> d.split(" ")[1]); //날짜 년월일 제외

            // 그래프 Set
            val targetDataSet =
                LineChartController.getLineData(targetEntries, "$targetMonth-$targetDay", Color.RED)

            // 그래프 Set
            val twoDaysDataSet = LineChartController.getLineData(
                twoDaysEntries,
                "$twoDaysHrvMonth-$twoDaysHrvDay",
                Color.BLUE
            )
            val twoDaysHrvChartdataSets = ArrayList<ILineDataSet>()
            twoDaysHrvChartdataSets.add(twoDaysDataSet)
            twoDaysHrvChartdataSets.add(targetDataSet)
            val twoDaysHrvChartData = LineData(twoDaysHrvChartdataSets)
            LineChartController.setChartOption(hrvChart, twoDaysHrvChartData, totalAXis)

            // 줌 인 상태에서 다른 그래프 봤을 경우 대비 줌 아웃
            LineChartController.setZoom(hrvChart)
            setHrvText()
        } else {
            // 파일이 없는 경우
        }
    }

    fun threeDaysHrvChartGraph() {
        hrvChart!!.clear()
        dateDisplay!!.text = "$threeDaysHrvMonth-$threeDaysHrvDay ~ $targetMonth-$targetDay"

        // 경로
        var directory = getHrvDirectory("LOOKHEART/$email/$targetYear/$targetMonth/$targetDay")

        // 파일 경로와 이름
        val targetFile = File(directory, "BpmData.csv")

        // 경로
        directory =
            getHrvDirectory("LOOKHEART/$email/$twoDaysHrvYear/$twoDaysHrvMonth/$twoDaysHrvDay")

        // 파일 경로와 이름
        val twoDaysHrvFile = File(directory, "BpmData.csv")

        // 경로
        directory =
            getHrvDirectory("LOOKHEART/$email/$threeDaysHrvYear/$threeDaysHrvMonth/$threeDaysHrvDay")

        // 파일 경로와 이름
        val threeDaysHrvFile = File(directory, "BpmData.csv")
        if (targetFile.exists() && twoDaysHrvFile.exists() && threeDaysHrvFile.exists()) {
            // 파일이 있는 경우

            /*
            target : 기준일
            twoDays : 기준일 -2
             */

            // Hrv data가 저장되는 배열 리스트
            val targetHrvArrayData = ArrayList<Double>()
            val twoDaysHrvArrayData = ArrayList<Double>()
            val threeDaysHrvArrayData = ArrayList<Double>()

            // Hrv time data가 저장되는 배열 리스트
            val targetHrvTimeData = ArrayList<String>()
            val twoDaysHrvTimeData = ArrayList<String>()
            val threeDaysHrvTimeData = ArrayList<String>()

            // 그래프의 x축(시간) y축(데이터)이 저장되는 Entry 리스트
            val targetEntries: MutableList<Entry> = ArrayList()
            val twoDaysEntries: MutableList<Entry> = ArrayList()
            val threeDaysEntries: MutableList<Entry> = ArrayList()

            // target(기준일) 데이터 저장
            try {
                // file read
                setHrvLoop(targetFile, targetHrvTimeData, targetHrvArrayData)
                setHrvLoop(twoDaysHrvFile, twoDaysHrvTimeData, twoDaysHrvArrayData)
                setHrvLoop(threeDaysHrvFile, threeDaysHrvTimeData, threeDaysHrvArrayData)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val firstIndex = targetHrvTimeData.size
            val secondIndex = twoDaysHrvTimeData.size
            val thirdIndex = threeDaysHrvTimeData.size
            val totalIndex = firstIndex + secondIndex + thirdIndex
            val totalAXis = ArrayList<String>()
            var j = 0
            var k = 0
            for (i in 0 until totalIndex) {
                var Xlabel: String
                if (i < firstIndex) {
                    Xlabel = targetHrvTimeData[i]
                    totalAXis.add(Xlabel)
                } else if (j < secondIndex) {
                    Xlabel = twoDaysHrvTimeData[j]
                    totalAXis.add(Xlabel)
                    j++
                } else if (k < thirdIndex) {
                    Xlabel = threeDaysHrvTimeData[k]
                    totalAXis.add(Xlabel)
                    k++
                }
            }
            Collections.sort(totalAXis) //시간 정렬
            var a = 0
            var b = 0
            var z = 0
            try {
                for (i in 0 until totalIndex) {
                    var HrvDataEntry: Entry
                    val time = totalAXis[i]
                    if (b < secondIndex) {
                        if (twoDaysHrvTimeData.contains(time)) {
                            HrvDataEntry = Entry(i.toFloat(), twoDaysHrvArrayData[b].toFloat())
                            twoDaysEntries.add(HrvDataEntry)
                            b++
                        }
                    }
                    if (a < firstIndex) {
                        if (targetHrvTimeData.contains(time)) {
                            HrvDataEntry = Entry(i.toFloat(), targetHrvArrayData[a].toFloat())
                            targetEntries.add(HrvDataEntry)
                            a++
                        }
                    }
                    if (z < thirdIndex) {
                        if (threeDaysHrvTimeData.contains(time)) {
                            HrvDataEntry = Entry(i.toFloat(), threeDaysHrvArrayData[z].toFloat())
                            threeDaysEntries.add(HrvDataEntry)
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
                "$twoDaysHrvMonth-$twoDaysHrvDay",
                Color.BLUE
            )

            // 그래프 Set
            val threeDaysDataSet = LineChartController.getLineData(
                threeDaysEntries,
                "$threeDaysHrvMonth-$threeDaysHrvDay",
                Color.parseColor("#138A1E")
            )
            val threeDaysHrvChartdataSets = ArrayList<ILineDataSet>()
            threeDaysHrvChartdataSets.add(threeDaysDataSet)
            threeDaysHrvChartdataSets.add(twoDaysDataSet)
            threeDaysHrvChartdataSets.add(targetDataSet)
            val HrvChartData = LineData(threeDaysHrvChartdataSets)
            LineChartController.setChartOption(hrvChart, HrvChartData, totalAXis)

            // 줌 인 상태에서 다른 그래프 봤을 경우 대비 줌 아웃
            LineChartController.setZoom(hrvChart)
            setHrvText()
        } else {
            // 파일이 없는 경우
        }
    }

    @Throws(IOException::class)
    fun setHrvLoop(file: File?, timeData: ArrayList<String>, arrayData: ArrayList<Double>) {
        val br = BufferedReader(FileReader(file))
        var line: String ?
        while (br.readLine().also { line = it } != null) {
            val columns =
                line!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() // 데이터 구분
            val hrvDataRow = columns[4].toDouble() // hrv data
            val hrv = columns[4].toInt() // minMaxAvg 찾는 변수
            val hrvTimeCheck = columns[0].split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray() // 시간 구분
            val myHrvTimeRow = hrvTimeCheck[0] + ":" + hrvTimeCheck[1] // 초 단위 제거
            calcMinMax(hrv)

            // 데이터 저장
            timeData.add(myHrvTimeRow)
            arrayData.add(hrvDataRow)
        }
        br.close()
    }

    fun setHrvText() {
        maxHrv!!.text = "" + max
        minHrv!!.text = "" + min
        avgHrv!!.text = "" + avg
        diffMinHrv!!.text = "-" + (avg - min)
        diffMaxHrv!!.text = "+" + (max - avg)
    }

    fun getHrvDirectory(name: String): File {
        return File(activity?.filesDir, name)
    }

    fun Clear() {
        avg = 0
        avgSum = 0
        avgCnt = 0
        max = 0
        min = 70
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
        twoDaysHrvDate = date.format(formatter)
        twoDaysHrvYear = date.format(yearFormat)
        twoDaysHrvMonth = date.format(monthFormat)
        twoDaysHrvDay = date.format(dayFormat)
        date = LocalDate.parse(targetDate, formatter)
        date = date.minusDays(2)

        // target을 기준으로 -2
        threeDaysHrvDate = date.format(formatter)
        threeDaysHrvYear = date.format(yearFormat)
        threeDaysHrvMonth = date.format(monthFormat)
        threeDaysHrvDay = date.format(dayFormat)
    }
}