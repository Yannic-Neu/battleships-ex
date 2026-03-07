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
import battleships_ex.gdx.ui.ButtonConfig;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.Theme;

public class EnterLobbyScreen extends ScreenAdapter {

    private final MyGame game;
    private Stage stage;

    public EnterLobbyScreen(MyGame game) {
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
        ButtonConfig secondaryButton = ButtonConfig.secondary(260f, 80f);

        //// placeholder/dummy
        GameButton generatedCodeButton = new GameButton("", secondaryButton, () -> {
            System.out.println("generateCode section clicked");
        });

        GameButton createLobbyButton = new GameButton("CREATE ROOM", primaryButton, () -> {
            game.setScreen(new LobbyScreen(game));
        });

        //// placeholder/dummy
        GameButton enterCodeButton = new GameButton("ENTER CODE", secondaryButton, () -> {
            System.out.println("enterCode section clicked");
        });

        GameButton joinLobbyButton = new GameButton("JOIN MATCH", primaryButton, () -> {
            game.setScreen(new LobbyScreen(game));
        });

        Label hostOperation = new Label("HOST OPERATION", new Label.LabelStyle(Theme.fontMedium, Theme.WHITE));
        Label commissionNewFleet = new Label(
            "Commission a new fleet and establish secure tactical links for your allies",
            new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        Label joinStrikeForce = new Label("JOIN STRIKE FORCE", new Label.LabelStyle(Theme.fontMedium, Theme.WHITE));
        Label enterCode = new Label(
            "Enter the 6-digit tactical encryption code to link with an existing fleet",
            new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        commissionNewFleet.setWrap(true);
        enterCode.setWrap(true);

        Table topArea = new Table();
        Table hostingPanel = new Table();
        Table joinPanel = new Table();

        topArea.setBackground(Theme.bluePanel);
        hostingPanel.setBackground(Theme.blackPanel);
        joinPanel.setBackground(Theme.blackPanel);

        hostingPanel.add(hostOperation).center().padTop(20).row();
        hostingPanel.add(commissionNewFleet).width(Value.percentWidth(0.8f, hostingPanel)).center().padTop(10).row();
        hostingPanel.add(generatedCodeButton).pad(10).center().row();
        hostingPanel.add(createLobbyButton).pad(10).center().row();

        joinPanel.add(joinStrikeForce).center().padTop(20).row();
        joinPanel.add(enterCode).width(Value.percentWidth(0.8f, joinPanel)).center().padTop(10).row();
        joinPanel.add(enterCodeButton).pad(10).center().row();
        joinPanel.add(joinLobbyButton).pad(10).center().row();

        root.defaults().expand().fillX();
        root.add(topArea).height(Value.percentHeight(0.1f, root)).row();
        root.add(hostingPanel).height(Value.percentHeight(0.4f, root)).row();
        root.add(joinPanel).height(Value.percentHeight(0.5f, root)).row();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            game.setScreen(new LobbyScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.setScreen(new MenuScreen(game));
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
