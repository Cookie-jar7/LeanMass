package com.leanmasscalculator.app.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class Calculation(
    var id: String = "",
    var userId: String = "",
    var weight: Double = 0.0,       // kg
    var height: Double = 0.0,       // cm
    var gender: String = "",
    var lbmValue: Double = 0.0,     //result
    var satisfactory: Boolean = false,
    var timestamp: Long = 0L        // when it was calculated
) {
    //format timestamp
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }


    fun getResultLabel(): String {
        return if (satisfactory) "Resultat satisfaisant" else "Resultat a surveiller"
    }
}