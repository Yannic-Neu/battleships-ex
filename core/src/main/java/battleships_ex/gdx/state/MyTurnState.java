package battleships_ex.gdx.state;

public class MyTurnState extends BaseGameState {

    @Override
    public String getName() {
        return "MyTurnState";
    }

    @Override
    public void onEnter(GameStateManager manager) {
        manager.notifyStateChanged(getName());
    }

    @Override
    public void onFireShot(GameStateManager manager, int x, int y) {
        manager.getGameController().fireShot(x, y);
    }

    public void onShotMissed(GameStateManager manager) {
        manager.transitionTo(new OpponentTurnState());
    }
}
