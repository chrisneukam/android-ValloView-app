/**
 * Main entry point for the ValloView app.
 *
 * Hosts the activity and Compose screen definitions used to configure the
 * service endpoint, verify connectivity, and display the embedded WebView.
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
package de.codingsolutions.valloview

import android.content.res.Configuration
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.os.LocaleListCompat
import de.codingsolutions.valloview.data.SettingsRepository
import de.codingsolutions.valloview.ui.theme.ValloViewTheme
import de.codingsolutions.valloview.util.ServiceVerifier
import kotlinx.coroutines.launch

/**
 * The main activity of the app.
 *
 * Initializes application state, configures edge-to-edge display, and hosts the
 * root [MainScreen] composable.
 */
class MainActivity : AppCompatActivity() {
    /** Repository used to persist and observe the configured service IP address. */
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository(this)
        enableEdgeToEdge()
        setContent {
            ValloViewTheme {
                MainScreen(settingsRepository)
            }
        }
    }
}

/**
 * The UI states that drive the main screen flow.
 *
 * The state machine manages the experience from IP discovery to service
 * verification, web content display, and error recovery.
 */
enum class AppState {
    /** Loading saved IP configuration from persistent storage. */
    CheckingIP,

    /** Requesting the user to enter the target service IP address. */
    SetupIP,

    /** Verifying the configured IP before attempting to load the WebView. */
    VerifyingService,

    /** Showing the WebView once the service was successfully verified. */
    ShowingWebView,

    /** Displaying an error view when service verification failed. */
    Error,
}

/**
 * Hosts the main application UI and orchestrates navigation between screens.
 *
 * Observes the saved service IP from [repository], verifies service access, and
 * displays either the setup flow, loading state, WebView, or error UI.
 *
 * @param repository repository used to persist the configured service IP.
 * @param modifier optional [Modifier] for the composable layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(repository: SettingsRepository, modifier: Modifier = Modifier) {
    var appState by remember { mutableStateOf(AppState.CheckingIP) }
    var savedIp by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    // Bottom Bar State
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val bottomBarHeight = if (isLandscape) 56.dp else 100.dp

    LaunchedEffect(Unit) {
        repository.ipAddressFlow.collect { ip ->
            savedIp = ip
            if (ip == null) {
                appState = AppState.SetupIP
            } else {
                launch {
                    verifyAndLoad(ip) { state, msg ->
                        appState = state
                        errorMessage = msg ?: ""
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (appState == AppState.ShowingWebView || appState == AppState.Error) {
                BottomAppBar(
                    modifier = Modifier.height(bottomBarHeight),
                    actions = {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { showSettings = true }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings),
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier =
            Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            when (appState) {
                AppState.CheckingIP -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
                AppState.SetupIP ->
                    SetupScreen(
                        onIpSave = { ip ->
                            scope.launch {
                                repository.saveIpAddress(ip)
                            }
                        },
                    )
                AppState.VerifyingService ->
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.verifying_service))
                    }
                AppState.ShowingWebView -> savedIp?.let { WebViewScreen(it) }
                AppState.Error ->
                    ErrorScreen(
                        message = errorMessage,
                        onRetry = {
                            savedIp?.let {
                                scope.launch {
                                    verifyAndLoad(it) { state, msg ->
                                        appState = state
                                        errorMessage = msg ?: ""
                                    }
                                }
                            } ?: run { appState = AppState.SetupIP }
                        },
                    )
            }

            if (showSettings) {
                ModalBottomSheet(
                    onDismissRequest = { showSettings = false },
                    sheetState = sheetState,
                ) {
                    SettingsSheetContent(
                        currentIp = savedIp ?: "",
                        onSave = { newIp ->
                            scope.launch {
                                repository.saveIpAddress(newIp)
                                showSettings = false
                            }
                        },
                    )
                }
            }
        }
    }
}

/**
 * Verifies that the configured service is reachable and updates UI state.
 *
 * @param ip IP address or URL entered by the user.
 * @param onResult callback invoked with the next [AppState] and an optional
 * error message.
 */
private suspend fun verifyAndLoad(ip: String, onResult: (AppState, String?) -> Unit) {
    onResult(AppState.VerifyingService, null)
    val isValid = ServiceVerifier.verifyService(ip)
    if (isValid) {
        onResult(AppState.ShowingWebView, null)
    } else {
        onResult(AppState.Error, null) // String handled in UI
    }
}

/**
 * Renders the initial setup screen for entering a target service IP.
 *
 * The user can save a valid IP address, which triggers persistence through the
 * provided [onIpSave] callback.
 *
 * @param onIpSave callback invoked when the entered IP is valid and ready to be persisted.
 * @param modifier optional [Modifier] for the composable layout.
 */
@Composable
fun SetupScreen(onIpSave: (String) -> Unit, modifier: Modifier = Modifier) {
    var ipInput by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.enter_ip_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = ipInput,
            onValueChange = {
                ipInput = it
                isError = false
            },
            label = { Text(stringResource(R.string.ip_label)) },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
        )
        if (isError) {
            Text(
                stringResource(R.string.invalid_ip),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (ServiceVerifier.isValidIp(ipInput)) {
                    onIpSave(ipInput)
                } else {
                    isError = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.save_connect))
        }
    }
}

