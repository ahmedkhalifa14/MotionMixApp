package com.ahmedkhalifa.motionmix.data.remote_data_source

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ahmedkhalifa.motionmix.data.model.Comment
import com.ahmedkhalifa.motionmix.data.model.Reel
import com.ahmedkhalifa.motionmix.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.tasks.asDeferred

class FireStoreService @Inject constructor(
    private val firebaseFireStore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) {

    private val reelsCollection = firebaseFireStore.collection("reels")
    private val usersCollection = firebaseFireStore.collection("Users")

    ///******************* Reels ***********************///
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

            reelsCollection.document(reel.id).set(reel).await()
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

            lastDocument?.let { query = query.startAfter(it) }

            val snapshot = query.get().await()

            val reels = snapshot.documents.mapNotNull { doc ->
                try {
                    if (doc.exists()) {
                        val reel = doc.toObject<Reel>()
                        reel?.let {
                            val comments = getCommentsForReel(it.id)
                            it.copy(comments = comments, id = doc.id)
                        }
                    } else null
                } catch (e: Exception) {
                    null
                }
            }

            val lastDoc = snapshot.documents.lastOrNull()
            Pair(reels, lastDoc)
        } catch (e: Exception) {
            Pair(emptyList(), null)
        }
    }

    suspend fun getCommentsForReel(reelId: String): List<Comment> {
        return try {
            val snapshot = reelsCollection.document(reelId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject<Comment>()?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
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
            val reelDoc = reelsCollection.document(reelId).get().await()
            if (!reelDoc.exists()) return false

            val commentRef = reelsCollection.document(reelId)
                .collection("comments")
                .document(comment.id)
            commentRef.set(comment).await()

            reelsCollection.document(reelId)
                .update("commentsCount", FieldValue.increment(1))
                .await()

            val addedComment = commentRef.get().await()
            addedComment.exists()
        } catch (e: Exception) {
            false
        }
    }

    /// ******************* User *********************** ///

    suspend fun saveUserInfo(user: User, imageUri: Uri?, context: Context) {
        val currentUser = firebaseAuth.currentUser ?: return
        val documentReference = usersCollection.document(currentUser.uid)

        try {
            val imageUrl = imageUri?.let { uploadImageToFirebaseStorage(it, context) }
            val updatedUser = if (imageUrl != null) {
                user.copy(userId = currentUser.uid, profilePictureLink = imageUrl)
            } else {
                user.copy(userId = currentUser.uid)
            }
            documentReference.set(updatedUser, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.d("FirebaseService", "saveUserInfo: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getUserInfo(): User? {
        val currentUser = firebaseAuth.currentUser ?: return null
        val documentReference = usersCollection.document(currentUser.uid)
        return try {
            val documentSnapshot = documentReference.get().await()
            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(User::class.java)?.copy(userId = documentSnapshot.id)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserInfo(user: User) {
        val currentUser = firebaseAuth.currentUser ?: return
        val documentReference = usersCollection.document(currentUser.uid)
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
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri) ?: return null
            val imageBytes = inputStream.use { it.readBytes() }

            storageReference.putBytes(imageBytes).await()
            storageReference.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    /// ******************* Follow System *********************** ///

    suspend fun followUser(targetUserId: String): Result<Unit> {
        val currentUserId = firebaseAuth.currentUser?.uid
            ?: return Result.failure(Exception("User not authenticated"))
        if (currentUserId == targetUserId) return Result.failure(Exception("Cannot follow self"))

        return try {
            val batch = firebaseFireStore.batch()

            val followingRef = usersCollection.document(currentUserId)
                .collection("following").document(targetUserId)
            batch.set(followingRef, mapOf("followedAt" to FieldValue.serverTimestamp()))

            val followersRef = usersCollection.document(targetUserId)
                .collection("followers").document(currentUserId)
            batch.set(followersRef, mapOf("followedAt" to FieldValue.serverTimestamp()))

            batch.update(usersCollection.document(currentUserId), "numberOfFollowing", FieldValue.increment(1))
            batch.update(usersCollection.document(targetUserId), "numberOfFollowers", FieldValue.increment(1))

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(targetUserId: String): Result<Unit> {
        val currentUserId = firebaseAuth.currentUser?.uid
            ?: return Result.failure(Exception("User not authenticated"))
        if (currentUserId == targetUserId) return Result.failure(Exception("Cannot unfollow self"))

        return try {
            val batch = firebaseFireStore.batch()

            val followingRef = usersCollection.document(currentUserId)
                .collection("following").document(targetUserId)
            batch.delete(followingRef)

            val followersRef = usersCollection.document(targetUserId)
                .collection("followers").document(currentUserId)
            batch.delete(followersRef)

            batch.update(usersCollection.document(currentUserId), "numberOfFollowing", FieldValue.increment(-1))
            batch.update(usersCollection.document(targetUserId), "numberOfFollowers", FieldValue.increment(-1))

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isFollowing(targetUserId: String): Boolean {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return false
        return try {
            val doc = usersCollection.document(currentUserId)
                .collection("following").document(targetUserId)
                .get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    /// ******************* Queries *********************** ///

    suspend fun getFollowers(
        userId: String,
        limit: Long = 20,
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<User>, DocumentSnapshot?> {
        return try {
            var query = usersCollection.document(userId)
                .collection("followers")
                .orderBy("followedAt", Query.Direction.DESCENDING)
                .limit(limit)

            lastDocument?.let { query = query.startAfter(it) }

            val snapshot = query.get().await()
            val followerIds = snapshot.documents.map { it.id }
            if (followerIds.isEmpty()) return Pair(emptyList(), null)

            val userDocs: List<Task<DocumentSnapshot>> = followerIds.map { usersCollection.document(it).get() }
            val users = awaitAll(*userDocs.map { it.asDeferred() }.toTypedArray())
                .mapNotNull { doc: DocumentSnapshot -> doc.toObject<User>()?.copy(userId = doc.id) }

            val lastDoc = snapshot.documents.lastOrNull()
            Pair(users, lastDoc)
        } catch (e: Exception) {
            Pair(emptyList(), null)
        }
    }

    suspend fun getFollowing(
        userId: String,
        limit: Long = 20,
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<User>, DocumentSnapshot?> {
        return try {
            var query = usersCollection.document(userId)
                .collection("following")
                .orderBy("followedAt", Query.Direction.DESCENDING)
                .limit(limit)

            lastDocument?.let { query = query.startAfter(it) }

            val snapshot = query.get().await()
            val followingIds = snapshot.documents.map { it.id }
            if (followingIds.isEmpty()) return Pair(emptyList(), null)

            val userDocs: List<Task<DocumentSnapshot>> = followingIds.map { usersCollection.document(it).get() }
            val users = userDocs.map { task -> task.await() }
                .mapNotNull { doc: DocumentSnapshot -> doc.toObject<User>()?.copy(userId = doc.id) }

            val lastDoc = snapshot.documents.lastOrNull()
            Pair(users, lastDoc)
        } catch (e: Exception) {
            Pair(emptyList(), null)
        }
    }

    suspend fun getUserSuggestions(limit: Long = 10): List<User> {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return emptyList()
        return try {
            val followingIds = usersCollection.document(currentUserId)
                .collection("following").get().await()
                .documents.map { it.id }

            val excludeIds = (followingIds + currentUserId).distinct().take(10)

            val query = usersCollection
                .whereNotIn(FieldPath.documentId(), if (excludeIds.isEmpty()) listOf("") else excludeIds)
                .orderBy("joinedAt", Query.Direction.DESCENDING)
                .limit(limit)

            query.get().await().toObjects(User::class.java).mapIndexed { idx, u ->
                u.copy(userId = query.get().await().documents[idx].id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getNearbySuggestions(
        currentLat: Double,
        currentLon: Double,
        radiusKm: Double = 50.0,
        limit: Int = 10
    ): List<User> {
        val currentUserId = firebaseAuth.currentUser?.uid ?: return emptyList()
        return try {
            val followingIds = usersCollection.document(currentUserId)
                .collection("following").get().await()
                .documents.map { it.id }.toSet()

            val allUsers = usersCollection.get().await().toObjects(User::class.java)
            allUsers.filter { user ->
                user.userId != currentUserId &&
                        !followingIds.contains(user.userId) &&
                        user.latitude.isNotEmpty() &&
                        user.longitude.isNotEmpty()
            }.mapNotNull { user ->
                try {
                    val dist = calculateDistance(
                        currentLat, currentLon,
                        user.latitude.toDouble(), user.longitude.toDouble()
                    )
                    Pair(user.copy(userId = user.userId), dist)
                } catch (e: NumberFormatException) {
                    null
                }
            }.filter { it.second <= radiusKm }
                .sortedBy { it.second }
                .take(limit)
                .map { it.first }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFollowCounts(userId: String): Triple<Int, Int, Int> {
        return try {
            val userDoc = usersCollection.document(userId).get().await()
            val user = userDoc.toObject<User>()
            val followersCount = user?.numberOfFollowers ?: 0
            val followingCount = user?.numberOfFollowing ?: 0

            val followingSnapshot = usersCollection.document(userId)
                .collection("following").get().await()
            val followingIds = followingSnapshot.documents.map { it.id }.toSet()

            val followerSnapshot = usersCollection.document(userId)
                .collection("followers").get().await()
            val followerIds = followerSnapshot.documents.map { it.id }.toSet()

            val friendsCount = followingIds.intersect(followerIds).size
            Triple(followersCount, followingCount, friendsCount)
        } catch (e: Exception) {
            Triple(0, 0, 0)
        }
    }

    suspend fun getFriends(
        userId: String,
        limit: Long = 20,
        lastDocument: DocumentSnapshot? = null
    ): Pair<List<User>, DocumentSnapshot?> {
        return try {
            var followingQuery = usersCollection.document(userId)
                .collection("following")
                .orderBy("followedAt", Query.Direction.DESCENDING)
                .limit(limit)

            lastDocument?.let { followingQuery = followingQuery.startAfter(it) }

            val followingSnapshot = followingQuery.get().await()
            val followingIds = followingSnapshot.documents.map { it.id }
            if (followingIds.isEmpty()) return Pair(emptyList(), null)

            val followerSnapshot = usersCollection.document(userId)
                .collection("followers").get().await()
            val followerIds = followerSnapshot.documents.map { it.id }.toSet()

            val friendIds = followingIds.filter { it in followerIds }
            if (friendIds.isEmpty()) return Pair(emptyList(), null)

            val userDocs: List<Task<DocumentSnapshot>> = friendIds.take(limit.toInt())
                .map { usersCollection.document(it).get() }
            val users = userDocs.map { task -> task.await() }
                .mapNotNull { doc: DocumentSnapshot -> doc.toObject<User>()?.copy(userId = doc.id) }

            val lastDoc = followingSnapshot.documents.lastOrNull()
            Pair(users, lastDoc)
        } catch (e: Exception) {
            Pair(emptyList(), null)
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
