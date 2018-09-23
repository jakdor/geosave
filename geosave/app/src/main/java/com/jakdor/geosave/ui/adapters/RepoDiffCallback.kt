/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.adapters

import android.support.v7.util.DiffUtil
import com.jakdor.geosave.common.model.firebase.Repo

/**
 * Difference callback for smart reloading of [RepositoryAdapter] content
 */
class RepoDiffCallback(private val oldRepos: List<Repo?>,
                       private val newRepos: List<Repo?>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldRepos[oldItemPosition] == newRepos[newItemPosition]
    }

    override fun getOldListSize(): Int {
        return oldRepos.size
    }

    override fun getNewListSize(): Int {
        return newRepos.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldRepos[oldItemPosition].toString() == newRepos[newItemPosition].toString()
    }

}