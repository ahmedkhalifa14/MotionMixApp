package com.ahmedkhalifa.motionmix.domain.repo.user_profile

import android.content.Context
import android.net.Uri
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.data.model.User

interface UserProfileRepo {
    suspend fun saveUserInfo(user: User,imageUri: Uri?,context: Context): Resource<Unit>
    suspend fun getUserInfo(): Resource<User?>
    suspend fun updateUserInfo(user: User): Resource<Unit>
}