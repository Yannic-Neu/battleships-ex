package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.data.Assets;
import battleships_ex.gdx.ui.BoardActor;
import battleships_ex.gdx.config.BoardConfig;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.ShipCard;
import battleships_ex.gdx.config.ShipCardConfig;
import battleships_ex.gdx.ui.Theme;

public class PlacementScreen extends ScreenAdapter {

    private final MyGame game;
    private Stage stage;
    private BoardActor boardActor;

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

        Table header = new Table();
        header.setBackground(Theme.darkBluePanel);

        Label title = new Label("PLACEMENT", new Label.LabelStyle(Theme.fontLarge, Theme.WHITE));
        GameButton randomizeButton = new GameButton(
            "RANDOMIZE",
            ButtonConfig.secondary(150f, 42f),
            () -> System.out.println("Randomize placement")
        );

        header.add(title).left().expandX().padLeft(16);
        header.add(randomizeButton).right().pad(10);

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

        Label dockingTitle = new Label("DOCKING STATION", new Label.LabelStyle(Theme.fontSmall, Theme.WHITE));

        ShipCard carrier = new ShipCard(new ShipCardConfig(95f, 82f, "CARRIER", Assets.ships.ship5h));
        ShipCard battleship = new ShipCard(new ShipCardConfig(95f, 82f, "BATTLESHIP", Assets.ships.ship4h));
        ShipCard destroyer = new ShipCard(new ShipCardConfig(95f, 82f, "DESTROYER", Assets.ships.ship3h));
        ShipCard submarine = new ShipCard(new ShipCardConfig(95f, 82f, "SUBMARINE", Assets.ships.ship3h));
        ShipCard patrol = new ShipCard(new ShipCardConfig(95f, 82f, "PATROL", Assets.ships.ship2h));

        Table shipRow = new Table();
        shipRow.defaults().padRight(10);

        shipRow.add(carrier);
        shipRow.add(battleship);
        shipRow.add(destroyer);
        shipRow.add(submarine);
        shipRow.add(patrol);

        ScrollPane shipScroller = new ScrollPane(shipRow);
        shipScroller.setFadeScrollBars(false);
        shipScroller.setScrollingDisabled(false, true);

        GameButton readyButton = new GameButton(
            "READY TO BATTLE",
            ButtonConfig.primary(300f, 56f),
            () -> System.out.println("Ready clicked")
        );

        root.defaults().growX();

        root.add(header).height(Value.percentHeight(0.08f, root)).row();
        root.add(boardSection).expandY().top().padTop(20).padLeft(20).padRight(20).row();

        Table dockingSection = new Table();
        dockingSection.setBackground(Theme.darkBluePanel);
        dockingSection.add(dockingTitle).left().pad(12).row();
        dockingSection.add(shipScroller).growX().height(95f).padLeft(12).padRight(12).padBottom(12).row();
        dockingSection.add(readyButton).center().padBottom(16);

        root.add(dockingSection).padTop(20).growX().bottom();
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
        if (boardActor != null) {
            boardActor.dispose();
        }
    }
}
