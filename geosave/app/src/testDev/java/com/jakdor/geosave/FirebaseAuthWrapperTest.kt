package com.jakdor.geosave

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    private val user = mock<FirebaseUser>()

    private val firebaseAuth = mock<FirebaseAuth>{
        on{ currentUser }.thenReturn(user)
    }
    private val firebaseAuthWrapper = FirebaseAuthWrapper(firebaseAuth)

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
     * Test correct method invocation
     */
    @Test

    fun firebaseLoginAnonymousTest(){
        firebaseAuthWrapper.firebaseLoginAnonymous()

        verify(firebaseAuth.signInAnonymously(), times(1))
    }
}