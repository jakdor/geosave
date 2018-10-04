/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.elements

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import androidx.preference.PreferenceDialogFragmentCompat
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView

import com.jakdor.geosave.R

/**
 * The Dialog for the [PreferenceSeekBar].
 */
class SeekBarPreferenceDialogFragmentCompat: PreferenceDialogFragmentCompat() {

    /**
     * The TimePicker widget
     */
    private lateinit var seekBar: SeekBar
    private lateinit var valueDisplay: TextView
    private var minVal: Int = 0
    private var maxVal: Int = 100

    /**
     * Obtain min/max progress for SeekBar from Bundle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        minVal = arguments?.getInt(MIN_KEY) ?: 0
        maxVal = arguments?.getInt(MAX_KEY) ?: 100
    }

    /**
     * Dialog binding, listeners setup
     */
    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        seekBar = view.findViewById(R.id.dialog_seekBar)
        seekBar.max = maxVal
        if(Build.VERSION.SDK_INT >= 26) {
            seekBar.min = minVal
        }
        seekBar.setOnSeekBarChangeListener(SeekBarListener())

        val closeButton = view.findViewById<Button>(R.id.dialog_seekBar_close_button)
        closeButton.setOnClickListener { dismiss() }

        valueDisplay = view.findViewById(R.id.dialog_seekBar_value)
        valueDisplay.text = seekBar.progress.toString()

        var value: Int? = null
        val preference = preference
        if (preference is PreferenceSeekBar) {
            value = preference.value
        }

        if (value != null) {
            seekBar.progress = value
        }
    }

    /**
     * Save modified value
     */
    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        val value = seekBar.progress
        val preference = preference
        if (preference is PreferenceSeekBar) {
            if (preference.callChangeListener(value)) {
                preference.value = value
            }
        }

    }

    override fun onDialogClosed(positiveResult: Boolean) {}

    /**
     * Listener for displaying current value of SeekBar
     */
    private inner class SeekBarListener: SeekBar.OnSeekBarChangeListener{
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            valueDisplay.text = p1.toString()
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    companion object {

        /**
         * Creates a new Instance of the SeekBarPreferenceDialogFragment and stores the key of the
         * related Preference
         * @param key of the Preference
         * @return new Instance of the SeekBarPreferenceDialogFragment
         */
        fun newInstance(key: String, minVal: Int, maxVal: Int):
                SeekBarPreferenceDialogFragmentCompat {
            val fragment = SeekBarPreferenceDialogFragmentCompat()
            val bundle = Bundle()
            bundle.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            bundle.putInt(MIN_KEY, minVal)
            bundle.putInt(MAX_KEY, maxVal)
            fragment.arguments = bundle
            return fragment
        }

        const val MIN_KEY = "min"
        const val MAX_KEY = "max"
    }
}
