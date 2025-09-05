package com.ahmedkhalifa.motionmix.di

import android.app.NotificationManager
import android.content.Context
import com.ahmedkhalifa.motionmix.services.VideoUploadingNotificationHandler
import com.ahmedkhalifa.motionmix.data.local_data_source.datastore.DataStoreManager
import com.ahmedkhalifa.motionmix.data.remote_data_source.FireStoreService
import com.ahmedkhalifa.motionmix.data.remote_data_source.FirebaseAuthenticationService
import com.ahmedkhalifa.motionmix.data.remote_data_source.chat.ChatFireStoreInterface
import com.ahmedkhalifa.motionmix.data.remote_data_source.chat.ChatMediaInterface
import com.ahmedkhalifa.motionmix.data.repository.app_pref.AppPreferencesRepoImpl
import com.ahmedkhalifa.motionmix.data.repository.auth.AuthRepoImpl
import com.ahmedkhalifa.motionmix.data.repository.chat.ChatRepositoryImpl
import com.ahmedkhalifa.motionmix.data.repository.follow.FollowRepositoryImpl
import com.ahmedkhalifa.motionmix.data.repository.post_reel.ReelRepositoryImpl
import com.ahmedkhalifa.motionmix.data.repository.post_reel.VideoUploadRepositoryImpl
import com.ahmedkhalifa.motionmix.data.repository.reel_actions.ReelActionsRepoImpl
import com.ahmedkhalifa.motionmix.data.repository.user_profile.UserProfileRepoImpl
import com.ahmedkhalifa.motionmix.domain.repo.app_preferences.AppPreferencesRepo
import com.ahmedkhalifa.motionmix.domain.repo.auth.AuthRepo
import com.ahmedkhalifa.motionmix.domain.repo.chat.ChatRepository
import com.ahmedkhalifa.motionmix.domain.repo.follow.FollowRepository
import com.ahmedkhalifa.motionmix.domain.repo.reel_actions.ReelActionsRepo
import com.ahmedkhalifa.motionmix.domain.repo.user_profile.UserProfileRepo
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
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuthenticationService: FirebaseAuthenticationService,
    ): AuthRepo = AuthRepoImpl(firebaseAuthenticationService)

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
    fun provideAppPreferencesRepo(
        dataStoreManager: DataStoreManager
    ): AppPreferencesRepo = AppPreferencesRepoImpl(dataStoreManager)


    @Provides
    fun provideUserProfileRepo(
        fireStoreService: FireStoreService
    ): UserProfileRepo = UserProfileRepoImpl(fireStoreService)


    @Provides
    fun provideReelActionsRepo(
        fireStoreService: FireStoreService
    ): ReelActionsRepo = ReelActionsRepoImpl(fireStoreService)


    @Provides
    fun provideChatRepo(
        chatFireStoreInterface: ChatFireStoreInterface,
        chatMediaInterface: ChatMediaInterface,
        firebaseAuthenticationService: FirebaseAuthenticationService
    ): ChatRepository = ChatRepositoryImpl(chatFireStoreInterface,chatMediaInterface,firebaseAuthenticationService)

    @Provides
    @Singleton
    fun provideFollowRepository(fireStoreService: FireStoreService): FollowRepository =
        FollowRepositoryImpl(fireStoreService)



    @Provides
    fun provideUploadVideoUseCase(
        repository: VideoUploadRepository
    ): UploadVideoUseCase = UploadVideoUseCase(repository)


    @Provides
    fun provideSaveReelUseCase(
        repository: ReelRepository
    ): SaveReelUseCase = SaveReelUseCase(repository)


}
