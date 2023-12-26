package com.library.KTLibrary.model

class GetArrDataModel {

    private var ecg: String? = null

    private var arr: String? = null
    // Getter
    // Setter
    open fun getArr(): String? {
        return arr
    }

    fun setWritetime(arr: String?) {
        this.arr = arr
    }

    fun getEcg(): String? {
        return ecg
    }

    fun setAddress(ecg: String?) {
        this.ecg = ecg
    }

    fun parseToSingleDoubleArray(): Array<Double> {
        if (ecg == null || ecg!!.isEmpty()) {
            return emptyArray() // 빈 배열 반환
        }
        val arrayStrings: Array<String>
//        var totalLength = 0 // 총 길이를 저장할 변수

        // ECG 14개 데이터 사이의 세미 콜론(;) 확인
        arrayStrings =
            if (ecg!!.contains(";")) ecg!!.split("\\];\\[".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray() else ecg!!.split("\\]\\[".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()

        // 각 배열의 길이를 계산
//        for (arrayString in arrayStrings) {
//            val parts = arrayString
//                .replace("[", "")
//                .replace("]", "")
//                .replace(";", "")
//                .split(",\\s*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//            totalLength += parts.size
//        }

        // 새 배열 생성
        val mergedArray = emptyList<Double>()
//        var currentPosition = 0 // 현재 복사 위치

        // 각 배열의 요소들을 새 배열에 복사
        for (arrayString in arrayStrings) {
            val parts = arrayString.replace("[", "").replace("]", "").replace(";", "")
                .split(",\\s*".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            for (part in parts) {
//                mergedArray[currentPosition++] = part.toDouble()
                mergedArray.plus(part.toDouble())
            }
        }
        return mergedArray.toTypedArray()!!
    }
}