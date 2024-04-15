package com.library.KTLibrary.fragment.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.library.KTLibrary.R
import com.library.KTLibrary.databinding.FragmentSummaryBinding
import com.library.KTLibrary.myEnum.BarChartType
import com.library.KTLibrary.myEnum.LineChartType
import com.library.KTLibrary.fragment.summary.barChart.BarChartFragment
import com.library.KTLibrary.fragment.summary.lineChart.LineChartFragment

class SummaryFragment : Fragment() {
    companion object {
        lateinit var lineChartType: LineChartType
        lateinit var barChartType: BarChartType
    }

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private val summaryFragmentManager: FragmentManager by lazy { childFragmentManager }
    private val fragmentList: MutableList<Fragment> by lazy { mutableListOf() }

    private var lineChartFragment: Fragment? = null
    private var barChartFragment: Fragment? = null

    private lateinit var buttonList: Array<Button>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentManager()
        setButtonList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun initShowChart() {
        binding.bpmBtn.callOnClick()
    }

    private fun setButtonList() {
        buttonList = arrayOf(binding.bpmBtn, binding.hrvBtn, binding.arrBtn, binding.calorieBtn, binding.stepBtn)
        updateBackground(binding.bpmBtn)
    }

    private fun setFragmentManager() {
        lineChartType = LineChartType.BPM

        lineChartFragment = LineChartFragment().also {
            summaryFragmentManager.beginTransaction().add(R.id.summary_frameLayout, it).commit()
            fragmentList.add(it)
        }

        // Line Chart
        binding.bpmBtn.setOnClickListener {
            showLineChartFragment(LineChartType.BPM)
            updateBackground(binding.bpmBtn)
        }

        binding.hrvBtn.setOnClickListener {
            showLineChartFragment(LineChartType.HRV)
            updateBackground(binding.hrvBtn)
        }

        // Bar Chart
        binding.arrBtn.setOnClickListener {
            showBarChartFragment(BarChartType.ARR)
            updateBackground(binding.arrBtn)

        }

        binding.calorieBtn.setOnClickListener {
            showBarChartFragment(BarChartType.CALORIE)
            updateBackground(binding.calorieBtn)
        }

        binding.stepBtn.setOnClickListener {
            showBarChartFragment(BarChartType.STEP)
            updateBackground(binding.stepBtn)
        }
    }

    private fun showLineChartFragment(type: LineChartType) {
        lineChartType = type
        lineChartFragment ?: LineChartFragment().also { lineChartFragment = it }
        lineChartFragment?.let { commitFragment(it) }
        (lineChartFragment as LineChartFragment).showChart(true)
    }

    private fun showBarChartFragment(type: BarChartType) {
        barChartType = type
        barChartFragment ?: BarChartFragment().also { barChartFragment = it }
        barChartFragment?.let { commitFragment(it) }
        (barChartFragment as BarChartFragment).showChart(true)
    }

    private fun commitFragment(selectFragment: Fragment) {
        summaryFragmentManager.beginTransaction().apply {
            fragmentList.forEach { fragment ->
                if (fragment == selectFragment) show(fragment)
                else hide(fragment)
            }
            commit()
        }

        if (!fragmentList.contains(selectFragment)) {
            summaryFragmentManager.beginTransaction().add(R.id.summary_frameLayout, selectFragment).commit()
            fragmentList.add(selectFragment)
        }
    }

    private fun updateBackground(selectButton: Button) {
        buttonList.forEach { button ->
            if (selectButton == button) {
                button.background = ContextCompat.getDrawable(requireContext(), R.drawable.summary_button_press)
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                updateImageColor(button, ContextCompat.getColor(requireContext(), R.color.white))
            } else {
                button.background = ContextCompat.getDrawable(requireContext(), R.drawable.summary_button_normal)
                button.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightGray))
                updateImageColor(button, ContextCompat.getColor(requireContext(), R.color.lightGray))
            }
        }
    }

    private fun updateImageColor(button: Button, color: Int) {
        val drawableTop = button.compoundDrawables[1]
        drawableTop?.let {
            val wrappedDrawable = DrawableCompat.wrap(it).mutate()
            DrawableCompat.setTint(wrappedDrawable, color)
            button.setCompoundDrawablesWithIntrinsicBounds(null, wrappedDrawable, null, null)
        }
    }
}