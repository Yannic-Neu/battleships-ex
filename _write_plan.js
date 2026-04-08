const fs = require('fs');
const path = require('path');
const dest = path.join(process.env.USERPROFILE, '.claude', 'plans', 'luminous-zooming-dusk-agent-ae964936d415c333f.md');

const content = `# Implementation Plan: 12 GitHub Issues for battleships-ex

## Repository: Yannic-Neu/battleships-ex
## Assignee: NWichter
## Base: main (pull fresh before each branch)

---

## Issue Overlap Analysis

1. **#21 and #30 overlap**: Both request GameStateManager (Singleton) + State Pattern. Issue #30 is the focused extraction of just the GameStateManager/State Pattern. Issue #21 bundles that with the Rules Engine dedication and controller restructuring. **Resolution**: Implement #30 first as the pure GameStateManager + State Pattern. Then #21 adds the Rules Engine dedication + controller wiring on top.

2. **#22, #25, and #26 overlap**: Issue #22 is the umbrella Action Card System. Issue #25 is the card interface + 5 implementations subset. Issue #26 is the card dealing + energy system + turn integration subset. **Resolution**: Implement #25 first (cards), then #26 (dealing/energy), and #22 becomes the integration/validation pass with server-side validation.

3. **#27, #28, and #29 overlap**: Issue #27 is the umbrella Real-time Features and Session Management. Issue #28 is real-time target preview. Issue #29 is session re-join + auto-cleanup. **Resolution**: Implement #28 and #29 as independent sub-features, then #27 becomes the integration branch.

---

## Dependency Graph (Implementation Order)

${'```'}
Phase 1 (Foundation):
  #30  GameStateManager + State Pattern
    |
Phase 2 (Core Game Logic):
  #21  Rules Engine + GameController/LobbyController wiring (depends on #30)
  #18  Firebase Game State Sync (depends on #30 for state awareness)
    |
Phase 3 (Gameplay):
  #19  Ship Placement Logic (depends on #21, #18)
    |
Phase 4 (Card System):
  #25  ActionCard implementations (depends on #21)
  #26  Card dealing + energy system (depends on #25)
  #22  Card system integration + server validation (depends on #25, #26, #18)
    |
Phase 5 (Real-time and Polish):
  #28  Real-time target preview (depends on #18)
  #29  Session re-join + auto-cleanup (depends on #18)
  #27  Real-time features integration (depends on #28, #29)
    |
Phase 6 (Independent Features):
  #33  Single Player AI (depends on #21, #19)
  #37  Lobby Settings (depends on #30)
${'```'}

---

## Branch 1: Issue #30 -- GameStateManager (Singleton) + State Pattern

**Branch name**: ${'`'}30-gamestatemanager-singleton-state-pattern${'`'}
**Priority**: HIGH (foundational -- everything depends on this)
**Note**: Remote branch exists with prior work. Evaluate if usable; otherwise implement fresh from main.

### What to Build

The project currently has NO GameStateManager and NO State Pattern (AGENTS.md section 2 mandates both). Game phase is tracked by which Screen is visible, and there are no guards against invalid actions per phase.

**New files to create:**
- ${'`'}core/src/main/java/battleships_ex/gdx/state/GameState.java${'`'} -- interface with enter(), exit(), and capability query methods
- ${'`'}core/src/main/java/battleships_ex/gdx/state/LobbyState.java${'`'} -- blocks game actions, allows lobby operations
- ${'`'}core/src/main/java/battleships_ex/gdx/state/PlacementState.java${'`'} -- allows ship placement, blocks firing
- ${'`'}core/src/main/java/battleships_ex/gdx/state/MyTurnState.java${'`'} -- allows firing and card play
- ${'`'}core/src/main/java/battleships_ex/gdx/state/OpponentTurnState.java${'`'} -- blocks all input, waits for remote event
- ${'`'}core/src/main/java/battleships_ex/gdx/state/GameOverState.java${'`'} -- blocks all game actions, allows return to menu
- ${'`'}core/src/main/java/battleships_ex/gdx/state/GameStateManager.java${'`'} -- Singleton, holds current GameState, delegates transitions
- ${'`'}core/src/test/java/battleships_ex/gdx/state/GameStateManagerTest.java${'`'} -- unit tests

