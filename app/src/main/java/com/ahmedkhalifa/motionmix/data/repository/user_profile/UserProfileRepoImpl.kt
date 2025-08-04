package com.ahmedkhalifa.motionmix.data.repository.user_profile

import android.content.Context
import android.net.Uri
import com.ahmedkhalifa.motionmix.common.utils.Resource
import com.ahmedkhalifa.motionmix.common.utils.Utils.tryCatch
import com.ahmedkhalifa.motionmix.data.model.User
import com.ahmedkhalifa.motionmix.data.remote_data_source.FireStoreService
import com.ahmedkhalifa.motionmix.domain.repo.user_profile.UserProfileRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserProfileRepoImpl @Inject constructor(
    private val fireStoreService: FireStoreService
) : UserProfileRepo {
    override suspend fun saveUserInfo(user: User,imageUri: Uri?,context: Context): Resource<Unit> =
        withContext(Dispatchers.IO) {
            tryCatch {
                val saveUserProfileResult = fireStoreService.saveUserInfo(user, imageUri = imageUri,context)
                Resource.Success(saveUserProfileResult)
            }
        }


    override suspend fun getUserInfo(): Resource<User?> =
        withContext(Dispatchers.IO) {
            tryCatch {
                val getUserProfileDataResult = fireStoreService.getUserInfo()
                Resource.Success(getUserProfileDataResult)
            }
        }


    override suspend fun updateUserInfo(user: User): Resource<Unit> =
        withContext(Dispatchers.IO) {
            tryCatch {
                val updateUserProfileResult = fireStoreService.updateUserInfo(user)
                Resource.Success(updateUserProfileResult)
            }
        }

}