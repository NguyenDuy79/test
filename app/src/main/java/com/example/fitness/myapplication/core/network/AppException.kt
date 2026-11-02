package com.example.fitness.myapplication.core.network

/**
 * Cây ngoại lệ thống nhất cho toàn bộ tầng Network & Core.
 */
sealed class AppException(
    override val message: String,
    override val cause: Throwable? = null,
) : Exception(message, cause) {

    sealed class Remote(message: String, cause: Throwable? = null) : AppException(message, cause) {
        data class Unauthorized(
            override val message: String = "Phiên đăng nhập đã hết hạn",
            val code: Int = 401,
        ) : Remote(message)

        data class NetworkUnavailable(
            override val message: String = "Không có kết nối mạng. Vui lòng kiểm tra lại Wifi/4G.",
        ) : Remote(message)

        data class Server(
            val code: Int,
            override val message: String = "Máy chủ đang bận ($code)",
        ) : Remote(message)

        data class Timeout(
            override val message: String = "Kết nối quá hạn, vui lòng thử lại.",
        ) : Remote(message)

        data class Parsing(
            override val message: String = "Không thể đọc dữ liệu phản hồi từ máy chủ.",
            override val cause: Throwable? = null,
        ) : Remote(message, cause)

        data class Other(
            val code: Int? = null,
            override val message: String,
            override val cause: Throwable? = null,
        ) : Remote(message, cause)
    }

    data class Local(
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppException(message, cause)
}
