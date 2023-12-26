package com.library.KTLibrary.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.library.KTLibrary.R
import com.library.KTLibrary.summary.SummaryArr
import com.library.KTLibrary.summary.SummaryBpm
import com.library.KTLibrary.summary.SummaryCal
import com.library.KTLibrary.summary.SummaryHRV
import com.library.KTLibrary.summary.SummaryStep
import com.library.KTLibrary.viewmodel.SharedViewModel

class SummaryFragment : Fragment() {
    var viewModel: SharedViewModel? = null
    private var bpm: Fragment? = null
    private var arr: Fragment? = null
    private var hrv: Fragment? = null
    private var cal: Fragment? = null
    private var step: Fragment? = null
    private lateinit var fragmentManager: FragmentManager
    private lateinit var bpmButton: LinearLayout
    private lateinit var arrButton: LinearLayout
    private lateinit var hrvButton: LinearLayout
    private lateinit var calButton: LinearLayout
    private lateinit var stepButton: LinearLayout
    private lateinit var bpmText: TextView
    private lateinit var arrText: TextView
    private lateinit var hrvText: TextView
    private lateinit var calText: TextView
    private lateinit var stepText: TextView
    private lateinit var bpmImg: ImageView
    private lateinit var arrImg: ImageView
    private lateinit var hrvImg: ImageView
    private lateinit var calImg: ImageView
    private lateinit var stepImg: ImageView

