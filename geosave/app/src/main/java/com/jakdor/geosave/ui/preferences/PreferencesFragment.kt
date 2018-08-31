package com.jakdor.geosave.ui.preferences

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.jakdor.geosave.R

class PreferencesFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_general, rootKey)
    }
}