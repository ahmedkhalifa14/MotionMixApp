package com.ahmedkhalifa.motionmix.di

import android.app.NotificationManager
import android.content.Context
import com.ahmedkhalifa.motionmix.VideoUploadingNotificationHandler
import com.ahmedkhalifa.motionmix.data.remote_data_source.FireStoreService
import com.ahmedkhalifa.motionmix.data.remote_data_source.FirebaseAuthenticationService
import com.ahmedkhalifa.motionmix.data.repository.AuthRepoImpl
import com.ahmedkhalifa.motionmix.data.repository.ReelRepositoryImpl
import com.ahmedkhalifa.motionmix.data.repository.VideoUploadRepositoryImpl
import com.ahmedkhalifa.motionmix.domain.repo.auth.AuthRepo
import com.ahmedkhalifa.motionmix.domain.repo.video_upload.ReelRepository
import com.ahmedkhalifa.motionmix.domain.repo.video_upload.VideoUploadRepository
import com.ahmedkhalifa.motionmix.domain.usecase.SaveReelUseCase
import com.ahmedkhalifa.motionmix.domain.usecase.UploadVideoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideApplicationContext(
        @ApplicationContext applicationContext: ApplicationContext
    ) = applicationContext

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuthenticationService: FirebaseAuthenticationService,
    ): AuthRepo = AuthRepoImpl(firebaseAuthenticationService)

//    @Provides
//    @Singleton
//    fun provideVideoUploadRepository(
//        @ApplicationContext context: Context,
//        uploadEventHandler: UploadEventHandler
//    ): VideoUploadRepository =
//        VideoUploadRepository(context, uploadEventHandler)




    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    @Singleton
    fun provideVideoUploadingNotificationHandler(
        @ApplicationContext context: Context,
        notificationManager: NotificationManager
    ): VideoUploadingNotificationHandler =
        VideoUploadingNotificationHandler(context, notificationManager)


    @Provides
    fun provideVideoUploadRepository(
        impl: VideoUploadRepositoryImpl
    ): VideoUploadRepository = impl

    @Provides
    fun provideReelRepository(
        fireStoreService: FireStoreService
    ): ReelRepository = ReelRepositoryImpl(fireStoreService)

    @Provides
    fun provideUploadVideoUseCase(
        repository: VideoUploadRepository
    ): UploadVideoUseCase = UploadVideoUseCase(repository)


    @Provides
    fun provideSaveReelUseCase(
        repository: ReelRepository
    ): SaveReelUseCase = SaveReelUseCase(repository)


}
