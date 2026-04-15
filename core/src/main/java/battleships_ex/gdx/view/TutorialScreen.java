package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.Theme;

public class TutorialScreen extends ScreenAdapter {
    private final MyGame game;
    private final Screen previousScreen;
    private Stage stage;
    private final Array<Texture> screenshots = new Array<>();
    private int currentIndex = 0;

    private Image currentImage;
    private Label paginationLabel;

    public TutorialScreen(MyGame game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        loadScreenshots();

        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(Theme.blackPanel);
        stage.addActor(root);

        // ---- HEADER ----
        Table topArea = new Table();
        topArea.setBackground(Theme.darkBluePanel);
        topArea.pad(10);

        topArea.add(new GameButton("BACK", ButtonConfig.secondary(80, 44),
            () -> game.setScreen(previousScreen))).left();

        topArea.add(new Label("TUTORIAL", new Label.LabelStyle(Theme.fontLarge, Theme.WHITE)))
            .expandX().center();

        topArea.add().width(120); // Spacer to balance header

        // ---- MAIN CONTENT (Screenshot) ----
        Table content = new Table();
        content.pad(10);
        currentImage = new Image();
        currentImage.setScaling(Scaling.fit);
        updateScreenshot();
        content.add(currentImage).grow().center();

        // ---- FOOTER (Pagination) ----
        Table footer = new Table();
        footer.setBackground(Theme.darkBluePanel);
        footer.pad(10);

        GameButton prevBtn = new GameButton("<", ButtonConfig.secondary(60, 60), this::previousPage);
        GameButton nextBtn = new GameButton(">", ButtonConfig.secondary(60, 60), this::nextPage);

        paginationLabel = new Label("", new Label.LabelStyle(Theme.fontMedium, Theme.WHITE));
        updatePaginationLabel();

        footer.add(prevBtn).padRight(20);
        footer.add(paginationLabel).expandX().center();
        footer.add(nextBtn).padLeft(20);

        root.add(topArea).growX().height(70).row();
        root.add(content).grow().row();
        root.add(footer).growX().height(80);
    }

    private void loadScreenshots() {
        int i = 1;
        while (true) {
            String path = "tutorial-screenshots/tutorial_img_" + i + ".png";
            if (Gdx.files.internal(path).exists()) {
                screenshots.add(new Texture(Gdx.files.internal(path)));
                i++;
            } else {
                break;
            }
        }

        // If no screenshots found, maybe try without the directory prefix if it was flat
        // but based on list_directory it is in assets/tutorial-screenshots/
    }

    private void updateScreenshot() {
        if (screenshots.size > 0) {
            currentImage.setDrawable(new TextureRegionDrawable(screenshots.get(currentIndex)));
        }
    }

    private void updatePaginationLabel() {
        if (screenshots.size > 0) {
            paginationLabel.setText((currentIndex + 1) + " / " + screenshots.size);
        } else {
            paginationLabel.setText("0 / 0");
        }
    }

    private void nextPage() {
        if (currentIndex < screenshots.size - 1) {
            currentIndex++;
            updateScreenshot();
            updatePaginationLabel();
        }
    }

    private void previousPage() {
        if (currentIndex > 0) {
            currentIndex--;
            updateScreenshot();
            updatePaginationLabel();
        }
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
    public void hide() {
        // Typically we want to dispose textures when screen is hidden if they are not shared
        dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        for (Texture tex : screenshots) {
            tex.dispose();
        }
        screenshots.clear();
    }
}
