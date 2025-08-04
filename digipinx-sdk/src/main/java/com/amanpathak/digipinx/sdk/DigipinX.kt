package com.amanpathak.digipinx.sdk

import com.amanpathak.digipinx.sdk.constants.CommonConstants
import com.amanpathak.digipinx.sdk.constants.DigipinXException
import com.amanpathak.digipinx.sdk.constants.GeographicConstants
import com.amanpathak.digipinx.sdk.constants.GridConstants
import com.amanpathak.digipinx.sdk.models.DigipinBoundingBox
import com.amanpathak.digipinx.sdk.models.DigipinCoordinate
import com.amanpathak.digipinx.sdk.models.DigipinModel
import com.amanpathak.digipinx.sdk.utils.DigipinXResult
import com.amanpathak.digipinx.sdk.utils.Utils
import com.amanpathak.digipinx.sdk.utils.Validation
import kotlin.math.ceil

/**
 * Main SDK class for DIGIPIN operations.
 *
 * Basic usage:
 * val sdk = DigipointSDK.Builder().build()
 * val code = sdk.generateDigipin(28.6139, 77.2090)
 * val coordinate = sdk.generateLatLon("39J438P582")
 */
class DigipinXS private constructor(
    private val config: Config
) {
    private var lastWarning: String? = null


    companion object {
        const val DIGIPOINT_CODE_LENGTH = CommonConstants.DIGIPOINT_CODE_LENGTH

        internal val SYMBOLS = GridConstants.SYMBOLS
        internal const val INDIA_MIN_LAT = GeographicConstants.INDIA_MIN_LAT
        internal const val INDIA_MAX_LAT = GeographicConstants.INDIA_MAX_LAT
        internal const val INDIA_MIN_LON = GeographicConstants.INDIA_MIN_LON
        internal const val INDIA_MAX_LON = GeographicConstants.INDIA_MAX_LON

        val INDIA_BOUNDS = DigipinBoundingBox(
            southwest = DigipinCoordinate(INDIA_MIN_LAT, INDIA_MIN_LON),
            northeast = DigipinCoordinate(INDIA_MAX_LAT, INDIA_MAX_LON)
        )

        fun init(): DigipinXS {
            val config = Config(DIGIPOINT_CODE_LENGTH)
            return DigipinXS(config)
        }
    }


    /** Generate DIGIPIN code from coordinates */
    fun generateDigipin(latitude: Double, longitude: Double): DigipinXResult<DigipinModel> {
        return try {
            // 1. Validate coordinates first
            val coordResult = DigipinCoordinate.create(latitude, longitude)
            if (coordResult is DigipinXResult.Error) {
                return coordResult
            }

            val coordinate = (coordResult as DigipinXResult.Success).data

            // 2. Check Indian bounds
            if (!isWithinIndianBounds(coordinate)) {
                return DigipinXResult.Error(
                    "Coordinate $coordinate is outside Indian bounds",
                    "OUT_OF_BOUNDS"
                )
            }

            // 3. Generate DIGIPIN
            val result = generateDigipinInternal(coordinate)
            if (result is DigipinXResult.Success) {
                DigipinXResult.Success(result.data)
            } else {
                result
            }
        } catch (e: Exception) {
            DigipinXResult.Error("Failed to generate DIGIPIN: ${e.message}", "GENERATION_FAILED")
        }
    }


    /** Generate coordinates from DIGIPIN code */
    fun generateLatLon(digipin: String): DigipinXResult<DigipinModel> {
        return try {

            // 1. Validate DIGIPIN format
            val validation = Validation.validateDigipointCode(digipin)
            if (!validation.isValid) {
                return DigipinXResult.Error(
                    validation.errorMessage ?: "Invalid DIGIPIN format",
                    "INVALID_FORMAT"
                )
            }
            lastWarning = validation.warningMessage


            // 2. Generate coordinates
            val (centerCoord, boundingBox) = generateLatLonInternal(digipin)
            if (centerCoord is DigipinXResult.Error) {
                return centerCoord
            }

            val center = (centerCoord as DigipinXResult.Success<DigipinCoordinate>).data
            val bounds = (boundingBox as DigipinXResult.Success<DigipinBoundingBox>).data

            // 3. Generate Digipin
            val codeResult = DigipinModel.create(digipin, center, bounds)
            if (codeResult is DigipinXResult.Success) {
                DigipinXResult.Success(codeResult.data)
            } else {
                codeResult
            }
        } catch (e: Exception) {
            DigipinXResult.Error(
                "Failed to generate coordinates: ${e.message}",
                "GENERATION_FAILED"
            )
        }
    }


    /** Check if coordinates are within Indian bounds */
    fun isWithinIndianBounds(coordinate: DigipinCoordinate): Boolean {
        return INDIA_BOUNDS.contains(coordinate)
    }

    /** Validate DIGIPIN code format */
    fun isValidDigipointCode(digipin: String): Boolean {
        return Validation.validateDigipointCode(digipin).isValid
    }

    /** Get neighboring DIGIPIN codes */
    fun getNeighbors(digipin: String, radius: Int = 1): DigipinXResult<List<DigipinModel>> {
        return try {
            // validate radius if needed
            val validation = Validation.validateRadius(radius)
            if (!validation.isValid) {
                lastWarning = null
                return DigipinXResult.Error(
                    validation.errorMessage ?: "Invalid radius",
                    "INVALID_RADIUS"
                )
            }
            lastWarning = validation.warningMessage

            // Generate center DIGIPIN
            val centerResult = generateLatLon(digipin)
            if (centerResult is DigipinXResult.Error) {
                return centerResult
            }

            val centerDigipoint = (centerResult as DigipinXResult.Success).data
            val neighbors = mutableListOf<DigipinModel>()

            // calculate grid size
            val gridSizeLat = (centerDigipoint.boundingBox.northeast.latitude -
                    centerDigipoint.boundingBox.southwest.latitude)
            val gridSizeLon = (centerDigipoint.boundingBox.northeast.longitude -
                    centerDigipoint.boundingBox.southwest.longitude)

            // find neighbors in grid
            for (latOffset in -radius..radius) {
                for (lonOffset in -radius..radius) {
                    if (latOffset == 0 && lonOffset == 0) continue // skip center

                    val neighborLat =
                        centerDigipoint.centerCoordinate.latitude + (latOffset * gridSizeLat)
                    val neighborLon =
                        centerDigipoint.centerCoordinate.longitude + (lonOffset * gridSizeLon)

                    // Validate coordinates safely
                    val coordResult = DigipinCoordinate.create(neighborLat, neighborLon)
                    if (coordResult is DigipinXResult.Success) {
                        val neighborCoord = coordResult.data
                        if (isWithinIndianBounds(neighborCoord)) {
                            val neighborResult = generateDigipin(neighborLat, neighborLon)
                            if (neighborResult is DigipinXResult.Success) {
                                neighbors.add(neighborResult.data)
                            }
                        }
                    }
                }
            }

            DigipinXResult.Success(neighbors)
        } catch (e: Exception) {
            DigipinXResult.Error("Failed to get neighbors: ${e.message}", "NEIGHBORS_FAILED")
        }
    }


    /** Find DIGIPIN codes within radius */
    fun findDigipointCodesInRadius(
        center: DigipinCoordinate,
        radiusMeters: Double
    ): DigipinXResult<List<DigipinModel>> {
        return try {
            // validate radius
            val validation = Validation.validateDistanceRadius(radiusMeters)
            if (!validation.isValid) {
                lastWarning = null
                return DigipinXResult.Error(
                    validation.errorMessage ?: "Invalid radius",
                    "INVALID_RADIUS"
                )
            }
            lastWarning = validation.warningMessage


            // Check if center is within Indian bounds
            if (!isWithinIndianBounds(center)) {
                return DigipinXResult.Error(
                    "Center coordinate is outside Indian bounds",
                    "OUT_OF_BOUNDS"
                )
            }

            // get center digipoint
            val centerResult = generateDigipin(center.latitude, center.longitude)
            if (centerResult is DigipinXResult.Error) {
                return centerResult
            }

            val centerDigipoint = (centerResult as DigipinXResult.Success).data

            val gridSizeMeters = Utils.calculateGridSizeMeters(centerDigipoint)
            val gridRadius = ceil(radiusMeters / gridSizeMeters).toInt()

            // limit radius for performance
            val safeGridRadius = gridRadius.coerceAtMost(100)
            if (safeGridRadius < gridRadius) {
                lastWarning =
                    "Search radius limited to ${safeGridRadius * gridSizeMeters}m for performance"
            }

            // get neighbors
            val neighborsResult = getNeighbors(centerDigipoint.digipin, safeGridRadius)
            if (neighborsResult is DigipinXResult.Error) {
                return neighborsResult
            }

            val candidates = (neighborsResult as DigipinXResult.Success).data + centerDigipoint

            // filter by actual distance
            val filtered = candidates.filter { candidate ->
                Utils.calculateDistance(center, candidate.centerCoordinate) <= radiusMeters
            }

            DigipinXResult.Success(filtered)
        } catch (e: Exception) {
            DigipinXResult.Error(
                "Failed to find DIGIPIN codes in radius: ${e.message}",
                "RADIUS_SEARCH_FAILED"
            )
        }
    }


    /** Create Google Maps URL */
    fun createGoogleMapsUrl(digipointCode: DigipinModel): DigipinXResult<String> {
        return try {
            DigipinXResult.Success(Utils.createMapsUrl(digipointCode))
        } catch (e: Exception) {
            DigipinXResult.Error(
                "Failed to create Google Maps URL: ${e.message}",
                "URL_CREATION_FAILED"
            )
        }
    }

    /** Get precision description */
    fun getPrecisionDescription(digipointCode: DigipinModel): DigipinXResult<String> {
        return try {
            DigipinXResult.Success(Utils.getPrecisionDescription(digipointCode))
        } catch (e: Exception) {
            DigipinXResult.Error(
                "Failed to get precision description: ${e.message}",
                "PRECISION_FAILED"
            )
        }
    }

    /** Calculate area in square meters */
    fun calculateAreaSquareMeters(digipointCode: DigipinModel): DigipinXResult<Double> {
        return try {
            DigipinXResult.Success(Utils.calculateAreaSquareMeters(digipointCode))
        } catch (e: Exception) {
            DigipinXResult.Error(
                "Failed to calculate area: ${e.message}",
                "AREA_CALCULATION_FAILED"
            )
        }
    }

    /** Calculate distance between coordinates */
    fun calculateDistance(
        coord1: DigipinCoordinate,
        coord2: DigipinCoordinate
    ): DigipinXResult<Double> {
        return try {
            DigipinXResult.Success(Utils.calculateDistance(coord1, coord2))
        } catch (e: Exception) {
            DigipinXResult.Error(
                "Failed to calculate distance: ${e.message}",
                "DISTANCE_CALCULATION_FAILED"
            )
        }
    }


    /** Internal method that returns Result */
    internal fun generateDigipinInternal(coordinate: DigipinCoordinate): DigipinXResult<DigipinModel> {
        return try {
            // validate bounds if enabled
            val validation = Validation.validateIndianBounds(coordinate)
            if (!validation.isValid) {
                return DigipinXResult.Error(
                    validation.errorMessage ?: "Coordinate outside Indian bounds",
                    "OUT_OF_BOUNDS"
                )
            }
            lastWarning = validation.warningMessage

            var latMin = INDIA_MIN_LAT
            var latMax = INDIA_MAX_LAT
            var lonMin = INDIA_MIN_LON
            var lonMax = INDIA_MAX_LON

            val codeBuilder = StringBuilder()

            repeat(config.precisionLevel) { _ ->
                val latDiv = (latMax - latMin) / 4
                val lonDiv = (lonMax - lonMin) / 4

                // row logic is reversed to match original algo
                var row = 3 - ((coordinate.latitude - latMin) / latDiv).toInt()
                var col = ((coordinate.longitude - lonMin) / lonDiv).toInt()

                // clamp values
                row = row.coerceIn(0, 3)
                col = col.coerceIn(0, 3)

                val symbol = SYMBOLS[row * 4 + col]
                codeBuilder.append(symbol)

                // update bounds for next iteration
                latMax = latMin + latDiv * (4 - row)
                latMin = latMin + latDiv * (3 - row)

                lonMin = lonMin + lonDiv * col
                lonMax = lonMin + lonDiv
            }

            val code = codeBuilder.toString()
            val (centerCoord, boundingBox) = generateLatLonInternal(code)

            if (centerCoord is DigipinXResult.Error) {
                return centerCoord
            }

            val center = (centerCoord as DigipinXResult.Success<DigipinCoordinate>).data
            val bounds = (boundingBox as DigipinXResult.Success<DigipinBoundingBox>).data

            val codeResult = DigipinModel.create(code, center, bounds)
            if (codeResult is DigipinXResult.Success) {
                DigipinXResult.Success(codeResult.data)
            } else {
                codeResult
            }
        } catch (e: Exception) {
            DigipinXResult.Error("Failed to generate DIGIPIN: ${e.message}", "GENERATION_FAILED")
        }
    }

    private fun generateLatLonInternal(code: String): Pair<DigipinXResult<DigipinCoordinate>, DigipinXResult<DigipinBoundingBox>> {
        return try {
            var latMin = INDIA_MIN_LAT
            var latMax = INDIA_MAX_LAT
            var lonMin = INDIA_MIN_LON
            var lonMax = INDIA_MAX_LON

            val cleanCode = code.replace("-", "")

            for (char in cleanCode) {
                var found = false
                var row = -1
                var col = -1

                // find char in grid
                for (r in 0..3) {
                    for (c in 0..3) {
                        if (SYMBOLS[r * 4 + c] == char) {
                            row = r
                            col = c
                            found = true
                            break
                        }
                    }
                    if (found) break
                }

                if (!found) {
                    val error = DigipinXResult.Error(
                        "Invalid character in DIGIPIN code: $char",
                        "INVALID_CHARACTER"
                    )
                    return Pair(error, error)
                }

                val latDiv = (latMax - latMin) / 4
                val lonDiv = (lonMax - lonMin) / 4

                val lat1 = latMax - latDiv * (row + 1)
                val lat2 = latMax - latDiv * row
                val lon1 = lonMin + lonDiv * col
                val lon2 = lonMin + lonDiv * (col + 1)

                // Update bounding box for next level
                latMin = lat1
                latMax = lat2
                lonMin = lon1
                lonMax = lon2
            }

            val centerCoord = DigipinCoordinate.create(
                latitude = (latMin + latMax) / 2,
                longitude = (lonMin + lonMax) / 2
            )

            val boundingBox = DigipinBoundingBox.create(
                southwest = DigipinCoordinate(latMin, lonMin),
                northeast = DigipinCoordinate(latMax, lonMax)
            )

            Pair(centerCoord, boundingBox)
        } catch (e: Exception) {
            val error =
                DigipinXResult.Error("Failed to decode DIGIPIN: ${e.message}", "DECODE_FAILED")
            Pair(error, error)
        }
    }


}

internal data class Config(
    val precisionLevel: Int = DigipinXS.DIGIPOINT_CODE_LENGTH
)

private fun Validation.ValidationResult.throwIfInvalid() {
    if (!isValid) {
        throw DigipinXException(errorMessage ?: "Validation failed")
    }
} 