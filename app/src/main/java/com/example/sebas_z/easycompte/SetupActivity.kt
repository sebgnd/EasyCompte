package com.example.sebas_z.easycompte

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.*
import java.util.concurrent.TimeUnit

//if day < current day -> date = day/month-1/year

class SetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        val setupSalaryView : EditText = findViewById(R.id.setupSalary)
        val setupPayView : EditText = findViewById(R.id.setupPay)
        val continueButton : Button = findViewById(R.id.continueButton)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPref.edit()

        var month : Int

        continueButton.setOnClickListener {
            val db : SQLiteDatabase = openOrCreateDatabase("easycompte-db", Context.MODE_PRIVATE, null)
            db.execSQL("CREATE TABLE IF NOT EXISTS transfer (tId INTEGER PRIMARY KEY , tSource TEXT NOT NULL, tDate TEXT NOT NULL, tAmount DOUBLE NOT NULL)")

            if ( setupSalaryView.text.toString() != "" && setupPayView.text.toString() != "" && setupSalaryView.text.toString().toInt() > 0 && checkDay(setupPayView.text.toString().toInt())) {
                editor.putString("salaryKey", setupSalaryView.text.toString()).apply()
                editor.putString("paydayKey", setupPayView.text.toString()).apply()
                editor.putBoolean("salaryAddedKey", false).apply()
                editor.putString("currencyKey", "Euro").apply()

                val contentValues = ContentValues()

                //set the right month
                if (setupPayView.text.toString().toInt() < Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                    month = Calendar.getInstance().get(Calendar.MONTH) + 1
                } else {
                    month = Calendar.getInstance().get(Calendar.MONTH)
                }

                var year = Calendar.getInstance().get(Calendar.YEAR)

                contentValues.apply {
                    put("tId", 1)
                    put("tDate", setupPayView.text.toString() + "/" + month + "/" + year)
                    put("tSource", "Salaire")
                    put("tAmount", setupSalaryView.text.toString())
                }
                db.insert("transfer", null, contentValues)

                val salaryWork = PeriodicWorkRequestBuilder<SalaryWorker>(6, TimeUnit.HOURS).build()
                WorkManager.getInstance().enqueue(salaryWork)

                db.close()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    fun checkDay(day : Int) : Boolean {
        //Leap year not implemented yet -> payday between 1 and 28 even in normal month
        return day in 1..28
    }
}
