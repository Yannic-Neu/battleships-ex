package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.data.Assets;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.Theme;

public class ProfileScreen extends ScreenAdapter {

    private final MyGame game;
    private Stage stage;

    private int currentPic = 0;
    private Image profileImage;
    private Label nameLabel;

    public ProfileScreen(MyGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(Theme.blackPanel);
        stage.addActor(root);

        // --- HEADER ---
        Table header = new Table();
        header.setBackground(Theme.darkBluePanel);
        header.pad(10);

        GameButton backButton = new GameButton("BACK", ButtonConfig.secondary(60, 40),
            () -> game.setScreen(new MenuScreen(game)));

        Label title = new Label("PROFILE",
            new Label.LabelStyle(Theme.fontLarge, Theme.WHITE));

        header.add(backButton).left();
        header.add(title).expandX().center();

        // --- PROFILE CONTENT ---
        Table content = new Table();
        content.setBackground(Theme.bluePanel);
        content.pad(20);

        profileImage = new Image(Assets.profileIcons[currentPic]);
        nameLabel = new Label("PLAYER", new Label.LabelStyle(Theme.fontMedium, Theme.WHITE));

        GameButton prev = new GameButton("<", ButtonConfig.secondary(40, 40), this::prevPic);
        GameButton next = new GameButton(">", ButtonConfig.secondary(40, 40), this::nextPic);

        GameButton changeName = new GameButton("CHANGE NAME", ButtonConfig.primary(180, 44),
            () -> System.out.println("Name change dialog TBD"));

        content.add(prev).padRight(10);
        content.add(profileImage).size(128, 128).pad(10);
        content.add(next).padLeft(10).row();

        content.add(nameLabel).padTop(10).row();
        content.add(changeName).padTop(15);

        // --- SAVE BUTTON ---
        GameButton save = new GameButton("SAVE", ButtonConfig.primary(240, 56),
            () -> game.setScreen(new MenuScreen(game)));

        root.add(header).growX().height(60).row();
        root.add(content).expand().center().row();
        root.add(save).pad(16).center();
    }

    private void prevPic() {
        currentPic = (currentPic - 1 + Assets.profileIcons.length) % Assets.profileIcons.length;
        profileImage.setDrawable(new Image(Assets.profileIcons[currentPic]).getDrawable());
    }

    private void nextPic() {
        currentPic = (currentPic + 1) % Assets.profileIcons.length;
        profileImage.setDrawable(new Image(Assets.profileIcons[currentPic]).getDrawable());
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
