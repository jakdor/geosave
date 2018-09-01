package com.jakdor.geosave.ui.preferences

import android.app.Application
import android.arch.lifecycle.MutableLiveData
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
        return true
    }

    /**
     * Handle click on logout option
     */
    fun onLogoutClicked(): Boolean{
        return true
    }

    /**
     * Handle click on delete account option
     */
    fun onDeleteAccountClicked(): Boolean{
        return true
    }

}