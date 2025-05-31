package com.ahmedkhalifa.motionmix.di

import com.ahmedkhalifa.motionmix.data.remote_data_source.FirebaseService
import com.ahmedkhalifa.motionmix.data.repository.AuthRepoImpl
import com.ahmedkhalifa.motionmix.domain.repo.auth.AuthRepo
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
    ) =applicationContext

    @Provides
    @Singleton
    fun provideAuthRepository(
        fireBaseService: FirebaseService,
    ): AuthRepo = AuthRepoImpl(fireBaseService)

}