package com.example.sebas_z.easycompte

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.*

const val DB_PATH = "//data/data/com.example.sebas_z.easycompte/databases/easycompte-db"

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SalaryWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val sharedPreferences : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val editor = sharedPreferences.edit()

    override fun doWork(): Result {

        val salaryAdded = sharedPreferences.getBoolean("salaryAddedKey", false)
        val salaryPref = sharedPreferences.getString("salaryKey", "")
        val paydayPref = sharedPreferences.getString("paydayKey", "")


        if (paydayPref != "" && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == paydayPref.toInt() && !salaryAdded) {
            val db = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE)
            createNewPeriod(db, salaryPref.toInt(), paydayPref.toInt())
            editor.putBoolean("salaryAddedKey", true).apply()
            db.close()
        }
        if (paydayPref != "" && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) != paydayPref.toInt() && salaryAdded) {
            editor.putBoolean("salaryAddedKey", false).apply()
        }
        return Result.SUCCESS
    }

    // TODO implement multiple month
    fun createNewPeriod(db : SQLiteDatabase, salary : Int, payday : Int) {

        var totalLastMonth = 0.0f
        val cursor: Cursor = db.rawQuery("SELECT tDate, tSource, tAmount FROM transfer", null)
        while (cursor.moveToNext()) {
            totalLastMonth += cursor.getString(2).toFloat()
        }
        cursor.close()

        db.execSQL("DELETE FROM transfer")
        Transfer.nbTransfer = 0
        insertInDb(Transfer(Date(payday, Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.YEAR)), "Salaire", salary.toString()), db)
        insertInDb(Transfer(Date(payday, Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.YEAR)), "Mois dernier", totalLastMonth.toString()), db)
    }

    fun insertInDb(transfer: Transfer, db : SQLiteDatabase) {
        val contentValues = ContentValues()
        contentValues.apply {
            put("tId", transfer.id)
            put("tDate", transfer.date.toString())
            put("tSource", transfer.source)
            put("tAmount", transfer.amount)
        }
        db.insert("transfer", null, contentValues)
    }
}