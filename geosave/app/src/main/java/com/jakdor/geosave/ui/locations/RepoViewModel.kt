/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.ui.locations

import android.app.Application
import com.jakdor.geosave.arch.BaseViewModel
import com.jakdor.geosave.common.repository.ReposRepository
import com.jakdor.geosave.utils.RxSchedulersFacade
import javax.inject.Inject

class RepoViewModel @Inject
constructor(application: Application, rxSchedulersFacade: RxSchedulersFacade,
            private val reposRepository: ReposRepository):
        BaseViewModel(application, rxSchedulersFacade) {

    /**
     * Return from [ReposRepository] to [ReposBrowserFragment]
     */
    fun returnFromRepoFragment(){
        reposRepository.chosenRepositoryIndexStream.onNext(-1)
    }
}