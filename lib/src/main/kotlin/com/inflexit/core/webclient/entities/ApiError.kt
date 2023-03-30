package com.inflexit.core.webclient.entities

data class ApiError(
    val code: Int,
    val message: String,
    val errorList: ArrayList<String>? = null
)
