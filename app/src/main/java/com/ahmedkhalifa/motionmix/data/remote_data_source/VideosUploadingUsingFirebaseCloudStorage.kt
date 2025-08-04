package com.ahmedkhalifa.motionmix.data.remote_data_source

import android.net.Uri
import android.util.Log
import com.ahmedkhalifa.motionmix.common.utils.UploadProgressListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class VideosUploadingUsingFirebaseCloudStorage @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val firebaseAuth: FirebaseAuth
) {



    fun uploadVideo(videoUri: Uri, listener: UploadProgressListener) {
//        val userUid = firebaseAuth.currentUser?.uid ?: run {
//            listener.onFailure()
//            return
//        }

        val fileName = "${System.currentTimeMillis()}.mp4"
        val storageRef = firebaseStorage.reference
            .child("reels/$55/$fileName")

        val uploadTask = storageRef.putFile(videoUri)

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            listener.onProgress(progress)
        }.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val mediaUrl = uri.toString()
                val thumbnailUrl = mediaUrl.replace(".mp4", "_thumb.jpg")
                listener.onSuccess(mediaUrl, thumbnailUrl)
            }.addOnFailureListener {
                listener.onFailure()
            }
        }.addOnFailureListener {
            listener.onFailure()
        }
    }
}