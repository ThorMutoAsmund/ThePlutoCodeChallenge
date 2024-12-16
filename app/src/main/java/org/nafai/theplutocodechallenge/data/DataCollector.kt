package org.nafai.theplutocodechallenge.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

// Class for collecting sensor data and persisting them to an SQLite database
class DataCollector(applicationContext: Context): SensorEventListener {
    var isRunning = false
        private set

    // Data class to hold timestamp and sensor x,y,z data
    data class SensorData(val timestamp: Long, val x: Float, val y: Float, val z: Float)

    private var applicationContextSet = false
    private var statusChangeListener: (() -> Unit)? = null
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null

    private var dataStorage: DataStorage? = null

    init {
        // If context not already set, get reference to sensors and create database
        sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        dataStorage = DataStorage(applicationContext)

        applicationContextSet = true
    }

    fun setOnStatusChangeListener(l: () -> Unit) {
        statusChangeListener = l
    }

    fun clearOnStatusChangeListener() {
        statusChangeListener = null
    }

    fun stop() {
        if (!isRunning) {
            return
        }

        dataStorage?.flushData()
        sensorManager?.unregisterListener(this)

        isRunning = false
        statusChangeListener?.invoke()
    }

    fun start() {
        if (isRunning) {
            return
        }

        // Only set as running if both sensors can be found
        if (accelerometer != null && gyroscope != null) {
            sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager?.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_GAME)

            isRunning = true
            statusChangeListener?.invoke()

            println("Accelerometer entries: " + dataStorage?.debugGetNumberOfAccelerometerDataEntries())
            println("Gyroscope entries: " + dataStorage?.debugGetNumberOfGyroscopeDataEntries())
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor) {
                accelerometer -> {

                    dataStorage?.addAccelerometerData(SensorData(timestamp=it.timestamp, x=it.values[0], y=it.values[1], z=it.values[2]))
                }
                gyroscope -> {
                    dataStorage?.addGyroscopeData(SensorData(timestamp=it.timestamp, x=it.values[0], y=it.values[1], z=it.values[2]))
                }
                else -> { }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }
}