package com.jakdor.geosave.ui.gpsinfo

import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.mvp.BasePresenter

class GpsInfoPresenter(view: GpsInfoContract.GpsInfoView,
                       private val gpsInfoRepository: GpsInfoRepository):
        BasePresenter<GpsInfoContract.GpsInfoView>(view),
        GpsInfoContract.GpsInfoPresenter{

    override fun start() {
        super.start()
        gpsInfoRepository.test()
    }

    override fun stop() {
        super.stop()
    }

    override fun pause() {
        super.pause()
    }

    override fun resume() {
        super.resume()
    }
}