/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.repository

import android.app.Activity
import android.content.Context
import android.support.v7.preference.PreferenceManager
import com.jakdor.geosave.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesRepository @Inject constructor(val context: Context) {

    init {
        locationUnits = context.getString(R.string.pref_location_units_key)
        altUnits = context.getString(R.string.pref_alt_units_key)
        accUnits = context.getString(R.string.pref_acc_units_key)
        speedUnits = context.getString(R.string.pref_speed_units_key)
        altApi = context.getString(R.string.pref_alt_api_key)
        altApiFreq = context.getString(R.string.pref_alt_api_freq_key)
        shareFull = context.getString(R.string.pref_share_full_key)
    }

    /**
     * Initialize preferences before user opens [com.jakdor.geosave.ui.preferences.PreferencesFragment]
     */
    fun loadInitialPrefs(){
        PreferenceManager.setDefaultValues(context, R.xml.pref_general, false)
    }

    private var sharedPreferences = context.getSharedPreferences(
            context.packageName + "_preferences", Activity.MODE_PRIVATE)

    fun save(key: String, value: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String, default: Int): Int {
        return sharedPreferences.getInt(key, default)
    }

    fun save(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    //weird, api 28 getString() with default value can still return null, bug?
    fun getString(key: String, default: String): String {
        return sharedPreferences.getString(key, default)!!
    }

    fun getBoolean(key: String, default: Boolean): Boolean{
        return sharedPreferences.getBoolean(key, default)
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
        lateinit var altUnits: String
        lateinit var accUnits: String
        lateinit var speedUnits: String
        lateinit var altApi: String
        lateinit var altApiFreq: String
        lateinit var shareFull: String
    }
}