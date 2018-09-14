/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.di

import com.jakdor.geosave.ui.gpsinfo.GpsInfoViewModel
import com.jakdor.geosave.ui.locations.LocationsViewModel
import com.jakdor.geosave.ui.map.MapViewModel
import com.jakdor.geosave.ui.preferences.PreferencesViewModel
import dagger.Subcomponent

/**
 * ViewModelFactory Dagger setup interface - App SubComponent
 * Called by [com.jakdor.geosave.arch.ViewModelFactory]
 */
@Subcomponent
interface ViewModelSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): ViewModelSubComponent
    }

    fun gpsInfoViewModel(): GpsInfoViewModel
    fun mapViewModel(): MapViewModel
    fun preferencesViewModel(): PreferencesViewModel
    fun locationsViewModel(): LocationsViewModel
}