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
        setTitles()
        compositeDisposable.add(gpsInfoRepository.subscribe(DataObserver()))
    }

    override fun pause() {
        super.pause()
        compositeDisposable.clear()
    }

    /**
     * Set cards titles
     */
    fun setTitles(){
        view?.setPositionTitle(R.string.pos_title)
        view?.setAltitudeTile(R.string.altitude_title)
        view?.setAccuracyTitle(R.string.accuracy_title)
        view?.setSpeedTitle(R.string.speed_title)
        view?.setBearingTitle(R.string.bearing_title)
        view?.setProviderTitle(R.string.provider_title)
    }

    /**
     * Update meters with new data
     */
    fun update(loc: UserLocation){
        val pos = String.format(Locale.US, "%f, %f", loc.latitude, loc.longitude)
        view?.setPositionField(pos)

        if(loc.altitude != 0.0){
            val alt = String.format("%.2f m", loc.altitude)
            view?.setAltitudeField(alt)
            view?.setProviderField(R.string.provider_gps)
        } else {
            view?.setProviderField(R.string.provider_gsm)
        }

        val acc = String.format("%.2f m", loc.accuracy)
        view?.setAccuracyField(acc)

        val speed = String.format("%.2f m/s", loc.speed)
        view?.setSpeedField(speed)

        val bearing = String.format("%.2f\u00b0", loc.bearing)
        view?.setBearingField(bearing)
    }

    /**
     * [UserLocationObserver] implementation
     */
    inner class DataObserver: UserLocationObserver() {
        override fun onNext(t: UserLocation) {
            update(t)
        }

        override fun onError(e: Throwable) {
            Timber.e(e)
        }

        override fun onComplete() {}
    }
}