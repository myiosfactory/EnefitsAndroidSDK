package com.enefits.manager

import android.content.Context
import android.content.SharedPreferences
import kotlin.jvm.Synchronized

class EnefitsPrefManager private constructor(private var ctx: Context) {
    fun saveStringPref(key: String?, value: String?) {
        val sharedPreferences: SharedPreferences = ctx.getSharedPreferences(
            SHARED_PREF_NAME, Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getStringPref(key: String?): String? {
        val sharedPreferences: SharedPreferences = ctx.getSharedPreferences(
            SHARED_PREF_NAME, Context.MODE_PRIVATE
        )
        return sharedPreferences.getString(key, "")
    }

    fun removeStringPref(key: String?) {
        val sharedPreferences: SharedPreferences = ctx.getSharedPreferences(
            SHARED_PREF_NAME, Context.MODE_PRIVATE
        )
        sharedPreferences.edit().remove(key).apply()
    }


    companion object {
        private var mInstance: EnefitsPrefManager? = null
        private const val SHARED_PREF_NAME = "EnefitsSDKPrefManager"
        const val KEY_SESSION = "session_data"
        @Synchronized
        fun getInstance(context: Context): EnefitsPrefManager? {
            if (mInstance == null) {
                mInstance = EnefitsPrefManager(context)
            }
            return mInstance
        }
    }
}