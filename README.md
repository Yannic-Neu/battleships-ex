# Battleships-ex

Battleships-EX is a turn-based multiplayer Battleship game for Android developed as part of the course TDT4240 – Software Architecture. The game extends the classic Battleship rules with Action Cards and online multiplayer support.


## Project Structure

The project follows a Model–View–Controller (MVC) architecture with explicit state management.

- `model/` – Core game logic and data (players, board, rules, action cards)
- `controller/` – Game and lobby controllers coordinating gameplay
- `state/` – Game states controlling valid actions per phase
- `view/` – libGDX screens and UI rendering
- `data/` – Backend abstraction and Firebase data sources
- `ui/` – UI components and card presentations

## Compilation and Execution

### Requirements
- Android Studio
- Android SDK
- Internet connection (for multiplayer)

### Run on Android
1. Open the project in Android Studio
2. Ensure the Android SDK is installed
3. Add `google-services.json` (given in the zip file) to the Android module
4. Sync Gradle
5. Run the app on an emulator or physical device

For multiplayer testing, run the app on two devices or emulators.

## Dependencies
- libGDX
- Firebase Realtime Database
- Android SDK

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.
- `android`: Android mobile platform. Needs Android SDK.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `android:lint`: performs Android project validation.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
