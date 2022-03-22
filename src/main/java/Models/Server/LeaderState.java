package Models.Server;

import Models.Client;
import Models.Room;

import java.util.concurrent.ConcurrentHashMap;

public class LeaderState {
    public final ConcurrentHashMap<String, Room> globalRoomList = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Client> globalClients = new ConcurrentHashMap<>();
    private static LeaderState instance;

    public static synchronized LeaderState getInstance() {
        ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
        ServerData leaderServer = ServerState.getServerStateInstance().getLeaderServerData();
        if (instance == null && currentServer.getServerID().equals(leaderServer.getServerID())) {
            instance = new LeaderState();
        }
        return instance;
    }

    public ConcurrentHashMap<String, Client> getGlobalClients() {
        return globalClients;
    }
    public void addClientToGlobalList(Client client){
        this.globalClients.put(client.getIdentity(),client);
    }
    public ConcurrentHashMap<String, Room> getGlobalRoomList() {
        return globalRoomList;
    }
    public void addRoomToGlobalList(Room room){
        this.globalRoomList.put(room.getRoomID(),room);
    }
}
