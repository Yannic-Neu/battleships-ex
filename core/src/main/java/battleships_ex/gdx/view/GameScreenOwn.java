package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.BoardConfig;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.config.ShipCardConfig;
import battleships_ex.gdx.data.Assets;
import battleships_ex.gdx.ui.BoardActor;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.ShipCard;
import battleships_ex.gdx.ui.Theme;

public class GameScreenOwn extends ScreenAdapter {

    private final MyGame game;
    private Stage stage;

    public GameScreenOwn(MyGame game) {
        this.game = game;
    }

    private BoardActor boardActor;

    @Override
    public void show() {
        stage = new Stage(new FitViewport(
            GameConfig.WORLD_WIDTH,
            GameConfig.WORLD_HEIGHT
        ));

        Gdx.input.setInputProcessor(stage);

        float sidePad = 16f;
        float contentWidth = Math.min(320f, GameConfig.WORLD_WIDTH - 2f * sidePad);

        ButtonConfig iconButtonCfg = ButtonConfig.secondary(44f, 44f);
        ButtonConfig tabActiveCfg = ButtonConfig.primary((contentWidth - 12f) / 2f, 44f);
        ButtonConfig tabInactiveCfg = ButtonConfig.secondary((contentWidth - 12f) / 2f, 44f);
        ButtonConfig fireButtonCfg = ButtonConfig.primary(contentWidth, 72f);

        //// placeholder/dummy
        GameButton menuButton = new GameButton("MN", iconButtonCfg, () -> { /// This should probably be a BACK button
            System.out.println("menu button clicked");
            game.setScreen(new LobbyScreen(game));
        });

        //// placeholder/dummy
        GameButton settingsButton = new GameButton("SETT", iconButtonCfg, () -> {
            System.out.println("settings button clicked");
        });

        GameButton fireButton = new GameButton("FIRE", fireButtonCfg, () -> {
            System.out.println("Fire button pressed");
        });

        GameButton enemyWatersButton = new GameButton("ENEMY WATERS", tabActiveCfg, () -> {
            System.out.println("enemy waters button pressed");
        });

        GameButton yourFleetButton = new GameButton("YOUR FLEET", tabInactiveCfg, () -> {
            System.out.println("your fleet button pressed");
            game.setScreen(new GameScreenOpp(game));
        });

        Label tacGrid = new Label("TACTICAL GRID 10 x 10", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));
        Label actionCards = new Label("ACTION CARDS", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        BoardConfig boardConfig = new BoardConfig(
            320f,
            10,
            new Color(0.05f, 0.10f, 0.20f, 1f),
            new Color(0.15f, 0.22f, 0.35f, 1f)
        );
        boardActor = new BoardActor(boardConfig);

        Table root = new Table();
        root.setFillParent(true);
        root.top().pad(sidePad);
        root.setBackground(Theme.blackPanel);
        stage.addActor(root);

        Table topArea = new Table();
        topArea.add(menuButton).left().pad(10);
        topArea.add().expandX();
        topArea.add(settingsButton).right().pad(10);

        Table switchInner = new Table();
        switchInner.setBackground(Theme.darkBluePanel);
        switchInner.pad(6f);
        switchInner.add(enemyWatersButton).width((contentWidth - 12f) / 2f).height(44f);
        switchInner.add(yourFleetButton).width((contentWidth - 12f) / 2f).height(44f);

        Table switchPanel = new Table();
        switchPanel.add(switchInner).width(contentWidth).height(56f).center();

        Table boardPanel = new Table();
        boardPanel.add(tacGrid).left().padTop(5).padLeft(10).padBottom(5).row();
        boardPanel.add(boardActor).size(boardConfig.size).center().row();

        Table actionsPanel = new Table();
        actionsPanel.add(actionCards).width(contentWidth).left().padBottom(12f).row();
        Table cardRow = new Table();
        cardRow.defaults().width((contentWidth - 20f) / 3f).height(96f).padRight(10f);

        ShipCard carrier = new ShipCard(new ShipCardConfig(95f, 82f, "CARRIER", Assets.ships.ship5h));
        ShipCard battleship = new ShipCard(new ShipCardConfig(95f, 82f, "BATTLESHIP", Assets.ships.ship4h));
        ShipCard destroyer = new ShipCard(new ShipCardConfig(95f, 82f, "DESTROYER", Assets.ships.ship3h));
        ShipCard submarine = new ShipCard(new ShipCardConfig(95f, 82f, "SUBMARINE", Assets.ships.ship3h));
        ShipCard patrol = new ShipCard(new ShipCardConfig(95f, 82f, "PATROL", Assets.ships.ship2h));

        cardRow.add(carrier);
        cardRow.add(battleship);
        cardRow.add(destroyer);
        cardRow.add(submarine);
        cardRow.add(patrol);

        ScrollPane cardScroller = new ScrollPane(cardRow);
        cardScroller.setFadeScrollBars(false);
        cardScroller.setScrollingDisabled(false, true);

        actionsPanel.add(actionCards).left().pad(12).row();
        actionsPanel.add(cardScroller).growX().height(95f).padLeft(12).padRight(12).padBottom(12).row();
        actionsPanel.add(fireButton).center().row();

        root.defaults().growX();
        root.add(topArea).height(48f).row();
        root.add(switchPanel).padTop(12f).row();
        root.add(boardPanel).expandY().padTop(10f).row();
        root.add(actionsPanel).padBottom(12f).row();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            game.setScreen(new LobbyScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.setScreen(new MenuScreen(game));
        }

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
