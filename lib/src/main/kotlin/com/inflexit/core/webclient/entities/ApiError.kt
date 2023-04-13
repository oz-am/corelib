package com.inflexit.core.webclient.entities

import java.io.Serializable

data class ApiError(
    val code: Int,
    val message: String? = null,
    val error_description: String? = null,
    val error: String? = null,
    val errorList: ArrayList<String>? = null
) : Serializable
