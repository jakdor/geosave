package com.jakdor.geosave.ui.map

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.UserLocation
import com.jakdor.geosave.databinding.FragmentMapOverlayBinding
import com.jakdor.geosave.di.InjectableFragment
import com.jakdor.geosave.ui.gpsinfo.GpsInfoViewModel
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

        binding.mapTypeCard?.visibility = View.GONE
        binding.mapTypeFab?.setOnClickListener { onMapTypeFabClicked() }

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
    }

    /**
     * Observe [GpsInfoViewModel] for updates on location [MutableLiveData] stream
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
            if(!initCamZoom) { //todo soft camera fallowing
                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(location.latitude, location.longitude), DEFAULT_ZOOM))
                initCamZoom = true
            }

            val pos = String.format(Locale.US, "%f, %f", location.latitude, location.longitude)
            map_location_text_view.text = pos
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
        binding.mapTypeFab?.visibility = View.GONE
        binding.mapTypeCard?.visibility = View.VISIBLE
    }

    /**
     * Hide MapTypeCard when user engages with the map
     */
    fun onMapInteraction(){
        binding.mapTypeFab?.visibility = View.VISIBLE
        binding.mapTypeCard?.visibility = View.GONE
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