    var layouts = emptyArray<LinearLayout>()
    var textViews = emptyArray<TextView>()
    var imageViews = emptyArray<ImageView>()
    private lateinit var view: View
    private var childFragment: SummaryBpm? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_summary, container, false)

        // LinearLayout
        bpmButton = view?.findViewById(R.id.summaryBpm)!!
        arrButton = view?.findViewById(R.id.summaryArr)!!
        hrvButton = view?.findViewById(R.id.summaryHRV)!!
        calButton = view?.findViewById(R.id.summaryCal)!!
        stepButton = view?.findViewById(R.id.summaryStep)!!
        // TextView
        bpmText = view?.findViewById(R.id.summaryBpmText)!!
        arrText = view?.findViewById(R.id.summaryArrText)!!
        hrvText = view?.findViewById(R.id.summaryHRVText)!!
        calText = view?.findViewById(R.id.summaryCalText)!!
        stepText = view?.findViewById(R.id.summaryStepText)!!
        // ImageView
        bpmImg = view?.findViewById(R.id.summaryBpmImg)!!
        arrImg = view?.findViewById(R.id.summaryArrImg)!!
        hrvImg = view?.findViewById(R.id.summaryHRVImg)!!
        calImg = view?.findViewById(R.id.summaryCalImg)!!
        stepImg = view?.findViewById(R.id.summaryStepImg)!!

        layouts = arrayOf(bpmButton, arrButton, hrvButton, calButton, stepButton)
        textViews = arrayOf(bpmText, arrText, hrvText, calText, stepText)
        imageViews = arrayOf(bpmImg, arrImg, hrvImg, calImg, stepImg)

        // Start Fragment
        bpm = SummaryBpm()
        childFragment = bpm as SummaryBpm?
        fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager?.beginTransaction()
        fragmentTransaction?.replace(R.id.summaryFrame, bpm as SummaryBpm)
        fragmentTransaction?.addToBackStack(null)
        fragmentTransaction?.commit()
        refresh()
        bpmButton?.setOnClickListener(View.OnClickListener {
            setColor(bpmButton, bpmText, bpmImg)
            if (bpm == null) {
                bpm = SummaryBpm()
                fragmentManager?.beginTransaction()?.add(R.id.summaryFrame, bpm as SummaryBpm)?.commit()
            }
            if (bpm != null) fragmentManager?.beginTransaction()?.show(bpm as SummaryBpm)?.commit()
            if (arr != null) fragmentManager?.beginTransaction()?.hide(arr!!)?.commit()
            if (hrv != null) fragmentManager?.beginTransaction()?.hide(hrv!!)?.commit()
            if (cal != null) fragmentManager?.beginTransaction()?.hide(cal!!)?.commit()
            if (step != null) fragmentManager?.beginTransaction()?.hide(step!!)?.commit()
        })
        arrButton?.setOnClickListener(View.OnClickListener {
            setColor(arrButton, arrText, arrImg)
            if (arr == null) {
                arr = SummaryArr()
                fragmentManager?.beginTransaction()?.add(R.id.summaryFrame, arr as SummaryArr)?.commit()
            }
            if (bpm != null) fragmentManager?.beginTransaction()?.hide(bpm as SummaryBpm)?.commit()
            if (arr != null) fragmentManager?.beginTransaction()?.show(arr!!)?.commit()
            if (hrv != null) fragmentManager?.beginTransaction()?.hide(hrv!!)?.commit()
            if (cal != null) fragmentManager?.beginTransaction()?.hide(cal!!)?.commit()
            if (step != null) fragmentManager?.beginTransaction()?.hide(step!!)?.commit()
        })
        hrvButton?.setOnClickListener(View.OnClickListener {
            setColor(hrvButton, hrvText, hrvImg)
            if (hrv == null) {
                hrv = SummaryHRV()
                fragmentManager?.beginTransaction()?.add(R.id.summaryFrame, hrv as SummaryHRV)?.commit()
            }
            if (bpm != null) fragmentManager?.beginTransaction()?.hide(bpm as SummaryBpm)?.commit()
            if (arr != null) fragmentManager?.beginTransaction()?.hide(arr!!)?.commit()
            if (hrv != null) fragmentManager?.beginTransaction()?.show(hrv!!)?.commit()
            if (cal != null) fragmentManager?.beginTransaction()?.hide(cal!!)?.commit()
            if (step != null) fragmentManager?.beginTransaction()?.hide(step!!)?.commit()
        })
        calButton?.setOnClickListener(View.OnClickListener {
            setColor(calButton, calText, calImg)
            if (cal == null) {
                cal = SummaryCal()
                fragmentManager?.beginTransaction()?.add(R.id.summaryFrame, cal as SummaryCal)?.commit()
            }
            if (bpm != null) fragmentManager?.beginTransaction()?.hide(bpm as SummaryBpm)?.commit()
            if (arr != null) fragmentManager?.beginTransaction()?.hide(arr!!)?.commit()
            if (hrv != null) fragmentManager?.beginTransaction()?.hide(hrv!!)?.commit()
            if (cal != null) fragmentManager?.beginTransaction()?.show(cal!!)?.commit()
            if (step != null) fragmentManager?.beginTransaction()?.hide(step!!)?.commit()
        })
        stepButton?.setOnClickListener(View.OnClickListener {
            setColor(stepButton, stepText, stepImg)
            if (step == null) {
                step = SummaryStep()
                fragmentManager?.beginTransaction()?.add(R.id.summaryFrame, step as SummaryStep)?.commit()
            }
            if (bpm != null) fragmentManager?.beginTransaction()?.hide(bpm as SummaryBpm)?.commit()
            if (arr != null) fragmentManager?.beginTransaction()?.hide(arr!!)?.commit()
            if (hrv != null) fragmentManager?.beginTransaction()?.hide(hrv!!)?.commit()
            if (cal != null) fragmentManager?.beginTransaction()?.hide(cal!!)?.commit()
            if (step != null) fragmentManager?.beginTransaction()?.show(step!!)?.commit()
        })
        return view
    }

    fun refresh() {
        viewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        viewModel!!.getSummaryRefreshCheck()
            .observe(viewLifecycleOwner, object : Observer<Boolean?> {
                override fun onChanged(check: Boolean?) {
                    if (arr != null && check == true) {
                        val fragmentTransaction = fragmentManager!!.beginTransaction()
                        fragmentTransaction.remove(arr!!)
                        arr = SummaryArr()
                        fragmentTransaction.add(R.id.summaryFrame, arr as SummaryArr, "newFragmentTag")
                        fragmentTransaction.commit()
                    }
                    if (hrv != null && check == true) {
                        val fragmentTransaction = fragmentManager!!.beginTransaction()
                        fragmentTransaction.remove(hrv!!)
                        hrv = SummaryHRV()
                        fragmentTransaction.add(R.id.summaryFrame, hrv as SummaryHRV, "newFragmentTag")
                        fragmentTransaction.commit()
                    }
                    if (cal != null && check == true) {
                        val fragmentTransaction = fragmentManager!!.beginTransaction()
                        fragmentTransaction.remove(cal!!)
                        cal = SummaryCal()
                        fragmentTransaction.add(R.id.summaryFrame, cal as SummaryCal, "newFragmentTag")
                        fragmentTransaction.commit()
                    }
                    if (step != null && check == true) {
                        val fragmentTransaction = fragmentManager!!.beginTransaction()
                        fragmentTransaction.remove(step!!)
                        step = SummaryStep()
                        fragmentTransaction.add(R.id.summaryFrame, step as SummaryStep, "newFragmentTag")
                        fragmentTransaction.commit()
                    }
                    if (bpm != null && check == true) {
                        val fragmentTransaction = fragmentManager!!.beginTransaction()
                        fragmentTransaction.remove(bpm!!)
                        bpm = SummaryBpm()
                        fragmentTransaction.add(R.id.summaryFrame, bpm as SummaryBpm, "newFragmentTag")
                        fragmentTransaction.commit()
                    }
                    setColor(bpmButton, bpmText, bpmImg)
                }
            })
    }

    fun setColor(layout: LinearLayout?, textView: TextView?, imageView: ImageView?) {
        BackgroundColor(layout)
        TextColor(textView)
        ImageColor(imageView)
    }

    fun BackgroundColor(layout: LinearLayout?) {
        // 클릭한 레이아웃의 색상 변경
        layout!!.background = ContextCompat.getDrawable(requireActivity(), R.drawable.summary_button_press)

        // 클릭하지 않은 레이아웃의 색상을 다른 색으로 변경
        for (otherLayout in layouts) {
            if (otherLayout !== layout) {
                otherLayout!!.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.summary_botton_normal)
            }
        }
    }

    @SuppressLint("ResourceType")
    fun TextColor(text: TextView?) {
        text!!.setTextColor(Color.WHITE)
        for (otherText in textViews) {
            if (otherText !== text) {
                otherText!!.setTextColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.lightGray,
                        null
                    )
                )
            }
        }
    }

    fun ImageColor(image: ImageView?) {
        image!!.setColorFilter(Color.WHITE)
        for (otherImageView in imageViews) {
            if (otherImageView !== image) {
                otherImageView!!.setColorFilter(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.lightGray,
                        null
                    )
                )
            }
        }
    }
}