/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.adapters

import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Handler
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.firebase.Location
import com.jakdor.geosave.databinding.LocationCardBinding
import com.jakdor.geosave.ui.locations.RepoViewModel
import timber.log.Timber
import java.util.*

class LocationAdapter(private var locationVector: Vector<Location?>,
                      private var viewModel: RepoViewModel?,
                      private val layoutHeight: Int?,
                      private val screenOrientation: Int?,
                      private val screenRatio: Float?):
        RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<LocationCardBinding>(
                layoutInflater, R.layout.location_card, parent, false)

        //height auto scaling
        var scalingFactor = 6.0f
        if(screenOrientation != null && screenRatio != null){
            when(screenOrientation){
                Configuration.ORIENTATION_PORTRAIT -> scalingFactor = 6.0f
                Configuration.ORIENTATION_LANDSCAPE -> scalingFactor = 6.0f / screenRatio
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
            binding.locationModel = location
        }
    }
}