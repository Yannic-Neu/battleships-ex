package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


import battleships_ex.gdx.controller.GameController;
import battleships_ex.gdx.controller.GameListener;
import battleships_ex.gdx.controller.LobbyController;
import battleships_ex.gdx.state.GameStateListener;
import battleships_ex.gdx.state.GameStateManager;

import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.rules.PlacementResult;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.board.BoardConfig;
import battleships_ex.gdx.ui.ActionCard;
import battleships_ex.gdx.ui.cards.ActionCardPresentation;
import battleships_ex.gdx.ui.cards.ActionCardPresentationBase;
// import battleships_ex.gdx.ui.cards.DoubleShotCardPresentation;
// import battleships_ex.gdx.ui.cards.EraseCardPresentation;
// import battleships_ex.gdx.ui.cards.ScanCardPresentation;
// import battleships_ex.gdx.ui.cards.ShieldCardPresentation;
// import battleships_ex.gdx.ui.cards.ParryCardPresentation;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.data.Assets;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.ui.BoardActor;
import battleships_ex.gdx.ui.CardTray;
import battleships_ex.gdx.ui.ConfirmationDialog;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.Theme;
import battleships_ex.gdx.ui.EnergyBar;
// import battleships_ex.gdx.model.cards.ShieldCard;
// import battleships_ex.gdx.model.cards.ScanCard;
// import battleships_ex.gdx.model.cards.ParryCard;
// import battleships_ex.gdx.model.cards.EraseCard;
// import battleships_ex.gdx.model.cards.DoubleShotCard;


public class BattleScreen extends ScreenAdapter implements GameStateListener {

    private enum ViewMode {
        OWN_FLEET, ENEMY_WATERS
    }

    private EnergyBar energyBar;
    private GameController gameController;
    private final List<ActionCard> actionCards = new ArrayList<>();
    private final MyGame game;
    private Stage stage;
    private ViewMode currentMode = ViewMode.ENEMY_WATERS;

    private BoardActor boardActor;
    private Label tacGridLabel;
    private CardTray actionCardTray;
    private GameButton fireButton;
    private Coordinate targetCoord;
    
    private battleships_ex.gdx.model.cards.ActionCard pendingCard;
    private boolean targetingMode = false;

    public BattleScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        gameController = game.getGameController();
        GameStateManager.getInstance().setStateListener(this);

        rebuildUI();

