/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.adapters

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.databinding.RepositoryCardBinding
import com.jakdor.geosave.ui.locations.ReposBrowserViewModel
import timber.log.Timber

/**
 * RecyclerView adapter for [com.jakdor.geosave.ui.locations.ReposBrowserFragment] items
 */
class RepositoryAdapter(private val reposList: MutableList<Repo?>,
                        private val viewModel: ReposBrowserViewModel?,
                        private val glide: RequestManager,
                        private val layoutHeight: Int?):
        RecyclerView.Adapter<RepositoryAdapter.RepositoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepositoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<RepositoryCardBinding>(
                layoutInflater, R.layout.repository_card, parent, false)

        //height auto scaling
        if(layoutHeight != null){
            binding.root.layoutParams.height = layoutHeight / 6
        }

        return RepositoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RepositoryViewHolder, position: Int) {
        if(position >= reposList.size){
            Timber.wtf("Invalid ViewHolder position")
            return
        }

        if(reposList[position] == null){
            Timber.e("repository on position: %d is null", position)
            return
        }

        val repo = reposList[position]
        holder.bind(repo!!)
        holder.binding.repoCardView.setOnClickListener { viewModel?.onRepositoryClicked(repo) }
    }

    override fun getItemCount(): Int {
        return reposList.size
    }

    /**
     * ViewHolder for repository_card.xml
     */
    inner class RepositoryViewHolder(val binding: RepositoryCardBinding):
            RecyclerView.ViewHolder(binding.root) {
        /**
         * Bind view with data
         */
        fun bind(repo: Repo){
            binding.repoModel = repo
            binding.executePendingBindings()
        }
    }
}