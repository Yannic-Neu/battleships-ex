package battleships_ex.gdx.state;

public class LobbyState extends BaseGameState {

    @Override
    public String getName() {
        return "LobbyState";
    }

    @Override
    public void onEnter(GameStateManager manager) {
        manager.notifyStateChanged(getName());
    }

    @Override
    public void onExit(GameStateManager manager) { }

    @Override
    public void onCreateLobby(GameStateManager manager) {
        manager.getLobbyController().createLobby(manager.getLocalPlayer());
    }

    @Override
    public void onJoinLobby(GameStateManager manager, String roomCode) {
        manager.getLobbyController().joinLobby(manager.getLocalPlayer(), roomCode);
    }

    @Override
    public void onLobbyReady(GameStateManager manager) {
        manager.transitionTo(new PlacementState());
    }
}
