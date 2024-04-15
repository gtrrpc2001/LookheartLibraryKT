package com.library.KTLibrary.fragment.arrList

import android.app.DatePickerDialog
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.google.gson.reflect.TypeToken
import com.library.KTLibrary.R
import com.library.KTLibrary.base.LoadingBar
import com.library.KTLibrary.base.MyDateTime
import com.library.KTLibrary.base.MyToast
import com.library.KTLibrary.controller.PeakController
import com.library.KTLibrary.databinding.FragmentArrListBinding
import com.library.KTLibrary.fragment.summary.lineChart.LineChartController
import com.library.KTLibrary.myEnum.DateType
import com.library.KTLibrary.myEnum.LineChartType
import com.library.KTLibrary.myEnum.TimePeriod
import com.library.KTLibrary.server.GsonSingleton
import com.library.KTLibrary.server.RetrofitServerController
import com.library.KTLibrary.server.UserProfileManager
import com.library.KTLibrary.server.checkError
import com.library.KTLibrary.server.checkIOError
import com.library.KTLibrary.server.checkTimeOut
import kotlinx.coroutines.launch

class ArrListFragment : Fragment() {
    companion object {
        private const val NEXT_DAY = true
        private const val PREV_DAY = false

//        private const val TYPE_ARR = "arr"
        private const val TYPE_FAST_ARR = "fast"
        private const val TYPE_SLOW_ARR = "slow"
        private const val TYPE_HEAVY_ARR = "irregular"


//        private const val STATUS_REST = "R"
        private const val STATUS_EXERCISE = "E"
        private const val STATUS_SLEEP = "S"
    }

    private enum class ButtonType {
        NUMBER, TEXT, EMERGENCY
    }

    private var _binding: FragmentArrListBinding? = null
    private val binding get() = _binding!!

    private var prevNumberButton: Button? = null
    private var prevTextButton: Button? = null

    private lateinit var targetDate: String
    private lateinit var lineChart: LineChart


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArrListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVar()
        initShowArrList()

        setNaviButtonEvent()
        setCalendarButtonEvent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initVar() {
        lineChart = binding.arrLineChart
        LineChartController.setLineChart(lineChart, false)
    }

    /**
     * Prev, Next Button Event
     **/
    private fun setNaviButtonEvent() {
        binding.prevBtn.setOnClickListener { naviButtonEvent(PREV_DAY) }
        binding.nextBtn.setOnClickListener { naviButtonEvent(NEXT_DAY) }
    }

    private fun naviButtonEvent(flag: Boolean) {
        targetDate = MyDateTime.dateCalculate(targetDate, 1, flag, TimePeriod.DAY)
        showArrList()
    }



    /**
     * Calendar Button Event
     **/
    private fun setCalendarButtonEvent() {
        binding.calendarBtn.setOnClickListener { showCalendar() }
        binding.dateDisplay.setOnClickListener { showCalendar() }
    }

    private fun showCalendar() {
        val splitDate = targetDate.split("-")
        if (splitDate.size < 3) return

        DatePickerDialog(requireContext(), R.style.RoundedDatePickerDialog,
            { _, selectedYear, selectedMonth, selectedDay ->
                targetDate = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                showArrList()
            }, splitDate[0].toInt(), splitDate[1].toInt() - 1, splitDate[2].toInt()
        ).show()
    }



    /**
     * Arr List
     **/
    fun initShowArrList() {
        targetDate = MyDateTime.getCurrentDateTime(DateType.DATE)
        showArrList()
    }

