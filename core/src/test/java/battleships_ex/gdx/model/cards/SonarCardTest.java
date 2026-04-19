package battleships_ex.gdx.model.cards;

import battleships_ex.gdx.model.board.Coordinate;
import battleships_ex.gdx.model.core.Player;
import battleships_ex.gdx.model.rules.StandardRulesEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SonarCardTest {

    private Player user;
    private Player opponent;
    private SonarCard card;
    private StandardRulesEngine engine;

    @BeforeEach
    void setUp() {
        user = new Player("u1", "User");
        opponent = new Player("o1", "Opponent");
        card = new SonarCard();
        engine = new StandardRulesEngine();
        ActionCardEffectProvider.getInstance().setEffects(engine);
        user.addEnergy(10);
    }

    @Test
    void testCanUse() {
        assertTrue(card.canUse(user, opponent));
        
        // Use all charges
        card.execute(user, opponent, new Coordinate(0, 0));
        card.execute(user, opponent, new Coordinate(1, 1));
        
        assertFalse(card.canUse(user, opponent), "Should be out of uses");
    }

    @Test
    void testInsufficientEnergy() {
        user.spendEnergy(user.getEnergy()); // zero energy
        assertFalse(card.canUse(user, opponent));
    }

    @Test
    void testExecuteDeductsEnergyAndUses() {
        int initialEnergy = user.getEnergy();
        int initialUses = card.getRemainingUses();
        
        card.execute(user, opponent, new Coordinate(0, 0));
        
        assertEquals(initialEnergy - 2, user.getEnergy());
        assertEquals(initialUses - 1, card.getRemainingUses());
    }

    @Test
    void testSonarMetadata() {
        // Place a ship near (5,5)
        // (5,5) adjacency includes (4,4), (4,5), (4,6), (5,4), (5,6), (6,4), (6,5), (6,6)
        opponent.getBoard().placeMine(new Coordinate(4, 4));
        
        ActionCardResult result = card.execute(user, opponent, new Coordinate(5, 5));
        
        assertEquals(ActionCardResult.Outcome.REVEALED, result.getOutcome());
        assertEquals(1, result.getMetadataAsInt("adjacentCount", -1));
        assertTrue(opponent.getBoard().hasBeenScanned(new Coordinate(5, 5)));
    }
}
