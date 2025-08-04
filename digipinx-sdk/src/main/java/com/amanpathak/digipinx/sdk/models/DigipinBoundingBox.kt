package com.amanpathak.digipinx.sdk.models

import com.amanpathak.digipinx.sdk.utils.DigipinXResult

data class DigipinBoundingBox(
    val southwest: DigipinCoordinate,
    val northeast: DigipinCoordinate
) {
    fun center(): DigipinCoordinate {
        val centerLat = (southwest.latitude + northeast.latitude) / 2
        val centerLon = (southwest.longitude + northeast.longitude) / 2
        return DigipinCoordinate(centerLat, centerLon)
    }

    fun contains(coordinate: DigipinCoordinate): Boolean {
        return coordinate.latitude >= southwest.latitude &&
                coordinate.latitude <= northeast.latitude &&
                coordinate.longitude >= southwest.longitude &&
                coordinate.longitude <= northeast.longitude
    }

    fun width(): Double = northeast.longitude - southwest.longitude

    fun height(): Double = northeast.latitude - southwest.latitude

    override fun toString(): String {
        return "BoundingBox(SW=$southwest, NE=$northeast)"
    }

    companion object {
        /** Create bounding box with validation */
        fun create(
            southwest: DigipinCoordinate,
            northeast: DigipinCoordinate
        ): DigipinXResult<DigipinBoundingBox> {
            return when {
                southwest.latitude > northeast.latitude ->
                    DigipinXResult.Error(
                        "Southwest latitude must be <= northeast latitude",
                        "INVALID_BOUNDS"
                    )

                southwest.longitude > northeast.longitude ->
                    DigipinXResult.Error(
                        "Southwest longitude must be <= northeast longitude",
                        "INVALID_BOUNDS"
                    )

                else -> DigipinXResult.Success(DigipinBoundingBox(southwest, northeast))
            }
        }
    }
}