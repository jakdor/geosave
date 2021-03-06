/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.model.firebase

import com.google.firebase.firestore.DocumentReference

/**
 * Firebase repository entry/document
 */
data class Repo
constructor(var name: String = "",
            var ownerUid: String = "",
            var info: String = "",
            var picUrl: String = "",
            var visibility: Int = 0,
            var security: Int = 0,
            var editorsUidList: MutableList<DocumentReference> = mutableListOf(),
            var locationsList: MutableList<Location> = mutableListOf(),
            var messagesList: MutableList<DocumentReference> = mutableListOf()) //todo messages as list of references to message bundles 16-32 entries each?