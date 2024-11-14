@file:Suppress("unused")

package ru.kotlin.homework.network

import ru.kotlin.homework.Circle
import java.lang.IllegalArgumentException
import java.time.LocalDateTime

/**
 * Известный вам список ошибок
 */
sealed class ApiException(message: String) : Throwable(message) {
    data object NotAuthorized : ApiException("Not authorized")
    data object NetworkException : ApiException("Not connected")
    data object UnknownException: ApiException("Unknown exception")
}

interface Logger<in E: Throwable> {
    fun log(response: NetworkResponse<*, E>)
    fun dumpLog()
}

interface ErrorDumper<out E: Throwable> {
    fun dump(): List<Pair<LocalDateTime, E>>
}

class ErrorLogger<E : Throwable>: Logger<E>, ErrorDumper<E> {

    private val errors = mutableListOf<Pair<LocalDateTime, E>>()

    override fun log(response: NetworkResponse<*, E>) {
        if (response is Failure) {
            errors.add(response.responseDateTime to response.error)
        }
    }

    override fun dumpLog() {
        errors.forEach { (date, error) ->
            println("Error at $date: ${error.message}")
        }
    }

    override fun dump(): List<Pair<LocalDateTime, E>> = errors.toList()
}

fun processThrowables(logger: Logger<Throwable>) {
    logger.log(Success("Success"))
    Thread.sleep(100)
    logger.log(Success(Circle))
    Thread.sleep(100)
    logger.log(Failure(IllegalArgumentException("Something unexpected")))

    logger.dumpLog()
}

fun processApiErrors(apiExceptionLogger: Logger<ApiException>) {
    apiExceptionLogger.log(Success("Success"))
    Thread.sleep(100)
    apiExceptionLogger.log(Success(Circle))
    Thread.sleep(100)

    val f = Failure(ApiException.NetworkException)
    apiExceptionLogger.log(f)

    apiExceptionLogger.dumpLog()
}

fun dumpErrors(logger: ErrorDumper<Throwable>) {
    logger.dump().forEach { (date, error) ->
        println("Error at $date: ${error.message}")
    }
}


fun main() {
    val logger = ErrorLogger<Throwable>()

    println("Processing Throwable:")
    processThrowables(logger)

    println("Processing Api:")
    processApiErrors(logger)

    println("Dump all errors:")
    dumpErrors(logger)
}

