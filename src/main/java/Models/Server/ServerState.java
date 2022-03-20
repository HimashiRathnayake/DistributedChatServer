package Models.Server;

import Models.Client;
import Models.Room;
import Services.ChatService.ChatClientService;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServerState {
    private static ServerState serverState;
    private ServerData currentServerData;
    private ServerData coordinatorServerData;
    private final ConcurrentMap<String, ServerData> serversList = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Room> roomList = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Client> clients = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, ChatClientService> clientServices = new ConcurrentHashMap<>();
    private final Logger logger = Logger.getLogger(ServerState.class);
//    private final ConcurrentMap<String, ServerData> higherPriorityServers = new ConcurrentHashMap<>();
//    private final ConcurrentMap<String, ServerData> lowerPriorityServers = new ConcurrentHashMap<>();
//    private final Queue<AbstractChatRequest> retryQueue = new ConcurrentLinkedQueue<>();

    public static synchronized ServerState getServerStateInstance() {
        if (serverState == null) {
            serverState = new ServerState();
        }
        return serverState;
    }

    public synchronized void setCurrentServerData(String serverID) {
        currentServerData = serversList.get(serverID);
    }

    public synchronized ServerData getServerDataById(String serverID) {
        return serversList.get(serverID);
    }

    public synchronized ServerData getCurrentServerData() {
        return currentServerData;
    }

    public synchronized ConcurrentMap<String, ServerData> getServersList() {
        return serversList;
    }

    public synchronized List<ServerData> getServersListAsArray() {
        return new ArrayList<>(serversList.values());
    }

    public ServerData getCoordinator() {
        return coordinatorServerData;
    }

    public synchronized ConcurrentMap<String, Room> getRoomList(){
        return roomList;
    }

    public void setCoordinator(ServerData coordinator) {
        this.coordinatorServerData = coordinator;
        logger.info("New coordinator is set");
    }

//    public synchronized List<ServerData> getHigherServerInfo() {
//        return new ArrayList<>(higherPriorityServers.values());
//    }
//
//    public synchronized List<ServerData> getLowerServerInfo() { return new ArrayList<>(lowerPriorityServers.values());
//    }

    public synchronized void setServerState(List<ServerData> serversList, String myServerId) {
        for (ServerData server : serversList) {
            if (!server.getServerID().equals(myServerId)) {
                System.out.println("Add server: " + server.getServerID());
                addServer(server, myServerId);
            } else {
                this.currentServerData = server;
            }
        }
    }


    public synchronized void addServer(ServerData externalServer, String myServerId) {
//        if (compare(myServerId, externalServer.getServerID()) < 0) {
//            higherServerInfo.put(externalServer.getServerId(), externalServer);
//        } else {
////            lowerServerInfo.put(myServerId, externalServer);
//            lowerServerInfo.put(externalServer.getServerId(), externalServer);
//        }
        serversList.put(externalServer.getServerID(), externalServer);

    }

//    private int compare(String myServerId, String externalServerId) {
//        if (null != myServerId && null != externalServerId) {
//            Integer server1Id = Integer.parseInt(myServerId.substring(1));
//            Integer server2Id = Integer.parseInt(externalServerId.substring(1));
//            return server1Id - server2Id;
//        }
//        return 0;
//    }

//    public synchronized boolean isCoordinator(){
//        return currentServerData.equals(coordinatorServerData);
//    }

    public void addNewRoom(Room room) {
        roomList.put(room.getRoomID(), room);
        logger.info("Main Hall Created");
    }

    public void addClientToRoom(String roomID, Client client){
        roomList.get(roomID).addClient(client);
    }

    public void deleteRoomByOwner(String clientID) {

        logger.info("Main Hall Created");
    }

    // get all client threads in a room associated with a given client
    public List<ChatClientService> getClientServicesInRoomByClient(Client sender){
        for (Room room: roomList.values()){
            if (room.getClients().contains(sender)){
                List<String> roomClients = room.getClients().stream().map(Client::getIdentity)
                        .filter(c -> !Objects.equals(c, sender.getIdentity())).toList();
                return roomClients.stream().map(clientServices::get).filter(Objects::nonNull).toList();
            }
        }
        return null;
    }

}
