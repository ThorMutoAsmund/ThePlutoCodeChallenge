package org.nafai.theplutocodechallenge.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import org.nafai.theplutocodechallenge.data.DataCollector.SensorData

// Class that persists sensor data to an SQLite database
public class DataStorage(context: Context, val sizeBeforePersisting: Int = 500) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val accelerometerDataList: MutableList<SensorData> = mutableListOf()
    private val gyroscopeDataList: MutableList<SensorData> = mutableListOf()

    companion object {
        private val DATABASE_NAME = "SensorDatabase"
        private val DATABASE_VERSION = 1
        private val TABLE_ACCELEROMETER = "AccelerometerDataTable"
        private val TABLE_GYROSCOPE = "GyroscopeDataTable"
        private val KEY_ID = "id"
        private val KEY_TIMESTAMP = "timestamp"
        private val KEY_X = "x"
        private val KEY_Y = "y"
        private val KEY_Z = "z"
    }

    fun debugGetNumberOfAccelerometerDataEntries(): Int {
        return debugGetNumberOfSensorDataEntries(TABLE_ACCELEROMETER)
    }

    fun debugGetNumberOfGyroscopeDataEntries(): Int {
        return debugGetNumberOfSensorDataEntries(TABLE_GYROSCOPE)
    }

    private fun debugGetNumberOfSensorDataEntries(tableName: String): Int {
        this.readableDatabase?.let { db ->
            var cursor: Cursor? = null
            try {
                cursor = db.rawQuery("SELECT COUNT(*) FROM $tableName", null)

                cursor.moveToFirst()
                return  cursor.getInt(0)
            }
            catch (e: SQLiteException) {
                // To be done
            }
            finally {
                cursor?.close()
                db.close()
            }
        }

        return -1
    }

    fun addAccelerometerData(data: SensorData) {
        accelerometerDataList.add(data)

        if (accelerometerDataList.count() >= sizeBeforePersisting) {
            storeSensorData(TABLE_ACCELEROMETER, accelerometerDataList.toList())
            accelerometerDataList.clear()
        }
    }

    fun addGyroscopeData(data: SensorData) {
        gyroscopeDataList.add(data)

        if (gyroscopeDataList.count() >= sizeBeforePersisting) {
            storeSensorData(TABLE_GYROSCOPE, gyroscopeDataList.toList())
            gyroscopeDataList.clear()
        }
    }

    fun flushData() {
        if (accelerometerDataList.isNotEmpty()) {
            storeSensorData(TABLE_ACCELEROMETER, accelerometerDataList.toList())
            accelerometerDataList.clear()
        }
        if (gyroscopeDataList.isNotEmpty()) {
            storeSensorData(TABLE_GYROSCOPE, gyroscopeDataList.toList())
            gyroscopeDataList.clear()
        }
    }

    private fun storeSensorData(tableName: String, data: List<SensorData>) {
        println("Storing $tableName data")
        this.writableDatabase?.let { db ->
            for (dataEntry in data) {
                val contentValues = ContentValues()
                contentValues.put(KEY_TIMESTAMP, dataEntry.timestamp)
                contentValues.put(KEY_X, dataEntry.x)
                contentValues.put(KEY_Y, dataEntry.y)
                contentValues.put(KEY_Z, dataEntry.z)

                val success = db.insert(tableName, null, contentValues)
                // React to success == false, not implemented yet
            }
            db.close()
        }
    }

    override fun onCreate(database: SQLiteDatabase?) {
        database?.execSQL("CREATE TABLE $TABLE_ACCELEROMETER ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_TIMESTAMP INTEGER, $KEY_X REAL, $KEY_Y REAL, $KEY_Z REAL)")
        database?.execSQL("CREATE TABLE $TABLE_GYROSCOPE ($KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, $KEY_TIMESTAMP INTEGER, $KEY_X REAL, $KEY_Y REAL, $KEY_Z REAL)")
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
        // Not implemented yet
    }
}