**Existing files to modify:**
- ${'`'}core/src/main/java/battleships_ex/gdx/MyGame.java${'`'} -- initialize GameStateManager singleton
- ${'`'}core/src/main/java/battleships_ex/gdx/controller/GameController.java${'`'} -- consult GameStateManager for turn guards
- ${'`'}core/src/main/java/battleships_ex/gdx/controller/LobbyController.java${'`'} -- transition to LobbyState on lobby creation/join

### Design Details

GameState interface provides: enter(GameStateManager), exit(GameStateManager), canFire(), canPlaceShip(), canPlayCard(), canLeaveLobby(), getStateName().

GameStateManager is a Singleton with: getInstance(), transitionTo(GameState), getCurrentState(), addListener(GameStateListener), reset() (for testing).

GameStateListener interface provides: onStateChanged(GameState oldState, GameState newState).

### Test Strategy
- Unit test each state capability query (canFire, canPlaceShip, canPlayCard)
- Test transitions: LobbyState -> PlacementState -> MyTurnState <-> OpponentTurnState -> GameOverState
- Test Singleton behavior (same instance, reset for tests)
- No libGDX dependency in state classes -- pure Java, testable without mocking

---

## Branch 2: Issue #21 -- Rules Engine Dedication + Controller Wiring

**Branch name**: ${'`'}21-rules-engine-game-controller-wiring${'`'}
**Priority**: HIGH
**Depends on**: #30

### What to Build

StandardRulesEngine is 100% complete. The gap is:
1. GameController does not trigger GameStateManager transitions on game events
2. LobbyController does not trigger state transitions
3. The FirebaseClient inner interface in GameController needs a stub for desktop

**Existing files to modify:**
- ${'`'}GameController.java${'`'} -- inject GameStateManager; on startGame() transition to MyTurnState/OpponentTurnState; on fireShot() miss transition to OpponentTurnState; on onRemoteShotReceived() miss transition to MyTurnState; on game over transition to GameOverState; guard fireShot/playActionCard with currentState.canFire()/canPlayCard()
- ${'`'}LobbyController.java${'`'} -- on createLobby() ensure LobbyState; on confirmReady() transition to PlacementState
- ${'`'}GameListener.java${'`'} -- add onTurnChanged(boolean isLocalTurn)
- ${'`'}LobbyListener.java${'`'} -- add onGamePhaseChanged(String phase) if needed

**New files to create:**
- ${'`'}core/src/main/java/battleships_ex/gdx/controller/StubFirebaseClient.java${'`'} -- no-op FirebaseClient
- ${'`'}core/src/test/java/battleships_ex/gdx/controller/GameControllerTest.java${'`'}

### Test Strategy
- Test GameController transitions state correctly on fireShot (miss -> OpponentTurnState, hit -> stays MyTurnState)
- Test that firing during OpponentTurnState is rejected
- Test LobbyController transitions to LobbyState, then PlacementState on ready

---

## Branch 3: Issue #18 -- Firebase Game State Sync

**Branch name**: ${'`'}18-firebase-game-state-sync${'`'}
**Priority**: HIGH
**Depends on**: #30

### What to Build

Currently only lobby data syncs via Firebase. Game moves, board states, and turns have NO Firebase sync.

**New files to create:**
- ${'`'}core/src/main/java/battleships_ex/gdx/data/GameDataSource.java${'`'} -- interface with pushMove(), pushBoardState(), pushTurnChange(), pushGameOver(), pushActionCardEvent(), addGameListener(), removeGameListener()
- ${'`'}core/src/main/java/battleships_ex/gdx/data/GameSnapshot.java${'`'} -- POJO for game state snapshots
- ${'`'}core/src/main/java/battleships_ex/gdx/data/BoardSnapshot.java${'`'} -- serializable board representation
- ${'`'}core/src/main/java/battleships_ex/gdx/data/StubGameDataSource.java${'`'} -- no-op for desktop
- ${'`'}android/src/main/java/battleships_ex/gdx/android/data/FirebaseGameDataSource.java${'`'} -- real Firebase implementation

**Existing files to modify:**
- ${'`'}GameController.java${'`'} -- replace FirebaseClient with GameDataSource; implement listener for remote moves
- ${'`'}MyGame.java${'`'} -- accept and store GameDataSource
- ${'`'}AndroidLauncher.java${'`'} -- instantiate FirebaseGameDataSource
- ${'`'}database.rules.json${'`'} -- add game state rules under rooms/$roomCode/game/

