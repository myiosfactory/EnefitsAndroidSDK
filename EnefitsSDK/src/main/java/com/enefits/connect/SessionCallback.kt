package com.enefits.connect

interface SessionCallback {
    fun onApproved()
    fun onConnected()
    fun onClosed()
    fun onResponse(response : String,id : String)
}