/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave.common.wrapper

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jakdor.geosave.common.model.firebase.User
import timber.log.Timber

class FirebaseAuthWrapper constructor(private var mAuth: FirebaseAuth,
                                      private val db: FirebaseFirestore) {

    /**
     * Check if user logged in
     */
    fun isLoggedIn(): Boolean {
        return if(mAuth.currentUser != null){
            Timber.i("User logged in")
            true
        } else {
            Timber.i("User not logged in")
            false
        }
    }

    /**
     * Send verification email after sign-in
     */
    fun firebaseSendEmailVerification() {
        val user = mAuth.currentUser
        if(user != null && !user.isEmailVerified){
            user.sendEmailVerification()
            Timber.i("User not verified, sending email")
        }
    }

    /**
     * Login as anonymous
     */
    fun firebaseLoginAnonymous() {
        mAuth.signInAnonymously().addOnCompleteListener {
            if(it.isSuccessful) {
                checkUserObj()
                Timber.i("Firebase anonymous login success")
            }
            else Timber.wtf("Unable to login as anonymous")
        }
    }

    /**
     * Check if user is logged anonymously
     */
    fun isAnonymous(): Boolean{
        val user = mAuth.currentUser
        return user?.isAnonymous ?: false
    }

    /**
     * Logout current user
     */
    fun logout(){
        mAuth.signOut()
        Timber.i("Firebase user logged out")
    }

    /**
     * Delete current user account
     */
    fun deleteAccount(recreate: Boolean){
        mAuth.currentUser?.delete()?.addOnCompleteListener {
            if(it.isSuccessful){
                Timber.i("Firebase user account deleted")
                if(recreate) firebaseLoginAnonymous()
            }
            else Timber.wtf("Unable to delete user account")
        }
    }

    /**
     * Check if user has [User] object stored on Firestore, if not create new
     */
    fun checkUserObj(){
        if(mAuth.currentUser != null) {
            db.collection("users").document(mAuth.currentUser!!.uid)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if(documentSnapshot.exists()) Timber.i("User already has User obj")
                        else pushNewUserObj(db)
                    }
                    .addOnFailureListener { pushNewUserObj(db) }
        }
    }

    /**
     * Creates new init [User] object and pushes it to Firestore
     */
    private fun pushNewUserObj(db: FirebaseFirestore){
        val newUser = User(mAuth.currentUser!!.displayName ?: "anonymous",
                mAuth.currentUser!!.photoUrl.toString(), mutableListOf())

        db.collection("users").document(mAuth.currentUser!!.uid)
                .set(newUser)
                .addOnSuccessListener { Timber.i("Pushed new User object to Firestore") }
                .addOnFailureListener { Timber.e("Error pushing new User to Firestore") }
    }

    /**
     * Get user id
     */
    fun getUid(): String? {
        return mAuth.currentUser?.uid
    }
}