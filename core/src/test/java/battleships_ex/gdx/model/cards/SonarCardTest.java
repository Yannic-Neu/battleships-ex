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
        
        // Use all charges - must hit tile first
        Coordinate c1 = new Coordinate(0, 0);
        Coordinate c2 = new Coordinate(1, 1);
        opponent.getBoard().attack(c1);
        opponent.getBoard().attack(c2);
        
        card.execute(user, opponent, c1);
        card.execute(user, opponent, c2);
        
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
        
        Coordinate target = new Coordinate(0, 0);
        opponent.getBoard().attack(target); // Must be hit
        
        card.execute(user, opponent, target);
        
        assertEquals(initialEnergy - 2, user.getEnergy());
        assertEquals(initialUses - 1, card.getRemainingUses());
    }

    @Test
    void testSonarMetadata() {
        Coordinate target = new Coordinate(5, 5);
        opponent.getBoard().attack(target); // Must be hit
        
        // Place a mine near (5,5)
        opponent.getBoard().placeMine(new Coordinate(4, 4));
        
        ActionCardResult result = card.execute(user, opponent, target);
        
        assertEquals(ActionCardResult.Outcome.REVEALED, result.getOutcome());
        assertEquals(1, result.getMetadataAsInt("adjacentCount", -1));
        assertTrue(opponent.getBoard().hasBeenScanned(target));
    }
}
