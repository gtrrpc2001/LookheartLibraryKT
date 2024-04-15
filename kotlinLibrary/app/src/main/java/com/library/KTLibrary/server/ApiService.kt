package com.library.KTLibrary.server
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object ApiService {
    private const val BASE_URL = "" // Real Address
    private const val SPARE_URL = "" // Real Spare Address

    val instance: RetrofitService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RetrofitService::class.java)
    }

    val spareInstance: RetrofitService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(SPARE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RetrofitService::class.java)
    }
}