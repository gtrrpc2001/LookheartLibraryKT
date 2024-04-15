package com.library.KTLibrary.server

import com.google.gson.annotations.SerializedName

class UserProfile {
    // Gson 응답 파싱
    @SerializedName("eq")
    var id: String? = null

    @SerializedName("eqname")
    var name: String? = null

    @SerializedName("email")
    var email: String? = null

    @SerializedName("userphone")
    var phone: String? = null

    @SerializedName("sex")
    var gender: String? = null

    @SerializedName("height")
    var height: String? = null

    @SerializedName("weight")
    var weight: String? = null

    @SerializedName("age")
    var age: String? = null

    @SerializedName("birth")
    var birthday: String? = null

    @SerializedName("signupdate")
    var joinDate: String? = null

    @SerializedName("sleeptime")
    var bedTime: String? = null

    @SerializedName("uptime")
    var wakeUpTime: String? = null

    @SerializedName("bpm")
    var bpm: String? = null

    @SerializedName("step")
    var step: String? = null

    @SerializedName("distanceKM")
    var distance: String? = null

    @SerializedName("calexe")
    var activityCalorie: String? = null

    @SerializedName("cal")
    var calorie: String? = null

    @SerializedName("alarm_sms")
    var ecgFlag: String? = null

    @SerializedName("differtime")
    var checkLogin: String? = null

    @SerializedName("phone")
    var guardian: String? = null
}