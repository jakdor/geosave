package com.jakdor.geosave.di

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

    //abstract fun mainViewModel(): MainViewModel
}