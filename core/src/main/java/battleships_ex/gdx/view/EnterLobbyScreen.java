package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.MyGame;

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

        Pixmap pixmap = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        Texture panelTexture = new Texture(pixmap);
        pixmap.dispose();

        TextureRegionDrawable drawable =
            new TextureRegionDrawable(new TextureRegion(panelTexture));

        Drawable bluePanel = drawable.tint(new Color(0f, 0f, 0.3f, 1f));
        Drawable darkBluePanel = drawable.tint(new Color(0, 0, 0.3f, 0.4f));

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(1.2f);

        BitmapFont font = new BitmapFont();

        Label hostOperation = new Label("HOST OPERATION", new Label.LabelStyle(titleFont, Color.WHITE));
        Label commistionNewFleet = new Label(
            "Commission a new fleet and establish a secure tactical links for your allies", new Label.LabelStyle(font, Color.GRAY));
        Label joinStrikeForce = new Label("JOIN STRIKE FORCE", new Label.LabelStyle(titleFont, Color.WHITE));
        Label enterCode = new Label("Enter the 6-digit tactical encryption code to link with an existing fleet", new Label.LabelStyle(font, Color.GRAY));

        commistionNewFleet.setWrap(true);
        enterCode.setWrap(true);

        Table topArea = new Table();
        Table hostingPanel = new Table();
        Table joinPanel = new Table();

        topArea.setBackground(bluePanel);

        hostingPanel.add(hostOperation).center().padTop(20).row();
        hostingPanel.add(commistionNewFleet).width(Value.percentWidth(0.8f, hostingPanel)).center().padTop(10).row();
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

        stage.act(delta);
        stage.draw();

        stage.getBatch().begin();
        stage.getBatch().end();
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
