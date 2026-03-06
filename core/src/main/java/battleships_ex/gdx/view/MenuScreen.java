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

public class MenuScreen extends ScreenAdapter {
    private final MyGame game;
    private Stage stage;

    private BitmapFont font;
    private Texture panelTexture;
    private Label hostOrJoin;

    public MenuScreen(MyGame game) { this.game = game; }

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

        panelTexture = new Texture(pixmap);
        pixmap.dispose();

        TextureRegionDrawable drawable =
            new TextureRegionDrawable(new TextureRegion(panelTexture));

        Drawable bluePanel = drawable.tint(new Color(0f, 0f, 0.3f, 1f));
        Drawable grayPanel = drawable.tint(new Color(0, 0, 0, 1f));


        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        font = new BitmapFont();
        font.getData().setScale(1.5f);
        hostOrJoin = new Label("HOST OR JOIN", new Label.LabelStyle(font, Color.GRAY));


        Table topArea = new Table();
        Table middlePanel = new Table();

        topArea.setBackground(bluePanel);

        middlePanel.add(hostOrJoin).expand().center();
        middlePanel.setBackground(grayPanel);

        root.defaults().expandX().fillX();

        root.add(topArea).height(Value.percentHeight(0.1f, root)).row();
        root.add(middlePanel).expandY().fillY().row();

    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f);




        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            game.setScreen(new EnterLobbyScreen(game));
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
        if (panelTexture != null) panelTexture.dispose();
    }
}
