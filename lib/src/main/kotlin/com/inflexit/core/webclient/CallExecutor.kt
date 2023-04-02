package com.inflexit.core.webclient

import com.inflexit.core.webclient.entities.ApiError
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.io.Serializable
import kotlin.reflect.KClass

class CallExecutor(val clientConfig: WebClientConfig) {

    internal fun <O> executeMono(output: WebClient.RequestHeadersSpec<*>, clazz: Class<O>): O? {
        return output
            .exchangeToMono { return@exchangeToMono clientConfig.handleResponse(it, clazz) }
            .onErrorResume{
                throw it
            }
            .cast(clazz)
            .block()
    }
}