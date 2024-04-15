package com.library.KTLibrary.fragment.summary.lineChart

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.library.KTLibrary.R
import com.library.KTLibrary.base.LoadingBar
import com.library.KTLibrary.base.MyDateTime
import com.library.KTLibrary.base.MyToast
import com.library.KTLibrary.databinding.FragmentLineChartBinding
import com.library.KTLibrary.fragment.summary.SummaryFragment
import com.library.KTLibrary.myEnum.ButtonType
import com.library.KTLibrary.myEnum.DateType
import com.library.KTLibrary.myEnum.LineChartType
import com.library.KTLibrary.myEnum.TimePeriod
import com.library.KTLibrary.server.RetrofitServerController
import com.library.KTLibrary.server.ServerDataClass
import com.library.KTLibrary.server.UserProfileManager
import kotlinx.coroutines.launch

class LineChartFragment : Fragment() {
    companion object {
        private const val NEXT_DAY = true
        private const val PREV_DAY = false
    }

    private var _binding: FragmentLineChartBinding? = null
    private val binding get() = _binding!!

    private lateinit var buttonList: Array<Button>
    private lateinit var buttonFlag: ButtonType
    private lateinit var targetDate: String
    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLineChartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lineChart = binding.lineChart
        LineChartController.setLineChart(lineChart)

        setDaysButtonEvent()
        setNaviButtonEvent()
        setCalendarButtonEvent()

        showChart(true)    // Start Chart
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Today, Two Days, Three Days Event
     * */
    private fun setDaysButtonEvent() {
        buttonList = arrayOf (binding.todayBtn, binding.twoDaysBtn, binding.threeDaysBtn)

        binding.todayBtn.setOnClickListener { daysButtonEvent(binding.todayBtn, ButtonType.TODAY) }
        binding.twoDaysBtn.setOnClickListener { daysButtonEvent(binding.twoDaysBtn, ButtonType.TWO_DAYS) }
        binding.threeDaysBtn.setOnClickListener { daysButtonEvent(binding.threeDaysBtn, ButtonType.THREE_DAYS) }
    }

    private fun daysButtonEvent(button: Button, buttonType: ButtonType) {
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
        binding.yesterdayBtn.setOnClickListener { naviButtonEvent(PREV_DAY) }
        binding.tomorrowBtn.setOnClickListener { naviButtonEvent(NEXT_DAY) }
    }

    private fun naviButtonEvent(flag: Boolean) {
        targetDate = MyDateTime.dateCalculate(targetDate, 1, flag, TimePeriod.DAY)
        showChart()
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
        if (checkInit) initData()

        LoadingBar.showNotTouchableLoadingBar(binding.progressBar, requireActivity(), true)
        LineChartController.initChart(lineChart)
        setValueUI(0f, 0f, 0f, 0)

        lifecycleScope.launch {
            getDataToServer()?.let { result ->
                val dataArrayList = getDataArrayList(result.split("\n").drop(1))
                val groupData = groupDataByDate(dataArrayList)
                val dictionary = getDictionary(groupData)

                val entries: MutableMap<String, ArrayList<Entry>> = mutableMapOf()
                var timeTable: Set<String> = emptySet()

                for ((date, data) in groupData) {
                    entries[date] = arrayListOf()
                    timeTable = timeTable.union(data.map { it.writeTime })
                }

                val sortedTimeTable = timeTable.sorted().toList()

                setEntries(entries, sortedTimeTable, dictionary)

                LineChartController.showLineChart(
                    lineChart,
                    LineChartController.getLineDataSet(entries, getGraphColor()),
                    sortedTimeTable,
                    SummaryFragment.lineChartType
                )

                LineChartController.resetZoom(lineChart)

            } ?: run {
                MyToast.showToast(requireContext(), resources.getString(R.string.noArrData))
            }

            LoadingBar.showNotTouchableLoadingBar(binding.progressBar, requireActivity(), false)
        }
    }

    private fun initData() {
        targetDate = MyDateTime.getCurrentDateTime(DateType.DATE)
        buttonFlag = ButtonType.TODAY

        updateTextView()
        setButtonColor(binding.todayBtn)
    }

