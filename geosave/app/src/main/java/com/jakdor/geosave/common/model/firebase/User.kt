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
 * Firebase user profile
 */
data class User
constructor(var nickname: String,
            var picUrl: String,
            var reposList: MutableList<DocumentReference>)