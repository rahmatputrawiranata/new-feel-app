package com.feel.feel

import android.app.Application
import android.util.Log
import com.orhanobut.hawk.Hawk

class Feel : Application() {
    override fun onCreate() {
        super.onCreate()
        Hawk.init(this).build()
    }
}