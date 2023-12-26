package com.library.KTLibrary.server

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitServerManager {

    @set:JvmName("Email")
    @get:JvmName("Email")
    var email: String? = null

//    private val BASE_URL = "http://121.152.22.85:40080/" // Real Address

    private val BASE_URL = "http://121.152.22.85:40081/"; // TEST Address
    private var instance: RetrofitServerManager? = null
    private var apiService: RetrofitService? = null


    @Synchronized
    fun getInstance(): RetrofitServerManager? {
        if (instance == null) instance = RetrofitServerManager
        return instance
    }

    var okHttpClient = OkHttpClient.Builder()
        .connectTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun initializeApiService() {
        if (apiService == null) {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            apiService = retrofit.create(
                RetrofitService::class.java
            )
        }
    }

    interface APICallback<T> {
        fun onSuccess(result: T)
        fun onFailure(e: Exception?)
    }

    interface ServerTaskCallback {
        fun onSuccess(result: String?)
        fun onFailure(e: Exception?)
    }

    fun getArrList(startDate: String?, endDate: String?, callback: ServerTaskCallback) {
        try {
            val mapParam: MutableMap<String, Any?> = HashMap()
            mapParam["eq"] = email
            mapParam["startDate"] = startDate
            mapParam["endDate"] = endDate

            // API 호출
            getArrListFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e) // 콜백 호출
        }
    }

    fun getArrListFromAPI(data: Map<String, Any?>, callback: ServerTaskCallback) {
        initializeApiService()
        val call = apiService!!.getArrList(
            data["eq"].toString(),
            data["startDate"].toString(),
            data["endDate"].toString()
        )
        executeCall(call, object : APICallback<String?> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun getArrData(date: String?, callback: ServerTaskCallback) {
        try {
            val mapParam: MutableMap<String, Any?> = HashMap()
            mapParam["eq"] = email
            mapParam["date"] = date

            // API 호출
            getArrDataFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e) // 콜백 호출
        }
    }

    fun getArrDataFromAPI(data: Map<String, Any?>, callback: ServerTaskCallback) {
        initializeApiService()
        val call = apiService!!.getArrData(data["eq"].toString(), data["date"].toString())
        executeCall(call, object : APICallback<String?> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun updatePWD(email: String, pw: String, callback: ServerTaskCallback) {
        Thread {
            try {
                val mapParam: MutableMap<String, Any> = HashMap()
                mapParam["kind"] = "updatePWD"
                mapParam["eq"] = email
                mapParam["password"] = pw
                // API 호출
                updatePWDFromAPI(mapParam, callback)
            } catch (e: Exception) {
            }
        }.start()
    }

    fun updatePWDFromAPI(data: Map<String, Any>?, callback: ServerTaskCallback) {
        initializeApiService()
        val call = apiService!!.updatePWD(data)
        executeCall(call, object : APICallback<String?> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun findID(name: String, phoneNumber: String, birthday: String, callback: ServerTaskCallback) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["name"] = name
            mapParam["phoneNumber"] = phoneNumber
            mapParam["birthday"] = birthday

            // API 호출
            findIDFromAPIMap(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun findIDFromAPIMap(data: Map<String, Any>, callback: ServerTaskCallback) {
        initializeApiService()
        val call = apiService!!.findID(
            data["name"].toString(),
            data["phoneNumber"].toString(),
            data["birthday"].toString()
        )
        executeCall(call, object : APICallback<String?> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun setGuardian(
        email: String,
        timezone: String,
        writetime: String,
        phone: ArrayList<String?>,
        callback: ServerTaskCallback
    ) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["eq"] = email
            mapParam["timezone"] = timezone
            mapParam["writetime"] = writetime
            mapParam["phones"] = phone

            // API 호출
            setGuardianFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun setGuardianFromAPI(data: Map<String, Any>?, callback: ServerTaskCallback) {
        initializeApiService()
        val call = apiService!!.setGuardian(data)
        executeCall(call, object : APICallback<String?> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun loginTask(email: String, pw: String, callback: ServerTaskCallback) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["eq"] = email
            mapParam["password"] = pw

            // API 호출
            loginTaskFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun loginTaskFromAPI(loginData: Map<String, Any>, callback: ServerTaskCallback) {
        initializeApiService()
        val call =
            apiService!!.checkLogin(loginData["eq"].toString(), loginData["password"].toString())
        executeCall(call, object : APICallback<String?> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun checkIdTask(email: String, callback: ServerTaskCallback) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["kind"] = "checkIDDupe"
            mapParam["eq"] = email

            // API 호출
            checkIDFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun checkIDFromAPI(loginData: Map<String, Any>, callback: ServerTaskCallback) {
        initializeApiService()
        Log.e("loginData", loginData["eq"].toString())
        val call = apiService!!.checkID(loginData["eq"].toString())
        executeCall(call, object : APICallback<String?> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun setSignupData(
        email: String,
        pw: String,
        name: String,
        gender: String,
        height: String,
        weight: String,
        age: String,
        birthday: String,
        callback: ServerTaskCallback
    ) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["kind"] = "checkReg"
            mapParam["eq"] = email
            mapParam["password"] = pw
            mapParam["eqname"] = name
            mapParam["email"] = email
            mapParam["phone"] = "01012345678"
            mapParam["sex"] = gender
            mapParam["height"] = height
            mapParam["weight"] = weight
            mapParam["age"] = age
            mapParam["birth"] = birthday
            mapParam["sleeptime"] = "23"
            mapParam["uptime"] = "7"
            mapParam["bpm"] = "90"
            mapParam["step"] = "3000"
            mapParam["distanceKM"] = "5"
            mapParam["calexe"] = "500"
            mapParam["cal"] = "3000"
            mapParam["alarm_sms"] = "0"
            mapParam["differtime"] = "0"

            // API 호출
            setProfileFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun setProfile(
        email: String,
        name: String,
        number: String?,
        gender: String,
        height: String,
        weight: String,
        age: String,
        birthday: String,
        sleep: String?,
        wakeup: String?,
        targetBpm: String?,
        targetStep: String?,
        targetDistance: String?,
        targetCal: String?,
        targetECal: String?,
        callback: ServerTaskCallback
    ) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["kind"] = "setProfile"
            mapParam["eq"] = email
            mapParam["eqname"] = name
            mapParam["email"] = email
            mapParam["phone"] = "01012345678"
            mapParam["sex"] = gender
            mapParam["height"] = height
            mapParam["weight"] = weight
            mapParam["age"] = age
            mapParam["birth"] = birthday
            mapParam["sleeptime"] = "23"
            mapParam["uptime"] = "7"
            mapParam["bpm"] = "90"
            mapParam["step"] = "3000"
            mapParam["distanceKM"] = "5"
            mapParam["calexe"] = "500"
            mapParam["cal"] = "3000"
            mapParam["alarm_sms"] = "0"
            mapParam["differtime"] = "0"

            // API 호출
            setProfileFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun setProfileFromAPI(userData: Map<String, Any>?, callback: ServerTaskCallback) {
        initializeApiService()
        val call = apiService!!.setProfile(userData)
        executeCall(call, object : APICallback<String?> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    //    public void getProfile(String email, UserDataCallback callback) {
    //
    //        try {
    //            Map<String, Object> mapParam = new HashMap<>();
    //            mapParam.put("eq", email);
    //
    //            getProfileFromAPI(mapParam, callback);  // API 호출
    //
    //        } catch (Exception e) {
    //            callback.onFailure(e);
    //        }
    //    }
    //    public void getProfileFromAPI(Map<String, Object> mapParam, UserDataCallback callback) {
    //
    //        initializeApiService();
    //
    //        Call<List<UserProfile>> call = apiService.getProfileData(mapParam.get("eq").toString());
    //
    //        executeCall(call, new APICallback<List<UserProfile>>() {
    //            @Override
    //            public void onSuccess(List<UserProfile> result) {
    //                try {
    //                    if (!result.isEmpty()) {
    //                        UserProfileManager.getInstance().setUserProfile(result.get(0));
    //                        callback.userData(UserProfileManager.getInstance().getUserProfile());  // 콜백 호출
    //                    }
    //                }catch (Exception ignored) {
    //                    callback.onFailure(ignored);
    //                }
    //            }
    //
    //            @Override
    //            public void onFailure(Exception e) {
    //                callback.onFailure(e);
    //            }
    //        });
    //    }
    fun sendTenSecondData(
        email: String,
        timezone: String,
        writeTime: String,
        bpm: Int,
        temp: Double,
        hrv: Int,
        step: Int,
        distance: Double,
        tCal: Double,
        eCal: Double,
        arrCnt: Int,
        callback: ServerTaskCallback
    ) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["kind"] = "BpmDataInsert"
            mapParam["eq"] = email
            mapParam["timezone"] = timezone
            mapParam["writetime"] = writeTime
            mapParam["bpm"] = bpm
            mapParam["hrv"] = hrv
            mapParam["cal"] = tCal
            mapParam["calexe"] = eCal
            mapParam["step"] = step
            mapParam["distanceKM"] = distance
            mapParam["arrcnt"] = arrCnt
            mapParam["temp"] = temp
            sendTenSecondDataToServer(mapParam, callback) // API 호출
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun sendTenSecondDataToServer(data: Map<String, Any>?, callback: ServerTaskCallback) {
        initializeApiService()
        val call = apiService!!.sendTenSecondData(data)
        executeCall(call, object : APICallback<String?> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun sendHourlyData(
        email: String,
        dataYear: String,
        dataMonth: String,
        dataDay: String,
        dataHour: String,
        timezone: String,
        step: String,
        distance: String,
        cal: String,
        eCal: String,
        arrCnt: String,
        callback: ServerTaskCallback
    ) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["kind"] = "calandInsert"
            mapParam["eq"] = email
            mapParam["datayear"] = dataYear
            mapParam["datamonth"] = dataMonth
            mapParam["dataday"] = dataDay
            mapParam["datahour"] = dataHour
            mapParam["ecgtimezone"] = timezone
            mapParam["step"] = step
            mapParam["distanceKM"] = distance
            mapParam["cal"] = cal
            mapParam["calexe"] = eCal
            mapParam["arrcnt"] = arrCnt
            sendHourlyDataToServer(mapParam, callback) // API 호출
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun sendHourlyDataToServer(data: Map<String, Any>?, callback: ServerTaskCallback) {
        initializeApiService()
        val call = apiService!!.sendHourlyData(data)
        executeCall(call, object : APICallback<String?> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun sendEcgData(
        email: String,
        writeTime: String,
        timezone: String,
        bpm: Int,
        ecgList: StringBuilder,
        callback: ServerTaskCallback
    ) {
        val stringEcgList = ecgList.toString()
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["kind"] = "ecgdataInsert"
            mapParam["eq"] = email
            mapParam["writetime"] = writeTime
            mapParam["ecgtimezone"] = timezone
            mapParam["bpm"] = bpm
            mapParam["ecgPacket"] = stringEcgList

            // API 호출
            sendEcgDataToServer(mapParam, callback)
        } catch (e: Exception) {
        }
    }

    //    public void sendEcgData(String email, String writeTime, String timezone, int bpm, StringBuilder ecgList, ServerTaskCallback callback) {
    //
    //        String stringEcgList = ecgList.toString();
    //
    ////        Log.i("ecg",String.valueOf(ecgList));
    //        try {
    //
    //            Map<String, Object> mapParam = new HashMap<>();
    //            mapParam.put("kind", "ecgByteInsert");
    //            mapParam.put("eq", email);
    //            mapParam.put("writetime", writeTime);
    //            mapParam.put("timezone", timezone);
    //            mapParam.put("bpm", bpm);
    //            mapParam.put("ecgPacket", stringEcgList);
    //
    //            // API 호출
    //            sendEcgDataToServer(mapParam, callback);
    //
    //        } catch (Exception e) {
    //        }
    //    }
    fun sendEcgDataToServer(data: Map<String, Any>?, callback: ServerTaskCallback) {
        initializeApiService()
        val call = apiService!!.sendEcgData(data)
        executeCall(call, object : APICallback<String?> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun sendArrData(
        email: String,
        timezone: String,
        writeTime: String,
        arrData: String,
        arrStatus: String,
        callback: ServerTaskCallback
    ) {
        Log.e("timezone", timezone)
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["kind"] = "arrEcgInsert"
            mapParam["eq"] = email
            mapParam["timezone"] = timezone
            mapParam["writetime"] = writeTime
            mapParam["ecgPacket"] = arrData
            mapParam["arrStatus"] = arrStatus

            // API 호출
            sendArrDataToServer(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun sendArrDataToServer(data: Map<String, Any>?, callback: ServerTaskCallback) {
        initializeApiService()
        val call = apiService!!.sendArrData(data)
        executeCall(call, object : APICallback<String?> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun sendEmergencyData(
        email: String,
        timezone: String,
        writeTime: String,
        address: String,
        callback: ServerTaskCallback
    ) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["kind"] = "arrEcgInsert"
            mapParam["eq"] = email
            mapParam["timezone"] = timezone
            mapParam["writetime"] = writeTime
            mapParam["ecgPacket"] = ""
            mapParam["arrStatus"] = ""
            mapParam["bodystate"] = "1"
            mapParam["address"] = address

            // API 호출
            sendEmergencyDataToServer(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun sendEmergencyDataToServer(data: Map<String, Any>?, callback: ServerTaskCallback) {
        initializeApiService()
        val call = apiService!!.sendArrData(data)
        executeCall(call, object : APICallback<String?> {
            override fun onSuccess(result: String?) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    private fun <T> executeCall(call: Call<T>?, callback: APICallback<T?>) {
        call!!.enqueue(object : Callback<T?> {
            override fun onResponse(call: Call<T?>, response: Response<T?>) {
                if (response.isSuccessful && response.body() != null) {
//                    Gson gson = new Gson();
//                    String jsonBody = gson.toJson(response.body());
//                    Log.e("callback", jsonBody);
//                    Log.e("body", (String) response.body());
                    callback.onSuccess(response.body())
                } else {
                    var errorBody: String? = ""
                    try {
                        errorBody = response.errorBody()!!.string()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    Log.e("errorBody", errorBody!!)
                    callback.onFailure(Exception("API call not successful"))
                }
            }

            override fun onFailure(call: Call<T?>, t: Throwable) {
                callback.onFailure(Exception(t))
            }
        })
    }

    fun setEmail(eq: String) {
        email = eq
    }

    fun getEmail(): String? {
        return email
    }

}