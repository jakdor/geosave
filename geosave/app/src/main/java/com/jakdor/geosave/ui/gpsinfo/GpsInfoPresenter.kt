package com.jakdor.geosave.ui.gpsinfo

import com.jakdor.geosave.mvp.BasePresenter

class GpsInfoPresenter(view: GpsInfoContract.GpsInfoView):
        BasePresenter<GpsInfoContract.GpsInfoView>(view),
        GpsInfoContract.GpsInfoPresenter