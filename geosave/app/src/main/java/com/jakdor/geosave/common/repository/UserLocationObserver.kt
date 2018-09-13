/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.repository

import com.jakdor.geosave.common.model.UserLocation
import io.reactivex.observers.DisposableObserver

/**
 * Abstract base class for [UserLocation] observers implementations
 */
abstract class UserLocationObserver : DisposableObserver<UserLocation>()