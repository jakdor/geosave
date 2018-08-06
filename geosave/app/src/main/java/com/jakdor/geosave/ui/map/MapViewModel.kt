package com.jakdor.geosave.ui.map

import android.app.Application
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.utils.RxSchedulersFacade
import javax.inject.Inject

class MapViewModel @Inject
constructor(application: Application,
            rxSchedulersFacade: RxSchedulersFacade):
        BaseViewModel(application, rxSchedulersFacade){

}