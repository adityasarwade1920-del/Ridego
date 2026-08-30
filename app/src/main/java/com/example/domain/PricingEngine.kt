package com.example.domain

import com.example.data.local.entity.PricingRuleEntity
import com.example.data.local.entity.PromoCodeEntity
import com.example.data.model.FareBreakdown
import com.example.data.model.VehicleCategory
import kotlin.math.max
import kotlin.math.round

object PricingEngine {

    fun calculateFare(
        rule: PricingRuleEntity?,
        category: VehicleCategory,
        distanceKm: Double,
        durationMinutes: Int,
        surgeMultiplier: Double = 1.0,
        promo: PromoCodeEntity? = null
    ): FareBreakdown {
        val baseFare = rule?.baseFare ?: (35.0 * category.baseMultiplier)
        val perKmRate = rule?.perKmRate ?: (25.0 * category.baseMultiplier)
        val perMinRate = rule?.perMinuteRate ?: (1.0 * category.baseMultiplier)
        val bookingFee = rule?.bookingFee ?: 10.0
        val minFare = rule?.minFare ?: (50.0 * category.baseMultiplier)
        val taxPercent = rule?.taxPercent ?: 5.0
        val commissionPercent = rule?.platformCommissionPercent ?: 18.0

        val distanceCharge = distanceKm * perKmRate
        val timeCharge = durationMinutes * perMinRate
        val subtotal = baseFare + distanceCharge + timeCharge + bookingFee

        val effectiveSurge = max(1.0, surgeMultiplier)
        val surgeAmount = if (effectiveSurge > 1.0) subtotal * (effectiveSurge - 1.0) else 0.0
        val beforeTax = subtotal + surgeAmount

        // Apply Promo discount if eligible
        var discount = 0.0
        if (promo != null && promo.isActive && beforeTax >= promo.minRideValue) {
            if (promo.fixedDiscount > 0.0) {
                discount = promo.fixedDiscount
            } else if (promo.discountPercent > 0.0) {
                discount = (beforeTax * (promo.discountPercent / 100.0))
            }
            if (promo.maxDiscount > 0.0 && discount > promo.maxDiscount) {
                discount = promo.maxDiscount
            }
        }

        val taxableAmount = max(0.0, beforeTax - discount)
        val taxes = taxableAmount * (taxPercent / 100.0)
        val calculatedTotal = max(minFare, taxableAmount + taxes)
        val totalFare = round(calculatedTotal)

        val platformCommission = round(totalFare * (commissionPercent / 100.0))
        val driverEarnings = max(0.0, totalFare - platformCommission)

        return FareBreakdown(
            baseFare = round(baseFare * 10) / 10,
            distanceKm = round(distanceKm * 10) / 10,
            distanceCharge = round(distanceCharge * 10) / 10,
            timeMinutes = durationMinutes,
            timeCharge = round(timeCharge * 10) / 10,
            bookingFee = round(bookingFee * 10) / 10,
            surgeMultiplier = effectiveSurge,
            surgeAmount = round(surgeAmount * 10) / 10,
            taxes = round(taxes * 10) / 10,
            discountAmount = round(discount * 10) / 10,
            totalFare = totalFare,
            platformCommission = platformCommission,
            driverEarnings = driverEarnings
        )
    }
}
