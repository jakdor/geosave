/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.adapters

import android.support.v7.util.DiffUtil
import com.jakdor.geosave.common.model.firebase.Location

/**
 * Difference callback for smart reloading of [LocationAdapter] content
 */
class LocationDiffCallback(private val oldLocations: List<Location?>,
                           private val newLocations: List<Location?>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldLocations[oldItemPosition] == newLocations[newItemPosition]
    }

    override fun getOldListSize(): Int {
        return oldLocations.size
    }

    override fun getNewListSize(): Int {
        return newLocations.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldLocations[oldItemPosition].toString() == newLocations[newItemPosition].toString()
    }

}