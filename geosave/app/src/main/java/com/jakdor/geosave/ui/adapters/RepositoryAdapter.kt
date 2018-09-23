/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.adapters

import android.databinding.DataBindingUtil
import android.os.Handler
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.jakdor.geosave.R
import com.jakdor.geosave.common.model.firebase.Repo
import com.jakdor.geosave.databinding.RepositoryCardBinding
import com.jakdor.geosave.ui.locations.ReposBrowserViewModel
import timber.log.Timber
import java.util.*

/**
 * RecyclerView adapter for [com.jakdor.geosave.ui.locations.ReposBrowserFragment] items
 */
class RepositoryAdapter(private var reposList: Vector<Repo?>,
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
     * Update adapter content
     */
    fun updateReposList(newReposList: MutableList<Repo?>){
        val oldRepoList = mutableListOf<Repo?>()
        oldRepoList.addAll(reposList)

        val handler = Handler()
        Thread(Runnable {
            val diffCallback = RepoDiffCallback(oldRepoList, newReposList)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            handler.post {
                diffResult.dispatchUpdatesTo(this)
                reposList.clear()
                reposList.addAll(newReposList)
            }
        }).start()
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

            glide.load(repo.picUrl)
                    .apply(RequestOptions().placeholder(R.drawable.repo_icon_placeholder).circleCrop())
                    .into(binding.repoCardIcon)
        }
    }
}