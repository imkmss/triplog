package com.example.triplog.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "triplog.db"
        private const val DB_VERSION = 2
        private const val TABLE_NAME = "travel_record"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE travel_record (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "place TEXT NOT NULL, " +
                    "visit_date TEXT NOT NULL, " +
                    "memo TEXT, " +
                    "photo_uri TEXT, " +
                    "latitude REAL DEFAULT 0.0, " +
                    "longitude REAL DEFAULT 0.0)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE travel_record ADD COLUMN latitude REAL DEFAULT 0.0")
            db.execSQL("ALTER TABLE travel_record ADD COLUMN longitude REAL DEFAULT 0.0")
        }
    }

    fun insert(record: TripRecord): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("place", record.place)
            put("visit_date", record.visitDate)
            put("memo", record.memo)
            put("photo_uri", record.photoUri)
            put("latitude", record.latitude)
            put("longitude", record.longitude)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAll(order: String = "DESC"): List<TripRecord> {
        val list = mutableListOf<TripRecord>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "id $order")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    TripRecord(
                        no = it.getInt(it.getColumnIndexOrThrow("id")),
                        place = it.getString(it.getColumnIndexOrThrow("place")),
                        visitDate = it.getString(it.getColumnIndexOrThrow("visit_date")),
                        memo = it.getString(it.getColumnIndexOrThrow("memo")) ?: "",
                        photoUri = it.getString(it.getColumnIndexOrThrow("photo_uri")) ?: "",
                        latitude = it.getDouble(it.getColumnIndexOrThrow("latitude")),
                        longitude = it.getDouble(it.getColumnIndexOrThrow("longitude"))
                    )
                )
            }
        }
        return list
    }

    fun update(record: TripRecord): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("place", record.place)
            put("visit_date", record.visitDate)
            put("memo", record.memo)
            put("photo_uri", record.photoUri)
            put("latitude", record.latitude)
            put("longitude", record.longitude)
        }
        return db.update(TABLE_NAME, values, "id = ?", arrayOf(record.no.toString()))
    }

    fun delete(no: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "id = ?", arrayOf(no.toString()))
    }

    fun deleteAll(): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, null, null)
    }
}