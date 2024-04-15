package com.library.KTLibrary.base

import android.app.Activity
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.library.KTLibrary.R

object LoadingBar {
    fun createLoadingBar(context: Context, layout: ConstraintLayout, redColor:Boolean = false): ProgressBar {
        val barColor = if (!redColor) R.color.myBlue else R.color.myRed

        val progressBar = ProgressBar(context).apply {
            id = View.generateViewId()

            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            }

            this.layoutParams = layoutParams

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val color = ContextCompat.getColor(context, barColor)
                val mode = BlendMode.SRC_IN
                indeterminateDrawable.colorFilter = BlendModeColorFilter(color, mode)
            } else {
                val color = ContextCompat.getColor(context, barColor)
                val filter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
                indeterminateDrawable.colorFilter = filter
            }
        }

        layout.addView(progressBar)

        return progressBar
    }

    fun showNotTouchableLoadingBar(progressBar: ProgressBar, activity: Activity, flag: Boolean) {
        if (flag) {
            progressBar.visibility = View.VISIBLE
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } else {
            progressBar.visibility = View.GONE
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }

    fun showLoadingBar(progressBar: ProgressBar, activity: Activity, flag: Boolean) {
        if (flag) progressBar.visibility = View.VISIBLE
        else progressBar.visibility = View.GONE
    }
}