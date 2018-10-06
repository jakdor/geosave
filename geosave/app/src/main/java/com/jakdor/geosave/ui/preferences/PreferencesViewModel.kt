/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.preferences

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.jakdor.geosave.R
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper
import com.jakdor.geosave.utils.RxSchedulersFacade
import javax.inject.Inject

class PreferencesViewModel @Inject
constructor(application: Application,
            rxSchedulersFacade: RxSchedulersFacade,
            private val firebaseAuthWrapper: FirebaseAuthWrapper):
        BaseViewModel(application, rxSchedulersFacade){

    private val app = application
    val hidePreferences = MutableLiveData<ArrayList<String>>()
    val accountOption = MutableLiveData<Int>()

    /**
     * Hide unnecessary options based on current app state/configuration
     */
    fun updateAvailableOptions(){
        val hideKeyArray = ArrayList<String>()

        if(firebaseAuthWrapper.isLoggedIn()){
            if(firebaseAuthWrapper.isAnonymous()){
                hideKeyArray.add(app.getString(R.string.pref_logout_key))
            } else{
                hideKeyArray.add(app.getString(R.string.pref_sign_in_login_key))
            }
        } else {
            hideKeyArray.add(app.getString(R.string.pref_logout_key))
            hideKeyArray.add(app.getString(R.string.pref_delete_account_key))
        }

        hidePreferences.postValue(hideKeyArray)
    }

    /**
     * Handle click on Sign-in/Login option
     */
    fun onSignInLoginClicked(): Boolean{
        accountOption.postValue(0) //lunch sign-in/login intent
        return true
    }

    /**
     * Handle click on logout option
     */
    fun onLogoutClicked(): Boolean{
        firebaseAuthWrapper.logout()
        updateAvailableOptions()
        accountOption.postValue(1) //display logout toast
        return true
    }

    /**
     * Handle click on delete account option
     */
    fun onDeleteAccountClicked(): Boolean{
        accountOption.postValue(2) //display confirmation dialog
        return true
    }

    /**
     * Delete account and login as anonymous
     */
    fun deleteAccount(){
        firebaseAuthWrapper.deleteAccount(recreate = true)
    }
}