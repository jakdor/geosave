package com.jakdor.geosave.di

import android.content.Context
import com.jakdor.geosave.App
import dagger.Module
import dagger.Provides

/**
 * App-wide dependencies injections
 */
@Module
class AppModule {

    @Provides
    fun provideContext(app: App): Context{
        return app.applicationContext
    }
}