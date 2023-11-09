package ru.oklookat.yandexauth4k

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request


internal class ConfirmationCodes {
    companion object {
        private const val ENDPOINT = "https://oauth.yandex.ru/device/code"

        /** Приложение запрашивает два кода — device_code для устройства и user_code для пользователя. Время жизни предоставленных кодов — 10 минут. По истечении этого времени коды нужно запросить заново.

        https://yandex.ru/dev/id/doc/dg/oauth/reference/simple-input-client.html#simple-input-client__get-codes
         */
        fun send(clientID: String, deviceId: String, deviceName: String?): Response {
            val formBody = FormBody.Builder()
                .add("client_id", clientID)
                .add("device_id", deviceId)
            if (deviceName != null) formBody.add("device_name", deviceName)

            val bodyBuild = formBody.build()

            val client = OkHttpClient()
            val mapper = jacksonObjectMapper()
            val req = Request.Builder().url(ENDPOINT)
                .post(bodyBuild).build()

            val resp: Response
            client.newCall(req).execute().use {
                if (!it.isSuccessful) throw HttpException("Send confirmation codes", it.code)
                if (it.body == null) throw HttpException("Send confirmation codes, empty body", it.code)
                resp = mapper.readValue(it.body!!.string(), Response::class.java)
            }
            return resp
        }
    }

    data class Response(
        @JsonProperty("device_code")
        val deviceCode: String,
        @JsonProperty("user_code")
        val userCode: String,
        @JsonProperty("verification_url")
        val verificationUrl: String,
        val interval: Int,
        @JsonProperty("expires_in")
        val expiresIn: Int
    )
}