**Performance**: Turn submission <=400ms via Firebase push() for moves (append-only) and updateChildren() for atomic turn+move writes. Compact payloads: only {row, col, hit, playerId} per move.

### Test Strategy
- Unit test StubGameDataSource, GameSnapshot/BoardSnapshot serialization
- Manual integration test on two Android devices for <400ms sync
- Test GameController processes remote moves correctly via listener

---

## Branch 4: Issue #19 -- Ship Placement Logic

**Branch name**: ${'`'}19-ship-placement-logic${'`'}
**Priority**: MEDIUM
**Depends on**: #21, #18

### What to Build

PlacementScreen has NO drag-and-drop, NO rotation, randomize is println stub. BoardActor has NO cell interaction.

**Existing files to modify (MAJOR):**
- ${'`'}BoardActor.java${'`'} -- add cell click detection (override hit()), ship rendering, hit/miss markers, drag target highlighting, BoardActorListener, Board model reference
- ${'`'}PlacementScreen.java${'`'} -- draggable ShipCards via libGDX DragAndDrop, call GameController.placeShip() on drop, tap-to-rotate, implement GameListener callbacks, implement randomize, READY button syncs to Firebase
- ${'`'}ShipCard.java${'`'} -- DragAndDrop.Source pattern, track placed state
- ${'`'}BoardConfig.java${'`'} -- add ship/highlight colors

**New files to create:**
- ${'`'}core/src/main/java/battleships_ex/gdx/ui/BoardActorListener.java${'`'} -- callback interface
- ${'`'}core/src/main/java/battleships_ex/gdx/util/RandomPlacementUtil.java${'`'} -- valid random placement generator
- ${'`'}core/src/test/java/battleships_ex/gdx/util/RandomPlacementUtilTest.java${'`'}

### Test Strategy
- Unit test RandomPlacementUtil: run 100 times, all placements valid, no overlaps
- Unit test coordinate conversion in BoardActor
- Manual test: drag, collision rejection, rotation, visual feedback

---

## Branch 5: Issue #25 -- ActionCard Implementations

**Branch name**: ${'`'}25-actioncard-implementations${'`'}
**Priority**: LOW
**Depends on**: #21

### What to Build

ActionCard interface and ActionCardResult exist. NO concrete implementations.

**New files (in core/src/main/java/battleships_ex/gdx/model/cards/):**
1. ${'`'}DoubleShotCard.java${'`'} -- fires two shots in one turn
2. ${'`'}ShieldCard.java${'`'} -- protects 2x2 area from next attack (needs Board.shieldedCells)
3. ${'`'}ParryCard.java${'`'} -- reflects next opponent shot (needs Player.parryActive flag)
4. ${'`'}EraseCard.java${'`'} -- removes hit markers from 3x3 area (needs Cell.resetHit())
5. ${'`'}ScanCard.java${'`'} -- reveals 3x3 area on opponent board (recon, REVEALED result)

**Existing files to modify:**
- ${'`'}Board.java${'`'} -- add shieldedCells set and isShielded()
- ${'`'}Cell.java${'`'} -- add resetHit()
- ${'`'}Player.java${'`'} -- add parryActive flag
- ${'`'}GameSession.java${'`'} -- check parry/shield in processMove()

**5 test files**, one per card. All pure Java, no libGDX.

---

## Branch 6: Issue #26 -- Card Dealing + Energy System

**Branch name**: ${'`'}26-card-dealing-energy-system${'`'}
**Priority**: LOW
**Depends on**: #25

### What to Build

**New files:**
- ${'`'}CardDealer.java${'`'} -- initial dealing and per-turn drawing (seeded RNG)
- ${'`'}CardFactory.java${'`'} -- creates card instances by type
- ${'`'}CardType.java${'`'} -- enum with energy costs
- ${'`'}EnergySystem.java${'`'} -- currentEnergy, maxEnergy, regenPerTurn, canAfford(), spend(), regenerate()
- ${'`'}CardDealerTest.java${'`'}, ${'`'}EnergySystemTest.java${'`'}

**Modified files:**
- ${'`'}Player.java${'`'} -- add EnergySystem field
- ${'`'}GameSession.java${'`'} -- deal on start, regenerate on turn, check canAfford before play
- ${'`'}ActionCard.java${'`'} interface -- add getEnergyCost()

