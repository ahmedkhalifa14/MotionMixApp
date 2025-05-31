package com.ahmedkhalifa.motionmix.domain.repo.auth

import com.ahmedkhalifa.motionmix.common.utils.LoginResult
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.google.firebase.auth.AuthResult

interface AuthRepo {
    suspend fun registerWithEmailAndPassword(email: String, password: String):Resource<Unit>
    suspend fun loginWithEmailAndPassword(email: String, password: String): Resource<LoginResult>
    suspend fun sendVerificationCode(phoneNumber: String): Resource<String>
    suspend fun verifyCode(verificationId: String, code: String):Resource<Unit>
    suspend fun signInWithGoogle(idToken: String): Resource<AuthResult>
    suspend fun logout() :Resource<Unit>
}