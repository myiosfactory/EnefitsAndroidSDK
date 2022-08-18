package com.enefits.connect

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.enefits.manager.EnefitsPrefManager
import okhttp3.OkHttpClient
import okhttp3.Request
import org.walletconnect.Session
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import org.json.JSONException

import org.json.JSONObject


class Enefits : SessionCallback {
    private lateinit var context: Context
    private lateinit var enefitsConfig: EnefitsConfig
    private lateinit var enefitsSessionCallback: EnefitsSessionCallback
    private lateinit var accountInfo: Session.PeerData

    private lateinit var appName: String
    private lateinit var apiKey: String
    private var enefitsBuilder: EnefitsBuilder? = null

    private var isSdkInit: Boolean = false
    private var connectedAccountAddress: String = ""
    private var isAccountConnected: Boolean = false
    private var isTempCheckAccount: Boolean = false

    companion object {
        @Volatile
        var instance: Enefits? = null
            get() {
                if (field == null) {
                    synchronized(Enefits::class.java) {
                        if (field == null) {
                            field = Enefits()
                        }
                    }
                }
                return field
            }
    }

    fun isInitComplete(): Boolean {
        return isSdkInit
    }

    fun isAccountConnected(): Boolean {
        if(!isInitComplete()){
            return false
        }

        if (enefitsBuilder != null && enefitsBuilder!!.isAccountConnected) {
            if (getConnectedAccount().isNotEmpty()) {
                isAccountConnected = true
            } else {
                isTempCheckAccount = true
                disconnect()
            }
        }

        return isAccountConnected
    }

    fun getConnectedAccount(): String {
        if (enefitsBuilder != null && enefitsBuilder!!.session != null) {
            connectedAccountAddress =
                enefitsBuilder!!.session!!.approvedAccounts()?.firstOrNull().toString()

            if (connectedAccountAddress == "null") {
                connectedAccountAddress = ""
            }
        }
        return connectedAccountAddress
    }

    fun getChainData(): JSONObject? {
        if (enefitsBuilder != null && enefitsBuilder!!.peerData != null) {
            accountInfo = enefitsBuilder!!.peerData!!
        }
        val chainInfoData =
            EnefitsPrefManager.getInstance(context)?.getStringPref(EnefitsPrefManager.KEY_SESSION)

        var chainDataObject: JSONObject? = null
        try {
            chainDataObject = JSONObject(chainInfoData)
        } catch (err: JSONException) {
            err.printStackTrace()
        }
        return chainDataObject
    }

    fun disconnect() {
        if (enefitsBuilder != null) {
            enefitsBuilder!!.disconnect()
            EnefitsPrefManager.getInstance(context)
                ?.removeStringPref(EnefitsPrefManager.KEY_SESSION)
        }
    }

    fun connectAccount(): Boolean {
        if (isInitComplete()) {
            if (enefitsBuilder != null) {
                enefitsBuilder!!.connectAccount()
            }
        } else {
            return false
        }
        return true
    }

    suspend fun getOffers(): String {

        var responseData = ""


        if (enefitsBuilder != null && !enefitsBuilder!!.isAccountConnected) {
            return "Connect with Enefits SDK to check offers"
        }

        val client = OkHttpClient().newBuilder()
            .build()

        val request: Request = Request.Builder()
            .url("https://api-dev.enefits.co/developer/v1/address/${getConnectedAccount()}/offers.json?api_key=${apiKey}")
            .method("GET", null)
            .build()


        return suspendCoroutine { data ->
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    responseData = e.message.toString()
                    data.resume(responseData)
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    responseData = if (response.isSuccessful) {
                        response.body!!.string()
                    } else {
                        response.message
                    }

                    data.resume(responseData)
                }
            })
        }
    }

    suspend fun init(
        context: Context,
        apiKey: String,
        appName: String,
        enefitsSessionCallback: EnefitsSessionCallback
    ): Boolean {

        this.context = context
        this.apiKey = apiKey
        this.appName = appName
        this.enefitsSessionCallback = enefitsSessionCallback

        val client = OkHttpClient().newBuilder()
            .build()
        val request: Request = Request.Builder()
            .url("https://api-dev.enefits.co/developer/v1/account.json?api_key=${apiKey}")
            .method("GET", null)
            .build()

        return suspendCoroutine { data ->
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    isSdkInit = false
                    data.resume(false)
                }

                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    if (response.isSuccessful) {
                        enefitsConfig = EnefitsConfig(
                            context = context,
                            bridgeUrl = "https://bridge.walletconnect.org",
                            appUrl = "enefits.co",
                            appName = appName,
                            appDescription = ""
                        )

                        enefitsBuilder = EnefitsBuilder.Builder(enefitsConfig, this@Enefits).build()

                        isSdkInit = true

                        fetchSession()
                        data.resume(true)
                    } else {
                        isSdkInit = false
                        data.resume(false)
                    }
                }
            })
        }
    }


    interface EnefitsSessionCallback {
        fun onSessionConnected(address: String, blockchainInfo: JSONObject) // account connected
        fun onSessionDisconnected() // account disconnected
        fun onAppInstalledOrNot(isAppInstalled: Boolean) // app installed or not callback
    }

    private fun fetchSession() {
        if (isInitComplete()) {
            if (enefitsBuilder != null) {
                enefitsBuilder!!.getConnectedAccount()
            }
        }
    }

    override fun onApproved() {
        if (getConnectedAccount() != null && getConnectedAccount().isNotEmpty()) {
            if (enefitsBuilder != null && enefitsBuilder!!.peerData != null) {
                accountInfo = enefitsBuilder!!.peerData!!
            }

            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                var blockchainObject: JSONObject? = null
                try {
                    blockchainObject = JSONObject()
                    blockchainObject.put("chainId", getChainData()?.getString("chainId"))
                    blockchainObject.put("networkId", getChainData()?.getString("networkId"))
                } catch (err: JSONException) {
                    err.printStackTrace()
                }
                enefitsSessionCallback.onSessionConnected(getConnectedAccount(), blockchainObject!!)
            }, 1000)
        }
    }

    override fun onConnected() {
        if (getConnectedAccount().isEmpty()) {
            val isAppInstalledOrNot = enefitsBuilder!!.requestHandshake()
            enefitsSessionCallback.onAppInstalledOrNot(isAppInstalledOrNot)
        }
    }

    override fun onClosed() {
        if (!isTempCheckAccount) {
            enefitsSessionCallback.onSessionDisconnected()
        }
        isTempCheckAccount = false
        isAccountConnected = false
    }

    override fun onResponse(response: String, id: String) {
        var chainDataObject: JSONObject? = null
        try {
            chainDataObject = JSONObject(response)
            chainDataObject.put("id", id)
        } catch (err: JSONException) {
            err.printStackTrace()
        }
        EnefitsPrefManager.getInstance(context)
            ?.saveStringPref(EnefitsPrefManager.KEY_SESSION, chainDataObject.toString())
    }
}