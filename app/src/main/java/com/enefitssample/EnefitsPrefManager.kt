package com.enefitssample

import android.content.Context
import android.content.SharedPreferences
import com.enefitssample.EnefitsPrefManager
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
        sharedPreferences.edit().remove(key).commit()
    }

    fun setBooleanPreference(key: String, value: Boolean) {
        val sharedPreferences: SharedPreferences = ctx.getSharedPreferences(
            SHARED_PREF_NAME, Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBooleanPreference(key: String): Boolean {
        val sharedPreferences: SharedPreferences = ctx.getSharedPreferences(
            SHARED_PREF_NAME, Context.MODE_PRIVATE
        )
        return sharedPreferences.getBoolean(key, false)
    }

    companion object {
        private var mInstance: EnefitsPrefManager? = null
        private const val SHARED_PREF_NAME = "EnefitsPrefManager"
        const val KEY_API = "api_key"
        @Synchronized
        fun getInstance(context: Context): EnefitsPrefManager? {
            if (mInstance == null) {
                mInstance = EnefitsPrefManager(context)
            }
            return mInstance
        }
    }
}