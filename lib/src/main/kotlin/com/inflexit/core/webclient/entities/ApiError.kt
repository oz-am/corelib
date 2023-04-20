package com.inflexit.core.webclient.entities

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class ApiError(
    @JsonProperty("code") var code: Int = 500,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("error_description") val errorDescription: String? = null,
    @JsonProperty("error") val error: String? = null,
    @JsonProperty("errorList") val errorList: ArrayList<String>? = null
) : Serializable
