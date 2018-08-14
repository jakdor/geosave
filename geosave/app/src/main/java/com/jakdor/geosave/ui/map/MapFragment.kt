package com.jakdor.geosave.ui.map

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.view.animation.FastOutLinearInInterpolator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.databinding.FragmentMapOverlayBinding
import com.jakdor.geosave.di.InjectableFragment
import com.jakdor.geosave.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_map_overlay.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class MapFragment: SupportMapFragment(), OnMapReadyCallback, InjectableFragment {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    var viewModel: MapViewModel? = null
    lateinit var binding: FragmentMapOverlayBinding

    private var map: GoogleMap? = null

    private var initCamZoom = false

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val mapView : FrameLayout =
                super.onCreateView(inflater, container, savedInstanceState) as FrameLayout
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_map_overlay, container, false)
        val overlay = binding.root
        mapView.addView(overlay)

        binding.mapTypePopup?.mapTypeCard?.visibility = View.GONE
        binding.mapTypeFab.setOnClickListener { onMapTypeFabClicked() }

        //resize map type icons to specific device dynamically
        GlideApp.with(this)
                .load(R.drawable.map_default)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.mapTypePopup?.mapTypeDefault?.mapTypeButtonIcon)

        GlideApp.with(this)
                .load(R.drawable.map_satellite)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.mapTypePopup?.mapTypeSatellite?.mapTypeButtonIcon)

        GlideApp.with(this)
                .load(R.drawable.map_hybrid)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.mapTypePopup?.mapTypeHybrid?.mapTypeButtonIcon)

        GlideApp.with(this)
                .load(R.drawable.map_terrain)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.mapTypePopup?.mapTypeTerrain?.mapTypeButtonIcon)

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
        observeUserLocation()
        observeMapType()
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
    fun handleUserLocation(location: UserLocation?) {
        if(location != null) {
            if(!initCamZoom) {
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude), DEFAULT_ZOOM))
                initCamZoom = true
            }

            val pos = String.format(Locale.US, "%f, %f", location.latitude, location.longitude)
            map_location_text_view.text = pos
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
        map?.setOnCameraMoveListener { onMapInteraction() }

        //load user preferences
        viewModel?.loadPreferences()

        //restore map type
        handleMapTypeChange(viewModel?.mapType?.value)

        try{
            map?.isMyLocationEnabled = true
        } catch (e: SecurityException){
            Timber.wtf("SecurityException thrown, location permission: %s", e.toString())
        }
    }

    override fun onDestroyView() {
        map?.clear()
        super.onDestroyView()
    }

    /**
     * Animate showing of MapTypeCard
     */
    fun onMapTypeFabClicked(){
        binding.mapTypeFab.visibility = View.GONE
        binding.mapTypePopup?.mapTypeCard?.visibility = View.VISIBLE
        binding.mapTypePopup?.mapTypeLayout?.visibility = View.GONE

        binding.mapTypePopup?.mapTypeCard?.translationY = 50.0f
        binding.mapTypePopup?.mapTypeCard?.translationX = 100.0f
        binding.mapTypePopup?.mapTypeCard?.scaleX = 0.75f
        binding.mapTypePopup?.mapTypeCard?.scaleY = 0.75f
        binding.mapTypePopup?.mapTypeCard?.alpha = 0.0f

        binding.mapTypePopup?.mapTypeCard?.animate()!!
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .translationX(0.0f)
                .translationY(0.0f)
                .setInterpolator(FastOutLinearInInterpolator())
                .setDuration(120)
                .withEndAction { binding.mapTypePopup?.mapTypeLayout?.visibility = View.VISIBLE }
                .start()
    }

    /**
     * Hide MapTypeCard when user engages with the map
     */
    fun onMapInteraction(){
        binding.mapTypeFab.visibility = View.VISIBLE
        binding.mapTypePopup?.mapTypeCard?.visibility = View.GONE
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