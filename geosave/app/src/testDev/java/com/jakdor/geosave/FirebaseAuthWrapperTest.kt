/*
 * GeoSave - app for easy sharing and collaborating on GPS related data
 * Copyright (C) 2018  Jakub Dorda
 *
 * Software under GPLv3 licence - full copyright notice available at:
 * https://github.com/jakdor/geosave/blob/master/README.md
 */

package com.jakdor.geosave

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.jakdor.geosave.common.wrapper.FirebaseAuthWrapper
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class FirebaseAuthWrapperTest{

    @get:Rule
    var thrown = ExpectedException.none()

    private var user = mock<FirebaseUser>()
    private var db = mock<FirebaseFirestore>()

    private var firebaseAuth = mock<FirebaseAuth>{
        on{ currentUser }.thenReturn(user)
    }
    private var firebaseAuthWrapper = FirebaseAuthWrapper(firebaseAuth, db)

    /**
     * Test login check
     */
    @Test
    fun isLoggedInTest(){
        Assert.assertTrue(firebaseAuthWrapper.isLoggedIn())
    }

    /**
     * Test if [FirebaseAuth] email verification invoked if conditions met
     */
    @Test
    fun firebaseSendEmailVerificationTest(){
        firebaseAuthWrapper.firebaseSendEmailVerification()

        verify(user, times(1)).sendEmailVerification()
    }

    /**
     * Test correct status returned
     */
    @Test
    fun isAnonymousTest(){
        //null, user not initialized - false
        Assert.assertFalse(firebaseAuthWrapper.isAnonymous())

        //true returned by User - true
        user = mock { on{ isAnonymous }.thenReturn(true) }
        firebaseAuth = mock { on{ currentUser }.thenReturn(user) }
        firebaseAuthWrapper = FirebaseAuthWrapper(firebaseAuth, db)

        Assert.assertTrue(firebaseAuthWrapper.isAnonymous())

        //false returned by User - false
        user = mock { on{ isAnonymous }.thenReturn(false) }
        firebaseAuth = mock { on{ currentUser }.thenReturn(user) }
        firebaseAuthWrapper = FirebaseAuthWrapper(firebaseAuth, db)

        Assert.assertFalse(firebaseAuthWrapper.isAnonymous())
    }

    /**
     * Test correct method invoked on FirebaseAuth object
     */
    @Test
    fun logoutTest(){
        firebaseAuthWrapper.logout()

        verify(firebaseAuth).signOut()
    }

    /**
     * Test correct method invoked on FirebaseAuth object
     */
    @Test
    fun deleteAccountTest(){
        firebaseAuthWrapper.deleteAccount(true)

        verify(user).delete()
    }
}