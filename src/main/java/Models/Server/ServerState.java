package Models.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ServerState {
    private static ServerState serverState;
    private ServerData currentServerData;
    private ServerData coordinatorServerData;
    private final ConcurrentMap<String, ServerData> serversList = new ConcurrentHashMap<>();
//    private final ConcurrentMap<String, ServerData> higherPriorityServers = new ConcurrentHashMap<>();
//    private final ConcurrentMap<String, ServerData> lowerPriorityServers = new ConcurrentHashMap<>();
//    private final Queue<AbstractChatRequest> retryQueue = new ConcurrentLinkedQueue<>();

    public static synchronized ServerState getServerStateInstance() {
        if (serverState == null) {
            serverState = new ServerState();
        }
        return serverState;
    }

    public synchronized void setServerState(String serverID) {
        currentServerData = serversList.get(serverID);
    }

    public synchronized ServerData getServerDataById(String serverID) {
        return serversList.get(serverID);
    }

    public synchronized ServerData getServerData() {
        return currentServerData;
    }

    public synchronized ConcurrentMap<String, ServerData> getServersData() {
        return serversList;
    }

    public synchronized List<ServerData> getServersDataAsArray() {
        return new ArrayList<>(serversList.values());
    }

    public ServerData getCoordinator() {
        return coordinatorServerData;
    }

    public void setCoordinator(ServerData coordinator) {
        this.coordinatorServerData = coordinator;
        System.out.println("New coordinator is set");
    }

//    public synchronized List<ServerData> getHigherServerInfo() {
//        return new ArrayList<>(higherPriorityServers.values());
//    }
//
//    public synchronized List<ServerData> getLowerServerInfo() { return new ArrayList<>(lowerPriorityServers.values());
//    }

    public synchronized void setServersList(List<ServerData> serversList, String myServerId) {
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
}
