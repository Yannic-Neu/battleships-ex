# Action Card System Design

## Overview
Action cards are special abilities that modify gameplay in Battleships EX. 
Unlike standard shots, they cost **energy** and have a **limited number of uses** per match.

## Architectural Principles

### 1. Strategy Pattern (Decoupling Effects)
Cards are decoupled from the core game engine through the `ActionCardEffect` interface.
- Cards describe **intent** (e.g., "Shoot this 2x2 area").
- The `RulesEngine` handles **implementation** (validating shots, sinking ships, triggering mines).
- This allows adding new cards without modifying the `RulesEngine` for every unique card logic.

### 2. Provider Pattern (Dependency Injection)
`ActionCardEffectProvider` acts as a central registry. At runtime, the `RulesEngine` registers itself as the effect handler. Cards access these effects via the provider, ensuring they don't need direct references to the engine.

### 3. MVC Separation
- **Model**: `ActionCard` implementations and the `RulesEngine`.
- **View**: `ActionCard` (UI component) and `BattleScreen`.
- **Controller**: `GameController` orchestrates the flow, handles targeting prompts, and synchronizes state.

## Core API: ActionCardEffect

All cards interact with the game through these primary methods:
- `shootTile(Player opponent, Coordinate coord)`: Single target attack.
- `shootArea(Player opponent, List<Coordinate> coords)`: Multiple target attack.
- `revealTileInfo(Player opponent, Coordinate coord)`: Information gathering (scans).
- `placeMineOnOwnBoard(Player user, Coordinate coord)`: Defensive placement.
- `triggerRandomShots(Player opponent, int count)`: Automatic counter-attacks.

## Metadata System
`ActionCardResult` includes a metadata map. This allows cards to pass unique data back to the UI (e.g., the number of adjacent ships found by a Sonar scan) without breaking the generic API.

## Card Selection & Deck Building
Matches are configured with exactly **4 cards**. 
1. The host selects cards during lobby creation.
2. Card names are synchronized via Firebase.
3. At game start, the `ActionCardRegistry` instantiates the selected cards for both players.

## The Big Four Cards

| Card | Energy | Uses | Effect |
| :--- | :--- | :--- | :--- |
| **Sonar** | 2 | 2 | Reveal the count of ships/mines in 8 adjacent tiles. |
| **Bomb** | 3 | 2 | Shoot a 2x2 area on the opponent's board. |
| **Mine** | 1 | 3 | Place a mine on your board. If hit, it fires 2 random shots at the attacker. |
| **Airstrike** | 4 | 1 | Clear an entire row or column. |
