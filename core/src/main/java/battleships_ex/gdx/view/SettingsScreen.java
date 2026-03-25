package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.Theme;

public class SettingsScreen extends ScreenAdapter {
    private final MyGame game;
    private final Screen previousScreen;
    private Stage stage;

    public SettingsScreen(MyGame game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(Theme.blackPanel);
        stage.addActor(root);

        Label title = new Label("SETTINGS", new Label.LabelStyle(Theme.fontLarge, Theme.WHITE));
        Label placeholder = new Label("Settings implementation coming soon...", new Label.LabelStyle(Theme.fontMedium, Theme.GRAY));

        GameButton backButton = new GameButton("BACK", ButtonConfig.primary(200f, 60f), () -> {
            game.setScreen(previousScreen);
        });

        root.add(title).padBottom(40).row();
        root.add(placeholder).padBottom(40).row();
        root.add(backButton).size(200f, 60f);
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
