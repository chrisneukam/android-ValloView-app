/**
 * Data persistence layer using DataStore for ValloView settings.
 *
 * Provides access to persisted application settings like the configured service
 * IP address. Uses Android DataStore (Preferences) for secure, asynchronous storage.
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
package de.codingsolutions.valloview.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Repository for managing ValloView application settings.
 *
 * Provides reactive access to persisted preferences through Flow, allowing
 * the UI layer to observe changes and respond automatically. All read and
 * write operations are safe for coroutines and UI layers.
 *
 * @param context application context used to access the DataStore instance.
 */
class SettingsRepository(private val context: Context) {
    companion object {
        /** DataStore key for the saved service IP address. */
        private val IP_ADDRESS_KEY = stringPreferencesKey("ip_address")
    }

    /**
     * Observable flow of the configured service IP address.
     *
     * Emits the currently saved IP address whenever it changes, or `null` if
     * no IP has been configured yet. Collectors on this flow receive updates
     * reactively without polling.
     */
    val ipAddressFlow: Flow<String?> =
        context.dataStore.data
            .map { preferences ->
                preferences[IP_ADDRESS_KEY]
            }

    /**
     * Persists a new service IP address to storage.
     *
     * The change is immediately reflected in [ipAddressFlow] for all observers.
     *
     * @param ip the IP address or URL to persist.
     */
    suspend fun saveIpAddress(ip: String) {
        context.dataStore.edit { preferences ->
            preferences[IP_ADDRESS_KEY] = ip
        }
    }
}
