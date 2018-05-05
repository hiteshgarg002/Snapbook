package com.hrrock.snapbook

import android.app.Application

/**
 * Created by hp-u on 9/10/2017.
 */

class ApplicationContextReference : Application() {
    companion object {
        internal lateinit var applicationContextReference: ApplicationContextReference
    }

    override fun onCreate() {
        super.onCreate()
        applicationContextReference = this
    }
}
