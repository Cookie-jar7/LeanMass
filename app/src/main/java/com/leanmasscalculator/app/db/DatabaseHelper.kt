package com.leanmasscalculator.app.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.leanmasscalculator.app.model.Calculation

//local persistence
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME    = "LeanMassCalculator.db"
        private const val DATABASE_VERSION = 1

        // Table name
        private const val TABLE_CALCULATIONS = "calculations"

        // Column names
        private const val COL_ID           = "id"
        private const val COL_USER_ID      = "user_id"
        private const val COL_WEIGHT       = "weight"
        private const val COL_HEIGHT       = "height"
        private const val COL_GENDER       = "gender"
        private const val COL_LBM_VALUE    = "lbm_value"
        private const val COL_SATISFACTORY = "satisfactory"
        private const val COL_TIMESTAMP    = "timestamp"
    }

    //creating db
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_CALCULATIONS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID TEXT NOT NULL,
                $COL_WEIGHT REAL NOT NULL,
                $COL_HEIGHT REAL NOT NULL,
                $COL_GENDER TEXT NOT NULL,
                $COL_LBM_VALUE REAL NOT NULL,
                $COL_SATISFACTORY INTEGER NOT NULL,
                $COL_TIMESTAMP INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    // upgrading db (changing structure...)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CALCULATIONS")
        onCreate(db)
    }

    // insert lbm calculation
    fun insertCalculation(calc: Calculation): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_ID,      calc.userId)
            put(COL_WEIGHT,       calc.weight)
            put(COL_HEIGHT,       calc.height)
            put(COL_GENDER,       calc.gender)
            put(COL_LBM_VALUE,    calc.lbmValue)
            put(COL_SATISFACTORY, if (calc.satisfactory) 1 else 0)  // SQLite has no boolean!
            put(COL_TIMESTAMP,    calc.timestamp)
        }
        return db.insert(TABLE_CALCULATIONS, null, values)
    }

    // GET all calculations for a user (most recent first)
    fun getCalculationsForUser(userId: String): List<Calculation> {
        val list = mutableListOf<Calculation>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_CALCULATIONS,
            null,                       // all columns
            "$COL_USER_ID = ?",         // WHERE clause
            arrayOf(userId),            // WHERE args
            null, null,                 // groupBy, having
            "$COL_TIMESTAMP DESC"       // ORDER BY
        )

        cursor.use {
            while (it.moveToNext()) {
                val calc = Calculation(
                    id          = it.getLong(it.getColumnIndexOrThrow(COL_ID)).toString(),
                    userId      = it.getString(it.getColumnIndexOrThrow(COL_USER_ID)),
                    weight      = it.getDouble(it.getColumnIndexOrThrow(COL_WEIGHT)),
                    height      = it.getDouble(it.getColumnIndexOrThrow(COL_HEIGHT)),
                    gender      = it.getString(it.getColumnIndexOrThrow(COL_GENDER)),
                    lbmValue    = it.getDouble(it.getColumnIndexOrThrow(COL_LBM_VALUE)),
                    satisfactory = it.getInt(it.getColumnIndexOrThrow(COL_SATISFACTORY)) == 1,
                    timestamp   = it.getLong(it.getColumnIndexOrThrow(COL_TIMESTAMP))
                )
                list.add(calc)
            }
        }
        return list
    }

    // DELETE a calculation
    fun deleteCalculation(id: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_CALCULATIONS, "$COL_ID = ?", arrayOf(id))
    }

    // DELETE all calculations for a user
    fun deleteAllForUser(userId: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_CALCULATIONS, "$COL_USER_ID = ?", arrayOf(userId))
    }
}