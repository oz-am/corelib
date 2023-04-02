package com.inflexit.core.webclient

import com.inflexit.core.exception.DomainException
import com.inflexit.core.exception.NetworkInterchangeException
import com.inflexit.core.exception.ServerException
import com.inflexit.core.webclient.entities.ApiError
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyExtractor
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import kotlin.reflect.KClass

class WebClientConfig {

    private var host:String = "localhost"
    private var port: Int = 80
    private var protocol: URLProtocol = URLProtocol.HTTPS
    private var headers: MultiValueMap<String, String> = LinkedMultiValueMap()
    private lateinit var consumer: ((HttpHeaders)->Unit)
    private lateinit var client: WebClient

    companion object{
        fun create(
            host: String? = null,
            port: Int? = null,
            protocol: URLProtocol? = null,
            headers: MultiValueMap<String, String>? = null
        ): Call {
            val clientConfig = WebClientConfig()
            clientConfig.url(host, port, protocol, headers)
            clientConfig.initClient()
            return Call(clientConfig)
        }
    }

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

    private val httpClient = HttpClient.create()
        .responseTimeout(Duration.ofSeconds(30))
        .doOnConnected{con -> con
            .addHandlerLast(ReadTimeoutHandler(30))
            .addHandlerLast(WriteTimeoutHandler(30))
        }

    private val conector = ReactorClientHttpConnector(httpClient)

    private fun initClient(){
        consumer = { it : HttpHeaders -> it.addAll(this.headers) }
        client = WebClient.builder()
            .baseUrl("${this.protocol.name}://${this.host}:${this.port}")
            .clientConnector(conector)
            .defaultHeaders(consumer)
            .build()
    }

    internal fun <T : Any>handleResponse(response: ClientResponse, clazz: KClass<T>): Mono<Any>? {


        if(response.statusCode().is2xxSuccessful)
            return response.bodyToMono(clazz.java)
        else if (response.statusCode().is4xxClientError)
            return response
                .bodyToMono(ApiError::class.java)
                .flatMap { errorBody ->
                    Mono.error(NetworkInterchangeException(errorBody))
                }
        else{
            val apiError = ApiError(
                code = response.statusCode().value(),
                message = "an error has been occured"
            )
            return Mono.error(ServerException(apiError))
        }

    }
    internal fun getClient() = client
}