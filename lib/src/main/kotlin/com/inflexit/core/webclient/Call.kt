package com.inflexit.core.webclient

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import java.io.Serializable

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

    private fun isFormDataContentType(contentType: String?) = contentType in listOf(
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE
        )

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
        body: Serializable?,
        pathParams: Array<String>? = null,
        queryParams: MultiValueMap<String, String>? = null,
        headers: MultiValueMap<String, String>? = null,
    ): O? {
        return post(
            route,
            body,
            pathParams,
            queryParams,
            headers,
            O::class.java
        )
    }

    inline fun <reified O : Any?> post(
        route: String,
        body: MultiValueMap<String, String>,
        pathParams: Array<String>? = null,
        queryParams: MultiValueMap<String, String>? = null,
        headers: MultiValueMap<String, String>? = null,
    ): O? {

        return post(
            route,
            body,
            pathParams,
            queryParams,
            headers,
            O::class.java
        )
    }

    fun <O> post(
        route: String,
        body: Any?,
        pathParams: Array<String>? = null,
        queryParams: MultiValueMap<String, String>? = null,
        headers: MultiValueMap<String, String>? = null,
        clazz: Class<O>
    ): O? {
        val output = clientConfig
            .getClient()
            .post()
            .apply {
                if(isFormDataContentType(headers?.getFirst(HttpHeaders.CONTENT_TYPE))){
                    body(BodyInserters.fromFormData(body as MultiValueMap<String, String>))
                } else if(body != null){
                    body(Mono.just(body), body::class.java)
                }
            }
            .uri{buildUri(it,route,pathParams,queryParams)}
            .headers { it : HttpHeaders -> headers?.let { _ -> it.addAll(headers) } }

        return callExecutor.executeMono(output, clazz)
    }
}