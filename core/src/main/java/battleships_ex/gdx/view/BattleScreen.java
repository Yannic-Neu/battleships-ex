package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;
import java.util.List;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.board.BoardConfig;
import battleships_ex.gdx.ui.ActionCard;
import battleships_ex.gdx.ui.cards.ActionCardPresentation;
import battleships_ex.gdx.ui.cards.ActionCardPresentationBase;
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
import battleships_ex.gdx.state.GameStateManager;
import battleships_ex.gdx.state.GameStateListener;
import battleships_ex.gdx.controller.LobbyController;
import battleships_ex.gdx.controller.GameController;
import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Cell;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.cards.ActionCardResult;
import battleships_ex.gdx.model.rules.PlacementResult;

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
    private GameButton fireButton;
    private Coordinate targetCoord;

    private Label turnLabel;
    private GameButton enemyWatersBtn;
    private GameButton yourFleetBtn;
    private Table actionsPanel;

    private battleships_ex.gdx.model.cards.ActionCard pendingCard;
    private boolean targetingMode = false;

    public BattleScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(
            GameConfig.WORLD_WIDTH,
            GameConfig.WORLD_HEIGHT
        ));
        Gdx.input.setInputProcessor(stage);
        gameController = game.getGameController();
        GameStateManager.getInstance().setStateListener(this);
        buildUI();
        updateAll();
    }

    @Override public void onStateChanged(String stateName) { updateAll(); }
    @Override public void onGameOver(String winnerName, String reason) {
        Gdx.app.postRunnable(() -> game.setScreen(new GameOverScreen(game, winnerName, reason)));
    }

    @Override public void onGameOver(String winnerName) {
        onGameOver(winnerName, null);
    }

    @Override public void onTurnChanged(String currentPlayerId) {
        boolean myTurn = currentPlayerId.equals(gameController.getLocalPlayer().getId());
        updateAll();
        
        final ViewMode targetMode = myTurn ? ViewMode.ENEMY_WATERS : ViewMode.OWN_FLEET;
        
        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
            @Override public void run() {
                if (currentMode != targetMode) {
                    currentMode = targetMode;
                    targetCoord = null;
                    targetingMode = false;
                    pendingCard = null;
                    if (boardActor != null) boardActor.clearPreviewCell();
                    updateAll();
                }
            }
        }, 1.0f);
    }
    @Override public void onHit(Coordinate coordinate, Ship ship) { updateEnergyFromGame(); updateBoard(); updateActionPanel(); }
    @Override public void onMiss(Coordinate coordinate) { updateBoard(); updateActionPanel(); }
    @Override public void onSunk(Coordinate coordinate, Ship ship) { updateEnergyFromGame(); updateBoard(); updateActionPanel(); }
    @Override public void onAlreadyShot(Coordinate coordinate) { updateFireButtonState(); }
    @Override public void onActionCardPlayed(ActionCardResult result) { updateEnergyFromGame(); updateBoard(); updateActionPanel(); }
    @Override public void onOpponentAbandoned() {
        // GameController will also trigger onGameOver("localPlayerName", "forfeit")
    }

    @Override
    public void onActionCardRejected(battleships_ex.gdx.model.cards.ActionCard card, String reason) {
        if (tacGridLabel != null) {
            final String originalText = tacGridLabel.getText().toString();
            tacGridLabel.setText("ERROR: " + reason);
            tacGridLabel.setColor(Theme.YELLOW);
            com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                @Override public void run() {
                    if (tacGridLabel != null) { tacGridLabel.setText(originalText); tacGridLabel.setColor(Theme.GRAY); }
                }
            }, 2.0f);
        }
    }

    @Override
    public void onCardTargetRequested(battleships_ex.gdx.model.cards.ActionCard card) {
        this.pendingCard = card;
        this.targetingMode = true;
        if (card instanceof battleships_ex.gdx.model.cards.MineCard) this.currentMode = ViewMode.OWN_FLEET;
        else this.currentMode = ViewMode.ENEMY_WATERS;
        updateBoard(); updateActionPanel(); updateModeSwitch();
    }

    @Override public void onShipPlaced(Ship ship) { updateBoard(); }
    @Override public void onShipRemoved(Ship ship) { updateBoard(); }
    @Override public void onPlacementRejected(PlacementResult.Reason reason) { updateBoard(); }
    @Override public void onOpponentPlacementReady(boolean ready) { updateBoard(); }
    @Override public void onJoinRejected(LobbyController.JoinRejectionReason reason) {}
    @Override public void onLobbyCreated(String roomCode) {}
    @Override public void onLobbyJoined() {}
    @Override public void onGuestJoined(String guestName) {}

    private void updateEnergyFromGame() {
        if (energyBar == null) return;
        energyBar.updateEnergy(gameController.getLocalPlayer().getEnergy());
    }

    private void buildUI() {
        float sidePad = 16f;
        float contentWidth = 320f;

        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(sidePad);
        root.setBackground(Theme.blackPanel);
        stage.addActor(root);

        // Header
        Table topArea = new Table();
        topArea.setBackground(Theme.darkBluePanel);
        GameButton backButton = new GameButton("BACK", ButtonConfig.secondary(60f, 44f), () -> new ConfirmationDialog(
            "RETREAT?",
            "Abandoning the battle will count as a defeat. Retreat anyway?",
            "RETREAT",
            "STAY",
            () -> {
                game.getGameController().abandonGame();
                game.setScreen(new MenuScreen(game));
            }
        ).show(stage));

        GameButton tutorialButton = new GameButton("?", ButtonConfig.secondary(44f,44f), () ->
            game.setScreen(new TutorialScreen(game, this)));

        turnLabel = new Label("", new Label.LabelStyle(Theme.fontLarge, Theme.WHITE));

        topArea.add(backButton).left().padLeft(10);
        topArea.add(turnLabel).center().expandX();
        topArea.add(tutorialButton).right().padRight(10);

        // Switch Mode
        Table switchInner = new Table();
        switchInner.setBackground(Theme.darkBluePanel);
        switchInner.pad(6f);

        float btnWidth = (contentWidth - 12f) / 2f;
        enemyWatersBtn = new GameButton("ENEMY WATERS", ButtonConfig.primary(btnWidth, 44f), () -> {
            if (currentMode != ViewMode.ENEMY_WATERS) {
                currentMode = ViewMode.ENEMY_WATERS;
                targetCoord = null; targetingMode = false; pendingCard = null;
                updateAll();
            }
        });

        yourFleetBtn = new GameButton("YOUR FLEET", ButtonConfig.secondary(btnWidth, 44f), () -> {
            if (currentMode != ViewMode.OWN_FLEET) {
                currentMode = ViewMode.OWN_FLEET;
                targetCoord = null; targetingMode = false; pendingCard = null;
                updateAll();
            }
        });

        switchInner.add(enemyWatersBtn).width(btnWidth).height(44f);
        switchInner.add(yourFleetBtn).width(btnWidth).height(44f);

        // Board
        BoardConfig boardConfig = new BoardConfig(
            contentWidth,
            10,
            new Color(0.05f, 0.10f, 0.20f, 1f),
            new Color(0.15f, 0.22f, 0.35f, 1f)
        );
        boardActor = new BoardActor(boardConfig);
        boardActor.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                setTarget(boardActor.pointToCoordinate(x, y)); return true;
            }
            @Override public void touchDragged(InputEvent event, float x, float y, int pointer) {
                setTarget(boardActor.pointToCoordinate(x, y));
            }
        });

        tacGridLabel = new Label("", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));
        Table boardTable = new Table();
        boardTable.add(tacGridLabel).left().padLeft(5f).padTop(5).padBottom(5).row();
        boardTable.add(boardActor).size(boardConfig.size).center().row();

        // Actions
        actionsPanel = new Table();

        // --- Root Assembly ---
        root.defaults().growX();
        root.add(topArea).height(48f).row();
        root.add(switchInner).width(contentWidth).height(56f).padTop(12f).center().row();
        root.add(boardTable).expandY().padTop(10f).row();
        root.add(actionsPanel).padBottom(12f).row();
    }

    private void updateAll() {
        updateHeader();
        updateModeSwitch();
        updateBoard();
        updateActionPanel();
    }

    private void updateHeader() {
        boolean myTurn = GameStateManager.getInstance().isMyTurn();
        turnLabel.setText(myTurn ? "YOUR TURN" : "OPPONENT'S TURN");
        turnLabel.setColor(myTurn ? Theme.WHITE : Theme.GRAY);
    }

    private void updateModeSwitch() {
        float contentWidth = 320f;
        float btnWidth = (contentWidth - 12f) / 2f;
        enemyWatersBtn.updateStyle(currentMode == ViewMode.ENEMY_WATERS ? ButtonConfig.primary(btnWidth, 44f) : ButtonConfig.secondary(btnWidth, 44f));
        yourFleetBtn.updateStyle(currentMode == ViewMode.OWN_FLEET ? ButtonConfig.primary(btnWidth, 44f) : ButtonConfig.secondary(btnWidth, 44f));
    }

    private void updateBoard() {
        boardActor.clearVisuals();
        Board targetBoard = (currentMode == ViewMode.OWN_FLEET) ? gameController.getLocalPlayer().getBoard() : gameController.getRemotePlayer().getBoard();

        boardActor.setBoardModel(targetBoard);
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                Cell cell = targetBoard.getCell(new Coordinate(r, c));
                if (cell.isHit()) {
                    if (cell.hasShip()) boardActor.markHit(cell.getCoordinate());
                    else boardActor.markMiss(cell.getCoordinate());
                }
            }
        }
        if (currentMode == ViewMode.OWN_FLEET) {
            boardActor.setMines(targetBoard.getMines());
        } else {
            boardActor.setMines(null);
        }
        boardActor.setScannedTiles(targetBoard.getScannedTiles());

        for (Ship ship : targetBoard.getShips()) {
            if (currentMode == ViewMode.OWN_FLEET || ship.isSunk()) {
                boardActor.addPlacedShip(ship.getType(), ship.getOccupiedCoordinates().iterator().next(), ship.getOrientation());
            }
        }

        if (targetCoord != null) setTarget(targetCoord);

        String gridTitle = (currentMode == ViewMode.OWN_FLEET) ? "DEFENSIVE GRID" : "TACTICAL GRID";
        if (targetingMode) {
            if (pendingCard instanceof battleships_ex.gdx.model.cards.SonarCard) gridTitle = "SELECT TILE TO SCAN (MUST BE HIT)";
            else if (pendingCard instanceof battleships_ex.gdx.model.cards.BombCard) gridTitle = "SELECT 2x2 AREA (TOP-LEFT)";
            else if (pendingCard instanceof battleships_ex.gdx.model.cards.MineCard) gridTitle = "PLACE MINE ON OWN WATERS";
            else if (pendingCard instanceof battleships_ex.gdx.model.cards.AirstrikeCard) gridTitle = "SELECT ROW/COLUMN";
        }
        tacGridLabel.setText(gridTitle);
    }

    private void updateActionPanel() {
        actionsPanel.clear();
        float contentWidth = 320f;

        if (GameStateManager.getInstance().isExModeEnabled()) {
            energyBar = new EnergyBar(); updateEnergyFromGame();
            final CardTray actionCardTray = new CardTray();
            actionCards.clear();
            for (battleships_ex.gdx.model.cards.ActionCard modelCard : gameController.getLocalPlayer().getCards()) {
                String name = ""; String shortDesc = ""; String longDesc = ""; TextureRegion icon = Assets.ships.shipPatrol2h;
                if (modelCard instanceof battleships_ex.gdx.model.cards.SonarCard) {
                    name = "SONAR"; shortDesc = "Adjacency"; longDesc = "Scan 3x3 on a HIT tile. Costs 2."; icon = Assets.shipWithSight;
                } else if (modelCard instanceof battleships_ex.gdx.model.cards.BombCard) {
                    name = "BOMB"; shortDesc = "2x2 Blast"; longDesc = "Shoot 2x2 area. Costs 3."; icon = Assets.logoWithShip;
                } else if (modelCard instanceof battleships_ex.gdx.model.cards.MineCard) {
                    name = "MINE"; shortDesc = "Defensive"; longDesc = "Place on own board. Costs 1."; icon = Assets.ships.shipSubmarine3h;
                } else if (modelCard instanceof battleships_ex.gdx.model.cards.AirstrikeCard) {
                    name = "AIRSTRIKE"; shortDesc = "Row/Col"; longDesc = "Clear row or column. Costs 4."; icon = Assets.ships.shipCarrier5h;
                }
                ActionCardPresentation pres = new ActionCardPresentationBase(name, shortDesc, longDesc, icon);
                actionCardTray.addCard(bindCard(name, pres, modelCard));
            }
            actionsPanel.add(energyBar).left().padLeft(5f).padBottom(6f).row();
            actionsPanel.add(new Label("ACTION CARDS", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY))).left().padLeft(5f).padBottom(12f).row();
            actionsPanel.add(actionCardTray).growX().height(95f).padBottom(12f).row();
            updateActionCardAvailability();
        }

        if (targetingMode) {
            boolean isAirstrike = pendingCard instanceof battleships_ex.gdx.model.cards.AirstrikeCard;
            float btnHeight = 60f;
            float pad = 5f;

            float cancelWidth, rotateWidth, activateWidth;
            if (isAirstrike) {
                cancelWidth = (contentWidth - 2 * pad) * 0.25f;
                rotateWidth = (contentWidth - 2 * pad) * 0.25f;
                activateWidth = (contentWidth - 2 * pad) * 0.50f;
            } else {
                cancelWidth = (contentWidth - pad) * 0.35f;
                activateWidth = (contentWidth - pad) * 0.65f;
                rotateWidth = 0;
            }

            GameButton cancelCardButton = new GameButton("CANCEL", ButtonConfig.secondary(cancelWidth, btnHeight), () -> {
                pendingCard = null; targetingMode = false; boardActor.clearPreviewCell(); currentMode = ViewMode.ENEMY_WATERS; updateAll();
            });

            fireButton = new GameButton("ACTIVATE", ButtonConfig.primary(activateWidth, btnHeight), () -> {
                if (fireButton.isDisabled()) return;
                if (pendingCard != null && targetCoord != null) {
                    gameController.playActionCard(pendingCard, targetCoord);
                    pendingCard = null; targetingMode = false; boardActor.clearPreviewCell(); updateAll();
                }
            });

            Table actionRow = new Table();
            actionRow.add(cancelCardButton).padRight(pad);
            actionRow.add(fireButton).padRight(isAirstrike ? pad : 0);

            if (isAirstrike) {
                GameButton rotateCardButton = new GameButton("ROTATE", ButtonConfig.secondary(rotateWidth, btnHeight), () -> {
                    if (pendingCard instanceof battleships_ex.gdx.model.cards.AirstrikeCard) {
                        ((battleships_ex.gdx.model.cards.AirstrikeCard) pendingCard).toggleOrientation();
                        if (targetCoord != null) setTarget(targetCoord);
                    }
                });
                actionRow.add(rotateCardButton);
            }
            actionsPanel.add(actionRow).center().padBottom(10).row();
        } else {
            fireButton = new GameButton("FIRE", ButtonConfig.primary(contentWidth, 60f), () -> {
                if (fireButton.isDisabled()) return;
                if (targetCoord != null) {
                    GameStateManager.getInstance().fireShot(targetCoord.getRow(), targetCoord.getCol());
                    targetCoord = null; boardActor.clearPreviewCell(); updateFireButtonState();
                }
            });
            actionsPanel.add(fireButton).center().row();
        }
        updateFireButtonState();
    }

    private void setTarget(Coordinate coord) {
        if (!GameStateManager.getInstance().isMyTurn()) return;
        this.targetCoord = coord;

        // Update sonar zoom
        if (boardActor != null) {
            boardActor.setSonarZoom(coord);
        }

        if (targetingMode && pendingCard != null) {
            List<Coordinate> area = new ArrayList<>();
            if (pendingCard instanceof battleships_ex.gdx.model.cards.BombCard) {
                for (int r = coord.getRow(); r <= coord.getRow() + 1; r++)
                    for (int c = coord.getCol(); c <= coord.getCol() + 1; c++)
                        if (r < 10 && c < 10) area.add(new Coordinate(r, c));
            } else if (pendingCard instanceof battleships_ex.gdx.model.cards.AirstrikeCard) {
                battleships_ex.gdx.model.cards.AirstrikeCard ac = (battleships_ex.gdx.model.cards.AirstrikeCard) pendingCard;
                if (ac.getOrientation() == battleships_ex.gdx.model.cards.AirstrikeCard.Orientation.ROW)
                    for (int i = 0; i < 10; i++) area.add(new Coordinate(coord.getRow(), i));
                else
                    for (int i = 0; i < 10; i++) area.add(new Coordinate(i, coord.getCol()));
            } else { area.add(coord); }
            assert boardActor != null;
            boardActor.setPreviewCells(area);
        } else {
            assert boardActor != null;
            boardActor.setPreviewCell(coord); }
        gameController.updatePreview(coord);
        updateFireButtonState();
    }

    private void updateFireButtonState() {
        if (fireButton == null) return;
        boolean myTurn = GameStateManager.getInstance().isMyTurn();
        boolean validTarget = targetCoord != null;
        if (validTarget && !targetingMode) {
            Player opponent = gameController.getRemotePlayer();
            if (opponent != null && opponent.getBoard().getCell(targetCoord).isHit()) validTarget = false;
            if (currentMode == ViewMode.OWN_FLEET) validTarget = false;
        }
        if (validTarget && targetingMode && pendingCard instanceof battleships_ex.gdx.model.cards.SonarCard) {
            Board targetBoard = gameController.getRemotePlayer().getBoard();
            if (!targetBoard.getCell(targetCoord).isHit()) validTarget = false;
        }
        if (validTarget && targetingMode && pendingCard instanceof battleships_ex.gdx.model.cards.MineCard) {
            Board ownBoard = gameController.getLocalPlayer().getBoard();
            Cell cell = ownBoard.getCell(targetCoord);
            if (cell.isHit() || cell.hasShip() || ownBoard.hasMine(targetCoord)) {
                validTarget = false;
            }
        }
        fireButton.setDisabled(!myTurn || !validTarget);
    }

    private ActionCard bindCard(String name, ActionCardPresentation presentation, battleships_ex.gdx.model.cards.ActionCard modelCard) {
        ActionCard uiCard = new ActionCard(new GameConfig.ActionCardConfig(95f, 82f, true, Theme.BLUE, name));
        uiCard.setModelCard(modelCard); uiCard.bind(presentation);
        uiCard.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                if (uiCard.isDisabled()) { if (getTapCount() >= 2) uiCard.showInfoPopup(stage); return; }
                if (getTapCount() >= 2) uiCard.showInfoPopup(stage);
                else {
                    if (targetingMode && pendingCard == modelCard && modelCard instanceof battleships_ex.gdx.model.cards.AirstrikeCard) {
                        ((battleships_ex.gdx.model.cards.AirstrikeCard) modelCard).toggleOrientation();
                        if (targetCoord != null) setTarget(targetCoord);
                    } else {
                        gameController.playActionCard(modelCard);
                        updateEnergyFromGame(); updateActionCardAvailability();
                    }
                }
            }
        });
        actionCards.add(uiCard); return uiCard;
    }

    private void updateActionCardAvailability() {
        boolean myTurn = GameStateManager.getInstance().isMyTurn();
        Player localPlayer = gameController.getLocalPlayer();
        int energy = localPlayer.getEnergy();
        for (ActionCard card : actionCards) {
            battleships_ex.gdx.model.cards.ActionCard modelCard = card.getModelCard();
            if (modelCard == null) continue;
            boolean alreadyPlayed = localPlayer.hasPlayedCardThisTurn(modelCard);
            boolean usable = myTurn && energy >= modelCard.getEnergyCost() && !alreadyPlayed;
            card.setDisabled(!usable);
        }
    }

    @Override public void render(float delta) { ScreenUtils.clear(0f, 0f, 0f, 1f); stage.act(delta); stage.draw(); }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() {
        if (GameStateManager.isInitialized()) {
            GameStateManager.getInstance().setStateListener(null);
        }
    }
    @Override public void dispose() { stage.dispose(); if (boardActor != null) boardActor.dispose(); }
}
