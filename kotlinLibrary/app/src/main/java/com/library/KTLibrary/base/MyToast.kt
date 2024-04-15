package com.library.KTLibrary.base

import android.content.Context
import android.widget.Toast
import com.library.KTLibrary.server.checkError

object MyToast {
    private var toast: Toast? = null

    fun showToast(context: Context, body: String) {
        toast?.cancel()
        toast = Toast.makeText(context, body, Toast.LENGTH_SHORT).apply {
            show()
        }
    }
}