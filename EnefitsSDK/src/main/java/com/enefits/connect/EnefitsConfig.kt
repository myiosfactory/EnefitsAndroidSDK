package com.enefits.connect

import android.content.Context
import org.walletconnect.Session

class EnefitsConfig(
    val context: Context, // init context
    val bridgeUrl: String, // bridge Url
    private val appUrl: String,  // app url
    private val appName: String, // app name
    private val appDescription: String, // app info
    private val appIcons: List<String>? = listOf(),
) {
    internal val clientMeta by lazy { Session.PeerMeta(appUrl, appName, appDescription, appIcons) }
}