package com.jakdor.geosave.ui.preferences

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.jakdor.geosave.R

class PreferencesFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_general, rootKey)
    }

    companion object {
        const val CLASS_TAG = "PreferencesFragment"

        fun newInstance(): PreferencesFragment{
            val bundle = Bundle()
            val fragment = PreferencesFragment()
            fragment.arguments = bundle
            fragment.retainInstance = true
            return fragment
        }
    }
}