        // The GameListener is now handled by the GameStateManager, which will
        // in turn call the GameStateListener methods implemented by this screen.
    }

    // -------------------------------------------------------------------------
    // GameStateListener implementation
    // -------------------------------------------------------------------------

    @Override
    public void onStateChanged(String stateName) {
        System.out.println("[BattleScreen] LOG: onStateChanged received, rebuilding UI.");
        rebuildUI();
    }

    // Unused GameStateListener methods
    @Override public void onLobbyCreated(String roomCode) {}
    @Override public void onLobbyJoined() {}
    @Override public void onGuestJoined(String guestName) {}
    @Override public void onJoinRejected(LobbyController.JoinRejectionReason reason) {}
    @Override public void onOpponentPlacementReady(boolean ready) {}
    @Override
    public void onGameOver(String winnerName) {
        Gdx.app.postRunnable(() -> {
            game.setScreen(new GameOverScreen(game, winnerName));
        });
    }

    @Override
    public void onTurnChanged(String currentPlayerId) {
        System.out.println("[BattleScreen] LOG: onTurnChanged received, rebuilding UI.");
        rebuildUI();
    }

    @Override public void onHit(Coordinate coordinate, Ship ship) {
        updateEnergyFromGame();
        rebuildUI();
    }

    @Override public void onMiss(Coordinate coordinate) {
        rebuildUI();
    }

    @Override public void onSunk(Coordinate coordinate, Ship ship) {
        updateEnergyFromGame();
        rebuildUI();
    }

    @Override public void onAlreadyShot(Coordinate coordinate) {
        updateFireButtonState();
    }

    @Override public void onShipPlaced(Ship ship) {}
    @Override public void onShipRemoved(Ship ship) {}
    @Override public void onPlacementRejected(PlacementResult.Reason reason) {}
    @Override
    public void onActionCardPlayed(battleships_ex.gdx.model.cards.ActionCardResult result) {
        updateEnergyFromGame();
        rebuildUI();
    }

    @Override
    public void onCardTargetRequested(battleships_ex.gdx.model.cards.ActionCard card) {
        this.pendingCard = card;
        this.targetingMode = true;

        // Mine card requires targeting own board
        if (card instanceof battleships_ex.gdx.model.cards.MineCard) {
            this.currentMode = ViewMode.OWN_FLEET;
        } else {
            this.currentMode = ViewMode.ENEMY_WATERS;
        }

        rebuildUI();
    }


    private void updateEnergyFromGame() {
        if (energyBar == null) return;

        int energy = gameController
            .getLocalPlayer()
            .getEnergy();

        energyBar.updateEnergy(energy);
    }

    private void rebuildUI() {
        stage.clear();
        actionCards.clear();

        float sidePad = 16f;
        float contentWidth = Math.min(320f, GameConfig.WORLD_WIDTH - 2f * sidePad);

        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(sidePad);
        root.setBackground(Theme.blackPanel);
        stage.addActor(root);

        // --- Header ---
        Table topArea = new Table();
        GameButton backButton = new GameButton("BACK", ButtonConfig.secondary(60f, 44f), () -> {
            new ConfirmationDialog(
                "ABANDON MISSION?",
                "Retreating during active combat results in an immediate loss. Do you still wish to withdraw?",
                "RETREAT",
                "STAY",
                () -> {
                    System.out.println("Player abandoned during battle - Result: LOSS");
                    game.setScreen(new MenuScreen(game));
                }
            ).show(stage);
        });

        Label turnLabel = new Label("", new Label.LabelStyle(Theme.fontLarge, Theme.WHITE));
        boolean myTurn = battleships_ex.gdx.state.GameStateManager.getInstance().isMyTurn();
        turnLabel.setText(myTurn ? "YOUR TURN" : "OPPONENT'S TURN");
        turnLabel.setColor(myTurn ? Theme.WHITE : Theme.GRAY);

        topArea.add(backButton).left().pad(10);
        topArea.add(turnLabel).expandX().center();
        topArea.add().width(60f).pad(10); // Balances the back button

        // --- View Switcher (State Toggle) ---
        Table switchInner = new Table();
        switchInner.setBackground(Theme.darkBluePanel);
        switchInner.pad(6f);

        GameButton enemyWatersBtn = new GameButton("ENEMY WATERS",
            currentMode == ViewMode.ENEMY_WATERS ? ButtonConfig.primary((contentWidth - 12f) / 2f, 44f) : ButtonConfig.secondary((contentWidth - 12f) / 2f, 44f),
            () -> {
                if (currentMode != ViewMode.ENEMY_WATERS) {
                    currentMode = ViewMode.ENEMY_WATERS;
                    rebuildUI();
                }
            });

        GameButton yourFleetBtn = new GameButton("YOUR FLEET",
            currentMode == ViewMode.OWN_FLEET ? ButtonConfig.primary((contentWidth - 12f) / 2f, 44f) : ButtonConfig.secondary((contentWidth - 12f) / 2f, 44f),
            () -> {
                if (currentMode != ViewMode.OWN_FLEET) {
                    currentMode = ViewMode.OWN_FLEET;
                    rebuildUI();
                }
            });

        switchInner.add(enemyWatersBtn).width((contentWidth - 12f) / 2f).height(44f);
        switchInner.add(yourFleetBtn).width((contentWidth - 12f) / 2f).height(44f);

        // --- Board Section ---
        String gridText = currentMode == ViewMode.ENEMY_WATERS ? "ENEMY TACTICAL GRID" : "YOUR STRATEGIC GRID";
        tacGridLabel = new Label(gridText, new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        BoardConfig boardConfig = new BoardConfig(320f, 10, new Color(0.05f, 0.10f, 0.20f, 1f), new Color(0.15f, 0.22f, 0.35f, 1f));
        boardActor = new BoardActor(boardConfig);

        battleships_ex.gdx.model.board.Board targetBoard = (currentMode == ViewMode.ENEMY_WATERS)
            ? gameController.getRemotePlayer().getBoard()
            : gameController.getLocalPlayer().getBoard();

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                battleships_ex.gdx.model.board.Cell cell = targetBoard.getCell(row, col);
                if (cell.isHit()) {
                    if (cell.hasShip()) {
                        boardActor.markHit(new Coordinate(row, col));
                    } else {
                        boardActor.markMiss(new Coordinate(row, col));
                    }
                }
            }
        }

        if (currentMode == ViewMode.ENEMY_WATERS) {
            boardActor.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    Coordinate coord = boardActor.pointToCoordinate(x, y);
                    setTarget(coord);
                    return true;
                }
                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    Coordinate coord = boardActor.pointToCoordinate(x, y);
                    setTarget(coord);
                }
            });
            // Restore visual target if it exists
            if (targetCoord != null) {
                boardActor.setPreviewCell(targetCoord);
            }

            // Draw sunken ships
            for (battleships_ex.gdx.model.board.Ship ship : targetBoard.getShips()) {
                if (ship.isSunk() && !ship.getOccupiedCoordinates().isEmpty()) {
                    Coordinate start = ship.getOccupiedCoordinates().iterator().next();
                    boardActor.addPlacedShip(ship.getType(), start, ship.getOrientation());
                }
            }
        }

        if (currentMode == ViewMode.OWN_FLEET) {
            List<battleships_ex.gdx.model.board.Ship> ships = targetBoard.getShips();
            for (battleships_ex.gdx.model.board.Ship ship : ships) {
                if (ship.isPlaced() && !ship.getOccupiedCoordinates().isEmpty()) {
                    Coordinate start = ship.getOccupiedCoordinates().iterator().next();
                    boardActor.addPlacedShip(ship.getType(), start, ship.getOrientation());
                }
            }
        }

        // --- Actions Section ---
        Table actionsPanel = new Table();

        boolean exMode = battleships_ex.gdx.state.GameStateManager.getInstance().isExModeEnabled();

        if (exMode) {
            energyBar = new EnergyBar();
            energyBar.updateEnergy(
                gameController.getLocalPlayer().getEnergy()
            );

            Label actionCardsLabel = new Label("ACTION CARDS", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

            actionCardTray = new CardTray();
            
            for (battleships_ex.gdx.model.cards.ActionCard modelCard : gameController.getLocalPlayer().getCards()) {
                String name = "";
                String shortDesc = "";
                String longDesc = "";
                TextureRegion icon = Assets.ships.shipPatrol2h; // Default

                if (modelCard instanceof battleships_ex.gdx.model.cards.SonarCard) {
                    name = "SONAR";
                    shortDesc = "Reveal adjacency";
                    longDesc = "Reveal number of ships/mines in adjacent tiles. Costs 2 energy. 2 uses.";
                    icon = Assets.shipWithSight;
                } else if (modelCard instanceof battleships_ex.gdx.model.cards.BombCard) {
                    name = "BOMB";
                    shortDesc = "2x2 Blast";
                    longDesc = "Shoot a 2x2 area on the opponent's board. Costs 3 energy. 2 uses.";
                    icon = Assets.logoWithShip;
                } else if (modelCard instanceof battleships_ex.gdx.model.cards.MineCard) {
                    name = "MINE";
                    shortDesc = "Defensive Trap";
                    longDesc = "Place a mine on your own board. Triggers random shots if hit. Costs 1 energy. 3 uses.";
                    icon = Assets.ships.shipSubmarine3h;
                } else if (modelCard instanceof battleships_ex.gdx.model.cards.AirstrikeCard) {
                    name = "AIRSTRIKE";
                    shortDesc = "Row/Col Clear";
                    longDesc = "Shoot an entire row or column. Costs 4 energy. 1 use.";
                    icon = Assets.ships.shipCarrier5h;
                }

                ActionCardPresentation presentation = new ActionCardPresentationBase(
                    name, shortDesc, longDesc, icon, 
                    ((battleships_ex.gdx.model.cards.BaseActionCard)modelCard).getRemainingUses()
                );
                
                actionCardTray.addCard(bindCard(name, presentation, modelCard));
            }

            actionsPanel.add(energyBar).left().padBottom(6f).row();
            actionsPanel.add(actionCardsLabel).left().padBottom(12f).row();
            actionsPanel.add(actionCardTray).growX().height(95f).padBottom(12f).row();
        }

        fireButton = new GameButton("FIRE", ButtonConfig.primary(contentWidth, 72f), () -> {
            if (fireButton.isDisabled()) return;
            if (targetCoord != null) {
                battleships_ex.gdx.state.GameStateManager.getInstance().fireShot(targetCoord.getRow(), targetCoord.getCol());
                // clear local target visually after shot
                targetCoord = null;
                boardActor.clearPreviewCell();
                updateFireButtonState();
            }
        });

        actionsPanel.add(fireButton).center().row();

        // --- Root Assembly ---
        root.defaults().growX();
        root.add(topArea).height(48f).row();
        root.add(switchInner).width(contentWidth).height(56f).padTop(12f).center().row();

        Table boardContainer = new Table();
        boardContainer.add(tacGridLabel).left().padTop(5).padBottom(5).row();
        boardContainer.add(boardActor).size(boardConfig.size).center().row();

        root.add(boardContainer).expandY().padTop(10f).row();
        root.add(actionsPanel).padBottom(12f).row();
        if (exMode) {
            updateActionCardAvailability();
        }
        updateFireButtonState();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    private void setTarget(Coordinate coord) {
        if (!battleships_ex.gdx.state.GameStateManager.getInstance().isMyTurn()) return;

        if (targetingMode && pendingCard != null) {
            gameController.playActionCard(pendingCard, coord);
            targetingMode = false;
            pendingCard = null;
            return;
        }

        this.targetCoord = coord;
        boardActor.setPreviewCell(coord);
        gameController.updatePreview(coord);
        updateFireButtonState();
    }

    private void updateFireButtonState() {
        if (fireButton == null) return;
        boolean myTurn = battleships_ex.gdx.state.GameStateManager.getInstance().isMyTurn();
        boolean validTarget = targetCoord != null;

        if (validTarget) {
            // Check if already hit
            // "Disable the "FIRE" button if the selected tile has already been targeted previously."
            Player opponent = gameController.getRemotePlayer();
            if (opponent != null) {
                // If cell is already hit, validTarget becomes false
                if (opponent.getBoard().getCell(targetCoord).isHit()) {
                    validTarget = false;
                }
            }
        }

        fireButton.setDisabled(!myTurn || !validTarget || currentMode != ViewMode.ENEMY_WATERS);
    }

    private ActionCard bindCard(String name, ActionCardPresentation presentation,
                                battleships_ex.gdx.model.cards.ActionCard modelCard) {

        ActionCard uiCard = new ActionCard(
            new GameConfig.ActionCardConfig(95f, 82f, true, Theme.BLUE, name)
        );
        uiCard.bind(presentation);

        // Attach the REAL gameplay card to UI card
        uiCard.setModelCard(modelCard);

        // Add click listener
        uiCard.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (uiCard.isDisabled()) {
                    // Even when disabled, allow info popup
                    uiCard.showInfoPopup(stage);
                    return;
                }
                if (getTapCount() >= 2) {
                    // Double-tap = show info
                    uiCard.showInfoPopup(stage);
                } else {
                    // Single tap = play card
                    gameController.playActionCard(modelCard);
                    updateEnergyFromGame();
                    updateActionCardAvailability();
                }
            }
        });

        actionCards.add(uiCard);
        return uiCard;
    }
    private void updateActionCardAvailability() {
        boolean myTurn = gameController.isLocalPlayerTurn();
        int energy = gameController.getLocalPlayer().getEnergy();

        for (ActionCard card : actionCards) {

            // TEMPORARY cost until wiring card model layer
            int cost = 1; // or 2 for shield later

            boolean usable =
                myTurn &&
                    energy >= cost;

            card.setDisabled(!usable);
        }
    }
}
