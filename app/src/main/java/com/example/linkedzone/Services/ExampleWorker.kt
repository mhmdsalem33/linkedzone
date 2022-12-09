package com.example.linkedzone.Services

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.Worker
import androidx.work.WorkerParameters

import com.example.linkedzone.ui.viewmodel.AddPostViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ExampleWorker @AssistedInject constructor(
    @Assisted appContext           : Context                 ,
    @Assisted workerParams         : WorkerParameters        ,
    private val demo               : DemoWorkerDependencies  ,
    //private val postRepository   : PostRepository          ,
    //private val addPostMvvm      : AddPostViewModel

) : Worker(appContext, workerParams)
{


    override fun doWork(): Result {


        for (i in 0..5)
            Log.d("testApp" , i.toString())


       // addPostMvvm.uploadPostDetails()

        return Result.success()
    }

}