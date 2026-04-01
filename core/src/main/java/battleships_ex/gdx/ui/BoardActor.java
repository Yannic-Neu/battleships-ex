package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

import com.badlogic.gdx.graphics.Color;

import battleships_ex.gdx.config.board.BoardConfig;
import battleships_ex.gdx.model.board.Coordinate;

public class BoardActor extends Actor {

    /** Semi-transparent yellow for the opponent's aim preview. */
    private static final Color PREVIEW_COLOR = new Color(1f, 1f, 0f, 0.35f);

    private final BoardConfig config;
    private final ShapeRenderer renderer = new ShapeRenderer();

    /** The cell the opponent is currently aiming at, or null if none. */
    private Coordinate previewCell;

    /** Elapsed time for blink animation. */
    private float blinkTimer;

    public BoardActor(BoardConfig config) {
        this.config = config;
        setSize(config.size, config.size);
    }

    /**
     * Sets the preview cell to highlight (opponent's aim).
     *
     * @param coord the cell to highlight, or null to clear
     */
    public void setPreviewCell(Coordinate coord) {
        this.previewCell = coord;
    }

    /**
     * Clears the preview cell.
     */
    public void clearPreviewCell() {
        this.previewCell = null;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        blinkTimer += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();

        renderer.setProjectionMatrix(getStage().getCamera().combined);

        float x = getX();
        float y = getY();
        float size = getWidth();
        float cell = size / config.gridSize;

        // Background
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(config.backgroundColor);
        renderer.rect(x, y, size, size);

        // Preview cell overlay (blinks between 0.2 and 0.5 alpha)
        if (previewCell != null
            && previewCell.getRow() >= 0 && previewCell.getRow() < config.gridSize
            && previewCell.getCol() >= 0 && previewCell.getCol() < config.gridSize) {

            float alpha = 0.2f + 0.3f * (0.5f + 0.5f * (float) Math.sin(blinkTimer * 4.0));
            renderer.setColor(new Color(1f, 1f, 0f, alpha));

            // Board coordinates: row 0 = top, but libGDX y=0 is bottom
            float cellX = x + previewCell.getCol() * cell;
            float cellY = y + (config.gridSize - 1 - previewCell.getRow()) * cell;
            renderer.rect(cellX, cellY, cell, cell);
        }

        renderer.end();

        // Grid lines
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
