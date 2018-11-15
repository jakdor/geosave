/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.map

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.common.model.firebase.Location
import com.jakdor.geosave.common.repository.LocationConverter
import com.jakdor.geosave.databinding.FragmentMapOverlayBinding
import com.jakdor.geosave.di.InjectableFragment
import com.jakdor.geosave.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_map_overlay.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Fragment displaying google map with user position and locations
 */
class MapFragment: SupportMapFragment(), OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, InjectableFragment { //todo investigate screen rotation memory leak from maps api

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: MapViewModel? = null
    lateinit var binding: FragmentMapOverlayBinding

    private var map: GoogleMap? = null

    private var initCamZoom = false
    private var camFollow = false
    private var locationFormat = 0
    private var mapGestureLock = false
    private lateinit var lastCoordinates: LatLng

    private lateinit var markerLocationMap: MutableMap<Marker, Location>
    private lateinit var indexRepoNamePair: ArrayList<Pair<Int, String>>
    private var selectedRepoSpinnerIndex: Int = 0

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val mapView : FrameLayout =
                super.onCreateView(inflater, container, savedInstanceState) as FrameLayout
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_map_overlay, container, false)
        val overlay = binding.root
        mapView.addView(overlay)

        binding.mapTypePopup.mapTypeCard.visibility = View.GONE
        binding.mapTypeFab.setOnClickListener { onMapTypeFabClicked() }

        //androidX lib temp bug fix
        binding.mapTypeFab.scaleType = ImageView.ScaleType.CENTER
        binding.mapCamFallowFab.scaleType = ImageView.ScaleType.CENTER

        binding.mapRepoSpinner.setOnTouchListener { _, motionEvent ->
            if(motionEvent.action == MotionEvent.ACTION_UP){
                if(binding.mapRepoSpinner.adapter == null){
                    Toast.makeText(
                            context, R.string.loading_repos_toast, Toast.LENGTH_SHORT).show()
                }
                else if(binding.mapRepoSpinner.adapter.isEmpty){
                    Toast.makeText(
                            context, R.string.add_location_no_repos_toast, Toast.LENGTH_LONG).show()
                }
            }
            return@setOnTouchListener false
        }

        //resize map type icons to specific device dynamically
        GlideApp.with(this)
                .load(R.drawable.map_default)
                .fitCenter()
                .into(binding.mapTypePopup.mapTypeDefault.mapTypeButtonIcon)

        GlideApp.with(this)
                .load(R.drawable.map_satellite)
                .fitCenter()
                .into(binding.mapTypePopup.mapTypeSatellite.mapTypeButtonIcon)

        GlideApp.with(this)
                .load(R.drawable.map_hybrid)
                .fitCenter()
                .into(binding.mapTypePopup.mapTypeHybrid.mapTypeButtonIcon)

        GlideApp.with(this)
                .load(R.drawable.map_terrain)
                .fitCenter()
                .into(binding.mapTypePopup.mapTypeTerrain.mapTypeButtonIcon)

        return mapView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(viewModel == null){
            viewModel = ViewModelProviders.of(this, viewModelFactory)
                    .get(MapViewModel::class.java)
        }

        binding.viewModel = viewModel
        viewModel?.requestUserLocationUpdates()
        viewModel?.loadPreferences()
        viewModel?.requestCurrentRepoLocationsUpdates()
        viewModel?.requestRepoSpinnerUpdates()
        observeUserLocation()
        observeMapType()
        observeLocationType()
        observeCamType()
        observeCurrentRepoLocationsList()
        observeRepoIndexPairList()
        observeChosenRepoIndex()
        observeToastStrId()
    }

    /**
     * load user preferences
     */
    override fun onResume() {
        super.onResume()
        viewModel?.loadPreferences()
    }

    /**
     * Request preferences update after returning from preferences tab
     */
    fun requestPreferencesUpdate(){
        viewModel?.loadPreferences()
    }

    /**
     * Observe [MapViewModel] for updates on location [MutableLiveData] stream
     */
    fun observeUserLocation(){
        viewModel?.location?.observe(this, Observer {
            handleUserLocation(it)
        })
    }

    /**
     * Handle new [UserLocation] object
     */
    fun handleUserLocation(location: UserLocation) {
        lastCoordinates = LatLng(location.latitude, location.longitude)

        if(!initCamZoom) {
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(lastCoordinates, DEFAULT_ZOOM))
            initCamZoom = true
        }

        if(camFollow) {
            val camUpdateFactory = CameraUpdateFactory.newLatLngZoom(lastCoordinates, DEFAULT_ZOOM)

            mapGestureLock = true
            map?.animateCamera(camUpdateFactory, 500, object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    mapGestureLock = false
                }

                override fun onCancel() {}
            })
        }

        when(locationFormat){
            0 -> { //decimal
                map_location_text_view.text =
                        LocationConverter.decimalFormat(location.latitude, location.longitude)
            }
            1 -> { //sexigesimal
                map_location_text_view.text =
                        LocationConverter.dmsFormat(location.latitude, location.longitude)
            }
            2 -> { //decimal degrees
                map_location_text_view.text =
                        LocationConverter.decimalDegreesFormat(location.latitude, location.longitude)
            }
            3 -> { //degrees decimal minutes
                map_location_text_view.text =
                        LocationConverter.dmFormat(location.latitude, location.longitude)
            }
        }
    }

    /**
     * Observe [MapViewModel] for updates on map type [MutableLiveData] stream
     */
    fun observeMapType(){
        viewModel?.mapType?.observe(this, Observer {
            handleMapTypeChange(it)
        })
    }

    /**
     * Handle map type changed
     */
    fun handleMapTypeChange(mapId: Int?){
        if(mapId != null) {
            binding.selectedMapType = mapId
            when(mapId){
                0 -> map?.mapType = GoogleMap.MAP_TYPE_NORMAL
                1 -> map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
                2 -> map?.mapType = GoogleMap.MAP_TYPE_HYBRID
                3 -> map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            }
        }
    }

    /**
     * Observe [MapViewModel] for updates on location type/format [MutableLiveData] stream
     */
    fun observeLocationType(){
        viewModel?.locationType?.observe(this, Observer {
            handleLocationTypeChange(it)
        })
    }

    /**
     * Save location format in local variable
     */
    fun handleLocationTypeChange(format: Int){
        this.locationFormat = format
    }

    /**
     * Observe [MapViewModel] for updates on camera type [MutableLiveData] stream
     */
    fun observeCamType(){
        viewModel?.camFollowType?.observe(this, Observer {
            handleCamType(it)
        })
    }

    /**
     * Save cam type in local variable
     */
    fun handleCamType(cam: Int){
        camFollow = when(cam){
            0 -> false
            1 -> true
            else -> false
        }

        loadCamFollowFabIcon()
    }

    /**
     * Observe [MapViewModel] for updates on current chosen repo [Location] lists
     */
    fun observeCurrentRepoLocationsList(){
        viewModel?.currentRepoLocationsList?.observe(this, Observer {
            handleCurrentRepoLocationsList(it)
        })
    }

    /**
     * Handle new [Location] list
     */
    fun handleCurrentRepoLocationsList(locations: MutableList<Location>){
        map?.clear()
        if(::markerLocationMap.isInitialized) markerLocationMap.clear()
        markerLocationMap = mutableMapOf()
        locations.forEach {
            addMapMarker(it)
        }
    }

    /**
     * Add map marker from [Location] obj
     */
    fun addMapMarker(location: Location){
        val latLng2 = LatLng(location.latitude, location.longitude)
        val markerBuilder = MarkerOptions()
                .position(latLng2)
                .title(location.name)
        if(map != null) {
            markerLocationMap[map!!.addMarker(markerBuilder)] = location
        }
    }

    /**
     * Observe [MapViewModel] for updates on repoIndexPairList
     */
    fun observeRepoIndexPairList(){
        viewModel?.repoIndexPairList?.observe(this, Observer { handleRepoIndexPairList(it) })
    }

    /**
     * Handle new repoIndexPairList
     */
    fun handleRepoIndexPairList(indexRepoNamePair: ArrayList<Pair<Int, String>>){
        this.indexRepoNamePair = indexRepoNamePair

        if(!this.indexRepoNamePair.isEmpty() && context != null) {
            val repoNames = mutableListOf<String>()
            this.indexRepoNamePair.forEach {
                repoNames.add(it.second)
            }

            val spinnerAdapter =
                    ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item, repoNames)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.mapRepoSpinner.adapter = spinnerAdapter

            setRepoSpinnerSelection()

            binding.mapRepoSpinner.onItemSelectedListener =
                    object: AdapterView.OnItemSelectedListener {
                        var spinnerInitFlag = true

                        override fun onItemSelected(
                                adapterView: AdapterView<*>, view: View, i: Int, l: Long){
                            if(spinnerInitFlag) {
                                spinnerInitFlag = false
                            } else {
                                viewModel?.observeRepo(indexRepoNamePair[i].first)
                            }
                        }

                        override fun onNothingSelected(adapterView: AdapterView<*>) {
                            return
                        }
                    }

            Timber.i("Loaded mapFragment repo spinner")
        }
    }

    private fun setRepoSpinnerSelection(){
        for(i in 0 until this.indexRepoNamePair.size){
            if(this.indexRepoNamePair[i].first == selectedRepoSpinnerIndex)
                binding.mapRepoSpinner.setSelection(i)
        }
    }

    /**
     * Observe [MapViewModel] for updates on user chosen repo index
     */
    fun observeChosenRepoIndex(){
        viewModel?.chosenRepoIndex?.observe(this, Observer { handleChosenRepoIndex(it) })
    }

    /**
     * Handle new chosen repo index
     */
    fun handleChosenRepoIndex(index: Int){
        selectedRepoSpinnerIndex = index
        setRepoSpinnerSelection()
    }

    /**
     * Observe [MapViewModel] for updates on toastStrId [MutableLiveData]
     */
    fun observeToastStrId(){
        viewModel?.toastStrId?.observe(this, Observer {
            displayToast(it)
        })
    }

    /**
     * display Toast by resources id
     */
    fun displayToast(strId: Int){
        Toast.makeText(context, strId, Toast.LENGTH_SHORT).show()
    }

    /**
     * Start loading map
     */
    override fun onCreate(p0: Bundle?) {
        super.onCreate(p0)
        getMapAsync(this)
    }

    /**
     * Map ready callback
     */
    override fun onMapReady(p0: GoogleMap?) {
        map = p0
        initCamZoom = false
        map?.setOnMapClickListener { onMapInteraction() }
        map?.setOnMapLongClickListener { onMapInteraction() }
        map?.setOnCameraMoveListener { handleUserMapMove() }
        map?.setOnMyLocationButtonClickListener { handleMyLocationButtonClick() }
        map?.setOnMarkerClickListener(this)

        //restore map type
        handleMapTypeChange(viewModel?.mapType?.value)

        try{
            map?.isMyLocationEnabled = true
        } catch (e: SecurityException){
            Timber.wtf("SecurityException thrown, location permission: %s", e.toString())
        }
    }

    /**
     * Handle click on marker
     */
    override fun onMarkerClick(p0: Marker?): Boolean {
        val location = markerLocationMap[p0]
        Timber.i("clicked on marker: %s", location.toString())
        return false
    }

    override fun onDestroyView() {
        map?.clear()
        super.onDestroyView()
    }

    /**
     * Load camFollowFab icon based on camFollow field
     */
    private fun loadCamFollowFabIcon(){
        when(camFollow){
            true -> binding.mapCamFallowFab.setImageDrawable(
                    context?.getDrawable(R.drawable.ic_follow))
            false -> binding.mapCamFallowFab.setImageDrawable(
                    context?.getDrawable(R.drawable.ic_follow_no))
        }
    }

    /**
     * Animate showing of MapTypeCard
     */
    @SuppressLint("RestrictedApi")
    fun onMapTypeFabClicked(){
        binding.mapTypeFab.visibility = View.GONE
        binding.mapCamFallowFab.visibility = View.GONE
        binding.mapTypePopup.mapTypeCard.visibility = View.VISIBLE
        binding.mapTypePopup.mapTypeLayout.visibility = View.GONE

        binding.mapTypePopup.mapTypeCard.translationY = 50.0f
        binding.mapTypePopup.mapTypeCard.translationX = -100.0f
        binding.mapTypePopup.mapTypeCard.scaleX = 0.75f
        binding.mapTypePopup.mapTypeCard.scaleY = 0.75f
        binding.mapTypePopup.mapTypeCard.alpha = 0.0f

        binding.mapTypePopup.mapTypeCard.animate()!!
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .translationX(0.0f)
                .translationY(0.0f)
                .setInterpolator(FastOutLinearInInterpolator())
                .setDuration(120)
                .withEndAction { binding.mapTypePopup.mapTypeLayout.visibility = View.VISIBLE }
                .start()
    }

    /**
     * Override default my location button behaviour to not clash with user map move detection
     */
    fun handleMyLocationButtonClick(): Boolean{
        if(::lastCoordinates.isInitialized){
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(lastCoordinates, DEFAULT_ZOOM))
        }
        return true
    }

    /**
     * Handle user map move gesture
     */
    fun handleUserMapMove(){
        if(!mapGestureLock){
            viewModel?.onCamUserMove()
            onMapInteraction()
        }
    }

    /**
     * Hide MapTypeCard when user engages with the map
     */
    @SuppressLint("RestrictedApi")
    fun onMapInteraction(){
        binding.mapTypeFab.visibility = View.VISIBLE
        binding.mapCamFallowFab.visibility = View.VISIBLE
        binding.mapTypePopup.mapTypeCard.visibility = View.GONE
    }

    companion object: InjectableFragment {
        const val CLASS_TAG = "MapFragment"

        private const val DEFAULT_ZOOM = 17.0f

        fun newInstance(): MapFragment{
            val args = Bundle()
            val fragment = MapFragment()
            fragment.arguments = args
            fragment.retainInstance = true
            return fragment
        }
    }
}