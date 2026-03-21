# StackOverflowApp

A Stack Overflow-inspired Android app built with Kotlin and Jetpack Compose, showcasing modern Android development practices, custom infrastructure without third-party libraries, and a clean, testable architecture.

## Project Overview

This application fetches and displays Stack Overflow user data, caches it locally, and allows users to persist followed favourites independently of remote refreshes. It is structured around clear architectural boundaries and modern Android UI patterns.

The project is organized into the following layers:

- **`data`**: Remote API access, local persistence, repositories, network client, parser, and image loading.
- **`domain`**: Core models and validation logic.
- **`ui`**: Jetpack Compose screens, reusable UI components, navigation, transitions, and `ViewModel` state handling.
- **`di`**: Manual dependency injection through a lightweight `AppContainer`.

## Visual Showcase

|                        User Grid (Polaroid Style)                        |                             Search & Filtering                              |                          Followed Users                           |
|:------------------------------------------------------------------------:|:---------------------------------------------------------------------------:|:-----------------------------------------------------------------:|
| ![Main Screen](https://i.ibb.co/dwJXG5wG/Screenshot-20260227-120540.png) |   ![Search UI](https://i.ibb.co/S4K9wBWc/Screenshot-20260227-121124.png)    | ![Followed Filter](https://i.ibb.co/k6xGvtR8/favourite-cache.gif) |
|             *Stylized polaroid cards with randomized tilts.*             |                 *Reactive real-time filtering and sorting.*                 |           *Independent persistence for favourite users.*          |

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/pack/compose) with [Material 3](https://m3.material.io/)
- **Concurrency**: [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) and [Flow](https://kotlinlang.org/docs/flow.html)
- **Navigation**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- **Networking**: Custom implementation using `HttpURLConnection` behind a `HttpClient` abstraction
- **JSON Parsing**: Custom parser using `JSONObject` behind a `UsersResponseParser` abstraction
- **Local Storage**:
    - `SharedPreferences` for persisted followed user IDs
    - Custom SQLite-backed `UserDatabase` for cached user data
- **Architecture**: Clean Architecture, Repository Pattern, MVVM, Unidirectional Data Flow, and manual Dependency Injection via `AppContainer`
- **Testing**:
    - **Unit Tests**: JUnit 4, `kotlinx-coroutines-test`
    - **UI / Instrumentation Tests**: Compose UI Test

## Key Features

- **Top Users Grid**: Fetches and displays Stack Overflow users in a stylized two-column polaroid grid.
- **User Details Page**: Tapping a user opens a dedicated details screen with richer profile information including badges, reputation, location, website, and bio.
- **Shared Element Transitions**: Smooth shared transitions animate the selected user card and image from the grid into the details screen.
- **Search, Sort, and Favourites Filtering**: Supports real-time user filtering and sorting.
    - **Search**: Filter by display name.
    - **Sort**: Choose between **Name**, **Popularity**, **Creation**, or **Modified** date.
    - **Direction**: Toggle between **Ascending** and **Descending** order.
    - **Favourites**: Filter the list to show only followed users.
- **Pagination**: Fetches additional users from the Stack Overflow API as the user scrolls, with a loading indicator and end-of-list detection.
- **Persistent Following System**: Followed users are stored locally and remain persisted independently of API refreshes.
- **Manual Image Loading & Memory Caching**: Profile images are fetched, decoded, cached, and displayed without third-party image libraries.
- **Offline-Friendly Cache Behaviour**:
    - Top users are cached locally
    - User details are enriched from the API when needed
    - Local data can be used as a fallback in failure scenarios

## Architectural Decisions

### Manual Dependency Injection (`AppContainer`)
The app uses a centralized `AppContainer` to manage dependencies manually, in line with the project constraint of avoiding external DI libraries.

- **Why**: A dedicated composition root keeps object creation explicit and allows constructor injection throughout the app.
- **Pros**: No library overhead, transparent dependency graph, easy substitution in tests, and simple singleton-style lifecycle management with `lazy`.
- **Cons**: More boilerplate as the app grows.
- **Alternative**: A DI framework such as Hilt would reduce wiring but was intentionally avoided for this project.

### Clean Architecture Layers
The codebase is divided into `data`, `domain`, and `ui` layers.

- **Why**: To separate concerns clearly and prevent business/data logic from leaking into UI code.
- **Pros**: Easier testing, easier refactoring, clearer ownership of responsibilities.
- **Cons**: Requires explicit model mapping between layers.

### Unidirectional Data Flow (UDF) & MVVM
The UI layer uses `ViewModel` classes to expose screen state and receive user-driven events.

- **Why**: This makes UI behaviour predictable and easier to debug.
- **Pros**: Clear state ownership, Compose-friendly rendering model, and easier testing of state transitions.
- **Cons**: Requires explicit state modelling for each screen.

### Lightweight Manual Infrastructure
Networking, JSON parsing, persistence, and image loading are all implemented manually behind abstractions instead of relying on libraries like Retrofit, Moshi, Room, Coil, or Glide.

- **Why**: To demonstrate understanding of the underlying mechanics and maintain full control over implementation details.
- **Pros**: Excellent learning value, highly explicit behaviour, minimal external dependency surface.
- **Cons**: More code to maintain and fewer built-in optimizations than mature libraries provide.

## Screen Overview

### Home Screen
The home screen is responsible for:

- Loading top Stack Overflow users
- Displaying them in a stylized polaroid grid
- Searching by display name
- Sorting by **Name**, **Popularity**, **Creation**, or **Modified** date with **Ascending/Descending** support
- Filtering to followed users (favorites) only
- Navigating to the details page

### Details Screen
The details screen is responsible for:

- Loading a specific user by ID
- Showing richer user profile information
- Displaying badge counts, reputation, location, website, and bio
- Supporting follow/unfollow actions from the details view
- Reusing the shared transition elements from the grid for a more polished navigation experience

## Data Flow Summary

1. The UI sends events to a `ViewModel`
2. The `ViewModel` requests data from a repository
3. The repository coordinates:
    - remote API access
    - local database reads/writes
    - followed-user persistence
4. Results are mapped into domain models
5. The `ViewModel` emits updated UI state
6. Compose renders the new state

## Testing Strategy

- **Unit Testing**: Focused on business logic, repository behaviour, parser correctness, caching logic, and `ViewModel` state transitions.
    - `UserRepositoryImpl` is tested with fake API and database implementations
    - `HomeViewModel` and `UserDetailsViewModel` are tested for loading, success, error, follow, refresh, search, and filtering behaviour
    - The custom network client, parser, image loader, and persistence layers are tested independently
- **UI Testing**:
    - Home screen rendering, filtering, and follow interactions are verified with Compose UI tests
    - Details screen content and interactions are tested in instrumentation
    - Navigation and shared transition support are verified with dedicated Compose/navigation tests
- **Testing Approach**:
    - Fakes are preferred over mocking frameworks
    - `semantics` and `testTag` are used to keep UI tests stable and intentional

## Challenges & Solutions

### Manual JSON Parsing
Without Moshi or Gson, a custom `JsonUsersResponseParser` was implemented using `JSONObject`.

- **Challenge**: Safely parsing partially missing or malformed fields
- **Solution**: Defensive parsing helpers were introduced to tolerate absent optional values while still protecting required fields

### Manual Image Loading
Without Coil or Glide, image loading and caching had to be built from scratch.

- **Challenge**: Avoiding repeated network requests and unnecessary bitmap decoding
- **Solution**: A custom `ImageLoader` was introduced to fetch, decode, and cache bitmaps in memory for reuse across compositions

### Shared Element Navigation
The details experience required coordinated transitions between screens in Compose.

- **Challenge**: Passing and scoping shared transition context cleanly
- **Solution**: Shared transition scopes are provided via composition locals, allowing transitions to remain expressive without excessive prop drilling

### Local + Remote Data Coordination
The app supports both cached user lists and richer detail fetches.

- **Challenge**: Preserving useful local data while still enriching the experience with remote details
- **Solution**: The repository uses local cache-first behaviour for lists and selectively fetches fuller user details when needed

## How to Run Locally

1. **Prerequisites**: Install [Android Studio](https://developer.android.com/studio)
2. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/StackOverflowApp.git
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
- [ ] **Multi-List Support**: Allow users to switch between different types of user lists (e.g., "Top Reputed", "Recently Active", "New Users").
- [ ] **Advanced Filtering**: Add more granular filtering options (e.g., filter by location, tags, or specific reputation ranges).
- [ ] **Offline Mode**: Enhance the local storage strategy to support a full offline-first experience.
- [ ] **Persistence Upgrade**: Evaluate and potentially migrate to [Room](https://developer.android.com/training/data-storage/room) or [DataStore](https://developer.android.com/topic/libraries/architecture/datastore).
- [ ] **Library Integration**: Consider integrating [Retrofit](https://square.github.io/retrofit/) for networking and [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) for DI.
- [ ] **Error Handling**: Implement a global error handling and reporting mechanism.
- [ ] **Observability**: Integrate logging, analytics, and crash reporting (e.g., Timber, Firebase Crashlytics) to monitor app health.
