package com.jakdor.geosave.main

import com.jakdor.geosave.mvp.BasePresenter

class MainPresenter(view: MainContract.MainView):
        BasePresenter<MainContract.MainView>(view),
        MainContract.MainPresenter