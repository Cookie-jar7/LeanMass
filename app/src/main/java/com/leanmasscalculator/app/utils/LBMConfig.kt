package com.leanmasscalculator.app.utils

object LBMConfig {
    // Boer formula coefficients
    const val MALE_WEIGHT_COEFF = 0.407
    const val MALE_HEIGHT_COEFF = 0.267
    const val MALE_CONSTANT = 19.2

    // for Women
    const val FEMALE_WEIGHT_COEFF = 0.252
    const val FEMALE_HEIGHT_COEFF = 0.473
    const val FEMALE_CONSTANT = 48.3

    //norm thresholds
    const val MALE_LBM_THRESHOLD   = 38.0   // kg
    const val FEMALE_LBM_THRESHOLD = 24.0

    const val GENDER_MALE   = "male"
    const val GENDER_FEMALE = "female"

    fun calculateLBM(weightKg: Double, heightCm: Double, gender: String): Double{
        return if (gender.lowercase() == GENDER_MALE) {
            (MALE_WEIGHT_COEFF * weightKg) + (MALE_HEIGHT_COEFF * heightCm) - MALE_CONSTANT
        } else {
            (FEMALE_WEIGHT_COEFF * weightKg) + (FEMALE_HEIGHT_COEFF * heightCm) - FEMALE_CONSTANT
        }
    }

    fun isSatisfactory(lbmValue: Double, gender: String): Boolean {
        return if (gender.equals(GENDER_MALE, ignoreCase = true)) {
            lbmValue >= MALE_LBM_THRESHOLD
        } else {
            lbmValue >= FEMALE_LBM_THRESHOLD
        }
    }
}