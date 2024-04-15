package com.library.KTLibrary.base

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView

object MyEditText {
    fun hideKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusedView = activity.currentFocus
        if (currentFocusedView is EditText) {
            currentFocusedView.clearFocus()
            inputMethodManager.hideSoftInputFromWindow(currentFocusedView.windowToken, 0)
        }
    }

    fun closeKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun hideKeyboardFrom(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm!!.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun keyboardUp(editText: EditText, scrollView: ScrollView) {
        scrollView.postDelayed({
            val location = IntArray(2)
            editText.getLocationOnScreen(location)
            val scrollY = location[1] - scrollView.height / 2 // EditText 위치 기반 스크롤 계산
            scrollView.smoothScrollTo(0, scrollY)
        }, 100)
    }
}