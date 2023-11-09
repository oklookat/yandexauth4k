package ru.oklookat.yandexauth4k

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class YandexAuth4k {
    companion object {
        /**
         * @param clientID ID приложения.
         *
         * @param clientSecret Secret приложения.
         *
         *
         * @param onUrlCode Перейти по URL Яндекса, войти в аккаунт, ввести код. Спустя несколько секунд будут получены токены.
         *
         * @param deviceName Имя устройства, которое следует показывать пользователям. Не длиннее 100 символов.
         *
         * Для мобильных устройств рекомендуется передавать имя устройства, заданное пользователем. Если такого имени нет, его можно собрать из модели устройства, названия и версии ОС и т. д.
         *
         * Если параметр device_name передан без параметра device_id, он будет проигнорирован. Яндекс OAuth сможет выдать только обычный токен, не привязанный к устройству.
         *
         * @param deviceId Уникальный идентификатор устройства, для которого запрашивается токен. Чтобы обеспечить уникальность, достаточно один раз сгенерировать UUID и использовать его при каждом запросе нового токена с данного устройства.
         *
         * Идентификатор должен быть не короче 6 символов и не длиннее 50. Допускается использовать только печатаемые ASCII-символы (с кодами от 32 до 126).
         *
         * Если параметр device_id передан без параметра device_name, в пользовательском интерфейсе токен будет помечен как выданный для неизвестного устройства.
         * */
        suspend fun start(
            clientID: String,
            clientSecret: String,
            deviceId: String,
            deviceName: String?,
            onUrlCode: (url: String, code: String) -> Unit
        ): Tokens.Response {
            val tokens: Tokens.Response
            runBlocking {
                // Запрашиваем коды.
                val codes = ConfirmationCodes.send(clientID, deviceId, deviceName)

                launch {
                    // Пользователь идет вводить код на странице Яндекса...
                    onUrlCode(codes.verificationUrl, codes.userCode)
                }

                tokens = Tokens.request(codes.deviceCode, codes.interval, clientID, clientSecret)
            }
            return tokens
        }
    }
}