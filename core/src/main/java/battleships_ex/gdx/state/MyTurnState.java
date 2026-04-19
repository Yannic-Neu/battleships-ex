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
        manager.getGameController().playActionCard(card);
    }


    public void onShotMissed(GameStateManager manager) {
        manager.transitionTo(new OpponentTurnState());
    }
}
