package com.jakdor.geosave.ui.preferences

import android.app.Application
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.utils.RxSchedulersFacade
import javax.inject.Inject

class PreferencesViewModel @Inject
constructor(application: Application,
            rxSchedulersFacade: RxSchedulersFacade):
        BaseViewModel(application, rxSchedulersFacade){

}