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

public class LobbyScreen extends ScreenAdapter {

    private final MyGame game;
    private Stage stage;

    public LobbyScreen(MyGame game) {
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

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(2.0f);

        BitmapFont vsFont = new BitmapFont();
        vsFont.getData().setScale(1.5f);

        BitmapFont font = new BitmapFont();

        Label missionPrep = new Label("MISSION PREPARATION", new Label.LabelStyle(titleFont, Color.WHITE));
        Label vs = new Label("VS", new Label.LabelStyle(vsFont, Color.GRAY));
        Label accessCode = new Label("SECTOR ACCESS CODE", new Label.LabelStyle(font, Color.GRAY));

        Table topArea = new Table();
        Table middlePanel = new Table();
        Table bottomPanel = new Table();

        topArea.setBackground(bluePanel);
        topArea.add(missionPrep).expand().center();

        middlePanel.defaults().expandX().center();

        middlePanel.add().expandY().row();
        middlePanel.add(vs).center().row();
        middlePanel.add().expandY().row();
        middlePanel.add(accessCode).padBottom(20);

        root.defaults().expand().fillX();

        root.add(topArea).height(Value.percentHeight(0.1f, root)).row();
        root.add(middlePanel).height(Value.percentHeight(0.6f, root)).row();
        root.add(bottomPanel).height(Value.percentHeight(0.3f, root)).row();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);


        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.setScreen(new MenuScreen(game));
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
