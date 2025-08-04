package com.amanpathak.digipinx.sdk.constants

internal object ErrorMessages {
    const val INVALID_DIGIPOINT_LENGTH = "DigiPin must be exactly 10 characters"
    const val INVALID_DIGIPOINT_CHARACTERS = "DigiPin contains invalid characters"
    const val INVALID_LATITUDE = "Latitude must be between -90 and 90"
    const val INVALID_LONGITUDE = "Longitude must be between -180 and 180"
    const val COORDINATE_OUT_OF_BOUNDS = "Coordinate is outside Indian geographical bounds"
    const val NEGATIVE_RADIUS = "Radius must be positive"
    const val EMPTY_COORDINATE_LIST = "Coordinate list cannot be empty"
}

open class DigipinXException(message: String, cause: Throwable? = null) : Exception(message, cause)