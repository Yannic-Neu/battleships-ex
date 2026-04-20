package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.board.Ship;
import battleships_ex.gdx.config.board.ShipType;
import battleships_ex.gdx.config.board.Orientation;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.rules.StandardRulesEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MineCardTest {

    private Player user;
    private Player opponent;
    private MineCard card;
    private StandardRulesEngine engine;

    @BeforeEach
    void setUp() {
        user = new Player("u1", "User");
        opponent = new Player("o1", "Opponent");
        card = new MineCard();
        engine = new StandardRulesEngine();
        ActionCardEffectProvider.getInstance().setEffects(engine);
        user.addEnergy(10);
    }

    @Test
    void testPlaceMine() {
        Coordinate target = new Coordinate(5, 5);
        ActionCardResult result = card.execute(user, opponent, target);
        
        assertEquals(ActionCardResult.Outcome.REVEALED, result.getOutcome());
        assertTrue(user.getBoard().hasMine(target));
        assertEquals(9, user.getEnergy());
    }

    @Test
    void testCannotPlaceMineOnShip() {
        Coordinate target = new Coordinate(0, 0);
        Ship patrol = new Ship(ShipType.PATROL, Orientation.HORIZONTAL);
        user.getBoard().placeShip(patrol, target, Orientation.HORIZONTAL);
        
        ActionCardResult result = card.execute(user, opponent, target);
        
        assertEquals(ActionCardResult.Outcome.NO_EFFECT, result.getOutcome());
        assertFalse(user.getBoard().hasMine(target));
        assertEquals(10, user.getEnergy(), "Energy should not be spent if placement failed");
    }

    @Test
    void testMineSinkingLastShipWinsGame() {
        // Opponent has one PATROL ship left with 1 HP
        Ship patrol = new Ship(ShipType.PATROL, Orientation.HORIZONTAL);
        opponent.getBoard().placeShip(patrol, new Coordinate(0, 0), Orientation.HORIZONTAL);
        engine.resolveShot(opponent.getBoard(), new Coordinate(0, 0)); // HIT first cell
        
        // User has a mine on their board at (5,5)
        user.getBoard().placeMine(new Coordinate(5, 5));
        
        // Opponent shoots at (5,5) and hits the mine
        battleships_ex.gdx.model.rules.ShotResult result = engine.shootTile(user, new Coordinate(5, 5));
        assertEquals(battleships_ex.gdx.model.rules.ShotResult.Outcome.MINE_HIT, result.getOutcome());
        
        // Trigger counter-shots against the opponent
        // We simulate the GameController logic here
        java.util.List<battleships_ex.gdx.model.rules.ShotResult> counterShots = engine.triggerRandomShots(opponent, 100); // Shoot everything
        
        boolean sunk = counterShots.stream().anyMatch(battleships_ex.gdx.model.rules.ShotResult::isSunk);
        assertTrue(sunk, "Mine counter-shot should have sunk the last ship");
        assertTrue(engine.hasWon(opponent.getBoard()), "Game should be won after last ship is sunk by mine");
    }

    @Test
    void testMineChaining() {
        // User has two mines
        user.getBoard().placeMine(new Coordinate(1, 1));
        user.getBoard().placeMine(new Coordinate(2, 2));
        
        // Opponent shoots (1,1)
        battleships_ex.gdx.model.rules.ShotResult result = engine.shootTile(user, new Coordinate(1, 1));
        assertEquals(battleships_ex.gdx.model.rules.ShotResult.Outcome.MINE_HIT, result.getOutcome());
        
        // First mine triggers counter-shots. If one hits (2,2), it should return MINE_HIT
        // StandardRulesEngine.triggerRandomShots calls shootTile, which handles mines
        java.util.List<battleships_ex.gdx.model.rules.ShotResult> counterShots = engine.triggerRandomShots(user, 100);
        
        boolean hitSecondMine = counterShots.stream().anyMatch(sr -> sr.getOutcome() == battleships_ex.gdx.model.rules.ShotResult.Outcome.MINE_HIT);
        assertTrue(hitSecondMine, "A mine counter-shot should be able to hit another mine (chaining)");
    }
}
