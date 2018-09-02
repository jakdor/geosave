package com.jakdor.geosave.di

import com.jakdor.geosave.ui.gpsinfo.GpsInfoViewModel
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
}