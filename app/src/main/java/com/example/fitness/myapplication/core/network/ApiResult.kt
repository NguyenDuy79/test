package com.example.fitness.myapplication.core.network

/**
 * Result pattern đại diện cho kết quả thành công hoặc thất bại của một tác vụ API/Core.
 * Tương thích với `Result<AppException, T>` của Flutter app_core.
 */
sealed class ApiResult<out T> {

    data class Success<out T>(val data: T) : ApiResult<T>()

    data class Failure(val exception: AppException) : ApiResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onFailure(action: (AppException) -> Unit): ApiResult<T> {
        if (this is Failure) action(exception)
        return this
    }

    inline fun <R> map(transform: (T) -> R): ApiResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Failure -> Failure(exception)
    }

    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (AppException) -> R,
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Failure -> onFailure(exception)
    }
}
