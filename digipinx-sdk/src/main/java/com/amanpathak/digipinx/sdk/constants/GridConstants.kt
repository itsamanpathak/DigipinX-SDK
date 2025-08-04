package com.amanpathak.digipinx.sdk.constants

internal object GridConstants {
    val DIGIPOINT_GRID = arrayOf(
        arrayOf('F', 'C', '9', '8'),
        arrayOf('J', '3', '2', '7'),
        arrayOf('K', '4', '5', '6'),
        arrayOf('L', 'M', 'P', 'T')
    )
    
    val SYMBOLS = DIGIPOINT_GRID.flatMap { it.toList() }
    const val DIGIPOINT_PATTERN = "[FC98J327K456LMPT]{10}"
    const val GRID_SIZE = 4
    const val GRID_ROWS = 4
    const val GRID_COLS = 4
} 