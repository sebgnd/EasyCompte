package com.example.sebas_z.easycompte

import java.time.DayOfWeek
import java.time.Month
import java.time.Year

class Date {

    var day : Int = 1
    var month : Int = 1
    var year : Int = 1900

    constructor(day : Int, month : Int, year : Int) {
        this.day = day
        this.month = month
        this.year = year
    }

    constructor(stringDate : String) {
        val dateElement = stringDate.split('/')
        if(dateElement.size == 3) {
            if (dateElement[0] != "" && dateElement[1] != "" && dateElement[2] != "") {
                this.day = dateElement[0].toInt()
                this.month = dateElement[1].toInt()
                this.year = dateElement[2].toInt()
            }
        }
    }

    fun checkDate() : Boolean {
        if (day in 1..31) {
            if (month == 4 || month == 6 || month == 9 || month == 11) {
                return day <= 30
            } else if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
                return true
            } else if (month == 2){
                if ((year%4 == 0 && year%100 != 0) || year%400 == 0) {
                    return day <= 29
                } else {
                    return day <= 28
                }
            } else {
                return false
            }
        } else {
            return false
        }
    }

    override fun toString() : String {
        return day.toString() + "/" + month.toString() + "/" + year.toString()
    }

    fun beforeNextPayday() : Date {
        val day : Int
        val month : Int
        val year : Int

        if (this.day == 1) {
            if (this.month == 4 || this.month == 6 || this.month == 9 || this.month == 11) {
                day = 30
            } else if (this.month == 2) {
                day = 28
            } else {
                day = 31
            }
            month = this.month
            year = this.year
        } else {
            day = this.day - 1
            if (this.month == 12) {
                month = 1
                year = this.year + 1
            } else {
                month = this.month + 1
                year = this.year
            }
        }
        return Date(day, month, year)
    }
}