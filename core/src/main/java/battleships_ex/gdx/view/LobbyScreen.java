package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.Theme;

public class LobbyScreen extends ScreenAdapter {

    private final MyGame game;
    private Stage stage;

    public LobbyScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(
            GameConfig.WORLD_WIDTH,
            GameConfig.WORLD_HEIGHT
        ));

        Gdx.input.setInputProcessor(stage);

        ButtonConfig primaryButton = ButtonConfig.primary(360f, 80f);
        ButtonConfig secondaryButton = ButtonConfig.secondary(260f, 80f);
        ButtonConfig navButton = ButtonConfig.secondary(80f, 44f);

        GameButton backButton = new GameButton("BACK", navButton, () -> {
            game.setScreen(new EnterLobbyScreen(game));
        });

        GameButton settingsButton = new GameButton("SETT", navButton, () -> {
            game.setScreen(new SettingsScreen(game, this));
        });

        GameButton lobbyCodeButton = new GameButton("", secondaryButton, () -> {
            System.out.println("lobbyCode section clicked");
        });

        GameButton startMatchButton = new GameButton("COMMENCE MISSION", primaryButton, () -> {
            game.setScreen(new PlacementScreen(game));
        });

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label missionPrep = new Label("MISSION PREPARATION", new Label.LabelStyle(Theme.fontLarge, Theme.WHITE));
        Label vs = new Label("VS", new Label.LabelStyle(Theme.fontMedium, Theme.GRAY));
        Label accessCode = new Label("SECTOR ACCESS CODE", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        Table topArea = new Table();
        Table middlePanel = new Table();
        Table bottomPanel = new Table();

        topArea.setBackground(Theme.bluePanel);
        topArea.add(backButton).left().padLeft(25);
        topArea.add(missionPrep).expand().center();
        topArea.add(settingsButton).right().padRight(25);

        middlePanel.defaults().expandX().center();
        middlePanel.add().expandY().row();
        middlePanel.add(vs).center().row();
        middlePanel.add().expandY().row();
        middlePanel.add(lobbyCodeButton).pad(10).center().row();
        middlePanel.add(accessCode).padBottom(20);

        bottomPanel.add(startMatchButton).center().row();
        root.defaults().expand().fillX();
        root.add(topArea).height(Value.percentHeight(0.1f, root)).row();
        root.add(middlePanel).height(Value.percentHeight(0.6f, root)).row();
        root.add(bottomPanel).height(Value.percentHeight(0.3f, root)).row();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.setScreen(new MenuScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
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
