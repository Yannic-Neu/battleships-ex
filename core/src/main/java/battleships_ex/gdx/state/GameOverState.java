package battleships_ex.gdx.state;

public class GameOverState extends BaseGameState {

    private final String winnerName;

    public GameOverState(String winnerName) {
        this.winnerName = winnerName != null ? winnerName : "Unknown";
    }

    @Override
    public String getName() {
        return "GameOverState";
    }

    @Override
    public void onEnter(GameStateManager manager) {
        manager.notifyGameOver(winnerName);
        manager.notifyStateChanged(getName());
    }

    public String getWinnerName() {
        return winnerName;
    }

    @Override
    public void onGameOver(GameStateManager manager, String winnerName) {
        // Already in terminal state
    }
}
