package com.example.apicalling.util

import java.text.NumberFormat
import java.util.*

object PriceUtils {
    const val USD_TO_TRY_RATE = 47.90

    /**
     * USD değerini güncel kurla TRY'ye çevirir ve formatlar.
     */
    fun formatUsdAsTry(priceInUsd: Double): String {
        val priceInTry = priceInUsd * USD_TO_TRY_RATE
        return formatTry(priceInTry)
    }

    /**
     * TRY değerini formatlar.
     */
    fun formatTry(priceInTry: Double): String {
        val locale = Locale("tr", "TR")
        val formatter = NumberFormat.getInstance(locale)
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return formatter.format(priceInTry) + " TL"
    }
}
