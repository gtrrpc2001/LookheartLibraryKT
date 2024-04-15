package com.library.KTLibrary.fragment.summary.barChart

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry
import com.library.KTLibrary.R
import com.library.KTLibrary.base.LoadingBar
import com.library.KTLibrary.base.MyDateTime
import com.library.KTLibrary.base.MyToast
import com.library.KTLibrary.databinding.FragmentBarChartBinding
import com.library.KTLibrary.fragment.summary.SummaryFragment
import com.library.KTLibrary.myEnum.BarChartType
import com.library.KTLibrary.myEnum.DateType
import com.library.KTLibrary.myEnum.TimePeriod
import com.library.KTLibrary.server.RetrofitServerController
import com.library.KTLibrary.server.ServerDataClass
import com.library.KTLibrary.server.UserProfile
import com.library.KTLibrary.server.UserProfileManager
import kotlinx.coroutines.launch
import java.time.LocalDate


class BarChartFragment: Fragment() {
    companion object {
        private const val NEXT_DAY = true
        private const val PREV_DAY = false
        private val DAY_OF_WEEK_ID = listOf(
            R.string.Monday, R.string.Tuesday, R.string.Wednesday,
            R.string.Thursday, R.string.Friday, R.string.Saturday, R.string.Sunday
        )
    }

    private data class ChartLabels(val primaryLabel: String, val secondaryLabel: String? = null)

    private var _binding: FragmentBarChartBinding? = null
    private val binding get() = _binding!!

    private var dayOfWeek: ArrayList<String> = arrayListOf()
    private var isChartVisible = false  // lifecycle 이전 함수 실행 방지 Flag

    private lateinit var buttonList: Array<Button>
    private lateinit var buttonFlag: TimePeriod
    private lateinit var targetDate: String
    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBarChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isChartVisible = true
        barChart = binding.barChart
        BarChartController.setBarChart(barChart)

        setDayOfWeek()
        setButtonEvent()
        setNaviButtonEvent()
        setCalendarButtonEvent()

