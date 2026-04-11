package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.data.DataCallback;
import battleships_ex.gdx.data.LobbyDataSource;
import battleships_ex.gdx.data.RoomCodeGenerator;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.Theme;

public class EnterLobbyScreen extends ScreenAdapter {

    private final MyGame game;
    private Stage stage;
    private String generatedCode;
    private Label statusLabel;

    public EnterLobbyScreen(MyGame game) {
        this.game = game;
        this.generatedCode = RoomCodeGenerator.generate();
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
        ButtonConfig navButton = ButtonConfig.secondary(80f, 44f);

        GameButton backButton = new GameButton("BACK", navButton, () -> {
            game.setScreen(new MenuScreen(game));
        });


        // Generated room code display
        GameButton generatedCodeButton = new GameButton(generatedCode, secondaryButton, () -> {});

        GameButton createLobbyButton = new GameButton("CREATE ROOM", primaryButton, () -> {
            createRoom();
        });

        // Text field for entering a room code
        TextField.TextFieldStyle tfStyle = new TextField.TextFieldStyle();
        tfStyle.font = Theme.fontMedium;
        tfStyle.fontColor = Theme.WHITE;
        tfStyle.background = Theme.darkBluePanel;

        TextField codeInput = new TextField("", tfStyle);
        codeInput.setMaxLength(6);
        codeInput.setMessageText("ENTER CODE");
        codeInput.setAlignment(1); // center

        GameButton joinLobbyButton = new GameButton("JOIN MATCH", primaryButton, () -> {
            String code = codeInput.getText().trim().toUpperCase();
            if (code.length() == 6) {
                joinRoom(code);
            }
        });

        Label hostOperation = new Label("HOST OPERATION", new Label.LabelStyle(Theme.fontMedium, Theme.WHITE));
        Label commissionNewFleet = new Label(
            "Commission a new fleet and establish secure tactical links for your allies",
            new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        Label joinStrikeForce = new Label("JOIN STRIKE FORCE", new Label.LabelStyle(Theme.fontMedium, Theme.WHITE));
        Label enterCode = new Label(
            "Enter the 6-digit tactical encryption code to link with an existing fleet",
            new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        statusLabel = new Label("", new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        commissionNewFleet.setWrap(true);
        enterCode.setWrap(true);

        Table topArea = new Table();
        Table hostingPanel = new Table();
        Table joinPanel = new Table();

        topArea.setBackground(Theme.bluePanel);
        topArea.add(backButton).left().padLeft(25);
        topArea.add().expandX();

        hostingPanel.setBackground(Theme.blackPanel);
        joinPanel.setBackground(Theme.blackPanel);

        hostingPanel.add(hostOperation).center().padTop(20).row();
        hostingPanel.add(commissionNewFleet).width(Value.percentWidth(0.8f, hostingPanel)).center().padTop(10).row();
        hostingPanel.add(generatedCodeButton).pad(10).center().row();
        hostingPanel.add(createLobbyButton).pad(10).center().row();

        joinPanel.add(joinStrikeForce).center().padTop(20).row();
        joinPanel.add(enterCode).width(Value.percentWidth(0.8f, joinPanel)).center().padTop(10).row();
        joinPanel.add(codeInput).width(260f).height(80f).pad(10).center().row();
        joinPanel.add(joinLobbyButton).pad(10).center().row();
        joinPanel.add(statusLabel).center().padTop(5).row();

        root.defaults().expand().fillX();
        root.add(topArea).height(Value.percentHeight(0.1f, root)).row();
        root.add(joinPanel).height(Value.percentHeight(0.5f, root)).row();
        root.add(hostingPanel).height(Value.percentHeight(0.4f, root)).row();
    }

    private void createRoom() {
        LobbyDataSource ds = game.getLobbyDataSource();
        String playerId = game.getPlayerId();

        ds.createLobby(generatedCode, playerId, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Gdx.app.postRunnable(() ->
                    game.setScreen(new LobbyScreen(game, generatedCode, playerId, true))
                );
            }

            @Override
            public void onFailure(String error) {
                Gdx.app.postRunnable(() ->
                    statusLabel.setText("Error: " + error)
                );
            }
        });
    }

    private void joinRoom(String code) {
        LobbyDataSource ds = game.getLobbyDataSource();

        ds.lobbyExists(code, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (!exists) {
                    Gdx.app.postRunnable(() -> statusLabel.setText("Room not found"));
                    return;
                }

                String playerId = game.getPlayerId();
                ds.joinLobby(code, playerId, new DataCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Gdx.app.postRunnable(() ->
                            game.setScreen(new LobbyScreen(game, code, playerId, false))
                        );
                    }

                    @Override
                    public void onFailure(String error) {
                        Gdx.app.postRunnable(() ->
                            statusLabel.setText("Error: " + error)
                        );
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
        stage.dispose();
    }
}
