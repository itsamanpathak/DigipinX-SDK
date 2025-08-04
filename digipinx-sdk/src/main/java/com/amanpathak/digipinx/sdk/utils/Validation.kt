package com.amanpathak.digipinx.sdk.utils

import com.amanpathak.digipinx.sdk.constants.CommonConstants
import com.amanpathak.digipinx.sdk.constants.ErrorMessages
import com.amanpathak.digipinx.sdk.constants.GeographicConstants
import com.amanpathak.digipinx.sdk.constants.GridConstants
import com.amanpathak.digipinx.sdk.models.DigipinCoordinate
import java.util.regex.Pattern

/**
 * Validation utilities for DIGIPOINT operations.
 */
internal object Validation {
    
    private val DIGIPOINT_PATTERN = Pattern.compile(GridConstants.DIGIPOINT_PATTERN)
    
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val warningMessage: String? = null
    )
    
    /**
     * Validates DIGIPOINT code format
     */
    fun validateDigipointCode(code: String): ValidationResult {
        if (code.isBlank()) {
            return ValidationResult(
                isValid = false,
                errorMessage = "DIGIPOINT code cannot be empty"
            )
        }
        
        if (code.length != CommonConstants.DIGIPOINT_CODE_LENGTH) {
            return ValidationResult(
                isValid = false,
                errorMessage = "${ErrorMessages.INVALID_DIGIPOINT_LENGTH}, got ${code.length}"
            )
        }
        
        if (!DIGIPOINT_PATTERN.matcher(code).matches()) {
            val invalidChars = code.filter { it !in GridConstants.SYMBOLS }
            return ValidationResult(
                isValid = false,
                errorMessage = "${ErrorMessages.INVALID_DIGIPOINT_CHARACTERS}: ${invalidChars.toSet().joinToString()}"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Validates coordinates
     */
    fun validateCoordinates(latitude: Double, longitude: Double): ValidationResult {
        if (latitude < -90.0 || latitude > 90.0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "${ErrorMessages.INVALID_LATITUDE}, got $latitude"
            )
        }
        
        if (longitude < -180.0 || longitude > 180.0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "${ErrorMessages.INVALID_LONGITUDE}, got $longitude"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Validates if coordinates are within Indian bounds
     */
    fun validateIndianBounds(coordinate: DigipinCoordinate): ValidationResult {
        val coordValidation = validateCoordinates(coordinate.latitude, coordinate.longitude)
        if (!coordValidation.isValid) {
            return coordValidation
        }
        
        if (!isWithinIndianBounds(coordinate)) {
            return ValidationResult(
                isValid = false,
                errorMessage = "${ErrorMessages.COORDINATE_OUT_OF_BOUNDS}: $coordinate"
            )
        }
        
        // Check if coordinate is near the boundary
        if (coordinate.latitude > GeographicConstants.INDIA_MAX_LAT - GeographicConstants.BOUNDARY_BUFFER ||
            coordinate.latitude < GeographicConstants.INDIA_MIN_LAT + GeographicConstants.BOUNDARY_BUFFER ||
            coordinate.longitude > GeographicConstants.INDIA_MAX_LON - GeographicConstants.BOUNDARY_BUFFER ||
            coordinate.longitude < GeographicConstants.INDIA_MIN_LON + GeographicConstants.BOUNDARY_BUFFER) {
            return ValidationResult(
                isValid = true,
                warningMessage = "Coordinate is near Indian geographical boundary, some nearby DIGIPOINTs might be unavailable"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Validates radius value for neighbor search.
     * 
     * @param radius The radius value to validate
     * @return ValidationResult containing success status and error message if any
     */
    fun validateRadius(radius: Int): ValidationResult {
        if (radius <= 0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "${ErrorMessages.NEGATIVE_RADIUS}, got $radius"
            )
        }
        
        if (radius > 100) {
            return ValidationResult(
                isValid = true,
                warningMessage = "Radius will be limited to maximum 100 grid cells for performance reasons"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Validates distance radius in meters.
     * 
     * @param radiusMeters The radius in meters to validate
     * @return ValidationResult containing success status and error message if any
     */
    fun validateDistanceRadius(radiusMeters: Double): ValidationResult {
        if (radiusMeters <= 0.0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "${ErrorMessages.NEGATIVE_RADIUS} in meters, got $radiusMeters"
            )
        }
        
        if (radiusMeters > 1000000.0) { // 1000 km limit
            return ValidationResult(
                isValid = true,
                warningMessage = "Very large radius may result in incomplete results due to grid cell limits"
            )
        }
        
        if (radiusMeters < CommonConstants.GRID_SIZE_METERS) {
            return ValidationResult(
                isValid = true,
                warningMessage = "Radius smaller than grid size (${CommonConstants.GRID_SIZE_METERS}m) may not find any results"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Validates a list of coordinates.
     * 
     * @param coordinates The list of coordinates to validate
     * @return ValidationResult containing success status and error message if any
     */
    fun validateCoordinateList(coordinates: List<DigipinCoordinate>): ValidationResult {
        if (coordinates.isEmpty()) {
            return ValidationResult(false, ErrorMessages.EMPTY_COORDINATE_LIST)
        }
        
        coordinates.forEachIndexed { index, coord ->
            val validation = validateCoordinates(coord.latitude, coord.longitude)
            if (!validation.isValid) {
                return ValidationResult(false, "Invalid coordinate at index $index: ${validation.errorMessage}")
            }
        }
        
        return ValidationResult(true, null)
    }
    
    /**
     * Validates precision level.
     * 
     * @param precision The precision level to validate
     * @return ValidationResult containing success status and error message if any
     */
    fun validatePrecisionLevel(precision: Int): ValidationResult {
        if (precision < 1 || precision > CommonConstants.DIGIPOINT_CODE_LENGTH) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Precision must be between 1 and ${CommonConstants.DIGIPOINT_CODE_LENGTH}, got $precision"
            )
        }
        
        if (precision < 8) {
            return ValidationResult(
                isValid = true,
                warningMessage = "Low precision level will result in large grid areas"
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Checks if coordinate is within Indian geographical bounds.
     */
    private fun isWithinIndianBounds(coordinate: DigipinCoordinate): Boolean {
        return coordinate.latitude >= GeographicConstants.INDIA_MIN_LAT &&
               coordinate.latitude <= GeographicConstants.INDIA_MAX_LAT &&
               coordinate.longitude >= GeographicConstants.INDIA_MIN_LON &&
               coordinate.longitude <= GeographicConstants.INDIA_MAX_LON
    }

} 