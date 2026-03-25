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
import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.LobbyDataSource;
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
    private boolean opponentJoined = false;

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
            if (opponentJoined) {
                game.getLobbyDataSource().removeLobbyListener(roomCode);
                game.setScreen(new PlacementScreen(game));
            }
        });

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label missionPrep = new Label("MISSION PREPARATION", new Label.LabelStyle(Theme.fontLarge, Theme.WHITE));
        Label vs = new Label("VS", new Label.LabelStyle(Theme.fontMedium, Theme.GRAY));
        Label accessCode = new Label("SECTOR ACCESS CODE", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        String waitingText = isHost ? "Waiting for opponent..." : "Connecting...";
        statusLabel = new Label(waitingText, new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        Table topArea = new Table();
        Table middlePanel = new Table();
        Table bottomPanel = new Table();

        GameButton backButton = new GameButton("BACK", ButtonConfig.secondary(100f, 50f), () -> {
            game.getLobbyDataSource().removeLobbyListener(roomCode);
            game.getLobbyDataSource().leaveLobby(roomCode, playerId, new DataCallback<Void>() {
                @Override
                public void onSuccess(Void result) {}
                @Override
                public void onFailure(String error) {}
            });
            game.setScreen(new EnterLobbyScreen(game));
        });

        topArea.setBackground(Theme.bluePanel);
        topArea.add(backButton).left().padLeft(10);
        topArea.add(missionPrep).expandX().center();

        middlePanel.defaults().expandX().center();
        middlePanel.add().expandY().row();
        middlePanel.add(statusLabel).center().row();
        middlePanel.add(vs).center().padTop(10).row();
        middlePanel.add().expandY().row();
        middlePanel.add(lobbyCodeButton).pad(10).center().row();
        middlePanel.add(accessCode).padBottom(20);

        bottomPanel.add(startMatchButton).center().row();
        root.defaults().expand().fillX();
        root.add(topArea).height(Value.percentHeight(0.1f, root)).row();
        root.add(middlePanel).height(Value.percentHeight(0.6f, root)).row();
        root.add(bottomPanel).height(Value.percentHeight(0.3f, root)).row();

        // Listen for lobby state changes
        game.getLobbyDataSource().addLobbyListener(roomCode, new DataCallback<LobbyDataSource.LobbySnapshot>() {
            @Override
            public void onSuccess(LobbyDataSource.LobbySnapshot snapshot) {
                Gdx.app.postRunnable(() -> {
                    if (snapshot.isFull()) {
                        opponentJoined = true;
                        statusLabel.setText("Opponent joined!");
                    } else {
                        opponentJoined = false;
                        statusLabel.setText("Waiting for opponent...");
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
