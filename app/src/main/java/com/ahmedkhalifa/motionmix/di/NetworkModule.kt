package com.ahmedkhalifa.motionmix.di

import com.ahmedkhalifa.motionmix.data.remote_data_source.FirebaseAuthenticationService
import com.ahmedkhalifa.motionmix.data.remote_data_source.VideosUploadingUsingFirebaseCloudStorage
import com.ahmedkhalifa.motionmix.data.remote_data_source.chat.ChatFireStoreInterface
import com.ahmedkhalifa.motionmix.data.remote_data_source.chat.ChatFireStoreService
import com.ahmedkhalifa.motionmix.data.remote_data_source.chat.ChatFirebaseStorage
import com.ahmedkhalifa.motionmix.data.remote_data_source.chat.ChatMediaInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFireBaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFireStore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuthenticationService(
        firebaseAuth: FirebaseAuth,
    ): FirebaseAuthenticationService =
        FirebaseAuthenticationService(firebaseAuth)


    @Provides
    @Singleton
    fun provideVideosUploadingUsingFirebaseCloudStorage(
        firebaseStorage: FirebaseStorage,
        firebaseAuth: FirebaseAuth,
    ): VideosUploadingUsingFirebaseCloudStorage =
        VideosUploadingUsingFirebaseCloudStorage(firebaseStorage, firebaseAuth)

    @Provides
    @Singleton
    fun provideChatFirebaseStorage(
        firebaseStorage: FirebaseStorage
    ): ChatMediaInterface =
        ChatFirebaseStorage(firebaseStorage)

    @Provides
    @Singleton
    fun provideChatFireStoreService(
        firestore: FirebaseFirestore
    ): ChatFireStoreInterface = ChatFireStoreService(firestore)


}
