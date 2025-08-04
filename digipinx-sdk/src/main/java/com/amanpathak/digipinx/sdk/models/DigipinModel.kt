package com.amanpathak.digipinx.sdk.models

import com.amanpathak.digipinx.sdk.constants.GridConstants
import com.amanpathak.digipinx.sdk.utils.DigipinXResult

data class DigipinModel(
    val digipin: String,
    val centerCoordinate: DigipinCoordinate,
    val boundingBox: DigipinBoundingBox
) {
    fun getFormattedCode(): String {
        return if (digipin.length == 10) {
            "${digipin.substring(0, 3)}-${digipin.substring(3, 6)}-${digipin.substring(6, 10)}"
        } else {
            digipin
        }
    }

    override fun toString(): String {
        return "Digipin(code='$digipin', center=$centerCoordinate)"
    }

    companion object {
        fun create(digipin: String, centerCoordinate: DigipinCoordinate, boundingBox: DigipinBoundingBox): DigipinXResult<DigipinModel> {
            return when {
                digipin.length != 10 ->
                    DigipinXResult.Error("Digipin must be exactly 10 characters, got ${digipin.length}", "INVALID_LENGTH")
                !digipin.all { it in GridConstants.SYMBOLS } ->
                    DigipinXResult.Error("Digipin contains invalid characters", "INVALID_CHARACTERS")
                else -> DigipinXResult.Success(DigipinModel(digipin, centerCoordinate, boundingBox))
            }
        }
    }
}