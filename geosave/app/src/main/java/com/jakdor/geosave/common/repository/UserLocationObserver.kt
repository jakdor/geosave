package com.jakdor.geosave.common.repository

import com.jakdor.geosave.common.model.UserLocation
import io.reactivex.observers.DisposableObserver

/**
 * Abstract base class for [UserLocation] observers implementations
 */
abstract class UserLocationObserver : DisposableObserver<UserLocation>()