package com.library.KTLibrary.server

class UserProfileManager {
    private var instance: UserProfileManager? = null

    private var userProfile: UserProfile? = null

    fun getInstance(): UserProfileManager? {
        if (instance == null) {
            instance = UserProfileManager()
        }
        return instance
    }

    fun getUserProfile(): UserProfile? {
        return userProfile
    }

    fun setUserProfile(myUserProfile: UserProfile?) {
        userProfile = myUserProfile
    }
}