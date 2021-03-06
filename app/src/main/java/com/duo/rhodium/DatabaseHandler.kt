package com.duo.rhodium

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase?) {
        var CREATE_MILESTONE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" + KEY_ID + " INTEGER PRIMARY KEY," +
                    KEY_MILESTONE_LOCATION + " TEXT," +
                    KEY_MILESTONE_TECH + " TEXT," +
                    KEY_MILESTONE_SIGNAL_STRENGTH + " TEXT," +
                    KEY_MILESTONE_C1 + " TEXT," +
                    KEY_MILESTONE_C2 + " TEXT," +
                    KEY_MILESTONE_RSCP + " TEXT," +
                    KEY_MILESTONE_ECNO + " TEXT," +
                    KEY_MILESTONE_RSRP + " TEXT," +
                    KEY_MILESTONE_RSRQ + " TEXT," +
                    KEY_MILESTONE_RAC + " TEXT," +
                    KEY_MILESTONE_TAC + " TEXT," +
                    KEY_MILESTONE_PLMN + " TEXT," +
                    KEY_MILESTONE_CELL_ID + " Text" + ");"

        db?.execSQL(CREATE_MILESTONE_TABLE)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }


    fun createMilestone(milestone: Milestone) {
        var db: SQLiteDatabase = writableDatabase
        var values: ContentValues = ContentValues()
        values.put(KEY_MILESTONE_LOCATION, milestone.location)
        values.put(KEY_MILESTONE_TECH, milestone.technology)
        values.put(KEY_MILESTONE_SIGNAL_STRENGTH, milestone.signalStrength)
        values.put(KEY_MILESTONE_C1, milestone.c1)
        values.put(KEY_MILESTONE_C2, milestone.c2)
        values.put(KEY_MILESTONE_RSCP, milestone.rscp)
        values.put(KEY_MILESTONE_ECNO, milestone.rsrp)
        values.put(KEY_MILESTONE_RSRQ, milestone.rsrq)
        values.put(KEY_MILESTONE_RAC, milestone.rac)
        values.put(KEY_MILESTONE_TAC, milestone.tac)
        values.put(KEY_MILESTONE_PLMN, milestone.plmn)
        values.put(KEY_MILESTONE_CELL_ID, milestone.cellID)

        var insert = db.insert(TABLE_NAME, null, values)

        Log.d("DATA INSERTED", "SUCCESS $insert")
        db.close()

    }

    fun readAMilestone(id: Int): Milestone {
        var db: SQLiteDatabase = writableDatabase
        var cursor: Cursor = db.query(
            TABLE_NAME, arrayOf(
                KEY_ID,
                KEY_MILESTONE_LOCATION,
                KEY_MILESTONE_TECH,
                KEY_MILESTONE_SIGNAL_STRENGTH,
                KEY_MILESTONE_C1,
                KEY_MILESTONE_C2,
                KEY_MILESTONE_RSCP,
                KEY_MILESTONE_ECNO,
                KEY_MILESTONE_RSRP,
                KEY_MILESTONE_RSRQ,
                KEY_MILESTONE_RAC,
                KEY_MILESTONE_TAC,
                KEY_MILESTONE_PLMN,
                KEY_MILESTONE_CELL_ID
            ), KEY_ID + "=?", arrayOf(id.toString()),
            null, null, null, null
        )

        if (cursor != null)
            cursor.moveToFirst()

        var milestone = Milestone()
        milestone.id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
        milestone.location = cursor.getString(
            cursor.getColumnIndex(
                KEY_MILESTONE_LOCATION
            )
        )
        milestone.technology = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_TECH))
        milestone.signalStrength = cursor.getString(
            cursor.getColumnIndex(
                KEY_MILESTONE_SIGNAL_STRENGTH
            )
        )
        milestone.c1 = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_C1))
        milestone.c2 = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_C2))
        milestone.rscp = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_RSCP))
        milestone.rsrp = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_RSRP))
        milestone.rsrq = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_RSRQ))
        milestone.rac = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_RAC))
        milestone.tac = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_TAC))
        milestone.plmn = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_PLMN))
        milestone.cellID = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_CELL_ID))

        return milestone
    }

    fun readMilestones(): ArrayList<Milestone> {

        var db: SQLiteDatabase = readableDatabase
        var list: ArrayList<Milestone> = ArrayList()

        var selectAll = "SELECT * FROM " + TABLE_NAME

        var cursor: Cursor = db.rawQuery(selectAll, null)

        //loop through our chores
        if (cursor.moveToFirst()) {
            do {
                var milestone = Milestone()

                milestone.id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                milestone.location = cursor.getString(
                    cursor.getColumnIndex(
                        KEY_MILESTONE_LOCATION
                    )
                )
                milestone.technology = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_TECH))
                milestone.signalStrength = cursor.getString(
                    cursor.getColumnIndex(
                        KEY_MILESTONE_SIGNAL_STRENGTH
                    )
                )
                milestone.c1 = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_C1))
                milestone.c2 = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_C2))
                milestone.rscp = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_RSCP))
                milestone.rsrp = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_RSRP))
                milestone.rsrq = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_RSRQ))
                milestone.rac = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_RAC))
                milestone.tac = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_TAC))
                milestone.plmn = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_PLMN))
                milestone.cellID = cursor.getString(cursor.getColumnIndex(KEY_MILESTONE_CELL_ID))

                list.add(milestone)

            } while (cursor.moveToNext())
        }


        return list

    }


}