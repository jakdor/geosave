package com.jakdor.geosave.arch

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.ArrayMap
import com.jakdor.geosave.di.ViewModelSubComponent
import com.jakdor.geosave.ui.gpsinfo.GpsInfoViewModel
import com.jakdor.geosave.ui.map.MapViewModel

import java.util.concurrent.Callable

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for ViewModel instances
 */
@Suppress("UNCHECKED_CAST")
@Singleton
class ViewModelFactory
/**
 * ViewModels injected into creators ArrayMap
 * @param viewModelSubComponent Dagger SubComponent ViewModel interface
 */
@Inject
constructor(viewModelSubComponent: ViewModelSubComponent) : ViewModelProvider.Factory {

    private val creators: ArrayMap<Class<*>, Callable<out ViewModel>> = ArrayMap()

    init {
        creators[GpsInfoViewModel::class.java] = Callable { viewModelSubComponent.gpsInfoViewModel() }
        creators[MapViewModel::class.java] = Callable { viewModelSubComponent.mapViewModel() }
    }

    /**
     * creates ViewModels
     * @param modelClass (viewModelChildName).class
     * @param <T> class
     * @return ViewModel custom instance
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        var creator: Callable<out ViewModel>? = creators[modelClass]
        if (creator == null) {
            for ((key, value) in creators) {
                if (modelClass.isAssignableFrom(key)) {
                    creator = value
                    break
                }
            }
        }

        if (creator == null) {
            throw IllegalArgumentException("Model class not found$modelClass")
        }

        try {
            return creator.call() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }
}
