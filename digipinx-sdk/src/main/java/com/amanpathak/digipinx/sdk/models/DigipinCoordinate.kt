package com.amanpathak.digipinx.sdk.models

import com.amanpathak.digipinx.sdk.utils.DigipinXResult

data class DigipinCoordinate(
    val latitude: Double,
    val longitude: Double
) {
    override fun toString(): String {
        return "($latitude, $longitude)"
    }

    companion object {
        fun create(latitude: Double, longitude: Double): DigipinXResult<DigipinCoordinate> {
            return when {
                latitude < -90.0 || latitude > 90.0 ->
                    DigipinXResult.Error("Latitude must be between -90 and 90, got $latitude", "INVALID_LATITUDE")
                longitude < -180.0 || longitude > 180.0 ->
                    DigipinXResult.Error("Longitude must be between -180 and 180, got $longitude", "INVALID_LONGITUDE")
                else -> DigipinXResult.Success(DigipinCoordinate(latitude, longitude))
            }
        }
    }
}