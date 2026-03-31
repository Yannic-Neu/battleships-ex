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

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.board.BoardConfig;
import battleships_ex.gdx.ui.ActionCard;
import battleships_ex.gdx.ui.ActionCardModel;
import battleships_ex.gdx.ui.DoubleShotCard;
import battleships_ex.gdx.ui.ShieldCard;
import battleships_ex.gdx.ui.ParryCard;
import battleships_ex.gdx.ui.EraseCard;
import battleships_ex.gdx.ui.ScanCard;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.config.ShipCardConfig;
import battleships_ex.gdx.data.Assets;
import battleships_ex.gdx.ui.BoardActor;
import battleships_ex.gdx.ui.CardTray;
import battleships_ex.gdx.ui.ConfirmationDialog;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.ShipCard;
import battleships_ex.gdx.ui.Theme;

import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.data.Assets;


public class BattleScreen extends ScreenAdapter {

    private enum ViewMode {
        OWN_FLEET, ENEMY_WATERS
    }

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

        rebuildUI();
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
        GameButton settingsButton = new GameButton("SETT", ButtonConfig.secondary(60f, 44f), () -> game.setScreen(new SettingsScreen(game, this)));

        topArea.add(backButton).left().pad(10);
        topArea.add().expandX();
        topArea.add(settingsButton).right().pad(10);

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
        Label actionCardsLabel = new Label("ACTION CARDS", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        actionCardTray = new CardTray();
        // TODO: Replace ShipCard with ActionCard interface implementation once created.
        //actionCardTray.addCard(new ShipCard(new ShipCardConfig(95f, 82f, "RECON", Assets.ships.ship2h)));
        //actionCardTray.addCard(new ShipCard(new ShipCardConfig(95f, 82f, "SALVO", Assets.ships.ship3h)));
        //actionCardTray.addCard(new ShipCard(new ShipCardConfig(95f, 82f, "AIRSTRIKE", Assets.ships.ship5h)));
        TextureRegion iconA = Assets.ships.ship2h;
        TextureRegion iconB = Assets.ships.ship3h;
        TextureRegion iconC = Assets.ships.ship4h;
        TextureRegion iconD = Assets.ships.ship5h;
        TextureRegion iconE = Assets.ships.ship2v;

        ActionCardModel m1 = new DoubleShotCard(iconA);
        ActionCardModel m2 = new ShieldCard(iconB);
        ActionCardModel m3 = new ParryCard(iconC);
        ActionCardModel m4 = new EraseCard(iconD);
        ActionCardModel m5 = new ScanCard(iconE);

        GameConfig.ActionCardConfig cfg1 = new GameConfig.ActionCardConfig(95f, 82f, Theme.BLUE, "DOUBLE SHOT");
        GameConfig.ActionCardConfig cfg2 = new GameConfig.ActionCardConfig(95f, 82f, Theme.BLUE, "SHIELD");
        GameConfig.ActionCardConfig cfg3 = new GameConfig.ActionCardConfig(95f, 82f, Theme.BLUE, "PARRY");
        GameConfig.ActionCardConfig cfg4 = new GameConfig.ActionCardConfig(95f, 82f, Theme.BLUE, "ERASE");
        GameConfig.ActionCardConfig cfg5 = new GameConfig.ActionCardConfig(95f, 82f, Theme.BLUE, "SCAN");

        ActionCard c1 = new ActionCard(cfg1); c1.bind(m1); actionCardTray.addCard(c1);
        ActionCard c2 = new ActionCard(cfg2); c2.bind(m2); actionCardTray.addCard(c2);
        ActionCard c3 = new ActionCard(cfg3); c3.bind(m3); actionCardTray.addCard(c3);
        ActionCard c4 = new ActionCard(cfg4); c4.bind(m4); actionCardTray.addCard(c4);
        ActionCard c5 = new ActionCard(cfg5); c5.bind(m5); actionCardTray.addCard(c5);

        GameButton fireButton = new GameButton("FIRE", ButtonConfig.primary(contentWidth, 72f), () -> {
            System.out.println("Executing Action Strategy...");
        });

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
}
