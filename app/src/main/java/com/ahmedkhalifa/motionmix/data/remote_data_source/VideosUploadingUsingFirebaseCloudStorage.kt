package com.ahmedkhalifa.motionmix.data.remote_data_source

import android.net.Uri
import com.ahmedkhalifa.motionmix.common.utils.UploadProgressListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

class VideosUploadingUsingFirebaseCloudStorage @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val firebaseAuth: FirebaseAuth,

) {

    fun uploadVideo(videoUri: Uri, listener: UploadProgressListener) {
        val userUid = firebaseAuth.currentUser?.uid ?: return
        val storageRef = firebaseStorage.reference
            .child("reels/$userUid/${System.currentTimeMillis()}.mp4")
        val uploadTask = storageRef.putFile(videoUri)
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress =
                (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            listener.onProgress(progress)
        }.addOnSuccessListener {
            listener.onSuccess()
        }.addOnFailureListener {
            listener.onFailure()
        }
    }

}



