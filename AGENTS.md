# Developer Guidelines & Constraints (agents.md)

This document outlines the critical architectural rules, constraints, and patterns that must be strictly followed when implementing the Battleships-EX Android game.

## 1. Architectural Boundaries & Modifiability
Modifiability is the primary quality attribute of this project. Breaking the separation of concerns will violate the core architectural requirements.

* **Strict MVC & Layered Structure:** You must enforce a strict separation between presentation (View), domain logic (Model), and data access.
* **One-Way Communication:** View components must only communicate with the Controller. **Never** allow the View to mutate the Model directly.
* **Dedicated Rules Engine:** All validation logic (hit/miss checks, ship placement validation) and game mechanics must be completely isolated within a single Rules Engine module. Do not bleed game rules into the UI or networking code.
* **Entity-Component-System (ECS):** The core domain should apply ECS principles to separate data (Components) from logic (Systems) to ensure features are easy to extend without tight coupling.

## 2. Design Patterns to Enforce
Do not use simple boolean flags or hardcoded logic for complex, shifting features. You must implement specific design patterns:

* **Strategy Pattern for Action Cards:** Action cards are expected to change frequently. Every Action Card must be implemented as a separate class conforming to a shared interface (e.g., `ActionCard`). Do not embed card effects directly into the core game loop.
* **State Pattern for Game Phases:** Game phases (e.g., `LobbyState`, `MyTurnState`, `OpponentTurnState`) must use explicit states. Do not use boolean flags to track the match phase. The control flow must block invalid user actions depending on the current state (e.g., blocking inputs during the opponent's turn).
* **Singleton Pattern:** Use a Singleton `GameStateManager` to centralize responsibility and ensure that all parts of the application interact with a single, consistent game state.

## 3. Framework Constraints (libGDX)
* **The Render Loop:** The libGDX framework imposes the constraint of the `ApplicationListener` interface. You must respect the libGDX main loop; all rendering and input polling is driven entirely by the framework's `render()` method.
* **UI Updates:** To maintain performance, avoid full UI rebuilds. Only update the specific affected areas and UI elements (e.g., single tiles) to keep frame times stable.

## 4. Backend & Networking (Firebase)
* **Asynchronous Event-Driven Flow:** Firebase Realtime Database does not use traditional synchronous request-response cycles. You must use an event-driven data synchronization model. Be careful to implement listeners and callbacks properly (e.g., `ValueEventListener`) to react to state changes asynchronously.
* **Client-Server Authority:** The server (Firebase) is authoritative. All actions and card executions must be validated on the server to ensure consistency and prevent client manipulation. Do not trust local client calculations for final outcomes.
* **Payload Management:** Exchange compact event messages (e.g., sending only the shot coordinate data) instead of sending the entire board state with every move. Only transmit full state snapshots when absolutely necessary, such as during a reconnect.
* **Resource Limits:** Synchronization and storage are strictly limited by Firebase's pricing quota. Monitor your WebSocket payloads and bound queue sizes for incoming/outgoing messages per client to prevent memory and quota exhaustion.

## 5. Performance & Resource Management
* **Lightweight Representations:** Model the game board and grids using lightweight representations like basic arrays.
* **Incremental Updates:** Update values incrementally (e.g., remaining ships and shots) rather than constantly scanning the full 10x10 board after every single action.
* **Target SDK:** Ensure the application successfully targets Android devices with API level 29 or higher.
