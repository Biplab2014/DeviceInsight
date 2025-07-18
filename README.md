# DeviceInsight 📱

A comprehensive Android app that provides detailed information about your device's hardware, software, and system specifications.

## 🚀 Features

- **Device Overview**: Manufacturer, model, brand, board, bootloader details
- **OS & Software Info**: Android version, API level, security patch, build information
- **CPU & Hardware**: Architecture, cores, frequency, supported ABIs
- **Memory & Storage**: RAM usage, internal/external storage details
- **Battery Info**: Level, status, health, temperature, charging information
- **Display Info**: Resolution, density, refresh rate, screen size
- **Network Info**: WiFi status, IP address, MAC address, signal strength
- **Sensor Info**: Complete list of available sensors with specifications
- **Camera Info**: Camera details, megapixels, supported resolutions
- **System Info**: Uptime, boot time, timezone, locale information

## 🏗️ Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture with clean architecture principles:

- **Data Layer**: Device information collectors and repositories
- **Domain Layer**: Use cases and UI state models
- **UI Layer**: Jetpack Compose screens and ViewModels

### Key Components

- `DeviceInfoCollector`: Collects device information from Android APIs
- `DeviceInfoRepository`: Manages data operations
- `GetDeviceInfoUseCase`: Business logic for fetching device info
- `DeviceInfoViewModel`: Manages UI state and user interactions
- `MainScreen`: Jetpack Compose UI with expandable cards

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM with StateFlow
- **Concurrency**: Kotlin Coroutines
- **JSON**: Gson for data serialization
- **Testing**: JUnit for unit tests

## 📱 UI Features

- **Material 3 Design**: Modern, clean interface
- **Expandable Cards**: Organized information in collapsible sections
- **Device Summary Header**: Quick overview of device and OS
- **Share Functionality**: Export device information as text
- **Refresh Button**: Update information on demand
- **Loading States**: Smooth loading experience
- **Error Handling**: Graceful error states with retry options

## 🔧 Permissions

The app requires minimal permissions:
- `ACCESS_NETWORK_STATE`: For network information
- `ACCESS_WIFI_STATE`: For WiFi details
- `INTERNET`: For potential future features

## 📦 Installation

1. Clone the repository
2. Open in Android Studio
3. Build and run on Android 7.0+ (API 24+)

```bash
git clone <repository-url>
cd DeviceInsight
./gradlew assembleDebug
./gradlew installDebug
```

## 🧪 Testing

Run unit tests:
```bash
./gradlew testDebugUnitTest
```

## 📄 License

This project is licensed under the MIT License.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📞 Support

For support or questions, please open an issue in the repository.

---

**DeviceInsight** - Know your device inside out! 🔍📱
