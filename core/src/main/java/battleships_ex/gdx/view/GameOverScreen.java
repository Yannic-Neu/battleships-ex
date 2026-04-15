package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.state.GameStateManager;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.Theme;

public class GameOverScreen extends ScreenAdapter {

    private final MyGame game;
    private final boolean isVictory;
    private Stage stage;

    public GameOverScreen(MyGame game, String winnerName) {
        this.game = game;

        String localPlayerName = game.getGameController().getLocalPlayer().getName();
        this.isVictory = localPlayerName.equals(winnerName);
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(Theme.blackPanel);
        stage.addActor(root);

        Label resultLabel = new Label(isVictory ? "YOU WON" : "YOU LOST",
            new Label.LabelStyle(Theme.fontLarge, isVictory ? Theme.WHITE : com.badlogic.gdx.graphics.Color.RED));

        GameButton menuButton = new GameButton("RETURN TO MENU", ButtonConfig.primary(300f, 60f), () -> {
            game.getGameController().cleanup();
            GameStateManager.destroy();
            game.setScreen(new MenuScreen(game));
        });

        root.add(resultLabel).padBottom(60f).row();
        root.add(menuButton).width(300f).height(60f);
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
