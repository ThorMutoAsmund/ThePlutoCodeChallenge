package org.nafai.theplutocodechallenge.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.nafai.theplutocodechallenge.data.DataCollector.SensorData

// Class that persists sensor data to an SQLite database
class DataStorage(context: Context, private val sizeBeforePersisting: Int = 500) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val accelerometerDataList: MutableList<SensorData> = mutableListOf()
    private val gyroscopeDataList: MutableList<SensorData> = mutableListOf()
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val DATABASE_NAME = "SensorDatabase"
        private const val DATABASE_VERSION = 1
        private const val TABLE_ACCELEROMETER = "AccelerometerDataTable"
        private const val TABLE_GYROSCOPE = "GyroscopeDataTable"
        private const val KEY_ID = "id"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_X = "x"
        private const val KEY_Y = "y"
        private const val KEY_Z = "z"
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
                return cursor.getInt(0)
            } catch (e: SQLiteException) {
                // To be done
            } finally {
                cursor?.close()
            }
        }

        return -1
    }

    fun addAccelerometerData(data: SensorData) {
        accelerometerDataList.add(data)

        if (accelerometerDataList.count() >= sizeBeforePersisting) {
            accelerometerDataList.toList().let {
                scope.launch {
                    storeSensorData(TABLE_ACCELEROMETER, it)
                }
            }
            accelerometerDataList.clear()
        }
    }

    fun addGyroscopeData(data: SensorData) {
        gyroscopeDataList.add(data)

        if (gyroscopeDataList.count() >= sizeBeforePersisting) {
            gyroscopeDataList.toList().let {
                scope.launch {
                    storeSensorData(TABLE_GYROSCOPE, it)
                }
            }
            gyroscopeDataList.clear()
        }
    }

    fun flushData() {
        if (accelerometerDataList.isNotEmpty()) {
            accelerometerDataList.toList().let {
                scope.launch {
                    storeSensorData(TABLE_ACCELEROMETER, it)
                }
            }
            accelerometerDataList.clear()
        }
        if (gyroscopeDataList.isNotEmpty()) {
            gyroscopeDataList.toList().let {
                scope.launch {
                    storeSensorData(TABLE_GYROSCOPE, it)
                }
            }
            gyroscopeDataList.clear()
        }
    }

    // Calling storeSensorData from a thread will block it if another transaction is already in progress
    private fun storeSensorData(tableName: String, data: List<SensorData>) {
        println("Storing ${data.count()} $tableName data")

        this.writableDatabase?.let { db ->
            // Ensure all inserts are done in same transaction
            db.transaction {
                for (dataEntry in data) {
                    val contentValues = ContentValues()
                    contentValues.put(KEY_TIMESTAMP, dataEntry.timestamp)
                    contentValues.put(KEY_X, dataEntry.x)
                    contentValues.put(KEY_Y, dataEntry.y)
                    contentValues.put(KEY_Z, dataEntry.z)

                    val success = db.insert(tableName, null, contentValues)
                    // React to success == false, not implemented yet
                }
            }
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
