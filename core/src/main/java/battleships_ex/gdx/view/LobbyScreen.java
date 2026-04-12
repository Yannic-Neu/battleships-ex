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
import battleships_ex.gdx.controller.LobbyController;
import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.LobbyDataSource;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.state.GameStateManager;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.Theme;

public class LobbyScreen extends ScreenAdapter {

    private final MyGame game;
    private final String roomCode;
    private final String playerId;
    private final boolean isHost;
    private Stage stage;
    private Label statusLabel;
    private GameButton startMatchButton;
    private GameButton readyButton;
    private boolean opponentJoined = false;
    private boolean opponentReady = false;
    private boolean iAmReady = false;

    public LobbyScreen(MyGame game, String roomCode, String playerId, boolean isHost) {
        this.game = game;
        this.roomCode = roomCode;
        this.playerId = playerId;
        this.isHost = isHost;
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

        GameButton lobbyCodeButton = new GameButton(roomCode, secondaryButton, () -> {});

        startMatchButton = new GameButton("COMMENCE MISSION", primaryButton, () -> {
            if (opponentJoined && opponentReady) {
                game.getLobbyDataSource().setLobbyStatus(roomCode, "ready", new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // The status update to "ready" will trigger transition via listener
                    }
                    @Override
                    public void onFailure(String error) {
                        Gdx.app.postRunnable(() -> statusLabel.setText("Error: " + error));
                    }
                });
            }
        });
        startMatchButton.setVisible(isHost);
        startMatchButton.setDisabled(true);

        readyButton = new GameButton("READY", primaryButton, () -> {
            iAmReady = !iAmReady;
            readyButton.setText(iAmReady ? "WAITING FOR HOST" : "READY");
            game.getLobbyDataSource().setGuestReady(roomCode, iAmReady, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {}
                @Override
                public void onFailure(String error) {
                    Gdx.app.postRunnable(() -> statusLabel.setText("Error: " + error));
                }
            });
        });
        readyButton.setVisible(!isHost);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label missionPrep = new Label("PREPARATION", new Label.LabelStyle(Theme.fontLarge, Theme.WHITE));
        Label vs = new Label("VS", new Label.LabelStyle(Theme.fontMedium, Theme.GRAY));
        Label accessCode = new Label("SECTOR ACCESS CODE", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        String waitingText = isHost ? "Waiting for opponent..." : "Connecting...";
        statusLabel = new Label(waitingText, new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        Table topArea = new Table();
        Table middlePanel = new Table();
        Table bottomPanel = new Table();

        GameButton backButton = new GameButton("BACK", ButtonConfig.secondary(80f, 44f), () -> {
            game.getLobbyDataSource().removeLobbyListener(roomCode);
            game.getLobbyDataSource().leaveLobby(roomCode, playerId, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {}
                @Override
                public void onFailure(String error) {}
            });
            game.setScreen(new EnterLobbyScreen(game));
        });

        topArea.setBackground(Theme.darkBluePanel);
        topArea.add(backButton).padLeft(25);
        topArea.add(missionPrep).center().expandX();

        middlePanel.defaults().expandX().center();
        middlePanel.add().expandY().row();
        middlePanel.add(statusLabel).center().row();
        middlePanel.add(vs).center().padTop(10).row();
        middlePanel.add().expandY().row();
        middlePanel.add(lobbyCodeButton).pad(10).center().row();
        middlePanel.add(accessCode).padBottom(20);

        bottomPanel.add(isHost ? startMatchButton : readyButton).center().row();
        root.defaults().expand().fillX();
        root.add(topArea).height(Value.percentHeight(0.1f, root)).row();
        root.add(middlePanel).height(Value.percentHeight(0.6f, root)).row();
        root.add(bottomPanel).height(Value.percentHeight(0.3f, root)).row();

        // Listen for lobby state changes
        game.getLobbyDataSource().addLobbyListener(roomCode, new DataCallback<LobbyDataSource.LobbySnapshot>() {
            @Override
            public void onSuccess(LobbyDataSource.LobbySnapshot snapshot) {
                Gdx.app.postRunnable(() -> {
                    opponentJoined = snapshot.isFull();
                    opponentReady = snapshot.guestReady;

                    if ("ready".equals(snapshot.status)) {
                        // Ensure GameStateManager is initialized for both players
                        Player localPlayer = new Player(playerId, isHost ? "Host" : "Guest");
                        LobbyController lobbyController = new LobbyController(game.getLobbyDataSource());
                        GameStateManager.init(game.getGameController(), lobbyController, localPlayer);

                        // Inject the lobby state into the controller and manager to bypass LobbyState
                        battleships_ex.gdx.model.lobby.Lobby lobby = new battleships_ex.gdx.model.lobby.Lobby(System.currentTimeMillis(), roomCode);
                        Player hostPlayer = new Player(snapshot.hostPlayerId, snapshot.hostPlayerName);
                        Player guestPlayer = new Player(snapshot.guestPlayerId, snapshot.guestPlayerName);
                        if (isHost) {
                            lobby.addPlayer(localPlayer);
                            lobby.addPlayer(guestPlayer);
                        } else {
                            lobby.addPlayer(hostPlayer);
                            lobby.addPlayer(localPlayer);
                        }
                        lobbyController.setActiveLobby(lobby);
                        lobbyController.setLocalPlayer(localPlayer);
                        GameStateManager.getInstance().forceMultiplayerPlacement(isHost ? guestPlayer : hostPlayer);

                        game.getLobbyDataSource().removeLobbyListener(roomCode);
                        game.setScreen(new PlacementScreen(game));
                        return;
                    }

                    if (isHost) {
                        if (!opponentJoined) {
                            statusLabel.setText("Waiting for opponent...");
                            startMatchButton.setDisabled(true);
                        } else if (!opponentReady) {
                            statusLabel.setText("Waiting for guest to be ready...");
                            startMatchButton.setDisabled(true);
                        } else {
                            statusLabel.setText("Guest is READY!");
                            startMatchButton.setDisabled(false);
                        }
                    } else {
                        if (opponentJoined) {
                            statusLabel.setText("Joined lobby!");
                        } else {
                            statusLabel.setText("Connecting...");
                        }
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                Gdx.app.postRunnable(() -> statusLabel.setText("Error: " + error));
            }
        });
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
        game.getLobbyDataSource().removeLobbyListener(roomCode);
        stage.dispose();
    }
}
