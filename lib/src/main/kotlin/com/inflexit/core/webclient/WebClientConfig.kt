package com.inflexit.core.webclient

import com.inflexit.core.webclient.entities.ApiError
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.function.Consumer
import kotlin.reflect.KClass

class WebClientConfig {

    var host:String = "localhost"
    var port: Int = 80
    var protocol: URLProtocol = URLProtocol.HTTPS
    var headers: MultiValueMap<String, String> = LinkedMultiValueMap()

    private fun url(
        host: String? = null,
        port: Int? = null,
        protocol: URLProtocol? = null,
        headers: MultiValueMap<String, String>? = null
    ) {
        host?.let { this.host = it }
        port?.let { this.port = it }
        protocol?.let { this.protocol = it }
        headers?.let {
            this.headers = it
            if(this.headers.contains(HttpHeaders.CONTENT_TYPE).not())
                this.headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        } ?: run {
            this.headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        }
    }

    private val consumer = { it : HttpHeaders -> it.addAll(this.headers) } as Consumer<HttpHeaders>

    private val httpClient = HttpClient.create()
        .responseTimeout(Duration.ofSeconds(30))
        .doOnConnected{con -> con
            .addHandlerLast(ReadTimeoutHandler(30))
            .addHandlerLast(WriteTimeoutHandler(30))
        }

    private val conector = ReactorClientHttpConnector(httpClient)

    private val client = WebClient.builder()
        .baseUrl("${this.protocol.name}://${this.host}:${this.port}")
        .clientConnector(conector)
        .defaultHeaders(consumer)
        .build()

    fun create(
        host: String? = null,
        port: Int? = null,
        protocol: URLProtocol? = null,
        headers: MultiValueMap<String, String>? = null
    ): WebClientConfig {
        url(host, port, protocol, headers)
        return this
    }

    fun <T : Any>handleResponse(response: ClientResponse, clazz: KClass<T>): Mono<Any>? {
        if(response.statusCode().is2xxSuccessful)
            return response.bodyToMono(clazz.java)
        else if (response.statusCode().is4xxClientError)
            return response.bodyToMono(ApiError::class.java)
        else
            return Mono.just("Server error")
    }

    //region HttpMethods

    fun <T : Any> get(
        route: String,
        pathParams: Array<String>? = null,
        queryParams: MultiValueMap<String, String>? = null,
        clazz: KClass<T>
    ): T? {
        return client
            .get()
            .uri {
                it.path(route)
                queryParams?.let {params ->
                    it.queryParams(params)
                }
                if(pathParams != null) {
                    it.build(pathParams)
                } else {
                    it.build()
                }
            }
            .exchangeToMono { return@exchangeToMono handleResponse(it, clazz) }
            .cast(clazz.java)
            .block()
    }

    fun <T : Any> post(
        route: String,
        body: Any? = null,
        pathParams: Array<String>? = null,
        queryParams: MultiValueMap<String, String>? = null,
        clazz: KClass<T>): T? {
         return client
            .post()
            .uri{
                it.path(route)
                queryParams?.let {params ->
                    it.queryParams(params)
                }
                if(pathParams != null) {
                    it.build(pathParams)
                } else {
                    it.build()
                }
            }
            .apply {
                if(body != null){
                    body(Mono.just(body), body::class.java)
                }
            }
             .exchangeToMono { return@exchangeToMono handleResponse(it, clazz) }
             .cast(clazz.java)
             .block()
    }
    //endregion
}