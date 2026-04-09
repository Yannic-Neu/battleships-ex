package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import battleships_ex.gdx.config.board.BoardConfig;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.config.board.ShipType;

public class BoardActor extends Actor {

    /** Semi-transparent yellow for the opponent's aim preview. */
    private static final Color PREVIEW_COLOR = new Color(1f, 1f, 0f, 0.35f); // Yellow for valid floating/aiming
    private static final Color ERROR_COLOR   = new Color(1f, 0f, 0f, 0.5f);  // Red for invalid floating
    private static final Color PLACED_COLOR  = new Color(0.5f, 0.5f, 0.5f, 1f); // Gray for placed ships

    private final BoardConfig config;
    private final ShapeRenderer renderer = new ShapeRenderer();

    /** The cell the opponent is currently aiming at, or null if none. */
    private Coordinate previewCell;

    /** Elapsed time for blink animation. */
    private float blinkTimer;

    // View-specific visual state
    private final List<PlacedShipVisual> placedShips = new ArrayList<>();
    private FloatingShipVisual floatingShip;

    public BoardActor(BoardConfig config) {
        this.config = config;
        setSize(config.size, config.size);
    }

    /** Helper class for View-only placed ship rendering */
    public static class PlacedShipVisual {
        public ShipType type;
        public Coordinate start;
        public Orientation orientation;
        public PlacedShipVisual(ShipType t, Coordinate s, Orientation o) { type = t; start = s; orientation = o; }
    }

    /** Helper class for View-only floating ship rendering */
    public static class FloatingShipVisual {
        public ShipType type;
        public Coordinate start;
        public Orientation orientation;
        public boolean isLegalVisual = true;
    }

    public void addPlacedShip(ShipType type, Coordinate start, Orientation orientation) {
        placedShips.add(new PlacedShipVisual(type, start, orientation));
    }

    public void setFloatingShip(FloatingShipVisual floating) {
        this.floatingShip = floating;
    }

    public void clearFloatingShip() {
        this.floatingShip = null;
    }

    public FloatingShipVisual getFloatingShip() {
        return this.floatingShip;
    }

    /** Converts a local click/drop coordinate to a Grid Coordinate */
    public Coordinate pointToCoordinate(float localX, float localY) {
        float cellSize = getWidth() / config.gridSize;
        int col = (int) (localX / cellSize);
        // LibGDX Y is up, but standard grids are Y down. Adjust based on your Coordinate system.
        int row = config.gridSize - 1 - (int) (localY / cellSize);

        // Clamp to grid
        col = Math.max(0, Math.min(config.gridSize - 1, col));
        row = Math.max(0, Math.min(config.gridSize - 1, row));

        return new Coordinate(row, col);
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

        // 1. Draw Background
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(config.backgroundColor);
        renderer.rect(x, y, size, size);

        // 2. Draw Placed Ships
        renderer.setColor(PLACED_COLOR);
        for (PlacedShipVisual ship : placedShips) {
            drawShipRect(x, y, ship.start, ship.type.getLength(), ship.orientation, cell);
        }

        // 3. Draw Floating Ship
        if (floatingShip != null) {
            renderer.setColor(floatingShip.isLegalVisual ? PREVIEW_COLOR : ERROR_COLOR);
            drawShipRect(x, y, floatingShip.start, floatingShip.type.getLength(), floatingShip.orientation, cell);
        }

        // 4. Draw preview cell overlay (blinks between 0.2 and 0.5 alpha)
        if (previewCell != null
            && previewCell.getRow() >= 0 && previewCell.getRow() < config.gridSize
            && previewCell.getCol() >= 0 && previewCell.getCol() < config.gridSize) {

            float alpha = 0.2f + 0.3f * (0.5f + 0.5f * (float) Math.sin(blinkTimer * 4.0));
            renderer.setColor(new Color(1f, 1f, 0f, alpha));

            float cellX = x + previewCell.getCol() * cell;
            float cellY = y + (config.gridSize - 1 - previewCell.getRow()) * cell;
            renderer.rect(cellX, cellY, cell, cell);
        }
        renderer.end();

        // 5. Draw Grid Lines Over Top
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

    private void drawShipRect(float boardX, float boardY, Coordinate start, int length, Orientation orientation, float cellSize) {
        float drawX = boardX + (start.getCol() * cellSize);
        float drawY = boardY + ((config.gridSize - 1 - start.getRow()) * cellSize);

        float w = (orientation == Orientation.HORIZONTAL) ? (cellSize * length) : cellSize;
        float h = (orientation == Orientation.VERTICAL) ? (cellSize * length) : cellSize;

        if (orientation == Orientation.VERTICAL) {
            drawY -= (cellSize * (length - 1));
        }

        renderer.rect(drawX, drawY, w, h);
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