    private fun showArrList() {
        binding.dateDisplay.text = targetDate
        LoadingBar.showNotTouchableLoadingBar(binding.progressBar, requireActivity(), true)
        setTextView(false, null)
        removeAllViews()

        lifecycleScope.launch {
            getArrList()?.let { result ->
                val items = fromJsonToList(result)

                setButtonList(items)

            } ?: run {
                MyToast.showToast(requireContext(), resources.getString(R.string.noArrData))
            }

            LoadingBar.showNotTouchableLoadingBar(binding.progressBar, requireActivity(), false)

            binding.scrollView.post {
                binding.scrollView.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    private suspend fun getArrList(): String? {
        UserProfileManager.getUserProfile()?.email?.let { email ->
            val mapParam: MutableMap<String, String> = hashMapOf(
                "eq" to email,
                "startDate" to targetDate,
                "endDate" to getEndDate()
            )

            RetrofitServerController.executeRequest(
                RetrofitServerController.HTTPMethod.GET,
                RetrofitServerController.EndPoint.GET_ARR_LIST.endPoint,
                null,
                mapParam
            )?.let { result ->
                return when {
                    checkIOError(result) || checkError(result) || checkTimeOut(result) || result.contains("result = 0") -> null
                    else -> result
                }
            }
        } ?: run { return null }
    }

    private fun fromJsonToList(result: String): List<ArrListItem> {
        val type = object : TypeToken<List<ArrListItem>>() {}.type
        return GsonSingleton.instance.fromJson(result, type)
    }

    private fun getEndDate(): String {
        return MyDateTime.dateCalculate(targetDate, 1, true, TimePeriod.DAY)
    }



    /**
     * Create Buttons
     **/
    private fun setButtonList(items: List<ArrListItem>) {
        var arrIdx = 1

        items.forEach {
            val numberButton: Button
            val writeTimeButton: Button
            var address: String? = null

            if(it.address == null) {
                // ARR
                numberButton = createButton(arrIdx.toString(), ButtonType.NUMBER, arrIdx)
                writeTimeButton = createButton(it.writetime, ButtonType.TEXT, arrIdx)
                arrIdx++
            } else {
                // EMERGENCY
                numberButton = createButton("E", ButtonType.NUMBER, arrIdx)
                writeTimeButton = createButton(it.writetime, ButtonType.TEXT, arrIdx)
                address = it.address
            }

            binding.numberBtnLayout.addView(numberButton)
            binding.writeTimeBtnLayout.addView(writeTimeButton)

            setButtonOnClickListener(numberButton, writeTimeButton, address)
        }
    }

    private fun createButton(title: String, type: ButtonType, buttonID: Int): Button {
        return Button(requireContext()).apply {
            id = buttonID
            text = title
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER

            this.layoutParams = getButtonParams()

            when (type) {
                ButtonType.NUMBER -> {
                    setTextColor(requireContext().getColor(R.color.white))
                    background = getDrawable(R.drawable.arr_button_normal)
                }
                ButtonType.TEXT -> {
                    setTextColor(requireContext().getColor(R.color.black))
                    background = getDrawable(R.drawable.home_bottom_button)
                }
                ButtonType.EMERGENCY -> {}
            }
        }
    }

    private fun getButtonParams() : LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // 너비
            LinearLayout.LayoutParams.WRAP_CONTENT  // 높이
        ).also {
            val margin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 5f, resources.displayMetrics).toInt()
            it.setMargins(0, margin, 0, margin) // 좌, 상, 우, 하
        }
    }

    private fun setButtonColor(
        selectNumberButton: Button,
        selectTextButton: Button,
        emergencyFlag: Boolean
    ) {
        if (!emergencyFlag) {
            selectNumberButton.background = getDrawable(R.drawable.arr_botton_press)
            selectTextButton.background = getDrawable(R.drawable.bpm_border)
        } else {
            selectNumberButton.background = getDrawable(R.drawable.emergency_press)
            selectTextButton.background = getDrawable(R.drawable.emergency_border)
        }
        prevNumberButton?.background = getDrawable(R.drawable.arr_button_normal)
        prevTextButton?.background = getDrawable(R.drawable.home_bottom_button)

        setButtonEnable(selectNumberButton, selectTextButton)

        prevNumberButton = selectNumberButton
        prevTextButton = selectTextButton
    }

    private fun setButtonOnClickListener(
        numberButton: Button,
        writeTimeButton: Button,
        address: String?
    ) {
        val emergencyFlag = address != null
        val writeTime = writeTimeButton.text.toString()

        numberButton.setOnClickListener {
            showArrData(writeTime, address)
            setButtonColor(numberButton, writeTimeButton, emergencyFlag)
        }

        writeTimeButton.setOnClickListener {
            showArrData(writeTime, address)
            setButtonColor(numberButton, writeTimeButton, emergencyFlag)
        }
    }

    private fun getDrawable(color: Int): Drawable? {
        return ContextCompat.getDrawable(requireActivity(), color)
    }

    private fun setButtonEnable(numberButton: Button, writeTimeButton: Button) {
        numberButton.isEnabled = false
        writeTimeButton.isEnabled = false
        prevNumberButton?.isEnabled = true
        prevTextButton?.isEnabled = true
    }




    /**
     * Show Arr Data
     **/
    private fun showArrData(writeTime: String, address: String?) {
        val emergencyFlag = address != null

        LoadingBar.showNotTouchableLoadingBar(binding.progressBar, requireActivity(), true)
        LineChartController.initChart(lineChart)

        lifecycleScope.launch {
            getArrData(writeTime)?.let { result ->
                val ecgPacket = fromJsonToData(result).first().ecgpacket
                val (entries, status) = getEntryData(ecgPacket, address)

                LineChartController.showLineChart(
                    lineChart,
                    LineChartController.getLineDataSet(entries, getGraphColor(emergencyFlag), true),
                    null,
                    LineChartType.HRV
                )

                setTextView(emergencyFlag, status)

                LineChartController.resetZoom(lineChart)

            } ?: run {
                MyToast.showToast(requireContext(), resources.getString(R.string.noArrData))
            }

            LoadingBar.showNotTouchableLoadingBar(binding.progressBar, requireActivity(), false)
        }
    }

