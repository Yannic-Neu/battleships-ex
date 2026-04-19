package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import battleships_ex.gdx.state.GameStateManager;
import battleships_ex.gdx.controller.LobbyController;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.SessionManager;
import battleships_ex.gdx.ui.ConfirmationDialog;
import battleships_ex.gdx.ui.GameButton;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import battleships_ex.gdx.data.Assets;

public class MenuScreen extends ScreenAdapter {
    private final MyGame game;
    private Stage stage;

    public MenuScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT));

        Image background = new Image(new TextureRegionDrawable(Assets.oceanBackground));
        background.setFillParent(true);
        stage.addActor(background);

        Gdx.input.setInputProcessor(stage);

        Image shipWithSight = new Image(new TextureRegionDrawable(Assets.shipWithSight));
        Image logo = new Image(new TextureRegionDrawable(Assets.logo));

        ButtonConfig primaryButton = ButtonConfig.primary(320f, 72f);
        ButtonConfig secondaryButton = ButtonConfig.secondary(320f, 72f);
        ButtonConfig navButton = ButtonConfig.secondary(130f, 44f);

        GameButton enterLobbyButton = new GameButton("MULTIPLAYER", primaryButton, () ->
            game.setScreen(new EnterLobbyScreen(game)));

        GameButton singlePlayerButton = new GameButton("SINGLEPLAYER", secondaryButton, () -> {
            Player localPlayer = new Player("P1", "Player 1");

            game.getGameController().initSinglePlayerSession(localPlayer);

            LobbyController lobbyController = new LobbyController(game.getLobbyDataSource());
            GameStateManager.init(game.getGameController(), lobbyController, localPlayer);

            Player botPLayer = game.getGameController().getRemotePlayer();
            GameStateManager.getInstance().forceSinglePlayerPlacement(botPLayer);

            game.setScreen(new PlacementScreen(game));
        });

        GameButton tutorialButton = new GameButton("TUTORIAL", secondaryButton, () ->
            game.setScreen(new TutorialScreen(game, this)));

        GameButton settingsButton = new GameButton("SETTINGS", navButton, () ->
            game.setScreen(new SettingsScreen(game, this)));

        GameButton profileButton = new GameButton("PROFILE", navButton, () ->
            game.setScreen(new ProfileScreen(game)));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Top nav bar
        Table topBar = new Table();
        topBar.add(profileButton).left().padLeft(20).expandX();
        topBar.add(settingsButton).right().padRight(20);

        // Logo area
        Table logoArea = new Table();
        logoArea.add(logo).center().row();
        logoArea.add(shipWithSight).center();

        // Button stack
        Table buttonStack = new Table();
        buttonStack.add(enterLobbyButton).padBottom(8f).row();
        buttonStack.add(singlePlayerButton).padBottom(8f).row();
        buttonStack.add(tutorialButton).padBottom(8f).row();

        // Root layout
        root.defaults().expandX().fillX();
        root.add(topBar).height(Value.percentHeight(0.09f, root)).row();
        root.add(logoArea).expandY().fillY().row();
        root.add(buttonStack).center().padBottom(32f).row();

        // Check for rejoinable session
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
