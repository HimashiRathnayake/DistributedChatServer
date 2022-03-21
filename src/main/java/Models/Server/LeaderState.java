package Models.Server;

import Models.Client;
import Models.Room;

import java.util.concurrent.ConcurrentHashMap;

public class LeaderState {
    public final ConcurrentHashMap<String, Room> globalRoomList = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Client> globalClients = new ConcurrentHashMap<>();
    private static LeaderState instance;

    public static synchronized LeaderState getInstance() {
        if (instance == null && ServerState.getServerStateInstance().getCurrentServerData().equals(ServerState.getServerStateInstance().getLeaderServerData())) {
            instance = new LeaderState();
        }
        return instance;
    }

    public ConcurrentHashMap<String, Client> getGlobalClients() {
        return globalClients;
    }

    public static void setInstance(LeaderState instance) {
        LeaderState.instance = instance;
    }

    public ConcurrentHashMap<String, Room> getGlobalRoomList() {
        return globalRoomList;
    }
}
