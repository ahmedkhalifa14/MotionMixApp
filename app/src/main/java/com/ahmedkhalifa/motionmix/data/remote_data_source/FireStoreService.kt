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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
    suspend fun getReelsPaginated(
        limit: Long = 10,
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<Reel>, DocumentSnapshot?> {
        return try {
            var query = reelsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)

            lastDocument?.let {
                query = query.startAfter(it)
            }

            val snapshot = query.get().await()

            val reels = snapshot.documents.mapNotNull { doc ->
                try {
                    if (doc.exists()) {
                        val reel = doc.toObject<Reel>()
                        reel?.let {
                            val comments = getCommentsForReel(it.id)
                            it.copy(comments = comments, id = doc.id)
                        }
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    e
                    null
                }
            }

            val lastDoc = snapshot.documents.lastOrNull()
            Pair(reels, lastDoc)
        } catch (e: Exception) {
            e
            Pair(emptyList(), null)
        }
    }

    suspend fun getCommentsForReel(reelId: String): List<Comment> {
        return try {
            val snapshot = reelsCollection
                .document(reelId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val comments = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject<Comment>()?.copy(id = doc.id)
                } catch (e: Exception) {
                    e
                    null
                }
            }

            comments
        } catch (e: Exception) {
            e
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
            e
            null
        }
    }

    suspend fun addComment(reelId: String, comment: Comment): Boolean {
        return try {
            val reelDoc = reelsCollection.document(reelId).get().await()
            if (!reelDoc.exists()) {
                return false
            }

            val commentRef = reelsCollection
                .document(reelId)
                .collection("comments")
                .document(comment.id)
            commentRef.set(comment).await()

            reelsCollection.document(reelId)
                .update("commentsCount", FieldValue.increment(1))
                .await()
            val addedComment = commentRef.get().await()
            addedComment.exists()
        } catch (e: Exception) {
            e
            false
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
            e
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
            // Check if file exists first
            val contentResolver = context.contentResolver

            // Try to get the actual file data immediately
            val inputStream = contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                return null
            }

            // Read all bytes into memory to avoid file deletion issues
            val imageBytes = inputStream.use { it.readBytes() }
            // Upload using byte array
            val uploadTask = storageReference.putBytes(imageBytes).await()
            val downloadUrl = storageReference.downloadUrl.await().toString()
            downloadUrl
        } catch (e: Exception) {
            e
            null
        }
    }


}
