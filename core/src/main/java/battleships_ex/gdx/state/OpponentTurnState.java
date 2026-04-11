package battleships_ex.gdx.state;

/**
 * Transition map:
 *   Remote MISS  → MyTurnState   (opponent missed, local player's turn)
 *   Remote HIT   → stays in OpponentTurnState (opponent gets another shot)
 *   Remote SUNK (game not over)  → stays in OpponentTurnState
 *   Remote SUNK (game over)      → GameOverState (via onGameOver)
 */
public class OpponentTurnState extends BaseGameState {

    @Override
    public String getName() {
        return "OpponentTurnState";
    }

    @Override
    public void onEnter(GameStateManager manager) {
        manager.notifyStateChanged(getName());
    }

    @Override
    public void onRemoteShotReceived(GameStateManager manager, int x, int y) {
        manager.getGameController().onRemoteShotReceived(x, y);
    }

    public void onRemoteMissed(GameStateManager manager) {
        manager.transitionTo(new MyTurnState());
    }

    @Override
    public void onFireShot(GameStateManager manager, int x, int y) {
        // Input blocked — not local player's turn.
        // #TODO View should disable the attack grid while in this state.
    }
}
