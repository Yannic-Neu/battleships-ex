package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.data.Assets;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.config.ShipCardConfig;
import battleships_ex.gdx.config.board.BoardConfig;
import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.config.board.ShipType;
import battleships_ex.gdx.ui.BoardActor;
import battleships_ex.gdx.ui.CardTray;
import battleships_ex.gdx.ui.ConfirmationDialog;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.ShipCard;
import battleships_ex.gdx.ui.Theme;

import battleships_ex.gdx.state.GameStateManager;
import battleships_ex.gdx.state.GameStateListener;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.rules.PlacementResult;
import battleships_ex.gdx.controller.LobbyController;

public class PlacementScreen extends ScreenAdapter implements GameStateListener {

    private final MyGame game;
    private Stage stage;

    // Core UI
    private BoardActor boardActor;
    private DragAndDrop dragAndDrop;
    private GameButton readyButton;
    private Label deployPhaseLabel;

    // Dynamic interaction areas
    private Table dynamicSection;
    private Table hintsTable;
    private Table actionPanel;

    // Interaction state
    private ShipCard activeDraggedCard;
    private int shipsPlacedCount = 0;

    public PlacementScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(
            GameConfig.WORLD_WIDTH,
            GameConfig.WORLD_HEIGHT
        ));
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(Theme.blackPanel);
        stage.addActor(root);

        // Header Setup
        Table header = new Table();
        header.setBackground(Theme.darkBluePanel);

        GameButton backButton = new GameButton("BACK", ButtonConfig.secondary(80f, 44f), () -> {
            new ConfirmationDialog(
                "ABANDON MISSION?",
                "Leaving now will result in an immediate defeat. Are you sure you want to retreat?",
                "RETREAT",
                "STAY",
                () -> {
                    game.setScreen(new MenuScreen(game));
                }
            ).show(stage);
        });

        Label title = new Label("PLACEMENT", new Label.LabelStyle(Theme.fontLarge, Theme.WHITE));
        header.add(backButton).left().padLeft(15);
        header.add(title).left().expandX().padLeft(16);

        // Board Setup
        BoardConfig boardConfig = new BoardConfig(
            320f,
            10,
            new Color(0.05f, 0.10f, 0.20f, 1f),
            new Color(0.15f, 0.22f, 0.35f, 1f)
        );
        boardActor = new BoardActor(boardConfig);

        Label deployPhase = new Label("DEPLOYMENT PHASE", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));
        Label gridTitle = new Label("10x10 STRATEGIC GRID", new Label.LabelStyle(Theme.fontMedium, Theme.WHITE));

        Table boardSection = new Table();
        boardSection.add(deployPhase).padTop(10).row();
        boardSection.add(gridTitle).padBottom(16).row();
        boardSection.add(boardActor).size(boardConfig.size).row();

        Label leftHint = new Label("Drag ships to grid", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));
        Label rightHint = new Label("Tap to rotate", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        Table hints = new Table();
        hints.add(leftHint).left().expandX();
        hints.add(rightHint).right();

        boardSection.add(hints).growX().padTop(12);

        // Docking Station Setup
        Label dockingTitle = new Label("DOCKING STATION", new Label.LabelStyle(Theme.fontSmall, Theme.WHITE));
        CardTray dockingTray = new CardTray();

        ShipCard carrierCard = new ShipCard(new ShipCardConfig(120f, 82f, "CARRIER", Assets.ships.shipCarrier5h));
        ShipCard cruiserCard = new ShipCard(new ShipCardConfig(110f, 82f, "CRUISER", Assets.ships.shipCruiser4h));
        ShipCard subCard = new ShipCard(new ShipCardConfig(90f, 82f, "SUBMARINE", Assets.ships.shipSubmarine3h));
        ShipCard destroyerCard = new ShipCard(new ShipCardConfig(90f, 82f, "DESTROYER", Assets.ships.shipDestroyer3h));
        ShipCard patrolCard = new ShipCard(new ShipCardConfig(80f, 82f, "PATROL", Assets.ships.shipPatrol2h));

        dockingTray.addCard(carrierCard);
        dockingTray.addCard(cruiserCard);
        dockingTray.addCard(subCard);
        dockingTray.addCard(destroyerCard);
        dockingTray.addCard(patrolCard);

        readyButton = new GameButton("READY TO BATTLE", ButtonConfig.primary(300f, 56f), () -> {
            if (shipsPlacedCount >= 5) {
                deployPhaseLabel.setText("WAITING FOR OPPONENT...");
                readyButton.setText("AWAITING SYNC...");
                GameStateManager.getInstance().confirmPlacementComplete();
                System.out.println("ready button pressed");
            }
        });

        // Assemble View
        root.defaults().growX();
        root.add(header).height(Value.percentHeight(0.08f, root)).row();
        root.add(boardSection).expandY().top().padTop(20).padLeft(20).padRight(20).row();

        Table dockingSection = new Table();
        dockingSection.setBackground(Theme.darkBluePanel);
        dockingSection.add(dockingTitle).left().pad(12).row();
        dockingSection.add(dockingTray).growX().height(95f).padLeft(12).padRight(12).padBottom(12).row();
        dockingSection.add(readyButton).center().padBottom(16);

        root.add(dockingSection).padTop(20).growX().bottom();
        // Initialize Drag and Drop
        setupDragAndDrop(carrierCard, ShipType.CARRIER);
        setupDragAndDrop(cruiserCard, ShipType.CRUISER);
        setupDragAndDrop(subCard, ShipType.SUBMARINE);
        setupDragAndDrop(destroyerCard, ShipType.DESTROYER);
        setupDragAndDrop(patrolCard, ShipType.PATROL);

        // Architecture: Bind as listener to intercept Model outcomes
        GameStateManager.getInstance().setStateListener(this);
    }

    private void buildHintsTable() {
        hintsTable = new Table();
        hintsTable.add(new Label("Drag ships to grid", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY))).left().expandX();
        hintsTable.add(new Label("Drop to setup", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY))).right();
    }

    private void buildActionPanel() {
        actionPanel = new Table();

        GameButton cancelBtn = new GameButton("CANCEL", ButtonConfig.secondary(90, 42), () -> {
            if (activeDraggedCard != null) activeDraggedCard.setVisible(true);
            boardActor.clearFloatingShip();
            activeDraggedCard = null;
            hideActionPanel();
        });

        GameButton rotateBtn = new GameButton("ROTATE", ButtonConfig.secondary(90, 42), () -> {
            BoardActor.FloatingShipVisual floating = boardActor.getFloatingShip();
            if (floating != null) {
                // Rotates perfectly on center axis (Java int division naturally rounds down for odds)
                int offset = floating.type.getLength() / 2;
                int r = floating.start.getRow();
                int c = floating.start.getCol();

                if (floating.orientation == Orientation.HORIZONTAL) {
                    floating.orientation = Orientation.VERTICAL;
                    floating.start = new Coordinate(r - offset, c + offset);
                } else {
                    floating.orientation = Orientation.HORIZONTAL;
                    floating.start = new Coordinate(r + offset, c - offset);
                }
                floating.isLegalVisual = true; // Reset color to check again on next confirm
            }
        });

        GameButton confirmBtn = new GameButton("CONFIRM", ButtonConfig.primary(110, 42), () -> {
            BoardActor.FloatingShipVisual floating = boardActor.getFloatingShip();
            if (floating != null) {
                // Architectural Hand-off: Send domain request to the backend controller
                Ship ship = new Ship(floating.type, floating.orientation);
                GameStateManager.getInstance().placeShip(ship, floating.start, floating.orientation);
            }
        });

        actionPanel.add(cancelBtn).padRight(10);
        actionPanel.add(rotateBtn).padRight(10);
        actionPanel.add(confirmBtn);
    }

    private void showActionPanel() {
        dynamicSection.clearChildren();
        dynamicSection.add(actionPanel).growX().padTop(12);
    }

    private void hideActionPanel() {
        dynamicSection.clearChildren();
        dynamicSection.add(hintsTable).growX().padTop(12);
    }

    private void setupDragAndDrop(ShipCard card, ShipType type) {
        if (dragAndDrop == null) {
            dragAndDrop = new DragAndDrop();

            // Register Board as the Drop Target
            dragAndDrop.addTarget(new DragAndDrop.Target(boardActor) {
                @Override
                public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                    Coordinate hoverCoord = boardActor.pointToCoordinate(x, y);
                    BoardActor.FloatingShipVisual floating = boardActor.getFloatingShip();
                    if (floating == null) {
                        floating = new BoardActor.FloatingShipVisual();
                        floating.type = (ShipType) payload.getObject();
                        floating.orientation = Orientation.HORIZONTAL;
                        boardActor.setFloatingShip(floating);
                    }
                    floating.start = hoverCoord;
                    floating.isLegalVisual = true; // resets to yellow when moving
                    return true;
                }

                @Override
                public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                    BoardActor.FloatingShipVisual floating = boardActor.getFloatingShip();
                    if (floating != null) {
                        floating.start = boardActor.pointToCoordinate(x, y);
                        showActionPanel();
                    }
                }
            });
        }

        // Register Card as Drag Source
        dragAndDrop.addSource(new DragAndDrop.Source(card) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                // If there's already a ship floating but unconfirmed, put it back
                if (activeDraggedCard != null && activeDraggedCard != card) {
                    activeDraggedCard.setVisible(true);
                }
                activeDraggedCard = card;
                card.setVisible(false); // Hide from dock immediately

                boardActor.clearFloatingShip();
                hideActionPanel();

                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                payload.setObject(type);
                return payload;
            }

            @Override
            public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
                // If dropped in the void (not on the board), return it to dock
                if (target == null) {
                    card.setVisible(true);
                    boardActor.clearFloatingShip();
                    activeDraggedCard = null;
                }
            }
        });
    }

    // --- GAME STATE LISTENER CALLBACKS ---

    @Override
    public void onShipPlaced(Ship ship) {
        // Extract the start coordinate (the first element in the LinkedHashSet)
        Coordinate startCoord = ship.getOccupiedCoordinates().iterator().next();

        // Validation Passed: Lock it in visually
        boardActor.addPlacedShip(ship.getType(), startCoord, ship.getOrientation());
        boardActor.clearFloatingShip();
        activeDraggedCard = null; // Stays hidden forever
        hideActionPanel();

        shipsPlacedCount++;
        if (shipsPlacedCount >= 5) {
            deployPhaseLabel.setText("FLEET FULLY DEPLOYED");
        }
    }

    @Override
    public void onPlacementRejected(PlacementResult.Reason reason) {
        // Validation Failed: Flash the floating ship red, but keep it active so they can move it
        BoardActor.FloatingShipVisual floating = boardActor.getFloatingShip();
        if (floating != null) {
            floating.isLegalVisual = false;
        }
    }

    @Override
    public void onStateChanged(String stateName) {
        // If the backend transitions to a combat state, jump to the Battle Screen automatically
        if (stateName.equals("MyTurnState") || stateName.equals("OpponentTurnState")) {
            game.setScreen(new BattleScreen(game));
        }
    }

    // Unused callbacks required by the interface contract
    @Override public void onGameOver(String winnerName) {}
    @Override public void onMiss(Coordinate coordinate) {}
    @Override public void onHit(Coordinate coordinate, Ship ship) {}
    @Override public void onSunk(Coordinate coordinate, Ship ship) {}
    @Override public void onAlreadyShot(Coordinate coordinate) {}
    @Override public void onActionCardPlayed(battleships_ex.gdx.model.cards.ActionCardResult result) {}
    @Override public void onLobbyCreated(String roomCode) {}
    @Override public void onLobbyJoined() {}
    @Override public void onGuestJoined(String guestName) {}
    @Override public void onJoinRejected(LobbyController.JoinRejectionReason reason) {}

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
    public void hide() {
        // CRITICAL: Unregister listener to prevent memory leaks when navigating away
        GameStateManager.getInstance().setStateListener(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        if (boardActor != null) boardActor.dispose();
    }
}
