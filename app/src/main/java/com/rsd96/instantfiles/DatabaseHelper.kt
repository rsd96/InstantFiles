package com.rsd96.instantfiles

import android.content.ContentValues
import android.content.Context
import android.content.pm.ApplicationInfo
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.File

/**
 * Created by Ramshad on 8/15/16.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {



    init {
        Log.d(TAG, "constructor")
    }


    companion object {

        private var sInstance: DatabaseHelper? = null

        val TAG = "DatabaseHelper"

        val DATABASE_NAME = "database.db"
        val DATABASE_VERSION = 1

        val TABLE_DATA_NAME = "data"
        val TABLE_DATA_COLUMN_CONTENT = "name"
        val TABLE_DATA_COLUMN_TYPE = "type"
        val CREATE_DATA_TABLE = "CREATE TABLE $TABLE_DATA_NAME  ( $TABLE_DATA_COLUMN_CONTENT TEXT, $TABLE_DATA_COLUMN_TYPE TEXT )"


        /*val TABLE_MEDIA_NAME = "media"
        val TABLE_MEDIA_COLUMN = "mediaColumn"
        val CREATE_MEDIA_TABLE = "CREATE TABLE " + TABLE_MEDIA_NAME + " (" +
                TABLE_MEDIA_COLUMN + " TEXT )"

        val TABLE_DOCS_NAME = "docs"
        val TABLE_DOCS_COLUMN = "docsColumn"
        val CREATE_DOCS_TABLE = "CREATE TABLE " + TABLE_DOCS_NAME + " (" +
                TABLE_DOCS_COLUMN + " TEXT )"*/


        @Synchronized
        fun getInstance(context: Context): DatabaseHelper {
            Log.d(TAG, "getInstance()")
            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            // See this article for more information: http://bit.ly/6LRzfx
            if (sInstance == null) {
                sInstance = DatabaseHelper(context.applicationContext)
            }
            return sInstance as DatabaseHelper
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "onCreate()")
        db.execSQL(CREATE_DATA_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade()")
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_DATA_NAME)
        onCreate(db)

    }

    // To put single value
    fun insertData(contentValue: String, typeValue: Types): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(TABLE_DATA_COLUMN_CONTENT, contentValue)
        contentValues.put(TABLE_DATA_COLUMN_TYPE, typeValue.toString())
        val result : Long = db.insert(TABLE_DATA_NAME, null, contentValues)
        return result != -1L
    }

    fun deleteData(value: String, type: Types) {

        val db = this.readableDatabase
        try {
            db.execSQL("DELETE FROM $TABLE_DATA_NAME WHERE $TABLE_DATA_COLUMN_CONTENT  =  '$value'")
        } catch (e: SQLException) {
            Log.d(TAG, "sqlexc")
        }
        db.close()
    }

    fun getAllData(table: String = TABLE_DATA_NAME): Cursor {
        Log.d(TAG, "getAllData()")
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM " + table, null)

    }

    fun isDataExists(value: String): Boolean {
        Log.d(TAG, "isDataExists()")
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_DATA_NAME WHERE $TABLE_DATA_COLUMN_CONTENT = \"$value\"", null)
        if (cursor.count <= 0) {
            cursor.close()
            return false
        }
        cursor.close()
        return true
    }

    /*fun addFilesToDB(table: String, column: String, fileList: List<File>) {
        val db = this.writableDatabase
        db.delete(TABLE_MEDIA_NAME, null, null)
        for (x in fileList) {
            insertData(table, column, x.toString())
            Log.d(TAG, "adding files to DB")
        }
    }*/

    fun getAppData(context: Context): MutableList<ApplicationInfo> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_DATA_NAME WHERE $TABLE_DATA_COLUMN_TYPE = \"${Types.APP.name}\"", null)
        var list: MutableList<ApplicationInfo> = mutableListOf()
        try {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        list.add(context.packageManager.getApplicationInfo(cursor.getString(0), 0))
                    } catch (e: Exception) {
                        Log.d(TAG, e.message)
                    }
                } while (cursor.moveToNext())
            }
        }finally {
            cursor.close()
        }
        return list
    }

    // get media or document data
    fun getFileData(type : Types): MutableList<File> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_DATA_NAME WHERE $TABLE_DATA_COLUMN_TYPE = \"${type.name}\"", null)
        var list: MutableList<File> = mutableListOf()
        try {
            if (cursor.moveToFirst()) {
                do {
                    list.add(File(cursor.getString(0)))
                } while (cursor.moveToNext())
            }
        }finally {
            cursor.close()
        }
        return list
    }


    // populates a list of File from available database
    fun getDataFromDB(table: String): MutableList<File> {
        Log.d(TAG, "getDataFromDB()")
        val file : MutableList<File> = mutableListOf()
        file.clear()
        val cursor = getAllData(table)
        if (cursor.moveToFirst()) {
            do {
                val f = File(cursor.getString(0))
                file.add(f)
            } while (cursor.moveToNext())
        }
        return file
    }
}
