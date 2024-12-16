package org.nafai.theplutocodechallenge

import android.app.Application
import android.provider.ContactsContract.Data
import org.nafai.theplutocodechallenge.data.DataCollector

class MyApplication : Application() {
    companion object {
        lateinit var dataCollector: DataCollector
    }
    override fun onCreate() {
        super.onCreate()

        dataCollector = DataCollector(applicationContext)
    }
}