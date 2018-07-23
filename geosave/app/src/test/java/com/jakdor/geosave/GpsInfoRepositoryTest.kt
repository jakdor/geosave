package com.jakdor.geosave

import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.common.repository.UserLocationObserver
import com.jakdor.geosave.utils.RxSchedulersFacade
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

    private val rxSchedulersFacade = mock<RxSchedulersFacade> {
        on { ui() }.thenReturn(Schedulers.computation())
    }

    private val gpsInfoRepository = GpsInfoRepository(rxSchedulersFacade)

    /**
     * Integration test for stream object handling
     */
    @Test
    fun streamTest(){
        val random = Random()
        val userLocation = UserLocation(random.nextDouble(), random.nextDouble(),
                random.nextDouble(), random.nextFloat(), TestUtils.randomString(),
                random.nextFloat(), random.nextFloat())

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