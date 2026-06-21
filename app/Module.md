# Module ValloView
ValloView is a compact Android module that provides a single-screen Compose-based interface for displaying a remote Vallox dashboard inside an embedded WebView. The module handles the target service configuration, verifies availability, and loads the view in the app once the destination address has been validated.

The architecture is intentionally lean: a single entry point (`MainActivity`) drives the UI, a lightweight persistence layer saves the target address, and a network utility verifies that the service is actually reachable.

# Package de.codingsolutions.valloview
The root package contains the core application logic and UI composition. It includes `MainActivity`, which serves as the entry point and is responsible for:

- Initializing the app theme environment and edge-to-edge display.
- Loading and observing the saved service address via `SettingsRepository`.
- Driving the UI state through the `AppState` state machine.
- Orchestrating connection verification, WebView display, and error handling.

The user interface is fully implemented in Compose and consists of several components defined in `MainActivity.kt`. These include:

- `MainScreen`: root container that switches between loading, setup, verification, WebView, and error states.
- `SetupScreen`: input screen for the target IP address with validation.
- `SettingsSheetContent`: bottom sheet for IP configuration, language selection, and version display.
- `WebViewScreen`: embedded browser that loads the remote service URL.
- `ErrorScreen`: error screen with retry logic.

The architecture relies on a reactive flow of persisted settings, so UI updates are automatically triggered when the target address changes.

# Package de.codingsolutions.valloview.data
This package contains data persistence for application settings.

## SettingsRepository
`SettingsRepository` encapsulates access to Android DataStore (Preferences). It provides:

- `ipAddressFlow`: a reactive `Flow<String?>` that emits the currently saved service IP or `null`.
- `saveIpAddress(ip: String)`: a suspending function that persists the new address.

This allows the UI to react to changes directly without polling. The repository is the single source of truth for persistent storage of the service configuration.

# Package de.codingsolutions.valloview.ui.theme
This package is responsible for the app's visual styling and contains the theme definitions for the Compose UI.

The UI architecture is organized into two layers:

- Root UI components in `de.codingsolutions.valloview` control the screen flow and interaction logic.
- The subpackage `de.codingsolutions.valloview.ui.theme` defines colors, typography, and theme management.

## Theme architecture
The theme components provide:

- `ValloViewTheme`: the central theme wrapper composable that chooses the correct color scheme based on the system theme.
- `DarkColorScheme` and `LightColorScheme`: fixed Vallox-style color palettes.
- `Typography`: Material 3 typography with a focused body text style.

The theme layer is designed so that it can optionally support dynamic colors on Android 12+, while preserving a consistent brand appearance by default.

# Package de.codingsolutions.valloview.util
This package contains helper functions for verifying the remote service.

## ServiceVerifier
`ServiceVerifier` is a static utility object that provides two main functions:

- `verifyService(ip: String): Boolean`: sends an HTTP request to the configured address, checks the HTTP status, and scans the HTML content for specific identifiers such as `IoGlobal.init()` and `dashboard-configuration-page`. The service is considered reachable only if both conditions are met.
- `isValidIp(ip: String): Boolean`: validates IPv4 addresses using a regular expression.

The object uses `OkHttpClient` with defined timeouts and performs the check on the IO dispatcher. It is the interface between the persisted destination address and the decision to show the `WebView`.

# System overview
ValloView is designed as a service wrapper app:

- Users enter a target IP or change it in the settings sheet.
- The app saves the address via `SettingsRepository`.
- `MainScreen` observes `ipAddressFlow` and triggers verification via `ServiceVerifier` when an address is present.
- If verification succeeds, the remote service is loaded in the `WebView`.
- On failure, the app shows an explanatory error message and offers a retry.

This architecture keeps business logic separate from UI code, uses modern Compose patterns, and leverages DataStore/Flow for reactive state management.