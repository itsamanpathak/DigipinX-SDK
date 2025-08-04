# DigipinX Android SDK

<div align="center">
  <img src="app/src/main/ic_launcher-playstore.png" alt="DigipinX Logo" width="120" height="120">
  <br>
  <em>Digipin finder SDK for Android</em>
</div>

## Overview

DigipinX Android SDK is an unofficial implementation of the Digipin location system for Android applications. This SDK is forked from the JavaScript version of the official Digipin SDK by India Post and provides comprehensive functionality for working with Digipin codes in Android applications.

## Features

- **Generate Digipin codes** from latitude/longitude coordinates
- **Convert Digipin codes** back to coordinates
- **Find neighboring** Digipin codes
- **Search codes within radius** of a location
- **Validate** Digipin codes and coordinates
- **Calculate distances** and areas
- **Generate Google Maps URLs** for locations
- **Safe error handling** with Result wrapper pattern
- **India-specific bounds** validation

## Installation

### Via JitPack

Add JitPack repository to your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation 'com.github.itsamanpathak:DigipinX-SDK:1.0.0'
}
```

## Quick Start

### Initialize the SDK

```kotlin
val digipinSDK = DigipinXS.init()
```

### Generate Digipin from Coordinates

```kotlin
val result = digipinSDK.generateDigipin(28.6139, 77.2090) // New Delhi
when (result) {
    is DigipinXResult.Success -> {
        val digipin = result.data
        println("Generated code: ${digipin.digipin}")
        println("Center: ${digipin.centerCoordinate}")
        println("Formatted: ${digipin.getFormattedCode()}")
    }
    is DigipinXResult.Error -> {
        println("Error: ${result.message}")
    }
}
```

### Generate Coordinates from Digipin

```kotlin
val result = digipinSDK.generateLatLon("39J438P582")
when (result) {
    is DigipinXResult.Success -> {
        val location = result.data
        println("Latitude: ${location.centerCoordinate.latitude}")
        println("Longitude: ${location.centerCoordinate.longitude}")
    }
    is DigipinXResult.Error -> {
        println("Error: ${result.message}")
    }
}
```

### Find Neighboring Codes

```kotlin
val result = digipinSDK.getNeighbors("39J438P582", radius = 1)
when (result) {
    is DigipinXResult.Success -> {
        result.data.forEach { neighbor ->
            println("Neighbor: ${neighbor.digipin}")
        }
    }
    is DigipinXResult.Error -> {
        println("Error: ${result.message}")
    }
}
```

### Search Within Radius

```kotlin
val center = DigipinCoordinate.create(28.6139, 77.2090)
if (center is DigipinXResult.Success) {
    val result = digipinSDK.findDigipointCodesInRadius(center.data, 1000.0) // 1km
    when (result) {
        is DigipinXResult.Success -> {
            println("Found ${result.data.size} codes within radius")
        }
        is DigipinXResult.Error -> {
            println("Error: ${result.message}")
        }
    }
}
```

## API Reference

### Core Methods

| Method | Description | Returns |
|--------|-------------|---------|
| `generateDigipin(lat, lon)` | Generate Digipin code from coordinates | `DigipinXResult<DigipinModel>` |
| `generateLatLon(code)` | Generate coordinates from Digipin code | `DigipinXResult<DigipinModel>` |
| `getNeighbors(code, radius)` | Find neighboring Digipin codes | `DigipinXResult<List<DigipinModel>>` |
| `findDigipointCodesInRadius(center, meters)` | Find codes within distance | `DigipinXResult<List<DigipinModel>>` |
| `isValidDigipointCode(code)` | Validate Digipin code format | `Boolean` |
| `isWithinIndianBounds(coordinate)` | Check if coordinate is within India | `Boolean` |

### Utility Methods

| Method | Description | Returns |
|--------|-------------|---------|
| `createGoogleMapsUrl(digipin)` | Create Maps URL for location | `DigipinXResult<String>` |
| `calculateDistance(coord1, coord2)` | Calculate distance between points | `DigipinXResult<Double>` |
| `calculateAreaSquareMeters(digipin)` | Calculate area of Digipin region | `DigipinXResult<Double>` |
| `getPrecisionDescription(digipin)` | Get precision description | `DigipinXResult<String>` |

### Data Models

#### DigipinModel
```kotlin
data class DigipinModel(
    val digipin: String,           // The 10-character code
    val centerCoordinate: DigipinCoordinate,
    val boundingBox: DigipinBoundingBox
) {
    fun getFormattedCode(): String // Returns formatted code (XXX-XXX-XXXX)
}
```

#### DigipinCoordinate
```kotlin
data class DigipinCoordinate(
    val latitude: Double,
    val longitude: Double
)
```

#### DigipinXResult
```kotlin
sealed class DigipinXResult<out T> {
    data class Success<T>(val data: T) : DigipinXResult<T>()
    data class Error(val message: String, val code: String?) : DigipinXResult<Nothing>()
}
```

## Error Handling

The SDK uses a `DigipinXResult` wrapper for safe error handling:

```kotlin
when (val result = digipinSDK.generateDigipin(lat, lon)) {
    is DigipinXResult.Success -> {
        // Handle success case
        val digipin = result.data
    }
    is DigipinXResult.Error -> {
        // Handle error case
        println("Error: ${result.message}")
        println("Code: ${result.code}")
    }
}
```

Common error codes:
- `INVALID_FORMAT` - Invalid Digipin code format
- `OUT_OF_BOUNDS` - Coordinates outside Indian bounds
- `INVALID_COORDINATES` - Invalid latitude/longitude values
- `GENERATION_FAILED` - Failed to generate code/coordinates

## Requirements

- **Minimum SDK**: 21 (Android 5.0)
- **Target SDK**: 35 (Android 14)
- **Java Version**: 17+
- **Language**: Kotlin
- **Geographic Scope**: India (coordinates must be within Indian bounds)

## Sample App

This repository includes a demo application that showcases all SDK features. To run the sample:

1. Clone this repository
2. Open in Android Studio
3. Run the `app` module

## Contributing

This is an unofficial implementation. Contributions are welcome! Please feel free to submit issues and pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Disclaimer

This is an **unofficial** SDK based on the official Digipin specification by India Post. It is not affiliated with or endorsed by India Post. Use at your own discretion.

---

<div align="center">
Made with ❤️ for the Android developer community
</div>