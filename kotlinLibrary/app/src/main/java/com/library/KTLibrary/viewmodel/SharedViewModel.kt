package com.library.KTLibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // Arr 갱신
    @JvmField
    val arrList = MutableLiveData(ArrayList<String>())
    private val email = MutableLiveData<String>()
    private val age = MutableLiveData<String>()
    private val gender = MutableLiveData<String>()
    private val height = MutableLiveData<String>()
    private val weight = MutableLiveData<String>()
    private val sleep = MutableLiveData<String>()
    private val wakeup = MutableLiveData<String>()
    private val bpm = MutableLiveData<String>()
    private val tCal = MutableLiveData<String>()
    private val eCal = MutableLiveData<String>()
    private val step = MutableLiveData<String>()
    private val distance = MutableLiveData<String>()
    private val emergency = MutableLiveData<Boolean>()
    private val arr = MutableLiveData<Boolean>()
    private val myo = MutableLiveData<Boolean>()
    private val nonContact = MutableLiveData<Boolean>()
    private val fastarr = MutableLiveData<Boolean>()
    private val slowarr = MutableLiveData<Boolean>()
    private val irregular = MutableLiveData<Boolean>()
    private val summaryRefreshCheck = MutableLiveData<Boolean>()
    private val arrRefreshCheck = MutableLiveData<Boolean>()

    fun setEmail(myEmail: String) {
        email.value = myEmail
    }

    fun `setSummaryRefreshCheck`(check: Boolean) {
        summaryRefreshCheck.value = check
    }


    fun setArrRefreshCheck(check: Boolean) {
        arrRefreshCheck.value = check
    }

    fun getSummaryRefreshCheck(): LiveData<Boolean> {
        return summaryRefreshCheck
    }

    fun getArrRefreshCheck(): LiveData<Boolean> {
        return arrRefreshCheck
    }

    // set
    fun setAge(newText: String) {
        age.value = newText
    }

    fun setGender(newText: String) {
        gender.value = newText
    }

    fun setHeight(newText: String) {
        height.value = newText
    }

    fun setWeight(newText: String) {
        weight.value = newText
    }

    fun setSleep(newText: String) {
        sleep.value = newText
    }

    fun setWakeup(newText: String) {
        wakeup.value = newText
    }

    fun setBpm(newText: String) {
        bpm.value = newText
    }

    fun setTCalText(newText: String) {
        tCal.value = newText
    }

    fun setECalText(newText: String) {
        eCal.value = newText
    }

    fun setStep(newText: String) {
        step.value = newText
    }

    fun setDistance(newText: String) {
        distance.value = newText
    }

    fun setEmergency(check: Boolean) {
        emergency.value = check
    }

    fun setArr(check: Boolean) {
        arr.value = check
    }

    fun setMyo(check: Boolean) {
        myo.value = check
    }

    fun setNonContact(check: Boolean) {
        nonContact.value = check
    }

    fun setFastarr(check: Boolean) {
        fastarr.value = check
    }

    fun setSlowarr(check: Boolean) {
        slowarr.value = check
    }

    fun setIrregular(check: Boolean) {
        irregular.value = check
    }

    fun addArrList(arrDate: String) {
        val currentList = arrList.value
        if (currentList != null) {
            currentList.add(arrDate)
            arrList.postValue(currentList)
        }
    }

    fun removeArrList(index: Int) {
        val currentList = arrList.value
        if (currentList != null && index >= 0 && index < currentList.size) {
            currentList.removeAt(index)
            arrList.value = currentList // LiveData 업데이트
        }
    }

    fun removeAllArrList() {
        val currentList = arrList.value
        if (currentList != null) {
            currentList.clear() // 모든 요소 지우기
            arrList.value = currentList // LiveData 업데이트
        }
    }

    // get

    fun getMyEmail(): LiveData<String> {
        return email
    }

    fun getArrList(): MutableLiveData<java.util.ArrayList<String>> {
        return arrList
    }

    fun getAge(): LiveData<String> {
        return age
    }

    fun getGender(): LiveData<String> {
        return gender
    }

    fun getHeight(): LiveData<String> {
        return height
    }

    fun getWeight(): LiveData<String> {
        return weight
    }

    fun getSleep(): LiveData<String> {
        return sleep
    }

    fun getWakeup(): LiveData<String> {
        return wakeup
    }

    fun getBpm(): LiveData<String> {
        return bpm
    }

    val tCalText: LiveData<String>
        get() = tCal
    val eCalText: LiveData<String>
        get() = eCal
    val stepText: LiveData<String>
        get() = step
    val distanceText: LiveData<String>
        get() = distance

    fun getEmergency(): LiveData<Boolean> {
        return emergency
    }

    fun getArr(): LiveData<Boolean> {
        return arr
    }

    fun getMyo(): LiveData<Boolean> {
        return myo
    }

    fun getNonContact(): LiveData<Boolean> {
        return nonContact
    }

    val fastArr: LiveData<Boolean>
        get() = fastarr

    fun getSlowarr(): LiveData<Boolean> {
        return slowarr
    }

    fun getIrregular(): LiveData<Boolean> {
        return irregular
    }
}