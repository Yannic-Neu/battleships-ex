package battleships_ex.gdx.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import battleships_ex.gdx.MyGame;
import battleships_ex.gdx.config.GameConfig;
import battleships_ex.gdx.config.ButtonConfig;
import battleships_ex.gdx.ui.GameButton;
import battleships_ex.gdx.ui.Theme;

public class SettingsScreen extends ScreenAdapter {
    private final MyGame game;
    private final Screen previousScreen;
    private Stage stage;

    // ---- NEW SETTINGS STATE ----
    private boolean vibration = true;
    private boolean hints = true;
    private boolean darkMode = false;

    private final String[] shipStyles = {"SPRITES", "RECTANGLES", "RETRO"};
    private int currentShipStyle = 0;

    public SettingsScreen(MyGame game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(Theme.blackPanel);
        stage.addActor(root);

        // ---- HEADER ----
        Table header = new Table();
        header.setBackground(Theme.darkBluePanel);
        header.pad(10);

        header.add(new GameButton("BACK", ButtonConfig.secondary(120, 50),
            () -> game.setScreen(previousScreen))).left();

        header.add(new Label("SETTINGS",
                new Label.LabelStyle(Theme.fontLarge, Theme.WHITE)))
            .expandX().center();

        // ---- SETTINGS LIST ----
        Table list = new Table();
        list.pad(20);

        list.add(settingRow("VIBRATION", () -> vibration = !vibration)).growX().padBottom(10).row();
        list.add(settingRow("HINTS", () -> hints = !hints)).growX().padBottom(10).row();
        list.add(settingRow("DARK MODE", () -> darkMode = !darkMode)).growX().padBottom(10).row();
        list.add(shipStyleSelector()).growX().padTop(20).row();

        // ---- FOOTER ----
        GameButton saveBtn = new GameButton("SAVE", ButtonConfig.primary(260, 56),
            () -> game.setScreen(previousScreen));

        root.add(header).growX().height(70).row();
        root.add(list).expand().top().growX().row();
        root.add(saveBtn).pad(20).center();
    }

    // ---- HELPER METHODS ----
    private Table settingRow(String label, Runnable toggle) {
        Table row = new Table();
        row.setBackground(Theme.darkBluePanel);
        row.pad(12);

        row.add(new Label(label,
                new Label.LabelStyle(Theme.fontMedium, Theme.WHITE)))
            .expandX().left();

        row.add(new GameButton("TOGGLE",
            ButtonConfig.secondary(120, 44),
            toggle)).right();

        return row;
    }

    private Table shipStyleSelector() {
        Table row = new Table();
        row.setBackground(Theme.bluePanel);
        row.pad(12);

        Label styleLabel = new Label(shipStyles[currentShipStyle],
            new Label.LabelStyle(Theme.fontMedium, Theme.WHITE));

        GameButton left = new GameButton("<", ButtonConfig.secondary(40, 40),
            () -> {
                currentShipStyle = (currentShipStyle + shipStyles.length - 1) % shipStyles.length;
                styleLabel.setText(shipStyles[currentShipStyle]); // IMPORTANT
            });

        GameButton right = new GameButton(">", ButtonConfig.secondary(40, 40),
            () -> {
                currentShipStyle = (currentShipStyle + 1) % shipStyles.length;
                styleLabel.setText(shipStyles[currentShipStyle]); // IMPORTANT
            });

        row.add(left).padRight(8);
        row.add(styleLabel).expandX().center();
        row.add(right).padLeft(8);

        return row;
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
