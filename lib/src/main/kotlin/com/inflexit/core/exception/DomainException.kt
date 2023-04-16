package com.inflexit.core.exception

import com.inflexit.core.webclient.entities.ApiError

open class DomainException(message: String?) : RuntimeException(message)

open class NetworkInterchangeException(val apiError: ApiError):RuntimeException(apiError.message ?: apiError.error)
open class ServerException(apiError: ApiError):DomainException(apiError.message)
