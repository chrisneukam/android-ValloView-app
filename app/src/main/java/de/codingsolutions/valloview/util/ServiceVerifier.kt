/**
 * Utility object for verifying the availability and validity of the target service.
 *
 * This object performs HTTP checks against the configured host and validates
 * user-entered IP addresses before they are persisted or used by the app.
 *
 * @author Christian Neukam
 * @version 1.0
 * @date 2026-06-08
 *
 * Copyright 2026 Christian Neukam. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codingsolutions.valloview.util

import android.util.Log
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Provides functions to verify the remote service and validate IP input.
 */
object ServiceVerifier {
    private const val TAG = "ServiceVerifier"

    private val client =
        OkHttpClient
            .Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

    private val IDENTIFIERS =
        listOf(
            "IoGlobal.init()",
            "dashboard-configuration-page",
        )

    /**
     * Verifies the configured service by issuing an HTTP request to the URL.
     *
     * The function checks that the response is successful and contains specific
     * identifiers that prove the target service page was loaded.
     *
     * @param ip IP address or URL of the remote service.
     * @return `true` when the service is reachable and the response contains the expected identifiers.
     */
    suspend fun verifyService(ip: String): Boolean = withContext(Dispatchers.IO) {
        val url = if (ip.startsWith("http")) ip else "http://$ip"
        Log.d(TAG, "Verifying service at: $url")
        val request = Request.Builder().url(url).build()

        try {
            client.newCall(request).execute().use { response ->
                Log.d(TAG, "Response code: ${response.code}")
                if (!response.isSuccessful) return@withContext false
                val body = response.body.string()

                val containsAllIdentifiers = IDENTIFIERS.all { body.contains(it) }
                Log.d(TAG, "Body contains all identifiers: $containsAllIdentifiers")

                if (!containsAllIdentifiers) {
                    Log.d(TAG, "Body preview: ${body.take(200)}")
                }
                containsAllIdentifiers
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying service", e)
            false
        }
    }

    /**
     * Validates that the given string is a properly formed IPv4 address.
     *
     * @param ip candidate IP address string.
     * @return `true` when the string matches the IPv4 numeric pattern.
     */
    fun isValidIp(ip: String): Boolean {
        val ipv4Pattern =
            "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$"
                .toRegex()
        return ip.matches(ipv4Pattern)
    }
}
