package battleships_ex.gdx.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import battleships_ex.gdx.config.board.BoardConfig;
import battleships_ex.gdx.model.board.Board;
import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.config.board.ShipType;

public class BoardActor extends Actor {

    private static final Color PREVIEW_COLOR = new Color(1f, 1f, 0f, 0.35f);
    private static final Color ERROR_COLOR   = new Color(1f, 0f, 0f, 0.5f);
    private static final Color PLACED_COLOR  = new Color(0.5f, 0.5f, 0.5f, 1f);
    private static final Color MINE_COLOR    = new Color(0.2f, 0.2f, 0.2f, 1f);
    private static final Color SCANNED_BG    = new Color(0f, 0.2f, 0.4f, 0.8f);

    private final BoardConfig config;
    private final ShapeRenderer renderer = new ShapeRenderer();

    private final List<Coordinate> previewCells = new ArrayList<>();
    private float blinkTimer;

    private final List<PlacedShipVisual> placedShips = new ArrayList<>();
    private FloatingShipVisual floatingShip;
    private final List<Coordinate> misses = new ArrayList<>();
    private final List<Coordinate> hits = new ArrayList<>();
    private final Set<Coordinate> mines = new HashSet<>();
    private final Set<Coordinate> scannedTiles = new HashSet<>();
    private Board boardModel;

    public void markMiss(Coordinate coord) { misses.add(coord); }
    public void markHit(Coordinate coord) { hits.add(coord); }
    public void setMines(Set<Coordinate> mineCoords) {
        this.mines.clear();
        if (mineCoords != null) this.mines.addAll(mineCoords);
    }
    public void setScannedTiles(Set<Coordinate> scanned) {
        this.scannedTiles.clear();
        if (scanned != null) this.scannedTiles.addAll(scanned);
    }
    public void setBoardModel(Board board) { this.boardModel = board; }

    public BoardActor(BoardConfig config) {
        this.config = config;
        setSize(config.size, config.size);
    }

    public static class PlacedShipVisual {
        public ShipType type;
        public Coordinate start;
        public Orientation orientation;
        public PlacedShipVisual(ShipType t, Coordinate s, Orientation o) { type = t; start = s; orientation = o; }
    }

    public static class FloatingShipVisual {
        public ShipType type;
        public Coordinate start;
        public Orientation orientation;
        public boolean isLegalVisual = true;
    }

    public void addPlacedShip(ShipType type, Coordinate start, Orientation orientation) {
        placedShips.add(new PlacedShipVisual(type, start, orientation));
    }

    public void removePlacedShip(ShipType type) {
        placedShips.removeIf(ship -> ship.type == type);
    }

    public void setFloatingShip(FloatingShipVisual floating) { this.floatingShip = floating; }
    public void clearFloatingShip() { this.floatingShip = null; }
    public FloatingShipVisual getFloatingShip() { return this.floatingShip; }

    public Coordinate pointToCoordinate(float localX, float localY) {
        float cellSize = getWidth() / config.gridSize;
        int col = (int) (localX / cellSize);
        int row = config.gridSize - 1 - (int) (localY / cellSize);
        col = Math.max(0, Math.min(config.gridSize - 1, col));
        row = Math.max(0, Math.min(config.gridSize - 1, row));
        return new Coordinate(row, col);
    }

    public void setPreviewCell(Coordinate coord) {
        this.previewCells.clear();
        if (coord != null) this.previewCells.add(coord);
    }

    public void setPreviewCells(List<Coordinate> coords) {
        this.previewCells.clear();
        if (coords != null) this.previewCells.addAll(coords);
    }

    public void clearPreviewCell() { this.previewCells.clear(); }

    @Override
    public void act(float delta) {
        super.act(delta);
        blinkTimer += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        renderer.setProjectionMatrix(getStage().getCamera().combined);
        float x = getX(); float y = getY();
        float size = getWidth(); float cell = size / config.gridSize;

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(config.backgroundColor);
        renderer.rect(x, y, size, size);

        renderer.setColor(SCANNED_BG);
        for (Coordinate scanned : scannedTiles) {
            float cellX = x + scanned.getCol() * cell;
            float cellY = y + (config.gridSize - 1 - scanned.getRow()) * cell;
            renderer.rect(cellX, cellY, cell, cell);
        }

        renderer.setColor(PLACED_COLOR);
        for (PlacedShipVisual ship : placedShips) {
            drawShipRect(x, y, ship.start, ship.type.getLength(), ship.orientation, cell);
        }

        if (floatingShip != null) {
            renderer.setColor(floatingShip.isLegalVisual ? PREVIEW_COLOR : ERROR_COLOR);
            drawShipRect(x, y, floatingShip.start, floatingShip.type.getLength(), floatingShip.orientation, cell);
        }

        if (!previewCells.isEmpty()) {
            float alpha = 0.2f + 0.3f * (0.5f + 0.5f * (float) Math.sin(blinkTimer * 4.0));
            renderer.setColor(new Color(1f, 1f, 0f, alpha));
            for (Coordinate previewCell : previewCells) {
                float cellX = x + previewCell.getCol() * cell;
                float cellY = y + (config.gridSize - 1 - previewCell.getRow()) * cell;
                renderer.rect(cellX, cellY, cell, cell);
            }
        }

        renderer.setColor(MINE_COLOR);
        for (Coordinate mine : mines) {
            float cx = x + mine.getCol() * cell + cell / 2f;
            float cy = y + (config.gridSize - 1 - mine.getRow()) * cell + cell / 2f;
            renderer.circle(cx, cy, cell * 0.3f);
        }
        renderer.end();

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        float pegRadius = cell * 0.25f;
        for (Coordinate miss : misses) {
            float cx = x + miss.getCol() * cell + cell / 2f;
            float cy = y + (config.gridSize - 1 - miss.getRow()) * cell + cell / 2f;
            renderer.setColor(Color.WHITE);
            renderer.circle(cx, cy, pegRadius);
        }
        for (Coordinate hit : hits) {
            float cx = x + hit.getCol() * cell + cell / 2f;
            float cy = y + (config.gridSize - 1 - hit.getRow()) * cell + cell / 2f;
            renderer.setColor(Color.RED);
            renderer.circle(cx, cy, pegRadius);
        }
        renderer.end();

        if (boardModel != null && !scannedTiles.isEmpty()) {
            batch.begin();
            for (Coordinate scanned : scannedTiles) {
                int count = boardModel.countAdjacentOccupancy(scanned);
                float cx = x + scanned.getCol() * cell + cell / 2f;
                float cy = y + (config.gridSize - 1 - scanned.getRow()) * cell + cell / 2f;
                Theme.fontSmall.setColor(Color.YELLOW);
                Theme.fontSmall.draw(batch, String.valueOf(count), cx - 4, cy + 4);
            }
            batch.end();
        }

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
        if (orientation == Orientation.VERTICAL) drawY -= (cellSize * (length - 1));
        renderer.rect(drawX, drawY, w, h);
    }

    public float getPrefWidth() { return config.size; }
    public float getPrefHeight() { return config.size; }
    public void dispose() { renderer.dispose(); }
}
