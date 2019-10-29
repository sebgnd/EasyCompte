package com.example.sebas_z.easycompte

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.RecyclerView


class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val sharedPref : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        addPreferencesFromResource(R.xml.preferences)
        onSharedPreferenceChanged(sharedPref, "currencyKey")
        onSharedPreferenceChanged(sharedPref, "paydayKey")
        onSharedPreferenceChanged(sharedPref, "salaryKey")
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when(key) {
            "currencyKey" -> {
                val connectionPref : Preference = findPreference(key)
                connectionPref.summary = sharedPreferences.getString(key, " ")
            }
            else -> {
                val connectionPref : Preference = findPreference(key)
                connectionPref.summary = sharedPreferences.getString(key, " ")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}