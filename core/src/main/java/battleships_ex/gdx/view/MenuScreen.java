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
import battleships_ex.gdx.ui.Theme;

public class MenuScreen extends ScreenAdapter {
    private final MyGame game;
    private Stage stage;

    public MenuScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(
            GameConfig.WORLD_WIDTH,
            GameConfig.WORLD_HEIGHT
        ));

        Gdx.input.setInputProcessor(stage);

        Label hostOrJoin = new Label("HOST OR JOIN", new Label.LabelStyle(Theme.fontMedium, Theme.GRAY));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table topArea = new Table();
        Table middlePanel = new Table();

        topArea.setBackground(Theme.bluePanel);
        middlePanel.setBackground(Theme.blackPanel);

        middlePanel.add(hostOrJoin).pad(20).row();

        root.defaults().expandX().fillX();
        root.add(topArea).height(Value.percentHeight(0.1f, root)).row();
        root.add(middlePanel).expandY().fillY().row();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);

        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            game.setScreen(new LobbyScreen(game));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            game.setScreen(new EnterLobbyScreen(game));
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
