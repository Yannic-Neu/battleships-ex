package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
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

        ButtonConfig primaryButton = ButtonConfig.primary(360f, 80f);
        ButtonConfig secondaryButton = ButtonConfig.secondary(360f, 80f);

        GameButton enterLobbyButton = new GameButton("MULTIPLAYER", primaryButton, () -> {
            game.setScreen(new EnterLobbyScreen(game));
        });

        GameButton singlePlayerButton = new GameButton("SINGLEPLAYER", secondaryButton, () -> {
            System.out.println("Singleplayer clicked");
        });

        GameButton tutorialButton = new GameButton("TUTORIAL", secondaryButton, () -> {
            System.out.println("Tutorial clicked");
        });

        Label hostOrJoin = new Label("HOST OR JOIN", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table topArea = new Table();
        Table middlePanel = new Table();

        topArea.setBackground(Theme.bluePanel);

        middlePanel.setBackground(Theme.blackPanel);

        middlePanel.add(enterLobbyButton).pad(10).center().row();
        middlePanel.add(singlePlayerButton).pad(10).center().row();
        middlePanel.add(hostOrJoin).center().row();
        middlePanel.add(tutorialButton).pad(40).center().row();

        root.defaults().expandX().fillX();
        root.add(topArea).height(Value.percentHeight(0.1f, root)).row();
        root.add(middlePanel).expandY().fillY().row();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);
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
