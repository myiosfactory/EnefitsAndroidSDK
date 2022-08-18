package com.enefits.manager

import org.walletconnect.Session

interface SessionManager {
    var session: Session? // connected account session

    val peerData: Session.PeerData? // connected account info

    val isAccountConnected: Boolean // check account connected or not

    fun connectAccount() // connect wallet account

    fun disconnect() // disconnect wallet account

    fun getConnectedAccount() // load already connected account

    fun requestHandshake() : Boolean// Starts an intent that performs the handshake between your DApp and a Wallet.


}