/**
 * Displays the settings bottom sheet content.
 *
 * Includes IP editing, language selection, and version metadata.
 *
 * @param currentIp the currently configured IP address shown in the editor.
 * @param onSave callback invoked when the user saves a new IP address.
 * @param modifier optional [Modifier] for the composable layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheetContent(
    currentIp: String,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var ipInput by remember { mutableStateOf(currentIp) }
    var isError by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val currentLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags()

    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.settings), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Left Column: IP Settings
                Column(modifier = Modifier.weight(1f)) {
                    IpSettingsSection(
                        ipInput = ipInput,
                        onIpChange = {
                            ipInput = it
                            isError = false
                        },
                        isError = isError,
                        onSave = {
                            if (ServiceVerifier.isValidIp(ipInput)) {
                                onSave(ipInput)
                            } else {
                                isError = true
                            }
                        },
                    )
                }

                // Right Column: Language & Version
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    LanguageSettingsSection(currentLocale)
                    Spacer(modifier = Modifier.weight(1f))
                    VersionInfo()
                }
            }
        } else {
            // Portrait: Vertical Layout
            IpSettingsSection(
                ipInput = ipInput,
                onIpChange = {
                    ipInput = it
                    isError = false
                },
                isError = isError,
                onSave = {
                    if (ServiceVerifier.isValidIp(ipInput)) {
                        onSave(ipInput)
                    } else {
                        isError = true
                    }
                },
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Spacer(modifier = Modifier.height(24.dp))

            LanguageSettingsSection(currentLocale)

            Spacer(modifier = Modifier.height(24.dp))
            VersionInfo()
        }
    }
}

/**
 * Renders the IP address editing section used in the settings sheet.
 *
 * @param ipInput current IP address text shown in the input field.
 * @param onIpChange callback invoked when the user changes the IP text.
 * @param isError whether the current input should be shown as invalid.
 * @param onSave callback invoked when the user presses the save button.
 * @param modifier optional [Modifier] for the composable layout.
 */
@Composable
fun IpSettingsSection(
    ipInput: String,
    onIpChange: (String) -> Unit,
    isError: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TextField(
            value = ipInput,
            onValueChange = onIpChange,
            label = { Text(stringResource(R.string.new_ip_label)) },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
        )
        if (isError) {
            Text(
                stringResource(R.string.invalid_ip),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.save_reconnect))
        }
    }
}

/**
 * Renders the language selection controls for the settings sheet.
 *
 * The selected locale is applied immediately through [AppCompatDelegate].
 *
 * @param currentLocale the current locale string used to determine selection state.
 * @param modifier optional [Modifier] for the composable layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsSection(currentLocale: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        val languages = listOf("en" to R.string.language_english, "de" to R.string.language_german)
        val selectedIndex =
            languages
                .indexOfFirst {
                    it.first == (
                        if (currentLocale.isEmpty()) {
                            "en"
                        } else {
                            currentLocale.take(
                                2,
                            )
                        }
                        )
                }.coerceAtLeast(0)

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            languages.forEachIndexed { index, (tag, labelRes) ->
                SegmentedButton(
                    selected = index == selectedIndex,
                    onClick = {
                        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(tag)
                        AppCompatDelegate.setApplicationLocales(appLocale)
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = languages.size,
                    ),
                ) {
                    Text(stringResource(labelRes))
                }
            }
        }
    }
}

/**
 * Displays the app version information in the settings sheet.
 *
 * @param modifier optional [Modifier] for the composable layout.
 */
@Composable
fun VersionInfo(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.version, BuildConfig.VERSION_NAME),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = modifier,
    )
}

/**
 * Loads the configured service inside an embedded [WebView].
 *
 * The IP is normalized into a URL and then loaded into the WebView.
 *
 * @param ip IP address or URL of the target service.
 * @param modifier optional [Modifier] for the composable layout.
 */
@Composable
fun WebViewScreen(ip: String, modifier: Modifier = Modifier) {
    val url = if (ip.startsWith("http")) ip else "http://$ip"
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                @Suppress("SetJavaScriptEnabled")
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                loadUrl(url)
            }
        },
        modifier = modifier.fillMaxSize(),
    )
}

/**
 * Displays an error message with an option to retry service verification.
 *
 * @param message descriptive error text to show.
 * @param onRetry callback invoked when the retry button is pressed.
 * @param modifier optional [Modifier] for the composable layout.
 */
@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val displayMessage = message.ifEmpty { stringResource(R.string.service_unavailable) }
        Text(
            displayMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

/**
 * Preview for the initial IP setup screen.
 *
 * Useful for verifying the portrait setup UI in Compose previews.
 */
@Preview(showBackground = true)
@Composable
private fun SetupScreenPreview() {
    ValloViewTheme {
        SetupScreen(onIpSave = {})
    }
}

/**
 * Preview for the error screen UI.
 *
 * Useful for validating error state styling in Compose previews.
 */
@Preview(showBackground = true)
@Composable
private fun ErrorScreenPreview() {
    ValloViewTheme {
        ErrorScreen(message = "", onRetry = {})
    }
}

/**
 * Preview for the settings sheet in portrait orientation.
 *
 * Renders the settings panel with IP and language controls.
 */
@Preview(showBackground = true)
@Composable
private fun SettingsSheetPortraitPreview() {
    ValloViewTheme {
        SettingsSheetContent(currentIp = "192.168.1.53", onSave = {})
    }
}

/**
 * Preview for the settings sheet in landscape orientation.
 *
 * Useful for verifying responsive layout behavior on wider screens.
 */
@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
private fun SettingsSheetLandscapePreview() {
    ValloViewTheme {
        SettingsSheetContent(currentIp = "192.168.1.53", onSave = {})
    }
}
