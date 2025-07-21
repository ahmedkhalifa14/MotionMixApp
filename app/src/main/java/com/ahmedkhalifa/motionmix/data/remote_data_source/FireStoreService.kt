package com.ahmedkhalifa.motionmix.data.remote_data_source


import android.util.Log
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.ahmedkhalifa.motionmix.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FireStoreService @Inject constructor(
    val firebaseFireStore: FirebaseFirestore,
    val firebaseAuth: FirebaseAuth
) {

    suspend fun saveReelToFireStore(
        mediaUrl: String,
        thumbnailUrl: String,
        description: String
    ): Result<Unit> {
        return try {
            // val userId = firebaseAuth.currentUser?.uid
           //    ?: return Result.failure(Exception("User not authenticated"))
            val reel = Reel(
                id = UUID.randomUUID().toString(),
                mediaUrl = mediaUrl,
                thumbnailUrl = thumbnailUrl,
                description = description,
                author = "userId",
                likesCount = 0,
                commentsCount = 0,
                sharesCount = 0,
                isLiked = false
            )

            val db = firebaseFireStore
            db.collection("reels")
                .document(reel.id)
                .set(reel)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun saveUserInfo(user: User) {
        // Check if the user is logged in
        val currentUser = firebaseAuth.currentUser ?: return
        val documentReference = firebaseFireStore.collection("Users")
            .document(currentUser.uid)
        try {
            documentReference.set(user, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.d(
                "FirebaseService", "saveUserInfo: ${e.message}"
            )
        }
    }

}