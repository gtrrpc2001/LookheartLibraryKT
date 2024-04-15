package com.library.KTLibrary.server

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface RetrofitService {
    @GET
    fun getData(
        @Url url: String,
        @QueryMap data: Map<String, String>
    ): Call<String>

    @POST
    fun postData(
        @Url url: String,
        @Body requestData: RequestBody
    ): Call<String>
}