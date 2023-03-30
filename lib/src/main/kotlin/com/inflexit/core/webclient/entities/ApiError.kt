package com.inflexit.core.webclient.entities

import java.io.Serializable

data class ApiError(
    val code: Int,
    val message: String,
    val errorList: ArrayList<String>? = null
) : Serializable
