package com.inflexit.core.interactor

import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

interface Interactor<in Request, out Response> {
    fun execute(request: Request): Response
}

interface InteractorExecutor {
    operator fun <RequestDto, ResponseDto, Request, Response> invoke(
        interactor: Interactor<Request, Response>,
        requestDto: RequestDto,
        requestConverter: (RequestDto) -> Request,
        responseConverter: (Response) -> ResponseDto
    ): CompletionStage<ResponseDto>

    operator fun <RequestDto, Request> invoke(
        interactor: Interactor<Request, Unit>,
        requestDto: RequestDto,
        requestConverter: (RequestDto) -> Request
    ) = invoke(interactor, requestDto, requestConverter) {}

    operator fun invoke(useCase: Interactor<Unit, Unit>) = invoke(useCase, Unit) {}
    operator fun <ResponseDto, Response> invoke(
        interactor: Interactor<Unit, Response>,
        responseConverter: (Response) -> ResponseDto
    ) = invoke(interactor, Unit, {}, responseConverter)
}

class InteractorExecutorImpl : InteractorExecutor {
    override fun <RequestDto, ResponseDto, Request, Response> invoke(
        interactor: Interactor<Request, Response>,
        requestDto: RequestDto,
        requestConverter: (RequestDto) -> Request,
        responseConverter: (Response) -> ResponseDto
    ): CompletionStage<ResponseDto> = CompletableFuture
        .supplyAsync { requestConverter(requestDto) }
        .thenApplyAsync { interactor.execute(it) }
        .thenApplyAsync { responseConverter(it) }
}
