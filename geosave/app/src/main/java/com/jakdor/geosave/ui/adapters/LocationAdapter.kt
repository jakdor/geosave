/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.adapters

import android.content.res.Configuration
import androidx.databinding.DataBindingUtil
import android.os.Handler
import androidx.recyclerview.widget.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.firebase.Location
import com.jakdor.geosave.common.repository.LocationConverter
import com.jakdor.geosave.common.repository.SharedPreferencesRepository
import com.jakdor.geosave.databinding.LocationCardBinding
import com.jakdor.geosave.ui.locations.RepoViewModel
import com.jakdor.geosave.utils.GlideApp
import timber.log.Timber
import java.util.*

class LocationAdapter(private var locationVector: Vector<Location?>,
                      private var viewModel: RepoViewModel?,
                      private val sharedPreferencesRepository: SharedPreferencesRepository,
                      private val layoutHeight: Int?,
                      private val screenOrientation: Int?,
                      private val screenRatio: Float?):
        RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<LocationCardBinding>(
                layoutInflater, R.layout.location_card, parent, false)

        //height auto scaling
        var scalingFactor = 7.0f
        if(screenOrientation != null && screenRatio != null){
            when(screenOrientation){
                Configuration.ORIENTATION_PORTRAIT -> scalingFactor = 7.0f
                Configuration.ORIENTATION_LANDSCAPE -> scalingFactor = 7.0f / screenRatio
            }
        }
        if(layoutHeight != null){
            binding.root.layoutParams.height = (layoutHeight / scalingFactor).toInt()
        }

        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        if(position >= locationVector.size){
            Timber.wtf("Invalid ViewHolder position")
            return
        }

        if(locationVector[position] == null){
            Timber.e("location on position: %d is null", position)
            return
        }

        val location = locationVector[position]
        holder.bind(location!!)

        holder.binding.locationCardView.setOnClickListener {
            viewModel?.onLocationCardClick(locationVector.indexOf(location))
        }
    }

    override fun getItemCount(): Int {
        return locationVector.size
    }

    /**
     * Update adapter content
     */
    fun updateReposList(newLocationList: MutableList<Location?>){
        val oldLocationList = mutableListOf<Location?>()
        oldLocationList.addAll(locationVector)

        val handler = Handler()
        Thread(Runnable {
            val diffCallback = LocationDiffCallback(oldLocationList, newLocationList)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            handler.post {
                diffResult.dispatchUpdatesTo(this)
                locationVector.clear()
                locationVector.addAll(newLocationList)
            }
        }).start()
    }

    /**
     * ViewHolder for location_card.xml
     */
    inner class LocationViewHolder(val binding: LocationCardBinding):
            RecyclerView.ViewHolder(binding.root) {
        /**
         * Bind view with data
         */
        fun bind(location: Location){
            binding.name = location.name

            //location
            when(sharedPreferencesRepository.getString(
                    SharedPreferencesRepository.locationUnits, "0").toInt()){
                0 -> { //decimal
                    binding.location =
                            LocationConverter.decimalFormat(location.latitude, location.longitude)
                }
                1 -> { //sexigesimal
                    binding.location =
                            LocationConverter.dmsFormat(location.latitude, location.longitude)
                }
                2 -> { //decimal degrees
                    binding.location = LocationConverter.
                            decimalDegreesFormat(location.latitude, location.longitude)
                }
                3 -> { //degrees decimal minutes
                    binding.location =
                            LocationConverter.dmFormat(location.latitude, location.longitude)
                }
            }

            //alt
            when(sharedPreferencesRepository.getString(
                    SharedPreferencesRepository.altUnits, "0").toInt()) {
                0 -> { //meters
                    binding.alt = String.format("%.2f m", location.altitude)
                }
                1 -> { //kilometers
                    binding.alt = String.format("%.4f km", location.altitude / 1000.0)
                }
                2 -> { //feats
                    binding.alt = String.format("%.2f ft", location.altitude * 3.2808399)
                }
                3 -> { //land miles
                    binding.alt = String.format("%.6f mi", location.altitude * 0.000621371192)
                }
            }

            //acc
            when(sharedPreferencesRepository.getString(
                    SharedPreferencesRepository.accUnits, "0").toInt()){
                0 -> { //meters
                    binding.acc = String.format("%.2f m", location.accuracy)
                }
                1 -> { //kilometers
                    binding.acc = String.format("%.4f km", location.accuracy / 1000.0)
                }
                2 -> { //feats
                    binding.acc = String.format("%.2f ft", location.accuracy * 3.2808399)
                }
                3 -> { //land miles
                    binding.acc = String.format("%.6f mi", location.accuracy * 0.000621371192)
                }
                4 -> { //nautical miles
                    binding.acc = String.format("%.6f nmi", location.accuracy * 0.000539956803)
                }
            }

            binding.executePendingBindings()

            GlideApp.with(binding.root)
                    .load(location.picUrl)
                    .apply(RequestOptions()
                            .placeholder(R.drawable.repo_icon_placeholder_square)
                            .centerCrop())
                    .into(binding.locationCardPic)
        }
    }
}