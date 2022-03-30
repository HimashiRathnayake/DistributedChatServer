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
    private ServerData leaderServerData;
    private ServerData richNeighborData;
    private boolean isIgnorant;
    private final int initialRounds = 2;
    private final ConcurrentMap<String, ServerData> serversList = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Room> roomList = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Client> clients = new ConcurrentHashMap<>();
    public ArrayList<String> globalRoomList = new ArrayList<>();
    public ArrayList<String> globalRoomServersList = new ArrayList<>();
    public ArrayList<String> globalRoomOwnersList = new ArrayList<>();
    public ArrayList<ArrayList<String>> globalRoomClientsList = new ArrayList<>();
    public ArrayList<String> globalClientId = new ArrayList<>();
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

    public synchronized ServerData getRichNeighborData() {
        return richNeighborData;
    }

    public synchronized Boolean getIsIgnorant() {
        return isIgnorant;
    }

    public synchronized int getInitialRounds() {
        return initialRounds;
    }

    public synchronized ArrayList<String> getGlobalRoomList() {
        return globalRoomList;
    }

    public synchronized ArrayList<String> getGlobalRoomServersList() {
        return globalRoomServersList;
    }

    public synchronized ArrayList<String> getGlobalRoomOwnersList() {
        return globalRoomOwnersList;
    }

    public synchronized ArrayList<ArrayList<String>> getGlobalRoomClientsList() {
        return globalRoomClientsList;
    }

    public synchronized ArrayList<String> getGlobalClientsIds() {
        return globalClientId;
    }

    public synchronized ConcurrentMap<String, ServerData> getServersList() {
        return serversList;
    }

    public synchronized List<ServerData> getServersListAsArray() {
        return new ArrayList<>(serversList.values());
    }

    public ServerData getLeaderServerData() {
        return leaderServerData;
    }

    public boolean isCurrentServerLeader(){
        return this.currentServerData.getServerID().equals(this.leaderServerData.getServerID());
    }

    public synchronized ConcurrentMap<String, Room> getRoomList(){
        return roomList;
    }

    public void setLeaderServerData(ServerData leaderServerData) {
        String prevLeaderID = leaderServerData.getServerID();
        this.leaderServerData = leaderServerData;
        if (prevLeaderID.equals(currentServerData.getServerID())){
            if(!globalClientId.isEmpty()) {
                for (String clientID: globalClientId){
                    Client client = new Client();
                    client.setIdentity(clientID);
                    LeaderState.getInstance().globalClients.put(clientID, client);
                }
            }
            if(!globalRoomList.isEmpty()){
                for (int j = 0; j< globalRoomList.size(); j++){
                    ArrayList<Client> clientsList = new ArrayList<>();
                    for (String clientID: globalRoomClientsList.get(j)){
                        Client client = new Client();
                        client.setIdentity(clientID);
                        clientsList.add(client);
                    }
                    Room room = new Room(globalRoomList.get(j),globalRoomServersList.get(j), globalRoomOwnersList.get(j), clientsList);
                    LeaderState.getInstance().globalRoomList.put(globalRoomList.get(j), room);
                }
            }
        }
    }

    public void setRichNeighborData(ServerData richNeighborData) {
        this.richNeighborData = richNeighborData;
    }

    public void setIsIgnorant(boolean isIgnorant) {
        this.isIgnorant = isIgnorant;
    }

    public synchronized void setGlobalRoomList(ArrayList<String> globalRoomList) {
        this.globalRoomList = globalRoomList;
    }

    public synchronized void setGlobalRoomServersList(ArrayList<String> globalRoomServersList) {
        this.globalRoomServersList = globalRoomServersList;
    }

    public synchronized void setGlobalRoomOwnersList(ArrayList<String> globalRoomOwnersList) {
        this.globalRoomOwnersList = globalRoomOwnersList;
    }

    public synchronized void setGlobalRoomClientsList(ArrayList<ArrayList<String>> globalRoomClientsList) {
        this.globalRoomClientsList = globalRoomClientsList;
    }

    public synchronized void setGlobalClientIDs(ArrayList<String> globalClientId) {
        this.globalClientId = globalClientId;
    }

    public synchronized void compareAndSetGlobalRoomClientsList(ArrayList<ArrayList<String>> globalRoomClientsList) {
        for (ArrayList<String> clientList: globalRoomClientsList) {
            if (!this.globalRoomClientsList.contains(clientList)) {
                this.globalRoomClientsList.add(clientList);
            }
        }
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

    public void removeClientFromRoom(String roomID, Client client){
        roomList.get(roomID).removeClient(client);
    }

    public String removeClientFromRoomWithFormerRoom(Client client) {
        for (Room room: roomList.values()){
            if (room.getClients().contains(client)){
                String formerRoom = room.getRoomID();
                room.removeClientByClienID(client.getIdentity());
                return formerRoom;
            }
        }
        return null;
    }

    public Room getOwningRoom(String clientID) {
        for (Room room: roomList.values()){
            if (Objects.equals(room.getOwner(), clientID)){
                return room;
            }
        }
        return null;
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
