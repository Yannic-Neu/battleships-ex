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

class AirstrikeCardTest {

    private Player user;
    private Player opponent;
    private AirstrikeCard card;
    private StandardRulesEngine engine;

    @BeforeEach
    void setUp() {
        user = new Player("u1", "User");
        opponent = new Player("o1", "Opponent");
        card = new AirstrikeCard();
        engine = new StandardRulesEngine();
        ActionCardEffectProvider.getInstance().setEffects(engine);
        user.addEnergy(10);
    }

    @Test
    void testAirstrikeRow() {
        card.setOrientation(AirstrikeCard.Orientation.ROW);
        Coordinate target = new Coordinate(5, 0);
        
        ActionCardResult result = card.execute(user, opponent, target);
        
        assertEquals(10, result.getAffectedCoordinates().size());
        for (int col = 0; col < 10; col++) {
            assertTrue(opponent.getBoard().getCell(new Coordinate(5, col)).isHit());
        }
    }

    @Test
    void testAirstrikeColumn() {
        card.setOrientation(AirstrikeCard.Orientation.COLUMN);
        Coordinate target = new Coordinate(0, 7);
        
        ActionCardResult result = card.execute(user, opponent, target);
        
        assertEquals(10, result.getAffectedCoordinates().size());
        for (int row = 0; row < 10; row++) {
            assertTrue(opponent.getBoard().getCell(new Coordinate(row, 7)).isHit());
        }
    }
}
