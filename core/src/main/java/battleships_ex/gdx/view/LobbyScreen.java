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
import battleships_ex.gdx.ui.CardSelectionDialog;

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
    private boolean exModeEnabled = true;
    private GameButton exModeButton;
    private GameButton selectCardsButton;
    private Label exModeLabel;
    private Label selectedCardsLabel;
    private java.util.List<String> currentSelectedCards = new java.util.ArrayList<>();

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
                // Ensure 4 cards are selected if in EX mode
                if (exModeEnabled && currentSelectedCards.size() != 4) {
                    Gdx.app.postRunnable(() -> statusLabel.setText("Select 4 cards first!"));
                    return;
                }

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

        // EX Mode toggle (host only)
        exModeLabel = new Label("MODE: EX", new Label.LabelStyle(Theme.fontSmall, Theme.WHITE));
        exModeButton = new GameButton("EX MODE", ButtonConfig.secondary(160f, 50f), () -> {
            exModeEnabled = !exModeEnabled;
            String label = exModeEnabled ? "EX MODE" : "CLASSIC MODE";
            exModeButton.setText(label);
            exModeLabel.setText(exModeEnabled ? "MODE: EX" : "MODE: CLASSIC");
            selectCardsButton.setVisible(isHost && exModeEnabled);
            selectedCardsLabel.setVisible(exModeEnabled);

            game.getLobbyDataSource().setExMode(roomCode, exModeEnabled, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {}
                @Override
                public void onFailure(String error) {
                    Gdx.app.postRunnable(() -> statusLabel.setText("Error: " + error));
                }
            });
        });
        exModeButton.setVisible(isHost);

        selectCardsButton = new GameButton("SELECT CARDS", ButtonConfig.secondary(180f, 50f), () -> {
            new CardSelectionDialog(cardNames -> {
                game.getLobbyDataSource().setSelectedCards(roomCode, cardNames, new DataCallback<Void>() {
                    @Override public void onSuccess(Void result) {}
                    @Override public void onFailure(String error) {
                        Gdx.app.postRunnable(() -> statusLabel.setText("Error: " + error));
                    }
                });
            }).show(stage);
        });
        selectCardsButton.setVisible(isHost && exModeEnabled);

        selectedCardsLabel = new Label("CARDS: NONE", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));
        selectedCardsLabel.setVisible(exModeEnabled);

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
        middlePanel.add(accessCode).padBottom(10).row();
        middlePanel.add(selectedCardsLabel).padBottom(20);

        Table controlsRow = new Table();
        controlsRow.add(exModeButton).padRight(10);
        controlsRow.add(selectCardsButton);

        bottomPanel.add(isHost ? controlsRow : exModeLabel).center().padBottom(10).row();
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
                    exModeEnabled = snapshot.exModeEnabled;
                    currentSelectedCards = snapshot.selectedCardNames;

                    // Update UI
                    if (!isHost) {
                        exModeLabel.setText(exModeEnabled ? "MODE: EX" : "MODE: CLASSIC");
                    }

                    if (currentSelectedCards.isEmpty()) {
                        selectedCardsLabel.setText("CARDS: NONE");
                    } else {
                        selectedCardsLabel.setText("CARDS: " + String.join(", ", currentSelectedCards));
                    }
                    selectedCardsLabel.setVisible(exModeEnabled);

                    if ("ready".equals(snapshot.status)) {
                        // Transition to game
                        Player localPlayer = new Player(playerId, isHost ? "Host" : "Guest");
                        LobbyController lobbyController = new LobbyController(game.getLobbyDataSource());
                        GameStateManager.init(game.getGameController(), lobbyController, localPlayer);
                        GameStateManager.getInstance().setExModeEnabled(exModeEnabled);

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
                        lobby.setSelectedCards(currentSelectedCards);

                        lobbyController.setActiveLobby(lobby);
                        lobbyController.setLocalPlayer(localPlayer);
                        GameStateManager.getInstance().forceMultiplayerPlacement(isHost ? guestPlayer : hostPlayer);

                        // Important: Pass cards to GameController
                        game.getGameController().initSession(localPlayer, isHost ? guestPlayer : hostPlayer, roomCode, isHost, currentSelectedCards);

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
                        } else if (exModeEnabled && currentSelectedCards.size() != 4) {
                            statusLabel.setText("Select 4 cards!");
                            startMatchButton.setDisabled(true);
                        } else {
                            statusLabel.setText("Guest is READY!");
                            statusLabel.getStyle().fontColor = com.badlogic.gdx.graphics.Color.GREEN;

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
