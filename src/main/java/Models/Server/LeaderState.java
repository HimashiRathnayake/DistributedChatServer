package Models.Server;

import Models.Client;
import Models.Room;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LeaderState {
    private final ConcurrentMap<String, ServerData> serversList = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Room> globalRoomList = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Client> globalClients = new ConcurrentHashMap<>();
    private static LeaderState instance;

    public static synchronized LeaderState getInstance() {
        if (instance == null && ServerState.getServerStateInstance().getCurrentServerData().equals(ServerState.getServerStateInstance().getCoordinator())) {
            instance = new LeaderState();
        }
        return instance;
    }
}
