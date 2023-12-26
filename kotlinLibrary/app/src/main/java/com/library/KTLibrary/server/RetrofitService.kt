package com.library.KTLibrary.server

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RetrofitService {
    // --------------------------- GET --------------------------- //
    @GET("msl/findID?")
    fun findID(
        @Query("eqname") name: String?,
        @Query("phone") phoneNumber: String?,
        @Query("birth") birthday: String?
    ): Call<String?>?

    // checkLogin
    @GET("msl/CheckLogin")
    fun checkLogin(@Query("empid") empid: String?, @Query("pw") pw: String?): Call<String?>?

    // checkID
    @GET("msl/CheckIDDupe")
    fun checkID(@Query("empid") empid: String?): Call<String?>?

    // getProfile
    @GET("msl/Profile")
    fun getProfileData(@Query("empid") empid: String?): Call<List<UserProfile?>?>?

    @GET("/mslecgarr/arrWritetime?")
    fun getArrList(
        @Query("eq") eq: String?,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): Call<String?>?

    @GET("mslecgarr/arrPreEcgData?")
    fun getArrData(@Query("eq") eq: String?, @Query("date") date: String?): Call<String?>?

    // --------------------------- POST --------------------------- //
    // tenSecondData
    @POST("mslbpm/api_data")
    fun sendTenSecondData(@Body data: Map<String, Any>?): Call<String?>?

    // hourlyData
    @POST("mslecgday/api_getdata")
    fun sendHourlyData(@Body data: Map<String, Any>?): Call<String?>?

    // EcgData
    @POST("mslecg/api_getdata")
    fun sendEcgData(@Body data: Map<String, Any>?): Call<String?>?

    //    @POST("mslecgbyte/api_getdata")
    //    Call<String> sendEcgData(@Body Map<String, Object> data);
    // ArrData
    @POST("mslecgarr/api_getdata")
    fun sendArrData(@Body data: Map<String, Any>?): Call<String?>?

    // setProfile And signup
    @POST("msl/api_getdata")
    fun setProfile(@Body data: Map<String, Any>?): Call<String?>?

    // guardian
    @POST("mslparents/api_getdata")
    fun setGuardian(@Body data: Map<String, Any>?): Call<String?>?

    // updatePWD
    @POST("msl/api_getdata")
    fun updatePWD(@Body data: Map<String, Any>?): Call<String?>?
}