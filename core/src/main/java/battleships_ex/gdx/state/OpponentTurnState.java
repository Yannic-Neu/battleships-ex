package battleships_ex.gdx.state;

import com.badlogic.gdx.utils.Timer;

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

        // If in single-player, trigger the bot logic after a short delay to simulate thinking
        if (manager.getGameController().isSinglePlayer()) {
            scheduleBotMove(manager);
        }
    }

    private void scheduleBotMove(GameStateManager manager) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                manager.getGameController().playBotTurn();

                // If the shot was a HIT/SUNK, the session remains in OpponentTurnState.
                // We must schedule the bot's next shot so it can continue its streak.
                if (manager.getGameController().isSinglePlayer() &&
                    !manager.getGameController().isLocalPlayerTurn() &&
                    manager.getGameController().isSessionActive()) {
                    scheduleBotMove(manager);
                }
            }
        }, 1.0f); // 1-second delay for UI readability
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
