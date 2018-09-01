package com.jakdor.geosave.ui.preferences

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import com.jakdor.geosave.R
import com.jakdor.geosave.di.InjectableFragment
import javax.inject.Inject

class PreferencesFragment: PreferenceFragmentCompat(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: PreferencesViewModel? = null

    lateinit var signInLogin: Preference
    lateinit var logout: Preference
    lateinit var deleteAccount: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_general, rootKey)

        signInLogin = findPreference(getString(R.string.pref_sign_in_login_key))
        signInLogin.setOnPreferenceClickListener { viewModel?.onSignInLoginClicked() ?: true }
        logout = findPreference(getString(R.string.pref_logout_key))
        logout.setOnPreferenceClickListener { viewModel?.onLogoutClicked() ?: true }
        deleteAccount = findPreference(getString(R.string.pref_delete_account_key))
        deleteAccount.setOnPreferenceClickListener { viewModel?.onDeleteAccountClicked() ?: true }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(viewModel == null){
            viewModel = ViewModelProviders.of(this, viewModelFactory)
                    .get(PreferencesViewModel::class.java)
        }

        viewModel?.updateAvailableOptions()
        observeHidePreferences()
    }

    /**
     * Observe [PreferencesViewModel] hide preferences [MutableLiveData]
     */
    fun observeHidePreferences(){
        viewModel?.hidePreferences?.observe(this, Observer {
            handleHidePreferences(it)
        })
    }

    /**
     * Handle request to hide preferences
     */
    fun handleHidePreferences(hideList: List<String>?){
        if(hideList != null){
            signInLogin.isVisible = true
            logout.isVisible = true
            deleteAccount.isVisible = true

            hideList.forEach {
                s: String -> findPreference(s).isVisible = false
            }
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