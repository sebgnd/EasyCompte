package com.example.sebas_z.easycompte

class Transfer {

    var source = "Virement"
    var amount = "0.0"
    var date = Date(1, 1, 2010)
    var id = 0

    companion object {
        var nbTransfer = 0
    }

    constructor(date: Date, source: String, amount: String) {
        this.source = source
        this.amount = amount
        this.date = Date(date.day, date.month, date.year)
        this.id = nbTransfer++
    }

    constructor(date: Date, source: String, amount: String, id: Int) {
        this.source = source
        this.amount = amount
        this.date = Date(date.day, date.month, date.year)
        this.id = id
    }
}