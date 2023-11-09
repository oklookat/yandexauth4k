package ru.oklookat.yandexauth4k

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Properties
import kotlin.io.path.Path

class YandexAuth4kTest {
    @Test
    fun start() {
        val props = Props()
        runBlocking {
            val result =
                YandexAuth4k.start(props.clientId, props.clientSecret, props.deviceId, props.deviceName) { url, code ->
                    println("URL: $url")
                    println("Code: $code")
                }
            assertTrue(result.accessToken.isNotEmpty())
            assertTrue(result.refreshToken.isNotEmpty())
            assertTrue(result.expiresIn > 0)
            assertTrue(result.tokenType.isNotEmpty())
        }
    }

    @Test
    fun refresh() {
        val props = Props()
        val result = Tokens.refresh(props.refreshToken, props.clientId, props.clientSecret)
        assertTrue(result.accessToken.isNotEmpty())
        assertTrue(result.refreshToken.isNotEmpty())
        assertTrue(result.expiresIn > 0)
        assertTrue(result.tokenType.isNotEmpty())
    }

    private class Props {
        val clientId: String
        val clientSecret: String
        val deviceId: String
        val deviceName: String
        val refreshToken: String

        init {
            Path("src/test/resources/.env.properties").toFile()
            val file = Path("src/test/resources/.env.properties").toFile()?: throw Exception("Null resource")
            val prop = Properties()
            file.inputStream().use {
                prop.load(it)
            }
            clientId = prop.getProperty("clientId")
            clientSecret = prop.getProperty("clientSecret")
            deviceId = prop.getProperty("deviceId")
            deviceName = prop.getProperty("deviceName")
            refreshToken= prop.getProperty("refreshToken")
        }
    }
}