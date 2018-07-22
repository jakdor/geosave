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
