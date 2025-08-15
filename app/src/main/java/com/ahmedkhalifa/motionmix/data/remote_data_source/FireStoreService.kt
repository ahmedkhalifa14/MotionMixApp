package com.ahmedkhalifa.motionmix.data.remote_data_source

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ahmedkhalifa.motionmix.data.model.Comment
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.ahmedkhalifa.motionmix.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject


class FireStoreService @Inject constructor(
    private val firebaseFireStore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) {


    ///*******************Reels***********************///
    private val reelsCollection = firebaseFireStore.collection("reels")
    // Save Reel Metadata to FireStore
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
                isLiked = false,
                likedUserIds = emptyList(),
                comments = emptyList()
            )

            reelsCollection
                .document(reel.id)
                .set(reel)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun getReels(): List<Reel> {
        return try {
            val snapshot = reelsCollection.get().await()
            Log.d("ReelsRepo", "Snapshot size: ${snapshot.documents.size}")
            snapshot.documents.mapNotNull { doc ->
                try {
                    val reel = doc.toObject<Reel>()
                    if (reel == null) {
                        Log.e("ReelsRepo", "toObject failed for doc ID: ${doc.id}")
                    }
                    reel?.copy(
                        comments = getComments(reel.id)
                    )
                } catch (e: Exception) {
                    Log.e("ReelsRepo", "Error in map for doc ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("ReelsRepo", "Error fetching reels", e)
            emptyList()
        }
    }

    suspend fun toggleLike(reelId: String, userId: String, isLiked: Boolean): Reel? {
        val reelRef = reelsCollection.document(reelId)
        return try {
            firebaseFireStore.runTransaction { transaction ->
                val snapshot = transaction.get(reelRef)
                val reel = snapshot.toObject<Reel>() ?: return@runTransaction null
                val newLikedUserIds = if (isLiked) {
                    reel.likedUserIds + userId
                } else {
                    reel.likedUserIds - userId
                }
                val newLikesCount = newLikedUserIds.size
                transaction.update(
                    reelRef,
                    mapOf(
                        "likedUserIds" to newLikedUserIds,
                        "likesCount" to newLikesCount
                    )
                )
                reel.copy(
                    likedUserIds = newLikedUserIds,
                    likesCount = newLikesCount,
                    isLiked = isLiked
                )
            }.await()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addComment(reelId: String, comment: Comment): Boolean {
        return try {
            reelsCollection
                .document(reelId)
                .collection("comments")
                .document(comment.id)
                .set(comment)
                .await()
            reelsCollection
                .document(reelId)
                .update("commentsCount", FieldValue.increment(1))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun getComments(reelId: String): List<Comment> {
        return try {
            val snapshot = reelsCollection
                .document(reelId)
                .collection("comments")
                .orderBy("timestamp")
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject<Comment>() }
        } catch (e: Exception) {
            emptyList()
        }
    }




    // USER ACCOUNT DATA
    suspend fun saveUserInfo(user: User, imageUri: Uri?, context: Context) {
        val currentUser = firebaseAuth.currentUser ?: return
        val documentReference = firebaseFireStore.collection("Users")
            .document(currentUser.uid)

        try {
            val imageUrl = imageUri?.let { uploadImageToFirebaseStorage(it, context) }
            Log.d("imageUrl", imageUrl.toString())
            val updatedUser =
                if (imageUrl != null) user.copy(profilePictureLink = imageUrl) else user

            documentReference.set(updatedUser, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.d("FirebaseService", "saveUserInfo: ${e.message}")
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
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
