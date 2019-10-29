package com.example.sebas_z.easycompte

import android.app.AlarmManager
import android.app.PendingIntent
import android.arch.lifecycle.LifecycleOwner
import android.arch.persistence.room.Room
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v14.preference.PreferenceFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.*
import java.util.concurrent.TimeUnit

var listItems : MutableList<Transfer> = mutableListOf<Transfer>()
var total : Float = 0.0f

// TODO Edit transfer

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity() {

    //Used for RecyclerList
    private lateinit var recyclerView : RecyclerView
    private lateinit var recyclerAdapter : RecyclerView.Adapter<*>

    //Used to get settings value stored in Shared Preferences
    private lateinit var sharedPref :  SharedPreferences
    private lateinit var currencyPref : String
    private lateinit var paydayPref : String
    private lateinit var salaryPref : String

    //Database
    private lateinit var db: SQLiteDatabase

    // Clean onResume
    override fun onResume() {
        //Reload data in the list/recycler view
        val db = openOrCreateDatabase("easycompte-db", Context.MODE_PRIVATE, null)
        listItems.clear()
        addDbItemsToList(db)

        //Update the currency and reload the RecyclerView
        currencyPref = sharedPref.getString("currencyKey", "")
        val currencyTotal : TextView = findViewById(R.id.currencyTotal)
        currencyTotal.text = getCurrency(currencyPref).toString()
        createRecyclerView()

        //Display day before next month's payday
        if (listItems.isNotEmpty()) {
            val totalDate: TextView = findViewById(R.id.totalDate)
            val beforeNextPaydayString ="au: " + listItems[0].date.beforeNextPayday().toString()
            totalDate.text = beforeNextPaydayString
        }
        db.close()
        super.onResume()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        if (!databaseExists( this, "easycompte-db")) {
            val intent = Intent(this, SetupActivity::class.java)
            startActivity(intent)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        paydayPref = sharedPref.getString("paydayKey", "")
        salaryPref = sharedPref.getString("salaryKey", "")
        currencyPref = sharedPref.getString("currencyKey", "")


        //Creating/open database
        db = openOrCreateDatabase("easycompte-db", Context.MODE_PRIVATE, null)
        db.execSQL("CREATE TABLE IF NOT EXISTS transfer (tId INTEGER PRIMARY KEY, tSource TEXT NOT NULL, tDate TEXT NOT NULL, tAmount DOUBLE NOT NULL)")

        //Remove all elements if listItems is not empty
        //And reset the amount of transfer
        if (listItems.isNotEmpty()) {
            listItems.clear()
        }

        //Add all the value in the db in listItems
        //We clear listItems when we create the activity
        //And reset the amount of transfer
        addDbItemsToList(db)

        //Display amount sum and change color (Display dans updateTotal)
        //sumAmount(list) -> all list's items
        //sumAmount(list, total) -> add last item
        total = sumAmount(listItems)
        updateTotal(total)


        //Popup menu when click on button
        val plusButton: FloatingActionButton = findViewById(R.id.plusButton)
        plusButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view: View = LayoutInflater.from(this).inflate(R.layout.popup_layout, null)
            val vTitle: EditText = view.findViewById(R.id.editTitle)
            val vAmount: EditText = view.findViewById(R.id.editAmount)
            val vDate: EditText = view.findViewById(R.id.editDate)
            val vButton: Button = view.findViewById(R.id.addButton)

            builder.setView(view)
            val dialog: AlertDialog = builder.create()

            //Add the value if they are correct
            vButton.setOnClickListener {
                if (vTitle.text.toString().isEmpty() || vAmount.text.toString().isEmpty() || vDate.text.toString().isEmpty()) {
                    Toast.makeText(this, R.string.emptyFields, Toast.LENGTH_SHORT).show()
                } else if (!checkDateTransfer(vDate.text.toString())) {
                    Toast.makeText(this, R.string.wrongDate, Toast.LENGTH_SHORT).show()
                } else {
                    listItems.add(Transfer(Date(vDate.text.toString()), vTitle.text.toString(), vAmount.text.toString()))

                    //Adding transfer to database
                    insertInDb(listItems[listItems.lastIndex], db)

                    recyclerAdapter.notifyItemInserted(listItems.size - 1)
                    total = sumAmount(listItems, total)
                    updateTotal(total)
                    dialog.dismiss()
                }
            }
            dialog.show()
        }

        //Access setting activity
        val settingButton: Button = findViewById(R.id.settingButton)
        settingButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

    }

    //Calc the sum of all the amount
    fun sumAmount(dataSet : MutableList<Transfer>) : Float {
        var sum : Float = 0f
        for (i in 0 until dataSet.size) {
            sum += dataSet[i].amount.toFloat()
        }
        return String.format("%.2f", (sum)).toFloat() + 0.0f
    }

    fun sumAmount(dataSet : MutableList<Transfer>, oldTotal: Float) : Float {
        val lastVal = dataSet[dataSet.size-1].amount.toFloat()
        return String.format("%.2f", ((oldTotal+lastVal))).toFloat() + 0.0f
    }

    fun removeTransferAmount(index: Int, oldTotal: Float) : Float {
        return String.format("%.2f", ((oldTotal - listItems[index].amount.toFloat()))).toFloat() + 0.0f
    }

    fun updateTotal(total : Float)  {
        val totalAmount : TextView = findViewById(R.id.totalAmount)
        val currencyTotal : TextView = findViewById(R.id.currencyTotal)
        currencyTotal.text = getCurrency(currencyPref).toString()
        totalAmount.text = total.toString()

        //change the color to red/green/grey
        if (total > 0) {
            totalAmount.setTextColor(ContextCompat.getColor(this, R.color.positiveAmount))
            currencyTotal.setTextColor(ContextCompat.getColor(this, R.color.positiveAmount))
        } else if (total < 0) {
            totalAmount.setTextColor(ContextCompat.getColor(this, R.color.negativeAmount))
            currencyTotal.setTextColor(ContextCompat.getColor(this, R.color.negativeAmount))
        } else {
            totalAmount.setTextColor(ContextCompat.getColor(this, R.color.zeroAmount))
            currencyTotal.setTextColor(ContextCompat.getColor(this, R.color.zeroAmount))
        }
    }

    fun createRecyclerView() {

        class ItemClick() : CustomClickListener {
            override fun onItemClick(v: View, position: Int, context: Context) {

                val builder = AlertDialog.Builder(context)
                val view = LayoutInflater.from(context).inflate(R.layout.delete_confirmation, null)
                val confirmationButton : Button = view.findViewById(R.id.confimationButton)
                val cancelButton : Button = view.findViewById(R.id.cancelButton)

                builder.setView(view)
                val dialog: AlertDialog = builder.create()

                confirmationButton.setOnClickListener {
                    deleteTransfer(position)
                    dialog.dismiss()
                }

                cancelButton.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            }
        }

        recyclerAdapter = ListAdapter(listItems, this, getCurrency(currencyPref), ItemClick())

        //The RecyclerView had its own layout (in this case a LinearLayout)
        val recyclerManager = LinearLayoutManager(this)
        val itemDecor = DividerItemDecoration(this, recyclerManager.orientation)
        recyclerView = findViewById<RecyclerView>(R.id.mainList).apply {

            //Each items has a fixed size
            setHasFixedSize(true)
            layoutManager = recyclerManager
            addItemDecoration(itemDecor)
            adapter = recyclerAdapter
        }
    }

    //Get currency symbol from the currency name
    fun getCurrency(currencyPref: String) : Char =
            when (currencyPref) {
                "Euro" -> '€'
                "Dollar Americain" -> '$'
                "Livre Sterling" -> '£'
                else -> ' '
            }

    //Insert the transfer in the database with content values
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

    //if the database doesnt exist -> return false
    fun databaseExists(context: Context, name : String) : Boolean {
        try {
            val checkDb = SQLiteDatabase.openDatabase((context.getDatabasePath(name)).absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            checkDb.close()
        }
        catch ( e : SQLException) {
            return false
        }
        return true
    }

    //if the date is between payday and the next payday
    fun checkDateTransfer(date : String) : Boolean {

        if (!checkDateFormat(date)) {
            return false
        }
        var dateElement = date.split('/')

        val dateTemp = Date(dateElement[0].toInt(), dateElement[1].toInt(), dateElement[2].toInt())
        val day = dateElement[0].toInt()
        val month = dateElement[1].toInt()
        val year = dateElement[2].toInt()

        val payDay = listItems[0].date.day
        val payMonth = listItems[0].date.month
        val payYear = listItems[0].date.year

        if (payMonth == 12 && ((month == payMonth && (day < payDay || year != payYear )) || (month == 1 && (day >= payDay || year == payYear)) && (month != payMonth || month != 1))) {
            println("Test 1")
            return false
        } else if (payMonth != 12 && (((month == payMonth && day < payDay) || (month == payMonth+1 && day >= payDay)) || (year != payYear) || month !in payMonth..payMonth + 1)) {
            println("Test 2")
            return false
        } else {
            return dateTemp.checkDate()
        }
    }

    //check the date format from a string -> dd/mm/yyyy
    fun checkDateFormat(date : String) : Boolean {
        var element = date.split('/')
        if (element.size != 3) {
            return false
        } else if (element[0] == "" || element[1] == "" || element[2] == "") {
            return false
        }
        return element.size == 3
    }

    //TODO Make sure transfer get right id
    fun addDbItemsToList(db: SQLiteDatabase) {
        val cursor: Cursor = db.rawQuery("SELECT tDate, tSource, tAmount, tId FROM transfer", null)
        while (cursor.moveToNext()) {
            listItems.add(Transfer(Date(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3).toInt()))
        }
        if(listItems.isNotEmpty()) {
            Transfer.nbTransfer = listItems[listItems.lastIndex].id + 1
        }
        cursor.close()
    }

    fun deleteTransfer(index: Int) {
        val id = listItems[index].id
        total = removeTransferAmount(index, total)
        updateTotal(total)
        listItems.removeAt(index)
        recyclerAdapter.notifyItemRemoved(index)
        db.execSQL("DELETE FROM transfer WHERE tId = $id")
    }
}