    private suspend fun getDataToServer(): String? {
        UserProfileManager.getUserProfile()?.email?.let { email ->
            val mapParam: MutableMap<String, String> = hashMapOf(
                "eq" to email,
                "startDate" to targetDate,
                "endDate" to getEndDate()
            )

            RetrofitServerController.executeRequest(
                RetrofitServerController.HTTPMethod.GET,
                RetrofitServerController.EndPoint.GET_BPM_DATA.endPoint,
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
    ) : ArrayList<ServerDataClass.BpmData> {
        val dataArrayList = ArrayList<ServerDataClass.BpmData>()

        dataList.forEach {
            ServerDataClass.BpmData.changeFormat(it).let { data ->
                when (SummaryFragment.lineChartType) {
                    LineChartType.BPM -> {
                        data?.bpm?.let { dataArrayList.add(data) }
                    }
                    LineChartType.HRV -> {
                        data?.hrv?.let { dataArrayList.add(data) }
                    }
                }
            }
        }

        return dataArrayList
    }

    private fun groupDataByDate(
        dataArrayList: ArrayList<ServerDataClass.BpmData>
    ): MutableMap<String, ArrayList<ServerDataClass.BpmData>> {
        val resultMap: MutableMap<String, ArrayList<ServerDataClass.BpmData>> = mutableMapOf()

        dataArrayList.forEach { data ->
            resultMap.getOrPut(data.writeDate) { arrayListOf() }.add(data)
        }

        return resultMap
    }

    private fun getDictionary(
        groupData: MutableMap<String, ArrayList<ServerDataClass.BpmData>>
    ): MutableMap<String, MutableMap<String, ServerDataClass.BpmData>> {
        // [ date : [ time : BpmData ]]
        val dictionary: MutableMap<String, MutableMap<String, ServerDataClass.BpmData>> = mutableMapOf()

        for ((date, dataForDate) in groupData) {
            val timeDictionary: MutableMap<String, ServerDataClass.BpmData> = mutableMapOf()

            for (data in dataForDate)
                timeDictionary[data.writeTime] = data

            dictionary[date] = timeDictionary
        }

        return dictionary
    }

    private fun setEntries(
        entries: MutableMap<String, ArrayList<Entry>>,
        timeTable: List<String>,
        dictionary: MutableMap<String, MutableMap<String, ServerDataClass.BpmData>>
    ) {
        var maxValue = 0.0f
        var minValue = 70.0f
        var avgValue = 0.0f

        timeTable.forEachIndexed { index, time ->
            for ((date, timeDict) in dictionary) {
                val value = when (SummaryFragment.lineChartType) {
                    LineChartType.BPM -> timeDict[time]?.bpm
                    LineChartType.HRV -> timeDict[time]?.hrv
                }

                value?.let {
                    entries[date]?.add(Entry(index.toFloat(), it))

                    maxValue = maxOf(maxValue, it)
                    minValue = minOf(minValue, it)
                    avgValue += it
                }
            }
        }

        setValueUI(maxValue, minValue, avgValue, timeTable.size)
    }

    private fun getGraphColor(): Array<Int> {
        return when (buttonFlag) {
            ButtonType.TODAY -> arrayOf(ContextCompat.getColor(requireContext(), R.color.myRed))
            ButtonType.TWO_DAYS -> arrayOf(
                ContextCompat.getColor(requireContext(), R.color.myRed),
                ContextCompat.getColor(requireContext(), R.color.myBlue)
            )
            ButtonType.THREE_DAYS -> arrayOf(
                ContextCompat.getColor(requireContext(), R.color.myRed),
                ContextCompat.getColor(requireContext(), R.color.myBlue),
                ContextCompat.getColor(requireContext(), R.color.myGreen)
                )
        }
    }

    private fun getEndDate(): String {
        return when(buttonFlag) {
            ButtonType.TODAY -> MyDateTime.dateCalculate(targetDate, 1, NEXT_DAY, TimePeriod.DAY)
            ButtonType.TWO_DAYS -> MyDateTime.dateCalculate(targetDate, 2, NEXT_DAY, TimePeriod.DAY)
            ButtonType.THREE_DAYS -> MyDateTime.dateCalculate(targetDate, 3, NEXT_DAY, TimePeriod.DAY)
        }
    }


    /**
     * UI
     * */
    private fun setValueUI(max: Float, min: Float, avg: Float, cnt: Int) {
        val avgValue = (avg / cnt).toInt()
        val maxValue = max.toInt() - avgValue
        val minValue = avgValue - min.toInt()

        binding.avgValue.text = avgValue.toString()

        binding.maxValue.text = max.toInt().toString()
        binding.difMaxValue.text = getString(R.string.plus_value, maxValue)

        binding.minValue.text = min.toInt().toString()
        binding.difMinValue.text = getString(R.string.minus_value, minValue)

        binding.dateTextView.text = getDateTextView()
    }

    private fun getDateTextView(): String {
        return when (buttonFlag) {
            ButtonType.TODAY -> targetDate
            ButtonType.TWO_DAYS, ButtonType.THREE_DAYS -> {
                val dayOffset = if(buttonFlag == ButtonType.TWO_DAYS) 1 else 2
                val startDate = MyDateTime.formatToDisplay(targetDate)
                val endDate = MyDateTime.dateCalculate(targetDate, dayOffset, NEXT_DAY, TimePeriod.DAY)
                "$startDate ~ ${MyDateTime.formatToDisplay(endDate)}"
            }
        }
    }

    private fun updateTextView() {
        val labelText = when(SummaryFragment.lineChartType) {
            LineChartType.BPM -> resources.getString(R.string.avgBPM)
            LineChartType.HRV -> resources.getString(R.string.avgHRV)
        }

        val unitText = when(SummaryFragment.lineChartType) {
            LineChartType.BPM -> resources.getString(R.string.fragment_bpm)
            LineChartType.HRV -> resources.getString(R.string.home_hrv_unit)
        }

        binding.label.text = labelText
        binding.unitLabel.text = unitText
    }
}