    private suspend fun getArrData(writeTime: String): String? {
        UserProfileManager.getUserProfile()?.email?.let { email ->
            val mapParam: MutableMap<String, String> = hashMapOf(
                "eq" to email,
                "startDate" to writeTime,
                "endDate" to ""
            )

            RetrofitServerController.executeRequest(
                RetrofitServerController.HTTPMethod.GET,
                RetrofitServerController.EndPoint.GET_ARR_DATA.endPoint,
                null,
                mapParam
            )?.let { result ->
                return if (result.contains("result = 0")) null
                else result
            }
        } ?: run { return null }
    }

    private fun fromJsonToData(result: String): Array<ArrDataItem> {
        return GsonSingleton.instance.fromJson(result, Array<ArrDataItem>::class.java)
    }

    private fun getEntryData(
        ecgPacket: String, address: String?
    ): Pair<MutableMap<String, ArrayList<Entry>>, ArrayList<String>> {
        val splitEcgData = ecgPacket.split(",")
        val entries: MutableMap<String, ArrayList<Entry>> = mutableMapOf()
        val status = ArrayList<String>()

        if (splitEcgData.first().contains(":")) {
            // Arr Data first(): (hh:mm:ss)
            var idx = 0f
            splitEcgData.forEachIndexed { _, value ->
                value.toFloatOrNull()?.let {
                    val conversionEcg = PeakController.conversionEcgData(it)
                    entries.getOrPut("arr") { arrayListOf() }.add(Entry(idx, conversionEcg))
                    idx++
                } ?: run { status.add(value) }
            }
        } else {
            // Emergency Data first(): (ecg)
            splitEcgData.forEachIndexed { index, value ->
                val conversionEcg = PeakController.conversionEcgData(value.toFloatOrNull() ?: 512f)
                entries.getOrPut("emergency") { arrayListOf() }.add(Entry(index.toFloat(), conversionEcg))
            }
            status.add(address ?: "notFound")
        }

        return Pair(entries, status)
    }

    private fun getGraphColor(flag: Boolean): Array<Int> {
        return if (flag) arrayOf(ContextCompat.getColor(requireContext(), R.color.myRed))
        else arrayOf(ContextCompat.getColor(requireContext(), R.color.myBlue))
    }


    /**
     * UI
     **/
    private fun setTextView(emergencyFlag: Boolean, textList: List<String>?) {
        textList?.let { it ->
            binding.graphLayout.visibility = View.VISIBLE

            val status = if(!emergencyFlag) {
                if (it.size >= 4) it.drop(2) else it
            } else it
            
            getValueText(emergencyFlag, status).let { values ->
                if (emergencyFlag) setEmergencyUI(values)
                else setArrUI(values)
            }

        } ?: run {
            binding.graphLayout.visibility = View.INVISIBLE
        }
    }

    private fun getValueText(emergencyFlag: Boolean, textList: List<String>?) : Pair<String?, String?> {
        return if(!emergencyFlag) {
            // Arr
            val status = getStatus(textList?.first())
            val type = getArrType(textList?.last())
            Pair(status, type)
        } else Pair(textList?.first(), null)    // Emergency
    }

    private fun getStatus(status: String?): String {
        return when(status) {
            STATUS_EXERCISE -> resources.getString(R.string.exercise)
            STATUS_SLEEP -> resources.getString(R.string.sleep)
            else -> resources.getString(R.string.rest)
        }
    }

    private fun getArrType(type: String?): String {
        return when(type) {
            TYPE_FAST_ARR -> resources.getString(R.string.typeFastArr)
            TYPE_SLOW_ARR -> resources.getString(R.string.typeSlowArr)
            TYPE_HEAVY_ARR -> resources.getString(R.string.typeHeavyArr)
            else -> resources.getString(R.string.typeArr)
        }
    }

    private fun setArrUI(values: Pair<String?, String?>) {
        setVisibility(true)
        binding.typeValue.text = values.second
        binding.typeLabel.text = resources.getString(R.string.arrType)
        binding.statusValue.text = values.first
        binding.statusLabel.text = resources.getString(R.string.arrState)
    }
    
    private fun setEmergencyUI(values: Pair<String?, String?>) {
        setVisibility(false)
        binding.typeValue.text = values.first
        binding.typeLabel.text = resources.getString(R.string.emergencyType)
    }

    private fun setVisibility(visible: Boolean) {
        binding.statusValue.visibility = if(visible) View.VISIBLE else View.INVISIBLE
        binding.statusLabel.visibility = if(visible) View.VISIBLE else View.INVISIBLE
    }

    private fun removeAllViews() {
        binding.numberBtnLayout.removeAllViews()
        binding.writeTimeBtnLayout.removeAllViews()
    }
}