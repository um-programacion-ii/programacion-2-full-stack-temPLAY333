package com.eventtickets.mobile

import android.app.Application
import com.eventtickets.mobile.data.local.TokenManager

class EventTicketsApplication : Application() {

    companion object {
        lateinit var instance: EventTicketsApplication
            private set
    }

    val tokenManager: TokenManager by lazy {
        TokenManager.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

