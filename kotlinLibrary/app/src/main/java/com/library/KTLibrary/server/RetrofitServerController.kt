package com.library.KTLibrary.server

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

object RetrofitServerController {
    var baseServerEnable = true
    var networkAvailable = true

    enum class HTTPMethod {
        GET, POST
    }

    enum class EndPoint(val endPoint: String) {
        // GET
        GET_VERSION("appversion/getVersion"),
        GET_BPM_DATA("mslbpm/api_getdata"),
        GET_ARR_LIST("mslecgarr/arrWritetime?"),
        GET_ARR_DATA("mslecgarr/arrWritetime?"),
        GET_HOURLY_DATA("mslecgday/day"),
        GET_FIND_ID("msl/findID?"),
        GET_CHECK_LOGIN("msl/CheckLogin"),
        GET_CHECK_ID_DUP("msl/CheckIDDupe"),
        GET_PROFILE("msl/Profile"),
        GET_SEND_SMS("mslSMS/sendSMS"),
        GET_CHECK_SMS("mslSMS/checkSMS"),
        GET_CHECK_PHONE_NUMBER("msl/checkPhone?"),
        GET_APP_KEY("msl/appKey?"),

        // POST
        POST_SEND_LOG("app_log/api_getdata"),
        POST_SEND_BLE_LOG("app_ble/api_getdata"),
        POST_SET_PROFILE("msl/api_getdata"),
        POST_SEND_TEN_SECOND_DATA("mslbpm/api_data"),
        POST_SEND_HOURLY_DATA("mslecgday/api_getdata"),
        POST_ECG_DATA("mslecgbyte/api_getdata"),
        POST_ARR_DATA("mslecgarr/api_getdata"),
        POST_SET_GUARDIAN("mslparents/api_getdata"),
        POST_SET_APP_KEY("msl/api_getdata")
    }

    suspend fun <T> executeRequest(
        type: HTTPMethod,
        url: String,
        requestData: T? = null,
        queryMap: Map<String, String>? = null
    ): String? = withContext(Dispatchers.IO) {
        if (!networkAvailable) return@withContext ErrorType.IO_ERROR.message

        try {
            val call: Call<String> = when (type) {

                HTTPMethod.GET -> {
                    if (queryMap != null) {
                        if (baseServerEnable) ApiService.instance.getData(url, queryMap)
                        else ApiService.spareInstance.getData(url, queryMap)
                    } else throw IllegalArgumentException("GET request requires a non-null queryMap")
                }

                HTTPMethod.POST -> {
                    if (requestData != null) {
                        val json = GsonSingleton.instance.toJson(requestData)
                        val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

                        if (baseServerEnable) ApiService.instance.postData(url, requestBody)
                        else ApiService.spareInstance.postData(url, requestBody)

                    } else throw IllegalArgumentException("POST request requires non-null requestData")
                }
            }

            val response = call.execute()

            if (response.isSuccessful)
                return@withContext response.body()
            else return@withContext RESPONSE_FALSE

        } catch (e: Exception) {
            Log.e("$type", e.toString())
            return@withContext when (e) {
                is SocketTimeoutException -> ErrorType.TIMEOUT.message
                is IOException -> ErrorType.IO_ERROR.message
                else -> ErrorType.ERROR.message
            }
        }
    }
}