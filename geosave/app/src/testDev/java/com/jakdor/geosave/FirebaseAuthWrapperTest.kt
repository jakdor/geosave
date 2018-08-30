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

    private var user = mock<FirebaseUser>()

    private var firebaseAuth = mock<FirebaseAuth>{
        on{ currentUser }.thenReturn(user)
    }
    private var firebaseAuthWrapper = FirebaseAuthWrapper(firebaseAuth)

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

        //true returned by FirebaseUser - true
        user = mock { on{ isAnonymous }.thenReturn(true) }
        firebaseAuth = mock { on{ currentUser }.thenReturn(user) }
        firebaseAuthWrapper = FirebaseAuthWrapper(firebaseAuth)

        Assert.assertTrue(firebaseAuthWrapper.isAnonymous())

        //false returned by FirebaseUser - false
        user = mock { on{ isAnonymous }.thenReturn(false) }
        firebaseAuth = mock { on{ currentUser }.thenReturn(user) }
        firebaseAuthWrapper = FirebaseAuthWrapper(firebaseAuth)

        Assert.assertFalse(firebaseAuthWrapper.isAnonymous())
    }
}