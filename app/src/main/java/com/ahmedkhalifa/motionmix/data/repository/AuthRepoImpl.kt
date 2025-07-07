package com.ahmedkhalifa.motionmix.data.repository

import com.ahmedkhalifa.motionmix.common.utils.LoginResult
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.common.utils.Utils.tryCatch
import com.ahmedkhalifa.motionmix.data.remote_data_source.FirebaseAuthenticationService
import com.ahmedkhalifa.motionmix.domain.repo.auth.AuthRepo
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepoImpl @Inject constructor(
    private val fireBaseService: FirebaseAuthenticationService,
) : AuthRepo {
    override suspend fun registerWithEmailAndPassword(
        email: String,
        password: String
    ): Resource<Unit> =
        withContext(Dispatchers.IO) {
            tryCatch {
                fireBaseService.registerWithEmailAndPassword(email, password)
                Resource.Success(Unit)
            }
        }

    override suspend fun loginWithEmailAndPassword(
        email: String,
        password: String
    ): Resource<LoginResult> =
        withContext(Dispatchers.IO) {
            tryCatch {
                val result = fireBaseService.loginWithEmailAndPassword(email, password)
                Resource.Success(result)
            }
        }

    override suspend fun sendVerificationCode(phoneNumber: String): Resource<String> =
        withContext(Dispatchers.IO) {
            tryCatch {
                val verificationId = fireBaseService.sendVerificationCode(phoneNumber)
                Resource.Success(verificationId)
            }
        }

    override suspend fun verifyCode(
        verificationId: String,
        code: String
    ): Resource<Unit> =
        withContext(Dispatchers.IO) {
            tryCatch {
                fireBaseService.verifyCode(verificationId, code)
                Resource.Success(Unit)
            }
        }

    override suspend fun signInWithGoogle(idToken: String): Resource<AuthResult> =
        withContext(Dispatchers.IO) {
            tryCatch {
                val authResult = fireBaseService.signInWithGoogle(idToken)
                Resource.Success(authResult)
            }
        }
    override suspend fun logout(): Resource<Unit> =
        withContext(Dispatchers.IO) {
            tryCatch {
                fireBaseService.logout()
                Resource.Success(Unit)
            }
        }

}