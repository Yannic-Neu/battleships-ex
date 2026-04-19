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
}
