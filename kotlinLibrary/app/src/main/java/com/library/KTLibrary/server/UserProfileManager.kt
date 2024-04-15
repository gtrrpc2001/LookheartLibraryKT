package com.library.KTLibrary.server

object UserProfileManager {

    private var userProfile: UserProfile? = null
    private var bleIdentifier: String? = null
    private var guardianPhoneNumbers: MutableList<String> = mutableListOf()

    var deviceName: String?
        get() = bleIdentifier
        set(value) {
            bleIdentifier = value
        }

    fun getUserProfile(): UserProfile? {
        return userProfile
    }

    fun setUserProfile(userProfile: UserProfile?) {
        this.userProfile = userProfile
    }

    fun addGuardian(phoneNumber: String) {
        guardianPhoneNumbers.add(phoneNumber)
    }

    fun getGuardian(): MutableList<String> {
        return guardianPhoneNumbers
    }
}