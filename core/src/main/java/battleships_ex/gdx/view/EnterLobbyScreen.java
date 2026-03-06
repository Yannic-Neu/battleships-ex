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

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Label hostOperation = new Label("HOST OPERATION", new Label.LabelStyle(Theme.fontMedium, Theme.WHITE));
        Label commissionNewFleet = new Label(
            "Commission a new fleet and establish secure tactical links for your allies",
            new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        Label joinStrikeForce = new Label("JOIN STRIKE FORCE", new Label.LabelStyle(Theme.fontMedium, Theme.WHITE));
        Label enterCode = new Label(
            "Enter the 6-digit tactical encryption code to link with an existing fleet",
            new Label.LabelStyle(Theme.fontSmall, Theme.GRAY));

        commissionNewFleet.setWrap(true);
        enterCode.setWrap(true);

        Table topArea = new Table();
        Table hostingPanel = new Table();
        Table joinPanel = new Table();

        topArea.setBackground(Theme.bluePanel);
        hostingPanel.setBackground(Theme.darkBluePanel);
        joinPanel.setBackground(Theme.darkBluePanel);

        hostingPanel.add(hostOperation).center().padTop(20).row();
        hostingPanel.add(commissionNewFleet).width(Value.percentWidth(0.8f, hostingPanel)).center().padTop(10).row();
        hostingPanel.add().expandY().row();

        joinPanel.add(joinStrikeForce).center().padTop(20).row();
        joinPanel.add(enterCode).width(Value.percentWidth(0.8f, joinPanel)).center().padTop(10).row();
        joinPanel.add().expandY().row();

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
