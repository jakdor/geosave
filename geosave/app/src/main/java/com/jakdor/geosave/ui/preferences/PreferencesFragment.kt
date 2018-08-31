package com.jakdor.geosave.ui.preferences

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.jakdor.geosave.R
import com.jakdor.geosave.di.InjectableFragment
import javax.inject.Inject

class PreferencesFragment: PreferenceFragmentCompat(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: PreferencesViewModel? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_general, rootKey)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(viewModel == null){
            viewModel = ViewModelProviders.of(this, viewModelFactory)
                    .get(PreferencesViewModel::class.java)
        }
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