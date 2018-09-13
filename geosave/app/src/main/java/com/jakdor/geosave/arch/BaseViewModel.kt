/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.arch

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.jakdor.geosave.utils.RxSchedulersFacade

import io.reactivex.disposables.CompositeDisposable

/**
 * Abstract base ViewModel
 */
abstract class BaseViewModel(application: Application,
                             protected val rxSchedulersFacade: RxSchedulersFacade):
        AndroidViewModel(application) {

    protected var disposable = CompositeDisposable()

    val loadingStatus = MutableLiveData<Boolean>()

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}
