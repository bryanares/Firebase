package com.example.firebaseathentication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AuthApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //FirebaseApp.initializeApp(this)
    }

}