package com.enefits.manager

import android.content.Intent
import android.net.Uri
import com.enefits.connect.EnefitsConfig
import com.enefits.connect.SessionCallback
import com.google.gson.Gson
import org.komputing.khex.extensions.toNoPrefixHexString
import org.walletconnect.Session
import org.walletconnect.impls.OkHttpTransport
import org.walletconnect.impls.WCSession
import org.walletconnect.impls.WCSessionStore
import java.util.*

class SessionRepository(
    private val payloadAdapter: Session.PayloadAdapter,
    private val storage: WCSessionStore,
    private val transporter: OkHttpTransport.Builder,
    private val enefitsConfig: EnefitsConfig,
    private val callback: SessionCallback
) : SessionManager, Session.Callback {

    private var isAppInstalledOrNot = false

    private var config = buildConfig()

    private val wcUri get() = config.toWCUri()

    override var session: Session? = null

    override val isAccountConnected
        get() = storage.list().firstOrNull() != null

    override val peerData: Session.PeerData?
        get() = storage.list()[0].peerData

    override fun connectAccount() {
        config = buildConfig()
        session = buildSession().apply {
            addCallback(this@SessionRepository)
            offer()
        }
    }

    override fun disconnect() {
        session?.kill()
        session?.clearCallbacks()
        storage.clean()
        session = null
    }

    override fun getConnectedAccount() {
        storage.list().firstOrNull()?.let {
            config = Session.Config(
                it.config.handshakeTopic,
                it.config.bridge,
                it.config.key,
                it.config.protocol,
                it.config.version
            )
            session = WCSession(
                it.config,
                payloadAdapter,
                storage,
                transporter,
                enefitsConfig.clientMeta
            ).apply { addCallback(this@SessionRepository) }
        }
    }

    override fun requestHandshake(): Boolean {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(wcUri)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            enefitsConfig.context.startActivity(intent)
            isAppInstalledOrNot = true
        } catch (e: Exception) {
            if (e.message.toString().contains("No Activity found")) {
                isAppInstalledOrNot = false
            }
        }
        return isAppInstalledOrNot
    }


    override fun onMethodCall(call: Session.MethodCall) {
        when (call) {
            is Session.MethodCall.Response -> {
                if (call.result != null){
                    callback.onResponse(Gson().toJson(call.result),call.id.toString())
                }
            }
        }
    }

    override fun onStatus(status: Session.Status) {
        when (status) {
            is Session.Status.Approved -> {
                callback.onApproved()
            }

            is Session.Status.Connected -> {
                callback.onConnected()
            }

            is Session.Status.Closed -> {
                callback.onClosed()
            }

        }
    }


    private fun buildConfig(): Session.Config {
        val handshakeTopic = UUID.randomUUID().toString()
        val key = ByteArray(32).also { Random().nextBytes(it) }.toNoPrefixHexString()
        return Session.Config(handshakeTopic, enefitsConfig.bridgeUrl, key, "wc", 1)
    }

    private fun buildSession() = WCSession(
        config.toFullyQualifiedConfig(),
        payloadAdapter,
        storage,
        transporter,
        enefitsConfig.clientMeta
    )

    private fun WCSessionStore.clean() = list().forEach { remove(it.config.handshakeTopic) }
}