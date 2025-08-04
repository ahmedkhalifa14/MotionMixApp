package com.ahmedkhalifa.motionmix.data.remote_data_source


import android.content.Context
import android.net.Uri
import android.util.Log
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.ahmedkhalifa.motionmix.data.model.User
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class FireStoreService @Inject constructor(
    val firebaseFireStore: FirebaseFirestore,
    val firebaseAuth: FirebaseAuth,
    val firebaseStorage: FirebaseStorage
) {

    suspend fun saveReelToFireStore(
        mediaUrl: String,
        thumbnailUrl: String,
        description: String
    ): Result<Unit> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))
            val reel = Reel(
                id = UUID.randomUUID().toString(),
                mediaUrl = mediaUrl,
                thumbnailUrl = thumbnailUrl,
                description = description,
                author = userId,
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

    // USER ACCOUNT DATA

//    suspend fun saveUserInfo(user: User) {
//        // Check if the user is logged in
//        val currentUser = firebaseAuth.currentUser ?: return
//        val documentReference = firebaseFireStore.collection("Users")
//            .document(currentUser.uid)
//        try {
//            documentReference.set(user, SetOptions.merge()).await()
//        } catch (e: Exception) {
//            Log.d(
//                "FirebaseService", "saveUserInfo: ${e.message}"
//            )
//        }
//    }

    suspend fun saveUserInfo(user: User, imageUri: Uri?,context: Context) {
        val currentUser = firebaseAuth.currentUser ?: return
        val documentReference = firebaseFireStore.collection("Users")
            .document(currentUser.uid)

        try {
            val imageUrl = imageUri?.let { uploadImageToFirebaseStorage(it,context) }
            Log.d("imageUrl",imageUrl.toString())
            val updatedUser = if (imageUrl != null) user.copy(profilePictureLink = imageUrl) else user

            documentReference.set(updatedUser, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.d("FirebaseService", "saveUserInfo: ${e.message}")
        }
    }


    suspend fun getUserInfo(): User? {
        val currentUser = firebaseAuth.currentUser
            ?: return null
        val documentReference = firebaseFireStore.collection("Users")
            .document(currentUser.uid)
        return try {
            val documentSnapshot = documentReference.get().await()
            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }


    suspend fun updateUserInfo(user: User) {
        val currentUser = firebaseAuth.currentUser ?: return
        val documentReference = firebaseFireStore.collection("Users")
            .document(currentUser.uid)
        Log.d("FirebaseService", "updateUserInfo: ${currentUser.uid}")
        try {
            documentReference.set(
                mapOf(
                    "firstName" to user.firstName,
                    "lastName" to user.lastName,
                    "username" to user.username,
                    "phoneNumber" to user.phoneNumber,
                    "email" to user.email,
                    "joinedAt" to user.joinedAt,
                    "location" to user.location,
                    "latitude" to user.latitude,
                    "longitude" to user.longitude,
                    "profilePictureLink" to user.profilePictureLink,
                    "numberOfFollowers" to user.numberOfFollowers,
                    "numberOfFollowing" to user.numberOfFollowing,
                    "likes" to user.likes
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.d("FirebaseService", "updateUserInfo: ${e.message}")
        }
    }





    suspend fun uploadImageToFirebaseStorage(imageUri: Uri, context: Context): String? {
        val currentUser = firebaseAuth.currentUser ?: return null
        val storageReference = firebaseStorage.reference
            .child("profile_images/${currentUser.uid}/${UUID.randomUUID()}.jpg")

        return try {
            Log.d("FirebaseService", "Starting upload for URI: $imageUri")

            // Check if file exists first
            val contentResolver = context.contentResolver

            // Try to get the actual file data immediately
            val inputStream = contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e("FirebaseService", "Cannot open input stream for URI: $imageUri")
                return null
            }

            // Read all bytes into memory to avoid file deletion issues
            val imageBytes = inputStream.use { it.readBytes() }
            Log.d("FirebaseService", "Read ${imageBytes.size} bytes from image")

            // Upload using byte array
            val uploadTask = storageReference.putBytes(imageBytes).await()
            Log.d("FirebaseService", "Upload task completed")

            val downloadUrl = storageReference.downloadUrl.await().toString()
            Log.d("FirebaseService", "Image uploaded successfully: $downloadUrl")

            downloadUrl
        } catch (e: Exception) {
            Log.e("FirebaseService", "Image upload failed: ${e.message}", e)
            null
        }
    }



}
