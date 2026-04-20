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

class BombCardTest {

    private Player user;
    private Player opponent;
    private BombCard card;
    private StandardRulesEngine engine;

    @BeforeEach
    void setUp() {
        user = new Player("u1", "User");
        opponent = new Player("o1", "Opponent");
        card = new BombCard();
        engine = new StandardRulesEngine();
        ActionCardEffectProvider.getInstance().setEffects(engine);
        user.addEnergy(10);
    }

    @Test
    void testExecuteHits2x2Area() {
        // Place a ship at (0,0) and (0,1)
        Ship patrol = new Ship(ShipType.PATROL, Orientation.HORIZONTAL);
        opponent.getBoard().placeShip(patrol, new Coordinate(0, 0), Orientation.HORIZONTAL);
        
        // Bomb at (0,0) hits (0,0), (1,0), (0,1), (1,1)
        ActionCardResult result = card.execute(user, opponent, new Coordinate(0, 0));
        
        // Since PATROL has length 2 and we hit both cells, it's a SUNK
        assertEquals(ActionCardResult.Outcome.SUNK, result.getOutcome());
        assertEquals(4, result.getAffectedCoordinates().size());
        assertTrue(opponent.getBoard().getCell(new Coordinate(0, 0)).isHit());
        assertTrue(opponent.getBoard().getCell(new Coordinate(1, 0)).isHit());
        assertTrue(opponent.getBoard().getCell(new Coordinate(0, 1)).isHit());
        assertTrue(opponent.getBoard().getCell(new Coordinate(1, 1)).isHit());
    }

    @Test
    void testSunkWithBomb() {
        // Place patrol at (0,0) and (0,1)
        Ship patrol = new Ship(ShipType.PATROL, Orientation.HORIZONTAL);
        opponent.getBoard().placeShip(patrol, new Coordinate(0, 0), Orientation.HORIZONTAL);
        
        // Bomb hits both cells
        ActionCardResult result = card.execute(user, opponent, new Coordinate(0, 0));
        
        assertEquals(ActionCardResult.Outcome.SUNK, result.getOutcome());
        assertTrue(patrol.isSunk());
    }
}
