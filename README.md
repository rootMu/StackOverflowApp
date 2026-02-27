# StackOverflowApp

A Stack Overflow-inspired Android app built with Kotlin and Jetpack Compose, showcasing modern Android development practices and a clean architecture.

## Project Overview

This application fetches and displays data from the Stack Overflow API, focusing on a robust and maintainable project structure. It follows a clean architecture pattern, organized into `data`, `domain`, and `ui` layers:

- **`data`**: Manages data sources (remote API and local storage), repositories, network clients, and parsers.
- **`domain`**: Encapsulates core business logic and use cases.
- **`ui`**: Implements the declarative UI using Jetpack Compose, following the **Unidirectional Data Flow (UDF)** pattern with **MVVM** principles.
- **`di`**: Handles dependency injection through a manual `AppContainer`.

## Visual Showcase

|                        User Grid (Polaroid Style)                        |                             Search & Filtering                              |                          Followed Users                           |
|:------------------------------------------------------------------------:|:---------------------------------------------------------------------------:|:-----------------------------------------------------------------:|
| ![Main Screen](https://i.ibb.co/dwJXG5wG/Screenshot-20260227-120540.png) |   ![Search UI](https://i.ibb.co/S4K9wBWc/Screenshot-20260227-121124.png)    | ![Followed Filter](https://i.ibb.co/k6xGvtR8/favourite-cache.gif) |
|             *Stylized polaroid cards with randomized tilts.*             |                 *Reactive real-time filtering and sorting.*                 |           *Independent persistence for favorite users.*           |

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/pack/compose) with [Material 3](https://m3.material.io/)
- **Concurrency**: [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Networking**: Custom implementation using `HttpURLConnection` (via `HttpClient` abstraction)
- **JSON Parsing**: Custom parsing (via `UsersResponseParser` abstraction)
- **Local Storage**: `SharedPreferences` (via `UserStore`) and a custom `UserDatabase`
- **Architecture**: Clean Architecture with Repository Pattern, **Unidirectional Data Flow (UDF)**, and manual Dependency Injection (`AppContainer`).
- **Testing**:
    - **Unit Tests**: JUnit 4, `kotlinx-coroutines-test`
    - **UI Tests**: Espresso, Compose UI Test

## Key Features

- **Reactive User Search & Filtering**: Real-time results powered by `snapshotFlow` and `combine` to merge search queries, sort orders, and "followed" status into a single reactive stream.
- **Persistent Following System**: A "Follow" feature that persists locally and survives API refreshes, implemented by merging separate local and remote data sources.
- **Manual Image Loading & Caching**: A custom-built `ImageLoader` that handles asynchronous fetching, bitmap decoding, and memory caching without relying on libraries like Coil or Glide.
- **Stylized Polaroid UI**: A custom-designed grid view using `graphicsLayer` to provide unique, randomized tilts and high-fidelity Material 3 components.

## Architectural Decisions

### Manual Dependency Injection (`AppContainer`)
The app uses a centralized `AppContainer` to manage dependencies manually, adhering to the project constraint of avoiding third-party libraries.
- **Why**: Using a dedicated container (the "Composition Root" pattern) allows for **Constructor Injection** throughout the app. This ensures that classes never create their own dependencies, making them easier to test and keeping the overall dependency graph visible and explicit in one place.
- **Pros**: No library overhead, explicit control over object lifecycles (e.g., using `lazy` for singletons), and high testability through simple constructor-based overrides.
- **Cons**: Requires manual boilerplate for wiring and "passing through" dependencies as the app grows.
- **Alternative**: **Direct Instantiation** would have been simpler initially but results in tight coupling and makes unit testing extremely difficult.

### Clean Architecture Layers
The project is strictly divided into `data`, `domain`, and `ui` packages.
- **Why**: To ensure a clear separation of concerns where business logic is independent of UI and data sourcing details.
- **Pros**: Highly testable (business logic can be tested in isolation), easier to swap out data sources, and clear boundaries for developers.
- **Cons**: Requires mapping between data models and domain models.

### Unidirectional Data Flow (UDF) & MVVM
The UI layer uses `ViewModel` to expose state as `StateFlow` and receives events from the Compose UI.
- **Why**: UDF ensures that the UI is a pure reflection of the state, making debugging and state reproduction much simpler.
- **Pros**: Predictable state transitions and excellent support for Compose's declarative nature.
- **Cons**: Requires defining explicit `UiState` objects for every screen.

## Testing Strategy

- **Unit Testing**: Focused on business logic and data integrity.
    - `UserRepository` is tested using Fakes for the network and storage layers.
    - `HomeViewModel` is tested using `StandardTestDispatcher` to verify state emissions in response to user actions (search, follow, refresh).
- **UI Testing**: 
    - Verified the "Polaroid" grid rendering and "Follow" toggle interactions using the Compose Testing library.
    - Used `testTag` and `semantics` to ensure tests remain robust even if the UI styling changes.

## Challenges & Solutions

- **Manual JSON Parsing**: Without a library like Moshi or Gson, I implemented a robust `JsonUsersResponseParser` using `JSONObject`. I handled potential missing fields and data type mismatches manually to ensure app stability.
- **Image Performance**: To prevent the grid from lagging during scroll, I implemented a simple memory cache in the `ImageLoader` and used `LaunchedEffect` in Compose to ensure images are only fetched when a card enters the composition.

## How to Run Locally

1. **Prerequisites**: Ensure you have [Android Studio](https://developer.android.com/studio) installed.
2. **Clone the Repository**:
   ```bash
   git clone https://github.com/rootMu/StackOverflowApp.git
   ```
3. **Open Project**: Open the project folder in Android Studio.
4. **Sync Gradle**: Wait for Android Studio to finish syncing the Gradle files.
5. **Run**: Select the `app` configuration and click the **Run** button (or press `Shift + F10`) to deploy to an emulator or physical device.

## Current Status and Roadmap

### Current Status
- **Foundation**: Established clean architecture with `data`, `domain`, and `ui` layers.
- **DI**: Manual dependency injection implemented via `AppContainer`.
- **Networking/Data**: Custom network client and parser abstractions are in place.
- **Persistence**: Basic `UserDatabase` for caching API data, and a separate `UserStore` (SharedPrefs) to persist user "follows" (favourites) independently.
- **UI**: Core features including User Search, Filtering, and User Favouriting are fully functional.

### Roadmap
- [ ] **User Details Feature**: Add a detailed view for individual users.
- [ ] **Pagination**: Implement a paging mechanism to fetch more users from the Stack Overflow API as the user scrolls.
- [ ] **Multi-List Support**: Allow users to switch between different types of user lists (e.g., "Top Reputed", "Recently Active", "New Users").
- [ ] **Advanced Filtering**: Add more granular filtering options (e.g., filter by location, tags, or specific reputation ranges).
- [ ] **Offline Mode**: Enhance the local storage strategy to support a full offline-first experience.
- [ ] **Persistence Upgrade**: Evaluate and potentially migrate to [Room](https://developer.android.com/training/data-storage/room) or [DataStore](https://developer.android.com/topic/libraries/architecture/datastore).
- [ ] **Library Integration**: Consider integrating [Retrofit](https://square.github.io/retrofit/) for networking and [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) for DI.
- [ ] **Error Handling**: Implement a global error handling and reporting mechanism.
- [ ] **Observability**: Integrate logging, analytics, and crash reporting (e.g., Timber, Firebase Crashlytics) to monitor app health.
