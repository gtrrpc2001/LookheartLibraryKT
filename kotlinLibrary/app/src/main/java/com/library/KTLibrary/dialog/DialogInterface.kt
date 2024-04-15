package com.library.KTLibrary.dialog


interface ActionDialogListener {
    fun onConfirm()
}


interface ActionCancelDialogListener {
    fun onConfirm()
    fun onCancel()
}

interface EditTextDialogListener {
    fun onConfirm(text: String?)
    fun onCancel()
}