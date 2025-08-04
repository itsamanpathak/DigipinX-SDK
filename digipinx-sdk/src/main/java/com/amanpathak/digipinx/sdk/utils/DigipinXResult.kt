package com.amanpathak.digipinx.sdk.utils

sealed class DigipinXResult<out T> {
    data class Success<T>(val data: T) : DigipinXResult<T>()
    data class Error(val message: String, val code: String? = null) : DigipinXResult<Nothing>()
}