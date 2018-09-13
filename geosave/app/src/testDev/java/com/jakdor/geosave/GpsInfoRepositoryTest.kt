/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave

import android.content.Context
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.common.repository.RestApiRepository
import com.jakdor.geosave.common.repository.SharedPreferencesRepository
import com.jakdor.geosave.common.repository.UserLocationObserver
import com.jakdor.geosave.utils.RxSchedulersFacade
import com.jakdor.geosave.utils.TestUtils
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.*

class GpsInfoRepositoryTest{

    @get:Rule
    private val thrown = ExpectedException.none()

    private val context = mock<Context>()
    private val rxSchedulersFacade = mock<RxSchedulersFacade> {
        on { computation() }.thenReturn(Schedulers.computation())
    }
    private val restApiRepository = mock<RestApiRepository>()
    private val sharedPreferencesRepository = mock<SharedPreferencesRepository>()

    private val gpsInfoRepository = GpsInfoRepository(
            context, rxSchedulersFacade, restApiRepository, sharedPreferencesRepository)

    /**
     * Integration test for stream object handling
     */
    @Test
    fun streamTest(){
        val random = Random()
        val userLocation = UserLocation(random.nextDouble(), random.nextDouble(),
                random.nextDouble(), random.nextFloat(), TestUtils.randomString(),
                random.nextFloat(), random.nextFloat(), false)

        val testObserver = TestObserver(userLocation)
        gpsInfoRepository.subscribe(testObserver)

        gpsInfoRepository.next(userLocation)
    }

    inner class TestObserver(private val expectedObj: UserLocation): UserLocationObserver(){

        override fun onNext(t: UserLocation) {
            Assert.assertEquals(expectedObj, t)
        }

        override fun onError(e: Throwable) {}
        override fun onComplete() {}
    }

}