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
    // Use pagination for better performance
    // In FireStoreService
// في FireStoreService - أضف logging مفصل
    suspend fun getReelsPaginated(limit: Long = 10, lastDocument: DocumentSnapshot? = null): Pair<List<Reel>, DocumentSnapshot?> {
        return try {
            Log.d("FireStoreService", "Getting reels with limit: $limit")

            var query = reelsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)

            lastDocument?.let {
                query = query.startAfter(it)
                Log.d("FireStoreService", "Starting after document: ${it.id}")
            }

            Log.d("FireStoreService", "Executing query...")
            val snapshot = query.get().await()
            Log.d("FireStoreService", "Query result: ${snapshot.documents.size} documents found")

            // جيب التعليقات لكل reel باستخدام async
            val reels = snapshot.documents.mapNotNull { doc ->
                try {
                    if (doc.exists()) {
                        val reel = doc.toObject<Reel>()
                        reel?.let {
                            // جيب التعليقات من الـ subcollection
                            val comments = getCommentsForReel(it.id)
                            it.copy(comments = comments, id = doc.id)
                        }
                    } else {
                        Log.w("FireStoreService", "Document ${doc.id} doesn't exist")
                        null
                    }
                } catch (e: Exception) {
                    Log.e("FireStoreService", "Error converting document ${doc.id}: ${e.message}", e)
                    null
                }
            }

            val lastDoc = snapshot.documents.lastOrNull()
            Log.d("FireStoreService", "Returning ${reels.size} reels, lastDoc: ${lastDoc?.id}")

            Pair(reels, lastDoc)
        } catch (e: Exception) {
            Log.e("FireStoreService", "Error getting reels: ${e.message}", e)
            Pair(emptyList(), null)
        }
    }

    // دالة جديدة علشان تجيب التعليقات من الـ subcollection
     suspend fun getCommentsForReel(reelId: String): List<Comment> {
        return try {
            Log.d("FireStoreService", "Getting comments for reel: $reelId")

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
                    Log.e("FireStoreService", "Error converting comment ${doc.id}: ${e.message}", e)
                    null
                }
            }

            Log.d("FireStoreService", "Found ${comments.size} comments for reel: $reelId")
            comments
        } catch (e: Exception) {
            Log.e("FireStoreService", "Error getting comments for reel $reelId: ${e.message}", e)
            emptyList()
        }
    }// Add compound index: timestamp (descending), isActive (ascending)


//    suspend fun getReels(): List<Reel> {
//        return try {
//            val snapshot = reelsCollection.get().await()
//            snapshot.documents.mapNotNull { doc ->
//                try {
//                    val reel = doc.toObject<Reel>()
//                    reel?.copy(
//                        comments = getComments(reel.id)
//                    )
//                } catch (e: Exception) {
//                    null
//                }
//            }
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }

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
            Log.d("FireStoreService", "🟡 Starting addComment for reel: $reelId")
            Log.d("FireStoreService", "🟡 Comment data: $comment")

            // ١. تأكد إن الـ reel موجود أصلاً
            val reelDoc = reelsCollection.document(reelId).get().await()
            if (!reelDoc.exists()) {
                Log.e("FireStoreService", "🔴 Reel doesn't exist: $reelId")
                return false
            }

            Log.d("FireStoreService", "🟢 Reel exists, adding comment...")

            // ٢. Add comment to subcollection
            val commentRef = reelsCollection
                .document(reelId)
                .collection("comments")
                .document(comment.id)

            Log.d("FireStoreService", "🟡 Firestore path: ${commentRef.path}")

            commentRef.set(comment).await()
            Log.d("FireStoreService", "✅ Comment document set successfully")

            // ٣. Update comments count in the reel document
            reelsCollection.document(reelId)
                .update("commentsCount", FieldValue.increment(1))
                .await()

            Log.d("FireStoreService", "✅ Comments count updated successfully")

            // ٤. Verify that the comment was actually added
            val addedComment = commentRef.get().await()
            if (addedComment.exists()) {
                Log.d("FireStoreService", "✅ Comment verified in Firestore")
                true
            } else {
                Log.e("FireStoreService", "🔴 Comment not found after adding!")
                false
            }

        } catch (e: Exception) {
            Log.e("FireStoreService", "🔴 Exception in addComment: ${e.message}", e)
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
