# About
## About Hourglass
Hourglass provides a wireless turn management system among connected devices. The system consists of multiple peripheral devices operated locally by individual users and a single central device that processes data from the peripherals. The central device manages the system state and provides additional data required for display on the peripherals. The central device does not need to be actively operated and provides game configuration options.

Prototype Demo: https://drive.google.com/file/d/1vfFwuXrZOG86haQg10q6Bc9yUWXI989V/view?usp=drive_link

## About Mobile
The central device in the Hourglass system is an Android application (Hourglass Companion) responsible for turn sequencing and state management of peripheral devices. The app supports multiple turn sequencing modes and establishes connections with peripherals before the game starts. Once all devices are connected, the user can configure and initiate the activity. During gameplay, the central device actively communicates with all peripherals and provides controls for managing the game state, including pausing and advancing turns. Game configurations can also be adjusted during an active session.

Turn Sequencing Modes:

- **Sequential**: Turns progress in a fixed sequence, with one user/device active at a time. Completion of a turn automatically advances to the next user.
- **Simultaneous**: All users are active simultaneously. The turn ends when all users have individually completed their turns.
- **Solo**: The device acts as a standalone hourglass. No sequencing occurs between devices, and each operates independently.
- **Buzzer**: Similar to the simultaneous mode, but turn completion is reported back to the central device in real-time and prioritized in order of completion. This mode is recommended for trivia or any activity that requires time-based comparisons between users' actions.

## About Android Development
The Hourglass Companion app is developed using **Jetpack Compose** for the UI

## Built With
- **Jetpack Compose** (UI development)
- **Hilt** (Dependency injection)
- **StateFlow** (Reactive state management)
- **ViewModel** (Lifecycle-aware data handling)
- **BLE API** (Device communication)

## Getting Started
To set up the development environment for the Hourglass Companion app, follow these steps:

### Prerequisites
- Install **Android Studio** (latest stable version)
- Ensure your development environment has the **Android SDK**
- Enable **developer mode** and **Bluetooth permissions** on a test Android device

### Installation
1. Clone the repository:
   ```sh
   git clone <repo_url>
   ```
2. Open the project in **Android Studio**.
3. Sync dependencies by running a Gradle sync.
4. Configure the Bluetooth permissions in `AndroidManifest.xml` if needed.
5. Build and run the app on a physical Android device (BLE communication is not available on most emulators).

## Usage
Once installed, the Hourglass Companion app will scan for and connect to available peripheral devices via BLE. Users can configure game settings and manage turns directly from the app interface. The app will continuously synchronize with peripherals to update turn sequencing and state management.

## Roadmap
- Support for additional turn sequencing modes
- Enhanced UI animations
- Customizable themes and settings

## Contributing
Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a feature branch.
3. Commit changes.
4. Open a pull request.

For major changes, open an issue first to discuss your proposed updates.

