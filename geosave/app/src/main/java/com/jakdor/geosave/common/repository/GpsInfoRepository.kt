package com.jakdor.geosave.common.repository

import android.content.Context
import com.jakdor.geosave.common.model.ElevationApi
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.utils.RxSchedulersFacade
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.util.*
import javax.inject.Singleton
import kotlin.concurrent.timerTask

/**
 * Repository for [UserLocation] objects managed by reactive stream
 */
@Singleton
class GpsInfoRepository(private val context: Context,
                        private val schedulers: RxSchedulersFacade,
                        private val restApiRepository: RestApiRepository,
                        private val sharedPreferencesRepository: SharedPreferencesRepository){

    private val disposable = CompositeDisposable()
    private val stream: BehaviorSubject<UserLocation> = BehaviorSubject.create()
    private lateinit var savedLocation: UserLocation

    private var apiCallsOn = false
    private var apiCallLockFlag = true
    private var apiCallInProgress = false
    private var apiFailFlag = false
    private var apiElevation: Int = -999
    private var apiTimer: Timer? = null
    private var apiCallInterval: Long = 20

    /**
     * Call for elevation update from Rest API service
     */
    fun callForElevationUpdate(data: UserLocation){
        apiCallInProgress = true
        if(restApiRepository.checkNetworkStatus(context))
            disposable.add(restApiRepository.getElevationApi(data.latitude, data.longitude)
                    .subscribeOn(schedulers.computation())
                    .observeOn(schedulers.computation())
                    .subscribe(
                            { result -> handleElevationUpdate(result) },
                            { error -> handleApiCallError(error) }
                    )
            )
    }

    /**
     * Handle api call fail/timeout
     */
    fun handleApiCallError(error: Throwable){
        apiCallInProgress = false
        apiFailFlag = true
        Timber.e(error)
    }

    /**
     * Handle received [ElevationApi] object
     */
    fun handleElevationUpdate(elevationApi: ElevationApi){
        if(elevationApi.elevationApiResults.isNotEmpty()) {
            apiCallInProgress = false
            val elevation = elevationApi.elevationApiResults[0].elevation
            if(elevation != null){
                apiElevation = elevation
                apiFailFlag = false

                next(savedLocation) //forced update

                Timber.i("Got elevation update from API: %d", elevation)
            } else {
                apiFailFlag = true
                Timber.e("No elevation data available for location / json parsing error")
            }
        }
    }

    /**
     * Starts timer for api calls
     */
    fun startElevationApiCalls(){
        if(sharedPreferencesRepository.getBoolean(SharedPreferencesRepository.altApi, false)){
            apiCallInterval = sharedPreferencesRepository.getInt(
                    SharedPreferencesRepository.altApiFreq, 20).toLong()
            apiTimer = Timer()
            apiTimer?.scheduleAtFixedRate(
                    timerTask { if(!apiCallInProgress) apiCallLockFlag = false },
                    0, apiCallInterval * 1000)
            apiCallsOn = true
            Timber.i("Started elevationApi timer")
        }
    }

    /**
     * Stops timer for api calls
     */
    fun stopElevationApiCalls(){
        if(apiTimer != null){
            apiTimer!!.cancel()
            apiTimer!!.purge()
        }
        apiCallsOn = false
        apiCallInProgress = false
        apiElevation = -999
        Timber.i("Stopped elevationApi timer")
    }

    /**
     * Check if preferences changed by user
     */
    fun checkForPreferencesChange(){
        if(!sharedPreferencesRepository.getBoolean(
                        SharedPreferencesRepository.altApi, false) && apiCallsOn){
            stopElevationApiCalls() //api calls off
        } else if(sharedPreferencesRepository.getBoolean(
                        SharedPreferencesRepository.altApi, false) && !apiCallsOn){
            startElevationApiCalls() //api calls on
        } else if(sharedPreferencesRepository.getBoolean(
                        SharedPreferencesRepository.altApi, false) &&
                sharedPreferencesRepository.getInt(
                        SharedPreferencesRepository.altApiFreq, 20).toLong() != apiCallInterval){
            stopElevationApiCalls() //api call interval changed
            startElevationApiCalls()
        }
    }

    /**
     * post new [UserLocation] to [BehaviorSubject] stream,
     * replace altitude with apiElevation if available
     */
    fun next(userLocation: UserLocation){
        savedLocation = userLocation

        if(!apiCallLockFlag) {
            callForElevationUpdate(userLocation)
            apiCallLockFlag = true
        }

        if(!apiFailFlag && apiElevation != -999 && apiCallsOn) {
            userLocation.altitude = apiElevation.toDouble()
            userLocation.elevationApi = true
        }

        stream.onNext(userLocation)
    }

    /**
     * Subscribe to [BehaviorSubject] stream
     */
    fun subscribe(observer: UserLocationObserver): Disposable{
        return stream.subscribeOn(schedulers.computation()).subscribeWith(observer)
    }

    /**
     * Clear CompositeDisposable
     */
    fun clearDisposable(){
        disposable.clear()
    }
}