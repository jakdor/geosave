package com.jakdor.geosave.ui.elements

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView

import com.jakdor.geosave.R

/**
 * The Dialog for the [PreferenceSeekBar].
 */
class SeekBarPreferenceDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    /**
     * The TimePicker widget
     */
    private lateinit var seekBar: SeekBar
    private lateinit var valueDisplay: TextView

    /**
     * {@inheritDoc}
     */
    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        seekBar = view.findViewById(R.id.dialog_seekBar)
        seekBar.setOnSeekBarChangeListener(SeekBarListener())

        val closeButton = view.findViewById<Button>(R.id.dialog_seekBar_close_button)
        closeButton.setOnClickListener { dismiss() }

        valueDisplay = view.findViewById(R.id.dialog_seekBar_value)

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
         * @param key The key of the related Preference
         * @return A new Instance of the SeekBarPreferenceDialogFragment
         */
        fun newInstance(key: String): SeekBarPreferenceDialogFragmentCompat {
            val fragment = SeekBarPreferenceDialogFragmentCompat()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = b

            return fragment
        }
    }
}
