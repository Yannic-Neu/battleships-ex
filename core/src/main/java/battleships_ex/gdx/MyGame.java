package battleships_ex.gdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import battleships_ex.gdx.data.Assets;
import battleships_ex.gdx.data.LobbyDataSource;
import battleships_ex.gdx.ui.Theme;
import battleships_ex.gdx.view.MenuScreen;

public class MyGame extends Game {
    public SpriteBatch batch;
    private final LobbyDataSource lobbyDataSource;

    /**
     * @param lobbyDataSource platform-specific lobby backend (Firebase on Android, stub on Desktop)
     */
    public MyGame(LobbyDataSource lobbyDataSource) {
        this.lobbyDataSource = lobbyDataSource;
    }

    public LobbyDataSource getLobbyDataSource() {
        return lobbyDataSource;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        Theme.init();
        Assets.load();

        setScreen(new MenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();

        Theme.dispose();
        Assets.dispose();

        super.dispose();
    }
}
