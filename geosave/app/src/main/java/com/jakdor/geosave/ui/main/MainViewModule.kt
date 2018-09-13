/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.main

import dagger.Binds
import dagger.Module

@Module
abstract class MainViewModule {

    @Binds
    abstract fun provideMainView(mainActivity: MainActivity): MainContract.MainView
}