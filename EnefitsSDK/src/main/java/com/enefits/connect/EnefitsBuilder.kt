package com.enefits.connect

import com.enefits.manager.SessionManager
import com.enefits.manager.SessionRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import org.walletconnect.impls.FileWCSessionStore
import org.walletconnect.impls.MoshiPayloadAdapter
import org.walletconnect.impls.OkHttpTransport
import java.io.File

class EnefitsBuilder private constructor(
    sessionManager: SessionManager
) : SessionManager by sessionManager {

    class Builder(config: EnefitsConfig, callback: SessionCallback) {
        private val sessionRepository by lazy {
            SessionRepository(
                payloadAdapter,
                storage,
                transporter,
                config,
                callback
            )
        }

        private val moshi by lazy { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }

        private val payloadAdapter by lazy { MoshiPayloadAdapter(moshi) }

        private val transporter by lazy {
            OkHttpTransport.Builder(
                OkHttpClient.Builder().build(),
                moshi
            )
        }

        private val file by lazy { File(config.context.cacheDir, "session_store.json") }

        private val storage by lazy { FileWCSessionStore(file.apply { createNewFile() }, moshi) }

        fun build() = EnefitsBuilder(
            sessionRepository
        )
    }
}