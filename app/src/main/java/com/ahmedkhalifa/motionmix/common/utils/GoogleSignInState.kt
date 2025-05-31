package com.ahmedkhalifa.motionmix.common.utils

import android.content.Intent
import com.ahmedkhalifa.motionmix.data.model.GoogleAccountUserInfo


sealed class GoogleSignInState {
    object Init : GoogleSignInState()
    class SignInIntent(val intent: Intent) : GoogleSignInState()
    class Success(val user: GoogleAccountUserInfo) : GoogleSignInState()
    class Error(val message: String) : GoogleSignInState()
}