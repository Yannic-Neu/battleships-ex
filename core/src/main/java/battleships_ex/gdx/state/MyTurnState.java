package battleships_ex.gdx.state;

import battleships_ex.gdx.model.cards.ActionCard;
import battleships_ex.gdx.model.cards.ActionCardResult;
import battleships_ex.gdx.model.core.Player;


public class MyTurnState extends BaseGameState {

    private boolean cardUsedThisTurn = false;
    private boolean canStillFire = true;

    @Override
    public String getName() {
        return "MyTurnState";
    }

    @Override
    public void onEnter(GameStateManager manager) {
        manager.notifyStateChanged(getName());
        Player me = manager.getLocalPlayer();
        me.gainTurnEnergy();   // +1 energy at start of turn
    }

    @Override
    public void onFireShot(GameStateManager manager, int x, int y) {
        manager.getGameController().fireShot(x, y);
    }

    public void onPlayCard(GameStateManager manager, ActionCard card) {
        Player me = manager.getLocalPlayer();
        Player opponent = manager.getRemotePlayer();

        // 1. One card per turn
        if (cardUsedThisTurn) {
            return; // or throw / notify UI
        }

        // 2. Card-level permission
        if (!card.canUse(me, opponent)) {
            return;
        }

        // 3. Energy check
        int cost = card.getEnergyCost();
        if (!me.canSpendEnergy(cost)) {
            return;
        }

        // 4. Spend energy
        me.spendEnergy(cost);

        // 5. Execute card
        ActionCardResult result = card.execute(me, opponent);

        // 6. Mark card usage
        cardUsedThisTurn = true;


        // 6.5 Control firing permission
        if (!card.allowsFireAfterUse()) {
            canStillFire = false;
        }

        // 7. End turn if required
        if (card.endsTurn()) {
            manager.transitionTo(new OpponentTurnState());
        }
    }


    public void onShotMissed(GameStateManager manager) {
        manager.transitionTo(new OpponentTurnState());
    }
}
