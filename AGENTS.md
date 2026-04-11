# Developer Guidelines & Constraints (AGENTS.md)

**Meta-Directive for AI Agents:** You are an autonomous developer agent assigned to work on the Battleships-EX Android game. You will be provided with this document, the codebase, and a single specific Issue/Ticket to resolve. **You must strictly adhere to the constraints and protocols in this document.** Do not refactor unrelated code, and do not violate the established architectural boundaries.

---

## 1. Agent Workflow Protocol
When you receive an issue, you must follow this exact execution flow:
1. **Analyze:** Read the issue thoroughly. Identify whether it requires UI updates (View), logic updates (Model/RulesEngine), or network syncing (Data Layer/Firebase).
2. **Discover:** Locate the existing components related to the task. **Do not reinvent the wheel.** If a UI button is needed, use `GameButton` and `ButtonConfig`. If a color is needed, use `Theme`.
3. **Plan:** Formulate your changes mentally or in a scratchpad before writing code. Ensure your plan respects the MVC boundaries.
4. **Execute:** Implement the fix minimally and cleanly. Only touch the files necessary to resolve the specific issue.
5. **Verify:** Ensure your changes do not break asynchronous listeners or memory management (e.g., ensure libGDX resources like Stages or Textures are disposed of if created dynamically).

## 2. Architectural Boundaries & Modifiability
Modifiability is the primary quality attribute of this project. Breaking the separation of concerns will result in immediate rejection of the code.

* **Strict MVC & Layered Structure:** You must enforce a strict separation between presentation (View), domain logic (Model), and data access.
* **One-Way Communication:** View components (`ScreenAdapter`, `Actor`) must only communicate with the Controller/State Manager. **Never** allow the View to mutate the Model directly.
* **Dedicated Rules Engine:** All validation logic (hit/miss checks, ship placement validation) and game mechanics must be completely isolated within the `RulesEngine` module. Do not bleed game rules into the UI or networking code.
* **Entity-Component-System (ECS) Principles:** The core domain should separate data components from logic to ensure features are easy to extend without tight coupling.

## 3. Design Patterns to Enforce
Do not use simple boolean flags or hardcoded `if/else` chains for complex, shifting features. You must use the established design patterns:

* **Strategy Pattern for Action Cards:** Action cards change frequently. Every Action Card must be a separate class conforming to a shared `ActionCard` interface. Do not embed card effects directly into the core game loop.
* **State Pattern for Game Phases:** Game phases (e.g., `LobbyState`, `PlacementState`, `MyTurnState`, `OpponentTurnState`) must use explicit states derived from `BaseGameState`. Do not use boolean flags like `isMyTurn` to track the match phase globally. The control flow must block invalid user actions by intercepting them in the current state.
* **Singleton Pattern:** Use the `GameStateManager` Singleton to centralize state transitions and ensure that all parts of the application interact with a single, consistent game state.
* **Observer/Listener Pattern:** Use interfaces like `GameStateListener`, `LobbyListener`, and `GameListener` to pass data from the backend/controllers back to the View.

## 4. Framework Constraints (libGDX) & UI Consistency
* **The Render Loop:** Respect the libGDX main loop. All rendering and input polling is driven entirely by the framework's `render(float delta)` method. Do not use standard Java `Thread.sleep()` or blocking calls on the main thread.
* **UI Updates & Scene2D:** Avoid full UI rebuilds. Only update the specific affected areas (`Label.setText()`, updating `Actor` properties) to keep frame times stable.
* **Standardized UI Components:** You must use the existing UI architecture:
    * `GameConfig` for world sizing.
    * `Theme` for fonts, colors, and panel backgrounds.
    * `GameButton` and `ButtonConfig` for interactable elements.
    * `ScreenUtils.clear()` for screen clearing.

## 5. Backend & Networking (Firebase)
* **Asynchronous Event-Driven Flow:** Firebase Realtime Database does not use synchronous request-response cycles. You must use an event-driven data synchronization model utilizing `DataCallback<T>`.
* **Thread Safety:** Firebase callbacks often return on a background thread. If updating the libGDX UI from a data callback, you **must** wrap the UI update in `Gdx.app.postRunnable(() -> { ... })`.
* **Client-Server Authority:** The server (Firebase rules/data structure) is authoritative. Ensure client-side validations (via `RulesEngine`) match expectations to avoid desyncs.
* **Payload Management:** Exchange compact event messages (e.g., sending only the shot coordinate data) instead of sending the entire board state with every move.
* **Listener Cleanup:** You **must** remove Firebase listeners when leaving a screen (e.g., calling `removeLobbyListener` in the `dispose()` or `hide()` methods of a Screen) to prevent memory leaks and zombie UI updates.

## 6. Performance & Resource Management
* **Lightweight Representations:** Model the game board and grids using lightweight representations like basic arrays or lists of `Coordinate` objects.
* **Incremental Updates:** Update values incrementally (e.g., tracking remaining ships and shots via counters) rather than constantly scanning the full 10x10 board after every single action.
* **Target SDK:** Ensure the application successfully targets Android devices with API level 29 or higher.

## 7. Error Handling & Edge Cases
* **Graceful Degradation:** If an async call fails (e.g., network disconnect), update the UI using the provided `onFailure(String error)` callbacks rather than throwing an unhandled Exception.
* **Defensive Programming:** Always null-check Firebase snapshots and GameStateManager properties before attempting to render them on the screen.
