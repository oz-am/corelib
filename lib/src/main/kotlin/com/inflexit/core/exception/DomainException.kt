package com.inflexit.core.exception

import com.inflexit.core.webclient.entities.ApiError

open class DomainException(message: String?) : RuntimeException(message)

open class NetworkInterchangeException(apiError: ApiError):DomainException(apiError.message)
open class ServerException(apiError: ApiError):DomainException(apiError.message)
