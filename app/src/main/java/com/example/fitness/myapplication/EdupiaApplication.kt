package com.example.fitness.myapplication

import android.app.Application
import com.example.fitness.myapplication.core.di.AppContainer

class EdupiaApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = AppContainer(this)
    }

    companion object {
        lateinit var instance: EdupiaApplication
            private set
    }
}
