/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.preferences

import android.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.os.Bundle
import androidx.preference.SwitchPreference
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import android.widget.Toast
import com.jakdor.geosave.R
import com.jakdor.geosave.di.InjectableFragment
import com.jakdor.geosave.ui.elements.PreferenceSeekBar
import com.jakdor.geosave.ui.main.MainActivity
import javax.inject.Inject
import com.jakdor.geosave.ui.elements.SeekBarPreferenceDialogFragmentCompat

class PreferencesFragment: PreferenceFragmentCompat(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: PreferencesViewModel? = null

    private lateinit var signInLogin: Preference
    private lateinit var logout: Preference
    private lateinit var deleteAccount: Preference
    private lateinit var altApi: SwitchPreference
    private lateinit var altApiFreq: PreferenceSeekBar

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_general, rootKey)

        signInLogin = findPreference(getString(R.string.pref_sign_in_login_key))
        signInLogin.setOnPreferenceClickListener { viewModel?.onSignInLoginClicked() ?: true }
        logout = findPreference(getString(R.string.pref_logout_key))
        logout.setOnPreferenceClickListener { viewModel?.onLogoutClicked() ?: true }
        deleteAccount = findPreference(getString(R.string.pref_delete_account_key))
        deleteAccount.setOnPreferenceClickListener { viewModel?.onDeleteAccountClicked() ?: true }
        altApi = findPreference(getString(R.string.pref_alt_api_key)) as SwitchPreference
        altApi.setOnPreferenceChangeListener { _, newValue -> hideShowAltApiFreq(newValue as Boolean) }
        altApiFreq = findPreference(getString(R.string.pref_alt_api_freq_key)) as PreferenceSeekBar
        hideShowAltApiFreq(altApi.isChecked)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(viewModel == null){
            viewModel = ViewModelProviders.of(this, viewModelFactory)
                    .get(PreferencesViewModel::class.java)
        }

        viewModel?.updateAvailableOptions()
        observeHidePreferences()
        observeAccountOption()
    }

    /**
     * Notify other parts off app of possible preferences update
     */
    override fun onPause() {
        super.onPause()
        (activity as MainActivity).presenter.notifyPossiblePreferencesChange()
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
    fun handleHidePreferences(hideList: List<String>){
        signInLogin.isVisible = true
        logout.isVisible = true
        deleteAccount.isVisible = true

        hideList.forEach {
            s: String -> findPreference(s).isVisible = false
        }
    }

    /**
     * Observe [PreferencesViewModel] account option [MutableLiveData]
     */
    fun observeAccountOption(){
        viewModel?.accountOption?.observe(this, Observer {
            handleAccountOption(it)
        })
    }

    /**
     * Handle user picked account option preference
     */
    fun handleAccountOption(option: Int){
        when(option){
            0 -> { //discard preferences fragment and lunch sign-in/login intent
                (activity as MainActivity).presenter.onGpsInfoTabClicked()
                (activity as MainActivity).firebaseSignInIntent()
            }
            1 -> { //display logout toast
                Toast.makeText(activity,
                        R.string.preferences_logged_out_toast,
                        Toast.LENGTH_SHORT).show()
            }
            2 -> { //show account delete confirmation dialog
                AlertDialog.Builder(context)
                        .setMessage(R.string.preferences_delete_account_dialog_message)
                        .setPositiveButton(R.string.preferences_delete_account_dialog_yes,
                                deleteAccountDialogListener)
                        .setNegativeButton(R.string.preferences_delete_account_dialog_no,
                                deleteAccountDialogListener)
                        .show()
            }
        }
        viewModel?.accountOption?.value = null
    }

    /**
     * Listener for account deletion confirmation dialog
     */
    private val deleteAccountDialogListener = DialogInterface.OnClickListener { dialogInterface, i ->
        when(i){
            DialogInterface.BUTTON_POSITIVE -> {
                (activity as MainActivity).presenter.onGpsInfoTabClicked()
                viewModel?.deleteAccount()
                Toast.makeText(activity,
                        R.string.preferences_delete_account_toast,
                        Toast.LENGTH_LONG).show()
            }
            DialogInterface.BUTTON_NEGATIVE -> dialogInterface.dismiss()
        }
    }

    /**
     * Hide or show altApiFreq depending on altApi state
     */
    private fun hideShowAltApiFreq(switch: Boolean): Boolean {
        when(switch){
            true -> altApiFreq.isVisible = true
            false -> altApiFreq.isVisible = false
        }
        return true
    }

    /**
     * Lunch preference dialogs
     */
    override fun onDisplayPreferenceDialog(preference: Preference) {
        var dialogFragment: DialogFragment? = null
        if (preference is PreferenceSeekBar) {
            dialogFragment = SeekBarPreferenceDialogFragmentCompat.newInstance(
                    preference.getKey(), 5, 120)
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(this.fragmentManager,
                    "android.support.v7.preference.PreferenceFragment.DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
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