package com.library.KTLibrary.model

class GetArrListModel {
    // Getter
    // Setter
    @set:JvmName("Writetime")
    @get:JvmName("Writetime")
    var writetime: String? = null

    @set:JvmName("Address")
    @get:JvmName("Address")
    var address: String? = null // !null == emergency

    fun getWritetime(): String? {
        return writetime
    }

    fun setWritetime(writetime: String?) {
        this.writetime = writetime
    }

    fun getAddress(): String? {
        return address
    }

    fun setAddress(address: String?) {
        this.address = address
    }
}