package com.example.triplog.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "triplog.db"
        private const val DB_VERSION = 3
        private const val TABLE_RECORD = "travel_record"
        private const val TABLE_PHOTO = "trip_photo"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE travel_record (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "place TEXT NOT NULL, " +
                    "visit_date TEXT NOT NULL, " +
                    "memo TEXT, " +
                    "latitude REAL DEFAULT 0.0, " +
                    "longitude REAL DEFAULT 0.0)"
        )
        db.execSQL(
            "CREATE TABLE trip_photo (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "record_id INTEGER NOT NULL, " +
                    "photo_uri TEXT NOT NULL, " +
                    "is_thumbnail INTEGER DEFAULT 0)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE travel_record ADD COLUMN latitude REAL DEFAULT 0.0")
            db.execSQL("ALTER TABLE travel_record ADD COLUMN longitude REAL DEFAULT 0.0")
        }
        if (oldVersion < 3) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS trip_photo (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "record_id INTEGER NOT NULL, " +
                        "photo_uri TEXT NOT NULL, " +
                        "is_thumbnail INTEGER DEFAULT 0)"
            )
        }
    }

    // 여행 기록 추가
    fun insert(record: TripRecord): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("place", record.place)
            put("visit_date", record.visitDate)
            put("memo", record.memo)
            put("latitude", record.latitude)
            put("longitude", record.longitude)
        }
        return db.insert(TABLE_RECORD, null, values)
    }

    // 사진 추가
    fun insertPhoto(recordId: Long, photoUri: String, isThumbnail: Boolean): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("record_id", recordId)
            put("photo_uri", photoUri)
            put("is_thumbnail", if (isThumbnail) 1 else 0)
        }
        return db.insert(TABLE_PHOTO, null, values)
    }

    // 전체 조회
    fun getAll(order: String = "DESC", orderBy: String = "id"): List<TripRecord> {
        val list = mutableListOf<TripRecord>()
        val db = readableDatabase
        val cursor = db.query(TABLE_RECORD, null, null, null, null, null, "$orderBy $order")
        cursor.use {
            while (it.moveToNext()) {
                val no = it.getInt(it.getColumnIndexOrThrow("id"))
                val photos = getPhotos(no)
                val thumbnail = photos.find { p -> p.isThumbnail } ?: photos.firstOrNull()
                list.add(
                    TripRecord(
                        no = no,
                        place = it.getString(it.getColumnIndexOrThrow("place")),
                        visitDate = it.getString(it.getColumnIndexOrThrow("visit_date")),
                        memo = it.getString(it.getColumnIndexOrThrow("memo")) ?: "",
                        photoUri = thumbnail?.photoUri ?: "",
                        latitude = it.getDouble(it.getColumnIndexOrThrow("latitude")),
                        longitude = it.getDouble(it.getColumnIndexOrThrow("longitude"))
                    )
                )
            }
        }
        return list
    }

    // 사진 목록 조회
    fun getPhotos(recordId: Int): List<TripPhoto> {
        val list = mutableListOf<TripPhoto>()
        val db = readableDatabase
        val cursor = db.query(TABLE_PHOTO, null, "record_id = ?", arrayOf(recordId.toString()), null, null, "id ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    TripPhoto(
                        id = it.getInt(it.getColumnIndexOrThrow("id")),
                        recordId = it.getInt(it.getColumnIndexOrThrow("record_id")),
                        photoUri = it.getString(it.getColumnIndexOrThrow("photo_uri")),
                        isThumbnail = it.getInt(it.getColumnIndexOrThrow("is_thumbnail")) == 1
                    )
                )
            }
        }
        return list
    }

    // 썸네일 설정
    fun setThumbnail(recordId: Int, photoId: Int) {
        val db = writableDatabase
        val reset = ContentValues().apply { put("is_thumbnail", 0) }
        db.update(TABLE_PHOTO, reset, "record_id = ?", arrayOf(recordId.toString()))
        val set = ContentValues().apply { put("is_thumbnail", 1) }
        db.update(TABLE_PHOTO, set, "id = ?", arrayOf(photoId.toString()))
    }

    // 사진 삭제
    fun deletePhoto(photoId: Int) {
        val db = writableDatabase
        db.delete(TABLE_PHOTO, "id = ?", arrayOf(photoId.toString()))
    }

    // 기록에 속한 사진 전체 삭제
    fun deletePhotos(recordId: Int) {
        val db = writableDatabase
        db.delete(TABLE_PHOTO, "record_id = ?", arrayOf(recordId.toString()))
    }

    // 수정
    fun update(record: TripRecord): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("place", record.place)
            put("visit_date", record.visitDate)
            put("memo", record.memo)
            put("latitude", record.latitude)
            put("longitude", record.longitude)
        }
        return db.update(TABLE_RECORD, values, "id = ?", arrayOf(record.no.toString()))
    }

    // 삭제
    fun delete(no: Int): Int {
        deletePhotos(no)
        val db = writableDatabase
        return db.delete(TABLE_RECORD, "id = ?", arrayOf(no.toString()))
    }

    // 전체 삭제
    fun deleteAll(): Int {
        val db = writableDatabase
        db.delete(TABLE_PHOTO, null, null)
        return db.delete(TABLE_RECORD, null, null)
    }
}