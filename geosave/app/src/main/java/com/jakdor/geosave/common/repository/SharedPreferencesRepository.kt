package com.jakdor.geosave.common.repository

import android.app.Activity
import android.content.Context
import com.jakdor.geosave.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesRepository @Inject constructor(context: Context) {

    init {
        locationUnits = context.getString(R.string.pref_location_units_key)
        altAccUnits = context.getString(R.string.pref_alt_acc_units_key)
        speedUnits = context.getString(R.string.pref_speed_units_key)
    }

    private var sharedPreferences
            = context.getSharedPreferences(context.packageName + "_preferences", Activity.MODE_PRIVATE)

    fun save(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String): Int {
        return sharedPreferences.getInt(key, 0)
    }

    fun save(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String, default: String): String {
        return sharedPreferences.getString(key, default)
    }

    fun remove(key: String) {
        val editor = sharedPreferences.edit()
        editor.remove(key)
        editor.apply()
    }

    fun clearAll() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    companion object {
        const val mapTypeKey = "map_type"
        lateinit var locationUnits: String
        lateinit var altAccUnits: String
        lateinit var speedUnits: String
    }
}