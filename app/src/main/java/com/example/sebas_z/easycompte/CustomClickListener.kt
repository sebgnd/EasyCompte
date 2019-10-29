package com.example.sebas_z.easycompte

import android.content.Context
import android.view.View

interface CustomClickListener {
    fun onItemClick(v: View, position: Int, context: Context)
}