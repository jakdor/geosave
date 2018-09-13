/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.elements

import android.content.Context
import android.content.res.TypedArray
import android.support.v7.preference.DialogPreference
import android.util.AttributeSet

import com.jakdor.geosave.R

/**
 * Custom preference with SeekBar dialog implemented in [SeekBarPreferenceDialogFragmentCompat]
 */
class PreferenceSeekBar @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null,
            defStyleAttr: Int = R.attr.preferenceStyle, defStyleRes: Int = defStyleAttr):
        DialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    /**
     * Saves the value to the SharedPreferences
     */
    var value: Int = 0
        set(`val`) {
            field = `val`
            persistInt(`val`)
        }

    /**
     * Called when a Preference is being inflated and the default value attribute needs to be read
     */
    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a!!.getInt(index, 0)
    }

    /**
     * Returns the layout resource that is used as the content View for the dialog
     */
    override fun getDialogLayoutResource(): Int {
        return R.layout.dialog_pref_slider
    }

    /**
     * Implement this to set the initial value of the Preference.
     */
    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        value = if (restorePersistedValue) getPersistedInt(this.value) else defaultValue as Int
    }
}
