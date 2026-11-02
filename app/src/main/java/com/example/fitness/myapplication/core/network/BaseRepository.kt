package com.example.fitness.myapplication.core.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Lớp Repository cơ sở xử lý an toàn mọi cuộc gọi API và phân loại Exception tương tự app_core.
 */
abstract class BaseRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun <T> callApiCatching(
        apiCall: suspend () -> ApiEnvelope<T>,
    ): ApiResult<T> = withContext(dispatcher) {
        try {
            val response = apiCall()
            if (response.isSuccessful && response.data != null) {
                ApiResult.Success(response.data)
            } else if (response.isSuccessful && response.data == null) {
                // Một số API trả 200 thành công nhưng data rỗng (vd submit tracking)
                @Suppress("UNCHECKED_CAST")
                ApiResult.Success(Unit as T)
            } else {
                ApiResult.Failure(
                    AppException.Remote.Other(
                        code = response.errorCode,
                        message = response.message.ifEmpty { "Yêu cầu không thành công (${response.errorCode})" },
                    )
                )
            }
        } catch (e: Throwable) {
            ApiResult.Failure(mapThrowableToAppException(e))
        }
    }

    suspend fun <T> callApiListCatching(
        apiCall: suspend () -> ApiListEnvelope<T>,
    ): ApiResult<List<T>> = withContext(dispatcher) {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                ApiResult.Success(response.data ?: emptyList())
            } else {
                ApiResult.Failure(
                    AppException.Remote.Other(
                        code = response.errorCode,
                        message = response.message.ifEmpty { "Yêu cầu danh sách không thành công" },
                    )
                )
            }
        } catch (e: Throwable) {
            ApiResult.Failure(mapThrowableToAppException(e))
        }
    }

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T,
    ): ApiResult<T> = withContext(dispatcher) {
        try {
            ApiResult.Success(apiCall())
        } catch (e: Throwable) {
            ApiResult.Failure(mapThrowableToAppException(e))
        }
    }

    private fun mapThrowableToAppException(throwable: Throwable): AppException {
        return when (throwable) {
            is AppException -> throwable
            is UnknownHostException -> AppException.Remote.NetworkUnavailable()
            is SocketTimeoutException -> AppException.Remote.Timeout()
            is HttpException -> {
                val code = throwable.code()
                val message = throwable.message() ?: "Lỗi máy chủ ($code)"
                when (code) {
                    401 -> AppException.Remote.Unauthorized(message = "Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.")
                    in 500..599 -> AppException.Remote.Server(code = code, message = "Máy chủ gặp sự cố ($code)")
                    else -> AppException.Remote.Other(code = code, message = message, cause = throwable)
                }
            }
            is SerializationException -> AppException.Remote.Parsing(
                message = "Lỗi xử lý định dạng dữ liệu",
                cause = throwable,
            )
            is IOException -> AppException.Remote.Other(
                message = throwable.localizedMessage ?: "Lỗi kết nối I/O",
                cause = throwable,
            )
            else -> AppException.Remote.Other(
                message = throwable.localizedMessage ?: "Đã xảy ra lỗi không xác định",
                cause = throwable,
            )
        }
    }
}
