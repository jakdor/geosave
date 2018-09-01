package com.jakdor.geosave.ui.preferences

import android.app.AlertDialog
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.widget.Toast
import com.jakdor.geosave.R
import com.jakdor.geosave.di.InjectableFragment
import com.jakdor.geosave.ui.main.MainActivity
import javax.inject.Inject

class PreferencesFragment: PreferenceFragmentCompat(), InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: PreferencesViewModel? = null

    private lateinit var signInLogin: Preference
    private lateinit var logout: Preference
    private lateinit var deleteAccount: Preference

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
        observeAccountOption()
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
     * Observe [PreferencesViewModel] account option [MutableLiveData]
     */
    fun observeAccountOption(){
        viewModel?.accountOption?.observe(this, Observer {
            handleAccountOption(it)
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

    /**
     * Handle user picked account option preference
     */
    fun handleAccountOption(option: Int?){
        if(option != null){
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