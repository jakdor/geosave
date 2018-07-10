package com.jakdor.geosave.ui.gpsinfo

import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.common.repository.UserLocationObserver
import com.jakdor.geosave.mvp.BasePresenter
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import java.util.*

class GpsInfoPresenter(view: GpsInfoContract.GpsInfoView,
                       private val gpsInfoRepository: GpsInfoRepository):
        BasePresenter<GpsInfoContract.GpsInfoView>(view),
        GpsInfoContract.GpsInfoPresenter{

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun start() {
        super.start()
    }

    override fun stop() {
        super.stop()
    }

    override fun resume() {
        super.resume()
        view?.setPositionTextView(R.string.value_unknown)
        compositeDisposable.add(gpsInfoRepository.subscribe(DataObserver()))
    }

    override fun pause() {
        super.pause()
        compositeDisposable.clear()
    }

    /**
     * [UserLocationObserver] implementation
     */
    inner class DataObserver: UserLocationObserver() {
        override fun onNext(t: UserLocation) {
            val pos = String.format(Locale.US, "%f, %f", t.latitude, t.longitude)
            view?.setPositionTextView(pos)
        }

        override fun onError(e: Throwable) {
            Timber.e(e)
        }

        override fun onComplete() {}
    }
}