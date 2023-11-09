package ru.oklookat.yandexauth4k

class HttpException(where: String,statusCode: Int) : Throwable() {
    override val message: String = "$where. Status code: $statusCode."
}