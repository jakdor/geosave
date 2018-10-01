/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.repository

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.jakdor.geosave.common.model.firebase.User
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

/**
 * Handles request to [User] documents other then current user
 */
class UserRepository(private val db: FirebaseFirestore) {

    val userPicUrlStream: PublishSubject<String> = PublishSubject.create()

    /**
     * Get pic url from given [User] [DocumentReference]
     */
    fun getUserPicUrl(ref: DocumentReference){
        ref.get().addOnSuccessListener {
            onUserPicSuccess(it)
        }.addOnFailureListener{
            Timber.e("Unable to get user pic: %s", it.toString())
        }
    }

    /**
     * Get pic url from given [User] uid as [String]
     */
    fun getUserPicUrl(path: String){
        db.collection("users").document(path).get().addOnSuccessListener {
            onUserPicSuccess(it)
        }.addOnFailureListener{
            Timber.e("Unable to get user pic: %s", it.toString())
        }
    }

    fun onUserPicSuccess(snap: DocumentSnapshot){
        val user = snap.toObject(User::class.java)
        if (user != null) userPicUrlStream.onNext(user.picUrl)
        else Timber.e("Unable to get user pic - user obj is null")
    }
}