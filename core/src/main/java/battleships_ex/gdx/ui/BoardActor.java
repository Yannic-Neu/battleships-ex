package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

import battleships_ex.gdx.config.board.BoardConfig;

public class BoardActor extends Actor {

    private final BoardConfig config;
    private final ShapeRenderer renderer = new ShapeRenderer();

    public BoardActor(BoardConfig config) {
        this.config = config;
        setSize(config.size, config.size);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();

        renderer.setProjectionMatrix(getStage().getCamera().combined);

        float x = getX();
        float y = getY();
        float size = getWidth();
        float cell = size / config.gridSize;

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(config.backgroundColor);
        renderer.rect(x, y, size, size);
        renderer.end();

        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(config.lineColor);

        for (int i = 0; i <= config.gridSize; i++) {
            float p = i * cell;
            renderer.line(x + p, y, x + p, y + size);
            renderer.line(x, y + p, x + size, y + p);
        }

        renderer.end();

        batch.begin();
    }

    public float getPrefWidth() {
        return config.size;
    }

    public float getPrefHeight() {
        return config.size;
    }

    public void dispose() {
        renderer.dispose();
    }
}
