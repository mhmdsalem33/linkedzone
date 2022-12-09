package com.example.linkedzone.di

import com.example.linkedzone.Services.DemoWorkerDependencies
import com.example.linkedzone.ui.viewmodel.AddPostViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
    @Singleton
    fun provideAuth()      = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore() = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideStorage()  = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideRealTimeDatabase() = FirebaseDatabase.getInstance()


    @Singleton
    @Provides
    fun provideDemoWork() : DemoWorkerDependencies {
        return DemoWorkerDependencies()
    }

   // @Singleton
    //@Provides
    //fun provideAddPostViewModel() : AddPostViewModel = AddPostViewModel( provideAuth() , provideFirestore() )



}

