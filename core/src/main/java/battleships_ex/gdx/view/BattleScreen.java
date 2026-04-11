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

import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.model.rules.PlacementResult;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.board.BoardConfig;
import battleships_ex.gdx.ui.ActionCard;
import battleships_ex.gdx.ui.cards.ActionCardPresentation;
import battleships_ex.gdx.ui.cards.DoubleShotCardPresentation;
import battleships_ex.gdx.ui.cards.EraseCardPresentation;
import battleships_ex.gdx.ui.cards.ScanCardPresentation;
import battleships_ex.gdx.ui.cards.ShieldCardPresentation;
import battleships_ex.gdx.ui.cards.ParryCardPresentation;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.data.Assets;
import battleships_ex.gdx.ui.BoardActor;
import battleships_ex.gdx.ui.CardTray;
import battleships_ex.gdx.ui.ConfirmationDialog;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.Theme;
import battleships_ex.gdx.ui.EnergyBar;
import battleships_ex.gdx.model.cards.ShieldCard;
import battleships_ex.gdx.model.cards.ScanCard;
import battleships_ex.gdx.model.cards.ParryCard;
import battleships_ex.gdx.model.cards.EraseCard;
import battleships_ex.gdx.model.cards.DoubleShotCard;


public class BattleScreen extends ScreenAdapter {

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

    public BattleScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        gameController = game.getGameController();

        rebuildUI();

        gameController.setListener(new GameListener() {
            @Override
            public void onHit(Coordinate c, Ship ship) {
                updateEnergyFromGame();
                updateActionCardAvailability();
            }
            @Override
            public void onActionCardPlayed(battleships_ex.gdx.model.cards.ActionCardResult result) {
                // Update energy and UI because cards cost energy
                updateEnergyFromGame();
                updateActionCardAvailability();
            }
                @Override
            public void onSunk(Coordinate c, Ship ship) {
                updateEnergyFromGame();
                updateActionCardAvailability();
            }

            @Override
            public void onMiss(Coordinate c) {
                updateActionCardAvailability();
                // no energy change
            }

            @Override
            public void onAlreadyShot(Coordinate c) {}

            @Override
            public void onShipPlaced(Ship ship) {}

            @Override
            public void onPlacementRejected(PlacementResult.Reason reason) {}

            @Override
            public void onGameOver(String winnerName) {}

            @Override
            public void onPreviewReceived(Coordinate coord) {}

            @Override
            public void onOpponentMoveReceived(int row, int col) {}

            @Override
            public void onOpponentDisconnected() {}

            @Override
            public void onOpponentReconnected() {}

            @Override
            public void onSessionTimeout() {}
        });
    }
    private void updateEnergyFromGame() {
        if (energyBar == null) return;

        int energy = gameController
            .getSession()
            .getLocalPlayer()
            .getEnergy();

        energyBar.updateEnergy(energy);
    }

    private void rebuildUI() {
        stage.clear();

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

        topArea.add(backButton).left().pad(10);
        topArea.add().expandX();

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

        // --- Actions Section ---
        Table actionsPanel = new Table();
        energyBar = new EnergyBar();
        energyBar.updateEnergy(
            gameController.getSession().getLocalPlayer().getEnergy()
        );


        Label actionCardsLabel = new Label("ACTION CARDS", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        actionCardTray = new CardTray();
        // TODO: Replace ShipCard with ActionCard interface implementation once created.
        //actionCardTray.addCard(new ShipCard(new ShipCardConfig(95f, 82f, "RECON", Assets.ships.ship2h)));
        //actionCardTray.addCard(new ShipCard(new ShipCardConfig(95f, 82f, "SALVO", Assets.ships.ship3h)));
        //actionCardTray.addCard(new ShipCard(new ShipCardConfig(95f, 82f, "AIRSTRIKE", Assets.ships.ship5h)));
        TextureRegion iconA = Assets.ships.shipPatrol2h;
        TextureRegion iconB = Assets.ships.shipPatrol2h;
        TextureRegion iconC = Assets.ships.shipPatrol2h;
        TextureRegion iconD = Assets.ships.shipPatrol2h;
        TextureRegion iconE = Assets.ships.shipPatrol2h;

        ActionCardPresentation m1 = new DoubleShotCardPresentation(iconA);
        ActionCardPresentation m2 = new ShieldCardPresentation(iconB);
        ActionCardPresentation m3 = new ParryCardPresentation(iconC);
        ActionCardPresentation m4 = new EraseCardPresentation(iconD);
        ActionCardPresentation m5 = new ScanCardPresentation(iconE);

        GameConfig.ActionCardConfig cfg1 = new GameConfig.ActionCardConfig(95f, 82f, true, Theme.BLUE, "DOUBLE SHOT");
        GameConfig.ActionCardConfig cfg2 = new GameConfig.ActionCardConfig(95f, 82f, true, Theme.BLUE, "SHIELD");
        GameConfig.ActionCardConfig cfg3 = new GameConfig.ActionCardConfig(95f, 82f, true, Theme.BLUE, "PARRY");
        GameConfig.ActionCardConfig cfg4 = new GameConfig.ActionCardConfig(95f, 82f, true, Theme.BLUE, "ERASE");
        GameConfig.ActionCardConfig cfg5 = new GameConfig.ActionCardConfig(95f, 82f, true, Theme.BLUE, "SCAN");


        actionCardTray.addCard(bindCard("DOUBLE SHOT",   m1, new DoubleShotCard()));
        actionCardTray.addCard(bindCard("SHIELD",        m2, new ShieldCard()));
        actionCardTray.addCard(bindCard("PARRY",         m3, new ParryCard()));
        actionCardTray.addCard(bindCard("ERASE",         m4, new EraseCard()));
        actionCardTray.addCard(bindCard("SCAN",          m5, new ScanCard()));

        GameButton fireButton = new GameButton("FIRE", ButtonConfig.primary(contentWidth, 72f), () -> {
            System.out.println("Executing Action Strategy...");
        });

        actionsPanel.add(energyBar).left().padBottom(6f).row();
        actionsPanel.add(actionCardsLabel).left().padBottom(12f).row();
        actionsPanel.add(actionCardTray).growX().height(95f).padBottom(12f).row();
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
        updateActionCardAvailability();
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
                if (uiCard.isDisabled()) return;

                // Play the REAL action card
                gameController.playActionCard(modelCard);

                updateEnergyFromGame();
                updateActionCardAvailability();
            }
        });

        actionCards.add(uiCard);
        return uiCard;
    }
    private void updateActionCardAvailability() {
        boolean myTurn = gameController.isLocalPlayerTurn();
        int energy = gameController.getSession().getLocalPlayer().getEnergy();

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
