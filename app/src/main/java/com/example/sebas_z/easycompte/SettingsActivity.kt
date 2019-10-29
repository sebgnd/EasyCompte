package com.example.sebas_z.easycompte

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.widget.Button
import com.example.sebas_z.easycompte.R.id.view

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val fragment : Fragment = SettingsFragment()
        val fragmentTransaction : FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.apply {
            replace(R.id.fragmentContainer, fragment, null)
            commit()
        }

        val bButton : Button = findViewById(R.id.backButton)
        bButton.setOnClickListener {
            onBackPressed()
        }
    }

}
