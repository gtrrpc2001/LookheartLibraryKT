package com.library.KTLibrary.server

const val REQUEST_TRUE = "true"
const val REQUEST_FALSE = "false"

const val RESPONSE_FALSE = "FALSE"
const val RESPONSE_ERROR = "ERROR"

fun requestTrue(result: String): Boolean { return result.contains(REQUEST_TRUE) }
fun requestFalse(result: String): Boolean { return result.contains(REQUEST_FALSE) }
fun checkError(result: String): Boolean { return result.contains(RESPONSE_ERROR) || result.contains(RESPONSE_FALSE) }
fun checkTimeOut(result: String): Boolean { return result.contains(ErrorType.TIMEOUT.message)}
fun checkIOError(result: String): Boolean { return result.contains(ErrorType.IO_ERROR.message)}

enum class ErrorType(val message: String) {
    TIMEOUT("Request timed out"),
    IO_ERROR("Network I/O error"),
    HTTP_ERROR("HTTP error"),
    ERROR("ERROR")
}