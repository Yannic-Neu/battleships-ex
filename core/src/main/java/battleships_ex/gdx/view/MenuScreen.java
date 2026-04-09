package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.SessionManager;
import battleships_ex.gdx.ui.ConfirmationDialog;
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
        ButtonConfig navButton = ButtonConfig.secondary(80f, 44f);

        GameButton enterLobbyButton = new GameButton("MULTIPLAYER", primaryButton, () -> {
            game.setScreen(new EnterLobbyScreen(game));
        });

        GameButton singlePlayerButton = new GameButton("SINGLEPLAYER", secondaryButton, () -> {
            // Generate a default local player
            Player localPlayer = new Player("P1", "Player 1");

            // Initialize the single-player backend
            game.getGameController().initSinglePlayerSession(localPlayer);

            // Proceed directly to ship placement
            game.setScreen(new PlacementScreen(game));
        });

        GameButton tutorialButton = new GameButton("TUTORIAL", secondaryButton, () -> {
            System.out.println("Tutorial clicked");
        });

        GameButton settingsButton = new GameButton("SETTINGS", navButton, () -> {
            game.setScreen(new SettingsScreen(game, this));
        });

        GameButton profileButton = new GameButton("PROFILE", ButtonConfig.secondary(200, 50),
            () -> game.setScreen(new ProfileScreen(game)));

        Label hostOrJoin = new Label("HOST OR JOIN", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table topArea = new Table();
        Table middlePanel = new Table();

        topArea.setBackground(Theme.bluePanel);
        topArea.add().expandX();
        topArea.add(profileButton).left().padLeft(25);
        topArea.add(settingsButton).right().padRight(25);

        middlePanel.setBackground(Theme.blackPanel);

        middlePanel.add(enterLobbyButton).pad(10).center().row();
        middlePanel.add(singlePlayerButton).pad(10).center().row();
        middlePanel.add(hostOrJoin).center().row();
        middlePanel.add(tutorialButton).pad(40).center().row();

        root.defaults().expandX().fillX();
        root.add(topArea).height(Value.percentHeight(0.1f, root)).row();
        root.add(middlePanel).expandY().fillY().row();

        // Check for rejoinable session (Issue #29)
        checkForRejoinableSession();
    }

    /**
     * Checks if there is a persisted session that can be rejoined.
     * If so, shows a confirmation dialog to the player.
     */
    private void checkForRejoinableSession() {
        SessionManager sm = game.getSessionManager();
        if (!sm.hasActiveSession()) return;

        sm.tryRejoin(new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean canRejoin) {
                if (!canRejoin) return;

                // Show dialog on the UI thread
                com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                    new ConfirmationDialog(
                        "REJOIN GAME?",
                        "You have an active game session. Would you like to rejoin?",
                        "REJOIN",
                        "NEW GAME",
                        () -> {
                            // Rejoin: navigate to BattleScreen
                            // (BattleScreen will restore state via SessionManager)
                            game.setScreen(new BattleScreen(game));
                        }
                    ).show(stage);
                });
            }

            @Override
            public void onFailure(String error) {
                System.out.println("[MenuScreen] Rejoin check failed: " + error);
            }
        });
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
