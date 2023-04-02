package com.inflexit.core.webclient

import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.io.Serializable
import kotlin.reflect.KClass

class Call(private val clientConfig: WebClientConfig) {

    private val callExecutor = CallExecutor(clientConfig)
    private val buildUri = {
            builder: UriBuilder,
            route: String,
            pathParams: Array<String>? ,
            queryParams: MultiValueMap<String, String>? ->

        builder.path(route)
        queryParams?.let {params ->
            builder.queryParams(params)
        }
        if(pathParams != null) {
            builder.build(pathParams)
        } else {
            builder.build()
        }
    }

    inline fun <reified O : Any?> get(
        route: String,
        pathParams: Array<String>? = null,
        queryParams: MultiValueMap<String, String>?=null
    ): O? {
        return get(
            route,
            pathParams,
            queryParams,
            O::class.java
        )
    }

    fun <O> get(
        route: String,
        pathParams: Array<String>? = null,
        queryParams: MultiValueMap<String, String>? = null,
        clazz: Class<O>
    ): O? {
        val output =  clientConfig
            .getClient()
            .get()
            .uri {buildUri(it,route,pathParams,queryParams)
            }

        return callExecutor.executeMono(output, clazz)
    }

    inline fun <reified O : Any?> delete(
        route: String,
        pathParams: Array<String>? = null,
    ): O? {
        return delete(
            route,
            pathParams,
            O::class.java
        )
    }

    fun <O> delete(
        route: String,
        pathParams: Array<String>? = null,
        clazz: Class<O>
    ): O? {
        val output =  clientConfig
            .getClient()
            .delete()
            .uri {buildUri(it,route,pathParams,null)}
        return callExecutor.executeMono(output, clazz)
    }

    inline fun <reified O : Any?> post(
        route: String,
        body: Any?,
        pathParams: Array<String>? = null,
        queryParams: MultiValueMap<String, String>? = null
    ): O? {
        return post(
            route,
            body,
            pathParams,
            queryParams,
            O::class.java
        )
    }

    fun <O> post(
        route: String,
        body: Any?,
        pathParams: Array<String>? = null,
        queryParams: MultiValueMap<String, String>? = null,
        clazz: Class<O>
    ): O? {
        val output = clientConfig
            .getClient()
            .post()
            .apply {
                if(body != null){
                    body(Mono.just(body), body::class.java)
                }
            }
            .uri{buildUri(it,route,pathParams,queryParams)}

        return callExecutor.executeMono(output, clazz)
    }
}