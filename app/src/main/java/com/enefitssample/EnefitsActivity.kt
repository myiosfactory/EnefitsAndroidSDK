package com.enefitssample

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.enefits.connect.Enefits
import com.enefitssample.databinding.ActivityEnefitsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import org.json.JSONObject


class EnefitsActivity : AppCompatActivity(), Enefits.EnefitsSessionCallback {
    private var progress: ProgressDialog? = null
    private lateinit var apiKey: String
    private lateinit var binding: ActivityEnefitsBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityEnefitsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()


        apiKey = EnefitsPrefManager.getInstance(this@EnefitsActivity)
            ?.getStringPref(EnefitsPrefManager.KEY_API).toString()

        if (apiKey.isNotEmpty()) {
            binding.edtApiKey.setText("$apiKey")
        }

        eventClickListener()
    }

    private fun eventClickListener() {

        binding.btnInit.setOnClickListener {
            hideKeyboard(it)
            checkSdkInit(EnefitsProcess.INIT)
        }

        binding.btnConnect.setOnClickListener {
            hideKeyboard(it)
            connectWallet()
        }

        binding.btnDisConnect.setOnClickListener {
            hideKeyboard(it)
            disConnectWallet()
        }

        binding.btnOffers.setOnClickListener {
            hideKeyboard(it)
            if (isNetworkConnected()) {
                getOffers()
            } else {
                Toast.makeText(
                    this@EnefitsActivity,
                    "Please check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnInitComplete.setOnClickListener {
            if (Enefits.instance!!.isInitComplete()) {
                Toast.makeText(
                    this@EnefitsActivity,
                    "     YES",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@EnefitsActivity,
                    "     NO",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnChainData.setOnClickListener {
            if (Enefits.instance!!.isInitComplete() && Enefits.instance!!.isAccountConnected()) {
                Toast.makeText(
                    this@EnefitsActivity,
                    "" + Enefits.instance!!.getChainData().toString(),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@EnefitsActivity,
                    "Connect with Enefits Sdk to get chain data",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnConnectedAccount.setOnClickListener {
            if (Enefits.instance!!.isInitComplete() && Enefits.instance!!.isAccountConnected()) {
                Toast.makeText(
                    this@EnefitsActivity,
                    "" + Enefits.instance!!.getConnectedAccount(),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@EnefitsActivity,
                    "Connect with Enefits Sdk to get connected account",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnAccountConnected.setOnClickListener {
            if (Enefits.instance!!.isAccountConnected()) {
                Toast.makeText(
                    this@EnefitsActivity,
                    "     YES",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@EnefitsActivity,
                    "     NO",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkSdkInit(enefitsProcess: EnefitsProcess) {
        apiKey = EnefitsPrefManager.getInstance(this@EnefitsActivity)
            ?.getStringPref(EnefitsPrefManager.KEY_API).toString()

        val tempApiKey = binding.edtApiKey.text.toString()

        if (apiKey.isEmpty()) {
            apiKey = tempApiKey
        }

        if (apiKey.isEmpty()) {
            Toast.makeText(this, "Please enter api key", Toast.LENGTH_SHORT).show()
            return
        } else if (!Enefits.instance!!.isInitComplete()) {
            initSdk(enefitsProcess)
        } else if (Enefits.instance!!.isInitComplete() && apiKey != tempApiKey) {
            apiKey = tempApiKey
            initSdk(enefitsProcess)
        } else {
            Toast.makeText(this@EnefitsActivity, "SDK already initialized", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun initSdk(enefitsProcess: EnefitsProcess) {
        showProgress()
        lifecycleScope.launch(Dispatchers.IO) {
            val isInit = Enefits.instance!!.init(
                this@EnefitsActivity,
                apiKey,
                getString(com.enefits.R.string.app_name),
                this@EnefitsActivity
            )

            dismissProgress()

            if (isInit) {
                withContext(Dispatchers.Main) {
                    Log.e("init", "init successfully")
                    EnefitsPrefManager.getInstance(this@EnefitsActivity)
                        ?.saveStringPref(EnefitsPrefManager.KEY_API, apiKey)
                    when (enefitsProcess) {
                        EnefitsProcess.CONNECT -> {
                            connectWallet()
                        }
                        EnefitsProcess.OFFERS -> {
                            getOffers()
                        }
                        EnefitsProcess.DISCONNECT -> {
                            disConnectWallet()
                        }
                        EnefitsProcess.INIT -> {
                            Toast.makeText(
                                this@EnefitsActivity,
                                "SDK initialized successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    EnefitsPrefManager.getInstance(this@EnefitsActivity)
                        ?.removeStringPref(EnefitsPrefManager.KEY_API)
                    Toast.makeText(this@EnefitsActivity,"Failed to initialize SDK",Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
        }
    }

    private fun connectWallet() {
        if (!Enefits.instance!!.isInitComplete() && apiKey.isNotEmpty()) {
            checkSdkInit(EnefitsProcess.CONNECT)
        } else if (!Enefits.instance!!.isInitComplete()) {
            Toast.makeText(this@EnefitsActivity, "Please initialize sdk", Toast.LENGTH_SHORT).show()
            return
        } else if (!Enefits.instance!!.isAccountConnected()) {
            val isConnectAccount = Enefits.instance!!.connectAccount()
            if (!isConnectAccount) {
                Toast.makeText(
                    this@EnefitsActivity,
                    "Please initialize sdk",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                this@EnefitsActivity,
                "Already connected with : ${Enefits.instance!!.getConnectedAccount()}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun disConnectWallet() {
        if (!Enefits.instance!!.isInitComplete() && apiKey.isNotEmpty()) {
            checkSdkInit(EnefitsProcess.DISCONNECT)
        } else if (!Enefits.instance!!.isInitComplete()) {
            Toast.makeText(this@EnefitsActivity, "Please initialize sdk", Toast.LENGTH_SHORT).show()
            return
        } else if (!Enefits.instance!!.isAccountConnected()) {
            Toast.makeText(this@EnefitsActivity, "No Active session found", Toast.LENGTH_SHORT)
                .show()
            return
        } else {
            Enefits.instance!!.disconnect()
        }
    }

    private fun getOffers() {
        if (!Enefits.instance!!.isInitComplete() && apiKey.isNotEmpty()) {
            checkSdkInit(EnefitsProcess.OFFERS)
        } else if (!Enefits.instance!!.isInitComplete()) {
            Toast.makeText(this@EnefitsActivity, "Please initialize sdk", Toast.LENGTH_SHORT).show()
            return
        } else if (!Enefits.instance!!.isAccountConnected()) {
            Toast.makeText(
                this@EnefitsActivity,
                "Connect with Enefits SDK to check offers",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else {
            showProgress()

            lifecycleScope.launch(Dispatchers.IO) {
                val response = Enefits.instance?.getOffers()

                dismissProgress()
                if (response != null && response.isNotEmpty()) {
                    val intent = Intent(this@EnefitsActivity, OfferActivity::class.java)
                    intent.putExtra("offers", response)
                    startActivity(intent)
                } else {
                    withContext(Dispatchers.IO) {
                        Toast.makeText(this@EnefitsActivity, "Offers not found", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    override fun onSessionConnected(address: String, blockchainInfo: JSONObject) {
        runOnUiThread(Runnable {
            dismissProgress()
            Toast.makeText(this, "Connected with : $address", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onSessionDisconnected() {
        runOnUiThread(Runnable {
            dismissProgress()
            Toast.makeText(this, "Disconnected ", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onAppInstalledOrNot(isAppInstalled: Boolean) {
        runOnUiThread(Runnable {
            if (!isAppInstalled) {
                Toast.makeText(
                    this,
                    "No supported wallets installed on this device",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showProgress() {
        progress = ProgressDialog(this)
        progress!!.setMessage("Please wait...")
        progress!!.show()
    }

    private fun dismissProgress() {
        if (progress != null && progress!!.isShowing) {
            progress!!.dismiss()
        }
    }

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}