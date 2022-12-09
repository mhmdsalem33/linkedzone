package com.example.linkedzone.di

import com.example.linkedzone.data.network.NotificationApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    @Provides
    @Singleton
    fun provideNotification() : NotificationApi =
        Retrofit.Builder()
                .baseUrl("https:fcm.googleapis.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificationApi::class.java)

}