        showChart(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setDayOfWeek() {
        DAY_OF_WEEK_ID.forEach {
            dayOfWeek.add(requireContext().resources.getString(it))
        }
    }

    /**
     * Day, Week, Month, Year
     * */
    private fun setButtonEvent() {
        buttonList = arrayOf(binding.dayBtn, binding.weekBtn, binding.monthBtn, binding.yearBtn)

        binding.dayBtn.setOnClickListener { buttonEvent(binding.dayBtn, TimePeriod.DAY) }
        binding.weekBtn.setOnClickListener { buttonEvent(binding.weekBtn, TimePeriod.WEEK) }
        binding.monthBtn.setOnClickListener { buttonEvent(binding.monthBtn, TimePeriod.MONTH) }
        binding.yearBtn.setOnClickListener { buttonEvent(binding.yearBtn, TimePeriod.YEAR) }
    }

    private fun buttonEvent(button: Button, buttonType: TimePeriod) {
        buttonFlag = buttonType
        setButtonColor(button)
        showChart()
    }

    private fun setButtonColor(selectButton: Button) {
        buttonList.forEach { button ->
            if (selectButton == button) {
                button.background = ContextCompat.getDrawable(requireActivity(), R.drawable.summary_button_press)
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                button.background = ContextCompat.getDrawable(requireActivity(), R.drawable.summary_button_noraml2)
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightGray))
            }
        }
    }

    /**
     * Yesterday, Tomorrow Button Event
     * */
    private fun setNaviButtonEvent() {
        binding.prevBtn.setOnClickListener { naviButtonEvent(PREV_DAY) }
        binding.nextBtn.setOnClickListener { naviButtonEvent(NEXT_DAY) }
    }

    private fun naviButtonEvent(flag: Boolean) {
        setTargetDate(flag)
        showChart()
    }

    private fun setTargetDate(flag: Boolean) {
        targetDate = when(buttonFlag) {
            TimePeriod.DAY -> MyDateTime.dateCalculate(targetDate, 1, flag, TimePeriod.DAY)
            TimePeriod.WEEK -> MyDateTime.dateCalculate(targetDate, 1, flag, TimePeriod.WEEK)
            TimePeriod.MONTH -> MyDateTime.dateCalculate(targetDate, 1, flag, TimePeriod.MONTH)
            TimePeriod.YEAR -> MyDateTime.dateCalculate(targetDate, 1, flag, TimePeriod.YEAR)
        }
    }

    /**
     * Calendar Button Event
     * */
    private fun setCalendarButtonEvent() {
        binding.calendarBtn.setOnClickListener { showCalendar() }
        binding.dateTextView.setOnClickListener { showCalendar() }
    }

    private fun showCalendar() {
        val splitDate = targetDate.split("-")
        if (splitDate.size < 3) return

        DatePickerDialog(requireContext(), R.style.RoundedDatePickerDialog,
            { _, selectedYear, selectedMonth, selectedDay ->
                targetDate = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                showChart()
            }, splitDate[0].toInt(), splitDate[1].toInt() - 1, splitDate[2].toInt()
        ).show()
    }

    /**
     * Chart
     * */
    fun showChart(checkInit: Boolean = false) {
        if (!isChartVisible) return
        else if (checkInit) initData()

        LoadingBar.showNotTouchableLoadingBar(binding.progressBar, requireActivity(), true)
        BarChartController.initChart(barChart)
        setUserInterface()

        lifecycleScope.launch {
            getDataToServer()?.let { result ->
                val dataArrayList = getDataArrayList(result.split("\n").drop(1))
                val groupData = groupDataByDate(dataArrayList)
                val dictionary = getDictionary(groupData)

                val (timeTable, dataTable) = getDataTable(dictionary)
                val entries: MutableMap<String, List<BarEntry>> = mutableMapOf()

                setEntries(entries, timeTable, dataTable)

                BarChartController.showBarChart(
                    barChart,
                    BarChartController.getBarDataSet(entries, getGraphColor()),
                    timeTable,
                    SummaryFragment.barChartType
                )

                BarChartController.resetZoom(barChart, timeTable.size)

            } ?: run {
                MyToast.showToast(requireContext(), resources.getString(R.string.noArrData))
            }

            LoadingBar.showNotTouchableLoadingBar(binding.progressBar, requireActivity(), false)
        }
    }

    private fun initData() {
        targetDate = MyDateTime.getCurrentDateTime(DateType.DATE)
        buttonFlag = TimePeriod.DAY
    }

    private suspend fun getDataToServer(): String? {
        UserProfileManager.getUserProfile()?.email?.let { email ->
            val startDate = getStartDate()
            val endDate = getEndDate(startDate)

            val mapParam: MutableMap<String, String> = hashMapOf(
                "eq" to email,
                "startDate" to startDate,
                "endDate" to endDate
            )

            RetrofitServerController.executeRequest(
                RetrofitServerController.HTTPMethod.GET,
                RetrofitServerController.EndPoint.GET_HOURLY_DATA.endPoint,
                null,
                mapParam
            )?.let { result ->
                return if (result.contains("result = 0")) null
                else result
            }
        } ?: run { return null }
    }

    private fun getDataArrayList(
        dataList: List<String>
    ) : ArrayList<ServerDataClass.HourlyData> {
        val dataArrayList = ArrayList<ServerDataClass.HourlyData>()

        dataList.forEach {
            ServerDataClass.HourlyData.changeFormat(it).let { data ->
                when (SummaryFragment.barChartType) {
                    BarChartType.ARR -> {
                        data?.arrCnt?.let {
                            dataArrayList.add(data)
                        }
                    }
                    BarChartType.CALORIE -> {
                        data?.takeIf { hourlyData ->
                            hourlyData.calorie != null && hourlyData.activityCalorie != null
                        }?.let { dataArrayList.add(data) }
                    }
                    BarChartType.STEP -> {
                        data?.takeIf { hourlyData ->
                            hourlyData.step != null && hourlyData.distance != null
                        }?.let { dataArrayList.add(data) }
                    }
                }
            }
        }

        return dataArrayList
    }

    private fun groupDataByDate(
        dataArrayList: ArrayList<ServerDataClass.HourlyData>
    ): MutableMap<String, ArrayList<ServerDataClass.HourlyData>> {
        val resultMap: MutableMap<String, ArrayList<ServerDataClass.HourlyData>> = mutableMapOf()

        dataArrayList.forEach { data ->
            data.writeDate?.let { resultMap.getOrPut(it) { arrayListOf() }.add(data) }
        }

        return resultMap
    }

    private fun findMonday(): String {
        val formatter = MyDateTime.getFormatter(DateType.DATE)
        val date = LocalDate.parse(targetDate, formatter)

        val dayOfWeek = date.dayOfWeek.value // (월요일=1, 일요일=7)

        val monday = if (dayOfWeek == 1)
            date
        else
            date.minusDays((dayOfWeek - 1).toLong())

        return monday.format(formatter)
    }

    private fun getStartDate(): String {
        val formatter = MyDateTime.getFormatter(DateType.DATE)
        return when (buttonFlag) {
            TimePeriod.DAY -> targetDate
            TimePeriod.WEEK -> findMonday()
            TimePeriod.MONTH -> LocalDate.parse(targetDate, formatter).withDayOfMonth(1).format(formatter)
            TimePeriod.YEAR -> LocalDate.parse(targetDate, formatter).withDayOfYear(1).format(formatter)
        }
    }

    private fun getEndDate(date: String): String {
        return when (buttonFlag) {
            TimePeriod.DAY -> MyDateTime.dateCalculate(date, 1, true, TimePeriod.DAY)
            TimePeriod.WEEK -> MyDateTime.dateCalculate(date, 1, true, TimePeriod.WEEK)
            TimePeriod.MONTH -> MyDateTime.dateCalculate(date, 1, true, TimePeriod.MONTH)
            TimePeriod.YEAR -> MyDateTime.dateCalculate(date, 1, true, TimePeriod.YEAR)
        }
    }


    private fun getDictionary(
        groupData: MutableMap<String, ArrayList<ServerDataClass.HourlyData>>
    ): MutableMap<String, MutableMap<String, ServerDataClass.HourlyData>> {
        // [ date : [ time : HourlyData ]]
        val dictionary: MutableMap<String, MutableMap<String, ServerDataClass.HourlyData>> = mutableMapOf()

        for ((date, dataForDate) in groupData) {
            val timeDictionary: MutableMap<String, ServerDataClass.HourlyData> = mutableMapOf()

            for (data in dataForDate) {
                data.writeTime?.let { timeDictionary[it] = data }
            }

            dictionary[date] = timeDictionary
        }

        return dictionary
    }

    private fun getDataTable(
        dictionary: MutableMap<String, MutableMap<String, ServerDataClass.HourlyData>>
    ) : Pair<ArrayList<String>, MutableMap<String, ServerDataClass.HourlyData>>{
        val timeTable = ArrayList<String>()
        val resultDict: MutableMap<String, ServerDataClass.HourlyData> = mutableMapOf()

        when (buttonFlag) {
            TimePeriod.DAY -> setDayDataTable(dictionary, resultDict, timeTable)
            TimePeriod.WEEK -> setWeekDataTable(dictionary, resultDict, timeTable)
            TimePeriod.MONTH -> setMonthDataTable(dictionary, resultDict, timeTable)
            TimePeriod.YEAR -> setYearDataTable(dictionary, resultDict, timeTable)
        }

        return Pair(timeTable, resultDict)
    }

    /** DAY **/
    private fun setDayDataTable(
        dictionary: MutableMap<String, MutableMap<String, ServerDataClass.HourlyData>>,
        resultDict: MutableMap<String, ServerDataClass.HourlyData>,
        timeTable: ArrayList<String>,
    ) {
        for ((_, hourlyData) in dictionary) {
            for ((time, data) in hourlyData) {
                val timeKey = time.substring(0, 2).toInt().toString()
                timeTable.add(timeKey)
                resultDict[timeKey] = data
            }
        }
    }


    /** WEEK **/
    private fun setWeekDataTable(
        dictionary: MutableMap<String, MutableMap<String, ServerDataClass.HourlyData>>,
        resultDict: MutableMap<String, ServerDataClass.HourlyData>,
        timeTable: ArrayList<String>,
    ) {
        var weekDate = findMonday()

        dayOfWeek.forEach {
            val timeKey = it

            timeTable.add(timeKey)

            dictionary[weekDate]?.also { hourlyDataMap ->
                resultDict[timeKey] = aggregateHourlyData(hourlyDataMap)
            } ?: run {
                resultDict[timeKey] =  ServerDataClass.HourlyData(
                eq = null, writeDateTime = null, writeDate = null, writeTime = null,
                step = 0f, distance = 0f, calorie = 0f, activityCalorie = 0f, arrCnt = 0f)
            }

            weekDate = MyDateTime.dateCalculate(weekDate, 1, true, TimePeriod.DAY)
        }
    }

    /** MONTH **/
    private fun setMonthDataTable(
        dictionary: MutableMap<String, MutableMap<String, ServerDataClass.HourlyData>>,
        resultDict: MutableMap<String, ServerDataClass.HourlyData>,
        timeTable: ArrayList<String>,
    ){
        dictionary.forEach { (date, hourlyDataMap) ->
            val monthKey = date.takeLast(2).toInt().toString()
            timeTable.add(monthKey)
            resultDict[monthKey] = aggregateHourlyData(hourlyDataMap)
        }
    }

    /** YEAR **/
    private fun setYearDataTable(
        dictionary: MutableMap<String, MutableMap<String, ServerDataClass.HourlyData>>,
        resultDict: MutableMap<String, ServerDataClass.HourlyData>,
        timeTable: ArrayList<String>,
    ){
        (1..12).map { it.toString().padStart(2, '0') }.toCollection(timeTable).forEach { month ->
            val findDate = "${targetDate.substring(0, 5)}$month"   // 2024-01 ~ 2024-12
            val filteredMap = dictionary.filterKeys { it.startsWith(findDate) }

            val monthlyData = ServerDataClass.HourlyData(
                eq = null, writeDateTime = null, writeDate = null, writeTime = null,
                step = 0f, distance = 0f, calorie = 0f, activityCalorie = 0f, arrCnt = 0f
            )

            filteredMap.takeIf { it.isNotEmpty() }?.let { map ->
                map.forEach { (_, hourlyDataMap) ->
                    aggregateHourlyData(hourlyDataMap).apply {
                        monthlyData.step = monthlyData.step?.plus((step ?: 0f))
                        monthlyData.distance = monthlyData.distance?.plus((distance ?: 0f))
                        monthlyData.calorie = monthlyData.calorie?.plus((calorie ?: 0f))
                        monthlyData.activityCalorie = monthlyData.activityCalorie?.plus((activityCalorie ?: 0f))
                        monthlyData.arrCnt = monthlyData.arrCnt?.plus((arrCnt ?: 0f))
                    }
                }
            }

            resultDict[month] = monthlyData
        }
    }

    private fun aggregateHourlyData(
        hourlyDataMap: MutableMap<String, ServerDataClass.HourlyData>
    ) : ServerDataClass.HourlyData {
        return hourlyDataMap.values.fold(ServerDataClass.HourlyData(
            eq = null, writeDateTime = null, writeDate = null, writeTime = null,
            step = 0f, distance = 0f, calorie = 0f, activityCalorie = 0f, arrCnt = 0f
        )) { acc, data ->
            acc.apply {
                step = (step ?: 0f) + (data.step ?: 0f)
                distance = (distance ?: 0f) + (data.distance ?: 0f)
                calorie = (calorie ?: 0f) + (data.calorie ?: 0f)
                activityCalorie = (activityCalorie ?: 0f) + (data.activityCalorie ?: 0f)
                arrCnt = (arrCnt ?: 0f) + (data.arrCnt ?: 0f)
            }
        }
    }

    /** ENTRIES **/
    private fun setEntries(
        entries: MutableMap<String, List<BarEntry>>,
        timeTable: List<String>,
        dataDict: MutableMap<String, ServerDataClass.HourlyData>
    ) {
        val label = getLabels()

        when (SummaryFragment.barChartType) {
            BarChartType.ARR -> {
                val (entry, sumArrCnt) = createEntriesList(timeTable, dataDict) { it.arrCnt }
                entries[label.primaryLabel] = entry
                setValueText(firstValue = sumArrCnt)
            }

            BarChartType.CALORIE, BarChartType.STEP -> {
                val firstValueSelector =
                    if (SummaryFragment.barChartType == BarChartType.CALORIE) {
                        data: ServerDataClass.HourlyData -> data.calorie
                    } else {
                        data: ServerDataClass.HourlyData -> data.step
                    }
                val secondValueSelector =
                    if (SummaryFragment.barChartType == BarChartType.CALORIE) {
                        data: ServerDataClass.HourlyData -> data.activityCalorie
                    } else {
                        data: ServerDataClass.HourlyData -> data.distance
                    }

                val (firstEntry, sumFirstValue) = createEntriesList(timeTable, dataDict, firstValueSelector)
                val (secondEntry, sumSecondValue) = createEntriesList(timeTable, dataDict, secondValueSelector)

                entries[label.primaryLabel] = firstEntry
                label.secondaryLabel?.let { entries[it] = secondEntry }

                setValueText(firstValue = sumFirstValue, secondValue = sumSecondValue)
            }
        }
    }

    private fun createEntriesList(
        timeTable: List<String>,
        dataDict: Map<String, ServerDataClass.HourlyData>,
        valueSelector: (ServerDataClass.HourlyData) -> Float?
    ): Pair<List<BarEntry>, Float> {
        val entry: ArrayList<BarEntry> = ArrayList()
        var sumValue = 0f

        timeTable.forEachIndexed { index, time ->
            val hourlyData = dataDict[time]
            val value = hourlyData?.let(valueSelector) ?: 0f

            entry.add(BarEntry(index.toFloat(), value))
            sumValue += value
        }

        return Pair(entry, sumValue)
    }

    private fun getLabels(): ChartLabels {
        return when (SummaryFragment.barChartType) {
            BarChartType.ARR -> ChartLabels(requireContext().getString(R.string.arr))
            BarChartType.CALORIE -> ChartLabels(
                requireContext().getString(R.string.tCalTitle),
                requireContext().getString(R.string.eCalTitle)
            )
            BarChartType.STEP -> ChartLabels(
                requireContext().getString(R.string.summaryStep),
                requireContext().getString(R.string.distance)
            )
        }
    }

    private fun getGraphColor(): Array<Int> {
        return when (SummaryFragment.barChartType) {
            BarChartType.ARR -> arrayOf(ContextCompat.getColor(requireContext(), R.color.myRed))
            BarChartType.CALORIE, BarChartType.STEP -> arrayOf(
                ContextCompat.getColor(requireContext(), R.color.myRed),
                ContextCompat.getColor(requireContext(), R.color.myBlue)
            )
        }
    }

    /**
     * UI
     * */
    private fun setUserInterface() {
        when (SummaryFragment.barChartType) {
            BarChartType.ARR -> {
                setLayoutVisibility()
                setLabelText(requireContext().getString(R.string.arrTimes))
            }
            BarChartType.CALORIE, BarChartType.STEP -> {
                setLayoutVisibility(false)

                val firstText =
                    if (SummaryFragment.barChartType == BarChartType.CALORIE)
                         requireContext().getString(R.string.tCalTitle)
                    else requireContext().getString(R.string.summaryStep)
                val secondText =
                    if (SummaryFragment.barChartType == BarChartType.CALORIE)
                         requireContext().getString(R.string.eCalTitle)
                    else requireContext().getString(R.string.travel_distance)

                setLabelText(firstText, secondText, false)
            }
        }

        binding.dateTextView.text = getDateTextView()
        setValueText(firstValue = 0f, secondValue = 0f)
    }

    private fun setLayoutVisibility(visibility: Boolean = true) {
        if (visibility) {
            binding.progressLayout.visibility = View.GONE
            binding.textLayout.visibility = View.VISIBLE
        } else {
            binding.textLayout.visibility = View.GONE
            binding.progressLayout.visibility = View.VISIBLE
        }
    }

    private fun setLabelText(firstText: String? = null, secondText: String? = null, singleGraphFlag: Boolean = true) {
        if (singleGraphFlag)
            binding.textLabel.text = firstText
        else {
            binding.firstTextLabel.text = firstText
            binding.secondTextLabel.text = secondText
        }
    }

    private fun setValueText(firstValue: Float? = null, secondValue: Float? = null) {
        val unitText = getUnitText()

        when (SummaryFragment.barChartType) {
            BarChartType.ARR -> binding.valueLabel.text = firstValue?.toInt().toString()
            BarChartType.CALORIE, BarChartType.STEP -> {
                setProgressBar(firstValue, secondValue)

                binding.firstValueText.text = getString(R.string.value_unit, firstValue?.toInt(), unitText?.primaryLabel)
                binding.secondValueText.text = getString(R.string.value_unit, secondValue?.toInt(), unitText?.secondaryLabel)
            }
        }
    }

    private fun getUnitText(): ChartLabels? {
        return when (SummaryFragment.barChartType) {
            BarChartType.CALORIE -> ChartLabels(requireContext().resources.getString(R.string.kcal_unit), requireContext().resources.getString(R.string.kcal_unit))
            BarChartType.STEP -> ChartLabels(requireContext().resources.getString(R.string.summaryStep), requireContext().resources.getString(R.string.distance_unit))
            else -> null
        }
    }

    private fun getDateTextView(): String {
        return when (buttonFlag) {
            TimePeriod.DAY -> targetDate
            TimePeriod.WEEK -> {
                val startDate = findMonday().substring(5)   // remove year
                val endDate = MyDateTime.dateCalculate(getEndDate(targetDate), 1, false, TimePeriod.DAY).substring(5)
                getString(R.string.day_day, startDate, endDate)
            }
            TimePeriod.MONTH -> targetDate.substring(0, 7)  // remove day
            TimePeriod.YEAR -> targetDate.substring(0, 4)   // remove month, day
        }
    }

    private fun setProgressBar(firstValue: Float?, secondValue: Float?) {
        UserProfileManager.getUserProfile()?.let {
            getGoalValue(it)?.let { goal ->
                firstValue?.let { first ->
                    secondValue?.let { second ->
                        val firstPercent = ((first / goal.first) * 100).toInt()
                        val secondPercent = ((second / goal.second) * 100).toInt()

                        binding.firstPercent.text = getString(R.string.string_percent, firstPercent)
                        binding.firstProgressbar.progress = firstPercent


                        binding.secondPercent.text = getString(R.string.string_percent, secondPercent)
                        binding.secondProgressbar.progress = secondPercent
                    }
                }
            }
        }
    }

    private fun getGoalValue(userProfile: UserProfile) : Pair<Int, Int>? {
        val dayCnt = getDayCount()

        return when (SummaryFragment.barChartType) {
            BarChartType.CALORIE -> userProfile.calorie?.toIntOrNull()?.let { calorie ->
                userProfile.activityCalorie?.toIntOrNull()?.let { activityCalorie ->
                    Pair(calorie * dayCnt, activityCalorie * dayCnt)
                }
            }
            BarChartType.STEP -> userProfile.step?.toIntOrNull()?.let { step ->
                userProfile.distance?.toIntOrNull()?.let { distance ->
                    Pair(step * dayCnt, (distance * 1000) * dayCnt) // distance: km -> m
                }
            }
            else -> null
        }
    }

    private fun getDayCount(): Int {
        return when(buttonFlag) {
            TimePeriod.DAY -> 1
            TimePeriod.WEEK -> 7
            TimePeriod.MONTH -> 30
            TimePeriod.YEAR -> 365
        }
    }
}