---

## Branch 7: Issue #22 -- Action Card System Integration

**Branch name**: ${'`'}22-action-card-system-integration${'`'}
**Priority**: LOW
**Depends on**: #25, #26, #18

### What to Build

Integration of cards + energy with Firebase server-side validation.

**Modified:** GameDataSource, FirebaseGameDataSource, GameController, BattleScreen, database.rules.json
**New:** ${'`'}ActionCardUI.java${'`'} (UI widget for card display)

---

## Branch 8: Issue #28 -- Real-time Target Preview

**Branch name**: ${'`'}28-realtime-target-preview${'`'}
**Priority**: LOW
**Depends on**: #18

### What to Build

Opponent sees aimed cell before confirm. Add pushAimPreview/addAimPreviewListener to GameDataSource, pulsing crosshair in BoardActor, selection push in BattleScreen, setAimTarget in GameController.

---

## Branch 9: Issue #29 -- Session Re-join + Auto-cleanup

**Branch name**: ${'`'}29-session-rejoin-auto-cleanup${'`'}
**Priority**: LOW
**Depends on**: #18

### What to Build

**New:** SessionPersistence.java, FirebasePresenceManager.java
**Modified:** GameDataSource (getFullGameState), GameController (rejoinGame), MenuScreen (Rejoin button), database.rules.json (presence nodes)

---

## Branch 10: Issue #27 -- Real-time Features Integration

**Branch name**: ${'`'}27-realtime-features-session-management${'`'}
**Priority**: LOW
**Depends on**: #28, #29

Integration of preview + rejoin. Add onOpponentReconnected/onOpponentDisconnected to GameListener, presence monitoring in GameController, disconnect overlay in BattleScreen.

---

## Branch 11: Issue #33 -- Single Player AI

**Branch name**: ${'`'}33-single-player-ai${'`'}
**Priority**: LOW
**Depends on**: #21, #19

**New:** AIOpponent interface, RandomAI, HuntTargetAI, AIGameController, 2 test files
**Modified:** MenuScreen (Single Player button), BattleScreen (accept AIGameController)

---

## Branch 12: Issue #37 -- Lobby Settings

**Branch name**: ${'`'}37-lobby-settings${'`'}
**Priority**: LOW
**Depends on**: #30

**New:** LobbySettings.java, LobbySettingsSnapshot.java
**Modified:** SettingsScreen (actual UI), Lobby (settings field), LobbyDataSource (updateLobbySettings), LobbyController (updateSettings), BoardConfig (configurable gridSize)

---

## Execution Procedure (Per Branch)

${'```'}bash
git checkout main && git pull origin main
git checkout -b <branch-name>
# implement changes
./gradlew core:test
git add <specific-files>
git commit -m "feat: <description> (#<issue-number>)"
git push -u origin <branch-name>
# create PR targeting main, assign to NWichter
${'```'}

---

## Summary Table

| Order | Issue | Branch Name | New Files | Modified Files | Depends On |
|-------|-------|-------------|-----------|----------------|------------|
| 1 | #30 | 30-gamestatemanager-singleton-state-pattern | 8 | 3 | None |
| 2 | #21 | 21-rules-engine-game-controller-wiring | 2 | 4 | #30 |
| 3 | #18 | 18-firebase-game-state-sync | 5 | 4 | #30 |
| 4 | #19 | 19-ship-placement-logic | 3 | 4 | #21, #18 |
| 5 | #25 | 25-actioncard-implementations | 10 | 4 | #21 |
| 6 | #26 | 26-card-dealing-energy-system | 6 | 3 | #25 |
| 7 | #22 | 22-action-card-system-integration | 1 | 5 | #25, #26, #18 |
| 8 | #28 | 28-realtime-target-preview | 0 | 5 | #18 |
| 9 | #29 | 29-session-rejoin-auto-cleanup | 2 | 4 | #18 |
| 10 | #27 | 27-realtime-features-session-management | 0 | 3 | #28, #29 |
| 11 | #33 | 33-single-player-ai | 5 | 2 | #21, #19 |
| 12 | #37 | 37-lobby-settings | 2 | 5 | #30 |
`;

fs.writeFileSync(dest, content, 'utf8');
console.log('Plan written:', content.length, 'chars to', dest);
