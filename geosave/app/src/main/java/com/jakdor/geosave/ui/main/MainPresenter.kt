/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.main

import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.repository.GpsInfoRepository
import com.jakdor.geosave.arch.BasePresenter
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.common.repository.CameraRepository
import com.jakdor.geosave.common.repository.ReposRepository
import com.jakdor.geosave.common.repository.ShareMessageFormatter
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper
import com.jakdor.geosave.utils.RxSchedulersFacade
import io.reactivex.disposables.CompositeDisposable
import java.io.File

class MainPresenter(view: MainContract.MainView,
                    private val gpsInfoRepository: GpsInfoRepository,
                    private val firebaseAuthWrapper: FirebaseAuthWrapper,
                    private val shareMessageFormatter: ShareMessageFormatter,
                    private val cameraRepository: CameraRepository,
                    private val reposRepository: ReposRepository,
                    private val schedulersFacade: RxSchedulersFacade):
        BasePresenter<MainContract.MainView>(view),
        MainContract.MainPresenter{

    private var currentTab = TabType.NONE
    private var backTab = TabType.NONE

    private var locationUpdates = false
    private var fallBackMode = false
    private var fallbackLocationUpdates = false

    private var compositeDisposable = CompositeDisposable()
    private lateinit var currentCameraRequestInfo: CameraRepository.CameraRequestInfo
    private var isSubscribedToCameraRequest = false
    private var isSubscribedToAddLocationRequest = false

    /**
     * Check firebase login
     */
    override fun start() {
        super.start()
        firebaseLogin(firebaseAuthWrapper.isLoggedIn())
    }

    /**
     * Load fragment on create view state
     */
    override fun create() {
        super.create()
        if(currentTab == TabType.NONE) { //first load
            view?.switchToGpsInfoFragment()
            currentTab = TabType.GPS_INFO
        } else { //presenter reattached after screen rotation
            when (currentTab) {
                TabType.GPS_INFO -> view?.switchToGpsInfoFragment()
                TabType.MAP -> view?.switchToMapFragment()
                TabType.LOCATIONS -> view?.switchToLocationsFragment()
                TabType.PREFERENCES -> view?.switchToPreferencesFragment()
                else -> {}
            }
        }

        if(!isSubscribedToCameraRequest) observeCameraRequests()
    }

    /**
     * Remove location updates on pause view state
     */
    override fun pause() {
        super.pause()
        if(locationUpdates){
            view?.stopLocationUpdates()
            gpsInfoRepository.stopElevationApiCalls()
            gpsInfoRepository.clearDisposable()
        } else if (fallbackLocationUpdates){
            view?.fallbackStopLocationUpdates()
        }
    }

    /**
     * Resume location updates on resume view state
     */
    override fun resume() {
        super.resume()
        if(locationUpdates){
            view?.gmsSetupLocationUpdates()
            gpsInfoRepository.startElevationApiCalls()
        } else if (fallbackLocationUpdates){
            fallbackStartup()
        }
    }

    /**
     * Gps info navigation icon clicked
     */
    override fun onGpsInfoTabClicked() {
        if(currentTab == TabType.PREFERENCES){ //return from preferences tab
            notifyPossiblePreferencesChange()
        }

        if(currentTab != TabType.GPS_INFO){
            view?.switchToGpsInfoFragment()
            currentTab = TabType.GPS_INFO
        }
        backTab = TabType.NONE
    }

    /**
     * Map navigation icon clicked
     */
    override fun onMapTabClicked() {
        if(currentTab == TabType.PREFERENCES){ //return from preferences tab
            notifyPossiblePreferencesChange()
        }

        if(currentTab != TabType.MAP){
            view?.switchToMapFragment()
            currentTab = TabType.MAP
        }
        backTab = TabType.NONE
    }

    /**
     * Locations navigation icon clicked
     */
    override fun onLocationsTabClicked() {
        if(currentTab == TabType.PREFERENCES){ //return from preferences tab
            notifyPossiblePreferencesChange()
        }

        if(currentTab != TabType.LOCATIONS){
            view?.switchToLocationsFragment()
            currentTab = TabType.LOCATIONS
        }
        backTab = TabType.NONE
    }

    /**
     * Add location menu option clicked
     */
    override fun onAddOptionClicked() {
        view?.lunchAddLocationDialog()

        if(reposRepository.reposListStream.hasValue()){
            loadAddLocationDialogRepoSpinner()
        } else {
            val disposable = CompositeDisposable()
            disposable.add(reposRepository.reposListStream
                    .subscribeOn(schedulersFacade.io())
                    .observeOn(schedulersFacade.ui())
                    .subscribe(
                            {
                                loadAddLocationDialogRepoSpinner()
                                disposable.clear()
                            },
                            { e -> e.printStackTrace() }
                    ))
        }
    }

    /**
     * Load AddLocationDialog Repos spinner
     */
    private fun loadAddLocationDialogRepoSpinner(){
        if(reposRepository.chosenRepositoryIndexStream.hasValue()) {
            view?.loadAddLocationDialogRepoSpinner(getReposWithPushPermissionIndexPair(),
                    reposRepository.chosenRepositoryIndexStream.value)
        } else {
            view?.loadAddLocationDialogRepoSpinner(
                    getReposWithPushPermissionIndexPair(), 0)
        }
    }

    /**
     * Return [ArrayList] of Index and [Repo] pairs to which user has push permission
     */
    fun getReposWithPushPermissionIndexPair(): ArrayList<Pair<Int, String>> {
        val repoIndexPair = arrayListOf<Pair<Int, String>>()

        if(reposRepository.reposListStream.hasValue()){
            for(i in 0 until reposRepository.reposListStream.value.size){
                val currentRepo = reposRepository.reposListStream.value[i]
                if(currentRepo != null) {
                    if(reposRepository.checkHasRepoPushPermission(
                                    currentRepo, firebaseAuthWrapper.getUid())){
                        repoIndexPair.add(Pair(i, currentRepo.name))
                    }
                }
            }
        }

        return repoIndexPair
    }

    /**
     * Handle click on AddLocationDialog upload clicked
     */
    override fun onAddLocationDialogUploadClicked(repoIndex: Int, name: String, info: String) {
        if(!gpsInfoRepository.isLastLocationInit()){
            view?.displayToast(R.string.no_location_available)
            return
        }

        if (!isSubscribedToAddLocationRequest){
            compositeDisposable.add(reposRepository.addLocationStatusStream
                    .subscribeOn(schedulersFacade.io())
                    .observeOn(schedulersFacade.ui())
                    .subscribe(
                            { result -> when(result){
                                    ReposRepository.RequestStatus.IDLE -> {}
                                    ReposRepository.RequestStatus.READY -> {
                                        view?.setAddLocationDialogLoadingStatus(false)
                                        view?.dismissAddLocationDialog()
                                    }
                                    ReposRepository.RequestStatus.ONGOING -> {
                                        view?.setAddLocationDialogLoadingStatus(true)
                                    }
                                    ReposRepository.RequestStatus.ERROR -> {
                                        view?.setAddLocationDialogLoadingStatus(false)
                                        view?.displayToast(R.string.add_repo_error_toast)
                                    }
                                    ReposRepository.RequestStatus.NO_NETWORK -> {
                                        view?.setAddLocationDialogLoadingStatus(false)
                                        view?.dismissAddLocationDialog()
                                        view?.displayToast(R.string.add_location_no_network_toast)
                                    }
                                    else -> {
                                        view?.setAddLocationDialogLoadingStatus(false)
                                        view?.displayToast(R.string.add_repo_error_toast)
                                    }
                                }
                            },
                            { e -> e.printStackTrace() }
                    ))

            isSubscribedToAddLocationRequest = true
        }

        val accRange = if(gpsInfoRepository.lastLocation.elevationApi){
            15.0f
        } else {
            gpsInfoRepository.lastLocation.accuracy * 2.5f
        }

        reposRepository.addRepoLocation(repoIndex, gpsInfoRepository.lastLocation,
                name, info, accRange, firebaseAuthWrapper.getUid() ?: "")
    }

    /**
     * Preferences menu option clicked
     */
    override fun onPreferencesOptionClicked() {
        if(currentTab != TabType.PREFERENCES) {
            backTab = currentTab
            currentTab = TabType.PREFERENCES
            view?.switchToPreferencesFragment()
        }
    }

    /**
     * Get back to fragment id in backTab variable
     */
    override fun switchBackFromPreferenceFragment(): Boolean {
        return if(backTab != TabType.NONE) {
            when (backTab) {
                TabType.GPS_INFO -> onGpsInfoTabClicked()
                TabType.MAP -> onMapTabClicked()
                TabType.LOCATIONS -> onLocationsTabClicked()
                else -> {}
            }
            true
        } else false
    }

    /**
     * Share menu option clicked, format text to share and lunch intent
     */
    override fun onShareOptionClicked() {
        if(currentTab == TabType.MAP) {
            view?.shareIntent(shareMessageFormatter.buildMapShare(gpsInfoRepository.lastLocation))
        } else {
            view?.shareIntent(shareMessageFormatter.buildGpsInfoShare(gpsInfoRepository.lastLocation))
        }
    }

    /**
     * Called on GMS connection established
     */
    override fun gmsConnected() {
        view?.checkPermissions()
    }

    /**
     * Called on GMS connection suspended
     */
    override fun gmsSuspended() {

    }

    /**
     * Called on GMS connection failed
     */
    override fun gmsFailed() {
        fallBackMode = true
        view?.checkPermissions()
    }

    /**
     * Called on GMS locationChangedListener() / native LocationManager onLocationChanged() called
     */
    override fun onLocationChanged(userLocation: UserLocation) {
        gpsInfoRepository.next(userLocation)
    }

    /**
     * Called on GMS location callback add
     */
    override fun gmsLocationUpdatesActive() {
        locationUpdates = true
    }

    /**
     * Handle permissions status
     */
    override fun permissionsGranted(status: Boolean) {
        when(status){
            true -> {
                if(!fallBackMode){
                    view?.gmsSetupLocationUpdates()
                    gpsInfoRepository.startElevationApiCalls()
                } else {
                    fallbackStartup()
                }
            }
            false -> view?.displayToast(R.string.gps_permissions_declined)
        }
    }

    /**
     * Handle gps enable dialog
     */
    override fun gmsGpsEnableDialog(result: Boolean) {
        when(result){
            true -> {
                view?.gmsSetupLocationUpdates()
                gpsInfoRepository.startElevationApiCalls()
            }
            false -> view?.displayToast(R.string.gps_enable_declined)
        }
    }

    /**
     * Setup and configure fallback location listener
     */
    override fun fallbackStartup() {
        view?.fallbackLocationManagerSetup()
        view?.fallbackCheckGps()
        view?.fallbackStartLocationUpdates()
    }

    /**
     * Fallback for GMS GPS auto-enable fail
     */
    override fun fallbackGpsAutoEnableFailed() {
       view?.fallbackLocationManagerSetup()
       view?.fallbackCheckGps()
    }

    /**
     * Handle user action in fallback gps turn on dialog
     */
    override fun fallbackGpsDialogUserResponse(response: Boolean) {
        when(response){
            true -> view?.fallbackTurnGpsIntent()
            false -> view?.displayToast(R.string.gps_fallback_dialog_no_toast)
        }
    }

    /**
     * Called on fallback gps location callback add
     */
    override fun fallbackLocationUpdatesActive() {
        fallbackLocationUpdates = true
    }

    /**
     * Firebase handle result of sign-in
     */
    override fun firebaseSignIn(status: Boolean) {
        if(status) {
            firebaseAuthWrapper.firebaseSendEmailVerification()
            firebaseAuthWrapper.checkUserObj()
        } else {
            if(!firebaseAuthWrapper.isLoggedIn()) {
                firebaseAuthWrapper.firebaseLoginAnonymous()
            }
        }
    }

    /**
     * Firebase user login status changed
     */
    override fun firebaseLogin(loggedIn: Boolean) {
        if(!loggedIn){
            view?.lunchFirstStartupDialog()
        }
    }

    override fun onFirstStartupDialogResult(response: Boolean) {
        if(response){
            view?.firebaseSignInIntent()
        } else {
            firebaseAuthWrapper.firebaseLoginAnonymous()
        }
    }

    /**
     * Forward preferences changed event to [GpsInfoRepository] and [MainActivity]
     */
    override fun notifyPossiblePreferencesChange() {
        gpsInfoRepository.checkForPreferencesChange()
        view?.notifyPossiblePreferencesChange()
    }

    /**
     * Observe camera requests from [CameraRepository]
     */
    private fun observeCameraRequests() {
        compositeDisposable.add(cameraRepository.cameraRequest
                .subscribeOn(schedulersFacade.io())
                .observeOn(schedulersFacade.ui())
                .subscribe(
                        { result -> handleCameraRequest(result) },
                        { e -> e.printStackTrace() }
                ))

        isSubscribedToCameraRequest = true
    }

    /**
     * Handle new [CameraRepository.CameraRequestInfo], start by checking runtime permissions
     */
    fun handleCameraRequest(cameraRequestInfo: CameraRepository.CameraRequestInfo){
        currentCameraRequestInfo = cameraRequestInfo
        view?.checkCameraPermissions()
    }

    /**
     * Camera permissions granted, continue handling [CameraRepository.CameraRequestInfo]
     */
    override fun cameraPermissionsGranted(status: Boolean) {
        if(status) view?.cameraRequest(currentCameraRequestInfo.feature)
        else view?.displayToast(R.string.toast_camera_permission_not_granted)
    }

    /**
     * Forward photo [File] back to [CameraRepository]
     */
    override fun onCameraResult(file: File) {
        cameraRepository.onCameraResult(currentCameraRequestInfo.tag, file)
    }

    enum class TabType{
        NONE, GPS_INFO, MAP, LOCATIONS, PREFERENCES
    }
}