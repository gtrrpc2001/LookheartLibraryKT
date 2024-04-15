package com.library.KTLibrary.server

object ServerDataClass {

    data class SignUp(
        val kind: String,
        val eq: String,
        val email: String,
        val password: String? = null,
        val eqname: String,
        val phone: String,
        val sex: String,
        val height: String,
        val weight: String,
        val age: String,
        val birth: String,
        val sleeptime: String,
        val uptime: String,
        val bpm: String,
        val step: String,
        val distanceKM: String,
        val calexe: String,
        val cal: String,
        val alarm_sms: String,
        val differtime: String
    )

    data class SetProfile(
        val kind: String,
        val eq: String,
        val email: String,
        val eqname: String,
        val phone: String,
        val sex: String,
        val height: String,
        val weight: String,
        val age: String,
        val birth: String,
        val sleeptime: String,
        val uptime: String,
        val bpm: String,
        val step: String,
        val distanceKM: String,
        val calexe: String,
        val cal: String,
        val alarm_sms: String,
        val differtime: String
    )

    data class SetGuardianPhoneNumber(
        val eq:  String,
        val timezone: String,
        val writetime: String,
        val phones: ArrayList<String>
    )

    data class ChangePassword(
        val kind: String,
        val eq: String,
        val password: String
    )

    data class SendHourlyData(
        val kind: String,
        val eq: String,
        val datayear: String,
        val datamonth: String,
        val dataday: String,
        val datahour: String,
        val ecgtimezone: String,
        val step: Int,
        val distanceKM: Int,
        val cal: Int,
        val calexe: Int,
        val arrcnt: Int
    )

    data class TenSecondData(
        val kind: String,
        val eq: String,
        val timezone: String,
        val writetime: String,
        val bpm: Int,
        val hrv: Int,
        val cal: Int,
        val calexe: Int,
        val step: Int,
        val distanceKM: Int,
        val arrcnt: Int,
        val temp: Double,
        val battery: Int
    )

    data class ArrData(
        val kind: String,
        val eq: String,
        val writetime: String,
        val ecgPacket: String
    )

    data class EcgData(
        val kind: String,
        val eq: String,
        val writetime: String,
        val timezone: String,
        val bpm: Int,
        val ecgPacket: ArrayList<Int>
    )

    data class EmergencyData(
        val kind: String,
        val eq: String,
        val timezone: String,
        val writetime: String,
        val ecgPacket: String,
        val arrStatus: String,
        val bodystate: String,
        val address: String
    )

    data class SendLog(
        val eq: String,
        val writetime: String,
        val gubun: String,
        val activity: String
    )

    data class SendBleLog(
        val eq: String,
        val phone: String,
        val writetime: String,
        val timezone: String,
        val activity: String,
        val serial: String
    )

    data class SetLoginFlag(
        val kind: String,
        val eq: String,
        val differtime: String
    )

    data class SetAppKey(
        val kind: String,
        val eq: String,
        val appKey: String
    )

    data class BpmData(
        val idx: String,
        val eq: String,
        val writeDateTime: String,
        val writeDate: String,
        val writeTime: String,
        val timezone: String,
        val bpm: Float?,
        val temp: Float?,
        val hrv: Float?
    ) {
        companion object {
            fun changeFormat(data: String): BpmData? {
                val parts = data.split("|")
                if (parts.size < 7) return null

                val splitDate = parts[2].split(" ")
                if (splitDate.size < 2) return null

                return BpmData(
                    idx = parts[0],
                    eq = parts[1],
                    writeDateTime = parts[2],
                    writeDate = splitDate[0],
                    writeTime = splitDate[1],
                    timezone = parts[3],
                    bpm = parts[4].toFloatOrNull(),
                    temp = parts[5].toFloatOrNull(),
                    hrv = parts[6].toFloatOrNull()
                )
            }
        }
    }

    data class HourlyData(
        val eq: String?,
        val writeDateTime: String?,
        val writeDate: String?,
        val writeTime: String?,
        var step: Float?,
        var distance: Float?,
        var calorie: Float?,
        var activityCalorie: Float?,
        var arrCnt: Float?
    ) {
        companion object {
            fun changeFormat(data: String): HourlyData? {
                val parts = data.split("|")
                if (parts.size < 12) return null

                val splitDateTime = parts[1].split(" ")
                if (splitDateTime.size < 2) return null

                return HourlyData(
                    eq = parts[0],
                    writeDateTime = parts[1],
                    writeDate = splitDateTime[0],
                    writeTime = splitDateTime[1],
                    step = parts[7].toFloatOrNull(),
                    distance = parts[8].toFloatOrNull(),
                    calorie = parts[9].toFloatOrNull(),
                    activityCalorie = parts[10].toFloatOrNull(),
                    arrCnt = parts[11].toFloatOrNull()
                )
            }
        }
    }

    fun setProfile(profile: SetProfile): SetProfile {
        UserProfileManager.getUserProfile()?.name = profile.eqname
        UserProfileManager.getUserProfile()?.phone = profile.phone
        UserProfileManager.getUserProfile()?.gender = profile.sex
        UserProfileManager.getUserProfile()?.height = profile.height
        UserProfileManager.getUserProfile()?.weight = profile.weight
        UserProfileManager.getUserProfile()?.age = profile.age
        UserProfileManager.getUserProfile()?.birthday = profile.birth
        UserProfileManager.getUserProfile()?.bedTime = profile.sleeptime
        UserProfileManager.getUserProfile()?.wakeUpTime = profile.uptime

        UserProfileManager.getUserProfile()?.bpm = profile.bpm
        UserProfileManager.getUserProfile()?.step = profile.step
        UserProfileManager.getUserProfile()?.distance = profile.distanceKM
        UserProfileManager.getUserProfile()?.activityCalorie = profile.calexe
        UserProfileManager.getUserProfile()?.calorie = profile.cal

        UserProfileManager.getUserProfile()?.ecgFlag = profile.alarm_sms
        UserProfileManager.getUserProfile()?.checkLogin = profile.differtime
        return profile
    }

}