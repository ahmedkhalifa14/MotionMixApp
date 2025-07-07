package com.ahmedkhalifa.motionmix.data.remote_data_source

import com.ahmedkhalifa.motionmix.common.utils.LoginResult
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FirebaseAuthenticationService @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {

    /*  Authentication  */
    //with email and password
    suspend fun registerWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        firebaseAuth.currentUser?.sendEmailVerification()
    }
    suspend fun loginWithEmailAndPassword(email: String, password: String): LoginResult {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            if (firebaseAuth.currentUser?.isEmailVerified == true) {
                LoginResult.Success
            } else {
                LoginResult.EmailNotVerified
            }
        } catch (e: FirebaseAuthException) {
            when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> LoginResult.EmailNotFound
                "ERROR_WRONG_PASSWORD" -> LoginResult.Error("Incorrect password")
                else -> LoginResult.Error(e.message ?: "Unknown Error Occurred")
            }
        } catch (e: Exception) {
            LoginResult.Error(e.message ?: "An Error Occurred")
        }
    }
    // with phone number
    suspend fun sendVerificationCode(phoneNumber: String): String {
        return withContext(Dispatchers.Main) {
            try {
                val verificationIdDeferred = CompletableDeferred<String>()

                val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            // Optional: Handle auto-verification if needed
                        }

                        override fun onVerificationFailed(exception: FirebaseException) {
                            verificationIdDeferred.completeExceptionally(exception)
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            verificationIdDeferred.complete(verificationId)
                        }
                    })
                    .build()

                PhoneAuthProvider.verifyPhoneNumber(options)

                // Wait for the verification ID with a timeout
                withTimeout(65_000) { // Slightly longer than Firebase's 60s timeout
                    verificationIdDeferred.await()
                }
            } catch (e: TimeoutCancellationException) {
                throw Exception("Verification timed out. Please try again.")
            } catch (e: Exception) {
                throw Exception("Failed to send verification code: ${e.message}")
            }
        }
    }
    suspend fun verifyCode(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)

        val task = CompletableDeferred<Unit>()
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { signInTask ->
            if (signInTask.isSuccessful) {
                task.complete(Unit)
            } else {
                val exception =
                    signInTask.exception ?: RuntimeException("Unknown error occurred")
                task.completeExceptionally(exception)
            }
        }
        task.await()
    }
    //signInWithGoogle
    suspend fun signInWithGoogle(idToken: String): AuthResult {
        val authCredential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        return firebaseAuth.signInWithCredential(authCredential).await()
    }
    //logout
    fun logout() = firebaseAuth.signOut()

}