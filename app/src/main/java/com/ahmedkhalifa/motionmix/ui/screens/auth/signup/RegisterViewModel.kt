package com.ahmedkhalifa.motionmix.ui.screens.auth.signup

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedkhalifa.motionmix.R
import com.ahmedkhalifa.motionmix.common.utils.Event
import com.ahmedkhalifa.motionmix.common.utils.GoogleSignInState
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.GoogleAccountUserInfo
import com.ahmedkhalifa.motionmix.domain.repo.auth.AuthRepo
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.accounts.AccountManager
import androidx.credentials.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.*


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepo: AuthRepo,
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _registerState = MutableStateFlow<Event<Resource<Unit>>>(Event(Resource.Init()))
    val registerState: StateFlow<Event<Resource<Unit>>> = _registerState
    /**
     * Register with email and password
     * @param email: String
     * @param password: String
     */
    fun registerWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _registerState.value = Event(Resource.Loading())
            try {
                authRepo.registerWithEmailAndPassword(email = email, password = password)
                    .let { result ->
                        _registerState.value = Event(result)
                    }
            } catch (e: Exception) {
                _registerState.value = Event(Resource.Error(e.message.toString()))
            }

        }
    }
    private val _sendVerificationCodeState =
        MutableStateFlow<Event<Resource<String>>>(Event(Resource.Init()))
    val sendVerificationCodeState: StateFlow<Event<Resource<String>>> = _sendVerificationCodeState
    /**
     * Send verification code to the user phone number
     * @param phoneNumber: String
     */
    fun sendVerificationCode(phoneNumber: String, countryCode: String = "+20") {
        viewModelScope.launch(Dispatchers.IO) {
            _sendVerificationCodeState.value = Event(Resource.Loading())
            try {
                val fullPhoneNumber = countryCode + phoneNumber
                authRepo.sendVerificationCode(phoneNumber = fullPhoneNumber)
                    .let { result ->
                        _sendVerificationCodeState.value = Event(result)
                    }
            } catch (e: Exception) {
                _sendVerificationCodeState.value = Event(Resource.Error(e.message ?: "Unknown error"))
            }
        }
    }
    private val _verifyCodeState = MutableStateFlow<Event<Resource<String>>>(Event(Resource.Init()))
    val verifyCodeState: StateFlow<Event<Resource<String>>> = _verifyCodeState
    /**
     * Verify the code sent to the user phone number
     * @param verificationId: String
     * @param code: String
     */
    fun verifyCode(verificationId: String, code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _verifyCodeState.value = Event(Resource.Loading())
            try {
                authRepo.verifyCode(verificationId = verificationId, code = code)

                _verifyCodeState.emit(Event(Resource.Success("Verification successful")))

            } catch (e: Exception) {
                _verifyCodeState.value = Event(Resource.Error(e.message.toString()))
            }
        }
    }


//    private val _signupWithGoogleState =
//        MutableStateFlow<Event<Resource<AuthState>>>(Event(Resource.Init()))
//    val signupWithGoogleState: StateFlow<Event<Resource<AuthState>>> = _signupWithGoogleState


    private val _googleSignInState = MutableStateFlow<Event<Resource<GoogleSignInState>>>(Event(Resource.Init()))
    val googleSignInState: StateFlow<Event<Resource<GoogleSignInState>>> = _googleSignInState.asStateFlow()

    fun signInWithGoogle() {
        viewModelScope.launch(Dispatchers.Main) {
            _googleSignInState.value = Event(Resource.Loading())
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(context.getString(R.string.server_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                handleCredentialResult(result)
            } catch (e: GetCredentialException) {
                val errorMessage = when (e.message) {
                    "No credentials available" -> "No Google accounts found. Please add a Google account in device settings."
                    else -> "Google sign-in failed: ${e.message}"
                }
                _googleSignInState.value = Event(Resource.Error(errorMessage))
            } catch (e: Exception) {
                _googleSignInState.value = Event(Resource.Error("An error occurred: ${e.message}"))
            }
        }
    }

    private suspend fun handleCredentialResult(result: GetCredentialResponse) {
        val credential = result.credential
        when (credential.type) {
            "com.google.id.token" -> {
                val idToken = credential.data.getString("id_token")
                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    _googleSignInState.value = Event(Resource.Error("Failed to get ID token"))
                }
            }
            else -> {
                _googleSignInState.value = Event(Resource.Error("Unexpected credential type: ${credential.type}"))
            }
        }
    }

    private suspend fun firebaseAuthWithGoogle(idToken: String) {
        try {
            val authResultResource = authRepo.signInWithGoogle(idToken)
            when (authResultResource) {
                is Resource.Success -> {
                    val firebaseUser = authResultResource.data?.user
                    if (firebaseUser != null) {
                        val user = GoogleAccountUserInfo(
                            id = firebaseUser.uid,
                            displayName = firebaseUser.displayName,
                            email = firebaseUser.email,
                            photoUrl = firebaseUser.photoUrl?.toString()
                        )
                        _googleSignInState.value = Event(Resource.Success(GoogleSignInState.Success(user)))
                    } else {
                        _googleSignInState.value = Event(Resource.Error("Firebase user is null"))
                    }
                }
                is Resource.Error -> {
                    _googleSignInState.value = Event(Resource.Error(authResultResource.message.toString()))
                }
                is Resource.Loading -> {
                    _googleSignInState.value = Event(Resource.Loading())
                }
                is Resource.Init -> {
                    _googleSignInState.value = Event(Resource.Init())
                }
            }
        } catch (e: Exception) {
            _googleSignInState.value = Event(Resource.Error("Firebase error: ${e.message}"))
        }
    }
}





