# ENEFITS ANDROID SDK 

This guide is for anyone who wishes to integrate the Enefits Mobile SDK into an Android native app.  
Before continuing, we highly recommend reviewing the General Usage guide to familiarize yourself with general SDK workflow concepts before continuing.

You will need to generate an API key with your Enefits account before continuing with integration.

To obtain an API Key, simply register at http://enefits.co, go to Developer in the account menu in the top right section.  
From there, generate an API Key and save this for all API requests as defined below.  

We only show this once so if you lose this API Key, you’ll have to re-generate from the Developer section.


##Run The Demo

* To run a live demo, visit https://bajaar.beanstalkapp.com/enefits-mobile-sdk?ref=b-AndroidV1.0 and provide your own API key where requested.


##Download The SDK

* The Enefits Android SDK can be downloaded from --> https://bajaar.beanstalkapp.com/enefits-mobile-sdk?ref=b-AndroidV1.0


##Installation

* Add the @jitpack repository to your gradle file

```
repositories {
	...
	maven { url 'https://jitpack.io' }
}
```

* Add these dependencies in your project gradle along with aar file


```
dependencies {
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1'
    
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.1'

    // WalletConnect
    implementation 'com.github.walletconnect:kotlin-walletconnect-lib:0.9.8'
    
    implementation 'com.github.komputing:khex:1.1.2'

    // JSON
    implementation 'com.squareup.moshi:moshi-adapters:1.13.0'
    
    implementation 'com.squareup.moshi:moshi-kotlin:1.13.0'
    
    // Http
    implementation 'com.squareup.okhttp3:okhttp:5.0.0-alpha.3'
    
    implementation 'com.google.code.gson:gson:2.9.1'
    
}
```

#SDK SETUP

##Initialize The SDK

```
 /**
  * Enefits SDK has initialize.
  *
  * @return boolean
  */

 Enefits.instance.init(context, YOUR_API_KEY_HERE, YOUR_APP_NAME_HERE, sesssionCallback)
```

* **SesssionCallback** 

```
class MyActivity : AppCompatActivity(), Enefits.EnefitsSessionCallback {

override fun onSessionConnected(address: String, blockchainInfo: JSONObject) {
        runOnUiThread(Runnable {
            Toast.makeText(this, "Connected with : $address", Toast.LENGTH_SHORT).show()
        })
    }

override fun onSessionDisconnected() {
        runOnUiThread(Runnable {
            Toast.makeText(this, "Disconnected ", Toast.LENGTH_SHORT).show()
        })
    }

override fun onAppInstalledOrNot(isAppInstalled: Boolean) {
        runOnUiThread(Runnable {
            if (!isAppInstalled) {
                Toast.makeText(this,"No supported wallets installed on this device",Toast.LENGTH_SHORT).show()
            }
        })
    }
}
```




##Connect Wallet

 Display a button or call-to-action (for example: Connect Wallet) which when pressed or clicked will prompt the user to connect a wallet.  
 After the user selects a wallet and initiates a session, the returned address will be available to the Enefits SDK to check NFTs and any offers the user is eligible for.

 * This function will open a popup with a list of supported providers (eg. MetaMask, Trust Wallet, Rainbow, etc) based their apps as installed on the device.
 
 * Enefits SDK will fire the callback onSessionConnected after successfully connecting with a blockchain account. If connection fails, the callback onSessionDisconnected will be fired. 

 ```
 /**
  * Connect to a blockchain account.
  *
  * @return boolean
  */

 Enefits.instance.connectAccount()
 ```



##Get All Enefits Offers

* This method will return all Offers that the user is eligible for based on the address they provided when they connected their wallet.
  
* Based on the response from this method and the existence of a specific id value that the Mobile App is looking for, your app logic will handle accordingly.

 ```
 Enefits.instance.getOffers()
 ```



##Helper Functions

* Use the functions provided below to create a tighter integration with the Enefits SDK and manage user connectivity states.


 ```
/**
 * Check if Enefits SDK has successfully initialised.
 *
 * @return boolean
 */

 
 Enefits.instance.isInitComplete()
 ```

 ```
/**
 * Check if Enefits SDK was able to connect to a blockchain account.
 *
 * @return boolean
 */
 
 
 Enefits.instance.isAccountConnected()
 ```


 ```
/**
 * Get the address of the connected account.
 *
 * @return string
 */

 
 Enefits.instance.getConnectedAccount()
 ```


 ```
/**
 * Get the blockchain info for the connected account.
 *
 * @return jsonObject
 */

  
 Enefits.instance.getChainData()
 ```


 ```
/**
 * Disconnect from the blockchain account. On successful disconnect,
 * Enefits fires callback event onSessionDisconnected
 */

 
 Enefits.instance.disconnect()
 ```
	




