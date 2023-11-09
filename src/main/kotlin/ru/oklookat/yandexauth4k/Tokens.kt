package ru.oklookat.yandexauth4k

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.delay
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request

class Tokens {
    internal companion object {
        private const val ENDPOINT = "https://oauth.yandex.ru/token"

        /** Пользователь еще не ввел код подтверждения. */
        private const val AUTHORIZATION_PENDING = "authorization_pending"

        /** Приложение с указанным идентификатором (параметр client_id) не найдено или заблокировано.

        Этот код также возвращается, если в параметре client_secret передан неверный пароль приложения. */
        private const val INVALID_CLIENT = "invalid_client"

        /** Неверный или просроченный код подтверждения.*/
        private const val INVALID_GRANT = "invalid_grant"

        suspend fun request(deviceCode: String, codesInterval: Int, clientId: String, clientSecret: String): Response {
            val formBody = FormBody.Builder()
                .add("grant_type", "device_code")
                .add("code", deviceCode)
                .add("client_id", clientId)
                .add("client_secret", clientSecret).build()

            val client = OkHttpClient()
            val mapper = jacksonObjectMapper()
            val req = Request.Builder().url(ENDPOINT)
                .post(formBody).build()

            while (true) {
                client.newCall(req).execute().use {
                    if (it.body == null) throw HttpException("Request tokens, null body", it.code)
                    val bodyStr = it.body!!.string()
                    if (it.isSuccessful) {
                        return mapper.readValue(bodyStr, Response::class.java)
                    }
                    val err = mapper.readValue(bodyStr, Error::class.java)
                    when (err.error) {
                        AUTHORIZATION_PENDING -> return@use
                        INVALID_CLIENT -> throw InvalidClientException(err)
                        INVALID_GRANT -> throw InvalidGrantException(err)
                        else -> throw err
                    }
                }
                delay(codesInterval.toLong())
            }
        }

        fun refresh(refreshToken: String, clientId: String, clientSecret: String): Response {
            val formBody = FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken)
                .add("client_id", clientId)
                .add("client_secret", clientSecret).build()

            val client = OkHttpClient()
            val mapper = jacksonObjectMapper()
            val req = Request.Builder().url(ENDPOINT)
                .post(formBody).build()

            client.newCall(req).execute().use {
                if (it.body == null) throw HttpException("Refresh tokens, null body", it.code)
                val bodyStr = it.body!!.string()
                if (!it.isSuccessful) {
                    val err = mapper.readValue(bodyStr, Error::class.java)
                    throw err
                }
                return mapper.readValue(bodyStr, Response::class.java)
            }
        }
    }

    data class InvalidClientException(val error: Error) : Throwable() {
        override val message: String = "Wrong client_id or client_secret."
    }

    data class InvalidGrantException(val error: Error) : Throwable() {
        override val message: String = "Incorrect or expired confirmation code."
    }

    data class Error(
        @JsonProperty("error_description")
        val errorDescription: String,
        val error: String,
    ) : Throwable()

    /** Яндекс.OAuth возвращает OAuth-токен, refresh-токен и время их жизни в JSON-формате.

    https://yandex.ru/dev/id/doc/dg/oauth/reference/simple-input-client.html#simple-input-client__token-body-title*/
    data class Response(
        /** Тип выданного токена. Всегда принимает значение «bearer».*/
        @JsonProperty("token_type")
        val tokenType: String,

        /** OAuth-токен с запрошенными правами или с правами, указанными при регистрации приложения. */
        @JsonProperty("access_token")
        val accessToken: String,

        /** Время жизни токена в секундах. */
        @JsonProperty("expires_in")
        val expiresIn: Long,

        /** Токен, который можно использовать для продления срока жизни соответствующего OAuth-токена.
        Время жизни refresh-токена совпадает с временем жизни OAuth-токена. */
        @JsonProperty("refresh_token")
        val refreshToken: String
    )
}