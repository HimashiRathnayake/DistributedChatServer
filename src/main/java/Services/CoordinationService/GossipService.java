package Services.CoordinationService;

import Handlers.CoordinationHandler.FastBullyHandler;
import Handlers.CoordinationHandler.GossipHandler;
import Models.Client;
import Models.Room;
import Models.Server.LeaderState;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GossipService extends Thread {
    private static final Logger logger = Logger.getLogger(GossipService.class);
    private final String operation; // wait, send, push or pull
    private String request;
    private final GossipHandler gossipHandler = new GossipHandler();
    private JSONObject gossipMsg;
    private ArrayList<String> serverids = new ArrayList<String>();

    public GossipService(String operation, String request, JSONObject gossipMsg) {
        this.operation = operation;
        this.request = request;
        this.gossipMsg = gossipMsg;
    }

    public GossipService(String operation) {
        this.operation = operation;
    }

    public GossipService(String operation, JSONObject gossipMsg) {
        this.operation = operation;
        this.gossipMsg = gossipMsg;
    }


    public ServerData selectRichNeighbour(ServerData currentServerData, ArrayList<String> serverIds) {
        ServerData richNeighbour;
        ConcurrentMap<String, ServerData> serverList = ServerState.getServerStateInstance().getServersList();
        ArrayList<String> lowPriorityServers = new ArrayList<>();
        for (ConcurrentMap.Entry<String, ServerData> entry : serverList.entrySet()) {
            if (Integer.parseInt(entry.getKey().substring(1)) > Integer.parseInt(currentServerData.getServerID().substring(1))) {
                //!entry.getKey().equals(requestServerID)
                if (!serverIds.contains(entry.getKey())){
                    lowPriorityServers.add(entry.getKey());
                }
            }
        }
        ArrayList<String> highPriorityServers = new ArrayList<>();
        for (ConcurrentMap.Entry<String, ServerData> entry : serverList.entrySet()) {
            if (Integer.parseInt(entry.getKey().substring(1)) < Integer.parseInt(currentServerData.getServerID().substring(1))) {
                if (!serverIds.contains(entry.getKey())){
                    highPriorityServers.add(entry.getKey());
                }
            }
        }
        if (lowPriorityServers.size()>0){
            Collections.sort(lowPriorityServers);
            richNeighbour = serverList.get(lowPriorityServers.get(0));
        } else if (highPriorityServers.size()>0) {
            Collections.reverse(highPriorityServers);
            richNeighbour = serverList.get(highPriorityServers.get(0));
        } else {
            richNeighbour = null;
        }
        return richNeighbour;
    }

    public void updateServerState(){
        ServerState serverSate = ServerState.getServerStateInstance();
        serverSate.setRichNeighborData(selectRichNeighbour(serverSate.getCurrentServerData(), this.serverids));
    }

    public void run() {
        // send messages to other servers corresponding gossip Algorithm.
        switch (operation) {
            case ("wait") -> {
                logger.info("Waiting for gossiping");
                ServerState serverSate = ServerState.getServerStateInstance();
                serverSate.setRichNeighborData(selectRichNeighbour(serverSate.getCurrentServerData(), this.serverids));
                serverSate.setIsIgnorant(true);
            }
            case ("send") -> {
                this.serverids.add((String) this.gossipMsg.get("serverid"));
                updateServerState();
                ServerData richNeighbour = ServerState.getServerStateInstance().getRichNeighborData();
                ServerState.getServerStateInstance().setIsIgnorant(false);
                switch (request) {
                    case ("gossipidentity") -> {
                        logger.info("Send gossipidentity message");
                        if (richNeighbour != null) {
                            String serverID = (String) this.gossipMsg.get("serverid");
                            ConcurrentHashMap<String, Client> updatedlist = (ConcurrentHashMap<String, Client>) this.gossipMsg.get("updatedlist");
                            ArrayList<String> clientid = new ArrayList<String>(updatedlist.keySet());

                            JSONObject gossip = this.gossipHandler.pushgossip("pushgossipidentity", serverID, clientid, 1);
                            MessageTransferService.sendToServers(gossip, richNeighbour.getServerAddress(), richNeighbour.getCoordinationPort());
                        }
                    }
                    case ("gossiproom") -> {
                        logger.info("Send gossiproom message");
                        if (richNeighbour != null) {
                            String serverID = (String) this.gossipMsg.get("serverid");
                            ConcurrentHashMap<String, Room> updatedlist = (ConcurrentHashMap<String, Room>) this.gossipMsg.get("updatedlist");
                            ArrayList<String> roomids = new ArrayList<String>(updatedlist.keySet());
                            //ArrayList<Room> rooms = new ArrayList<Room>();
                            ArrayList<String> roomservers = new ArrayList<String>();
                            ArrayList<String> roomowners = new ArrayList<String>();
                            ArrayList<ArrayList<String>> clientids = new ArrayList<ArrayList<String>>();
                            for (Room room : updatedlist.values()) {
                                roomservers.add(room.getServer());
                                roomowners.add(room.getOwner());
                                ArrayList<String> roomclientids = new ArrayList<String>();
                                for (Client client : room.getClients()) {
                                    roomclientids.add(client.getIdentity());
                                }
                                clientids.add(roomclientids);
                            }

                            JSONObject gossip = this.gossipHandler.pushgossipRoom("pushgossiproom", serverID, roomids, roomservers,roomowners, clientids , 1);
                            MessageTransferService.sendToServers(gossip, richNeighbour.getServerAddress(), richNeighbour.getCoordinationPort());
                        }
                    }
                }
            }
            case ("push") -> {
                this.serverids.add((String) this.gossipMsg.get("serverid"));
                this.serverids.add((String) this.gossipMsg.get("sender"));
                this.serverids.add(ServerState.getServerStateInstance().getLeaderServerData().getServerID());
                updateServerState();

                ServerData richNeighbour = ServerState.getServerStateInstance().getRichNeighborData();

                switch (request) {
                    case ("pushgossipidentity") -> {
                        if (richNeighbour != null ) {
                            logger.info("Send pushgossipidentity message");
                            String serverID = (String) this.gossipMsg.get("serverid");
                            ArrayList<String> clientid = ServerState.getServerStateInstance().globalClientId;
                            long gossiprounds = (long) this.gossipMsg.get("rounds");

                            JSONObject gossip = this.gossipHandler.pushgossip("pushgossipidentity", serverID, clientid, (int) (gossiprounds+1));
                            MessageTransferService.sendToServers(gossip, richNeighbour.getServerAddress(), richNeighbour.getCoordinationPort());
                        } else {
                            logger.info("Stop gossiping - null neighbour");
                        }
                    }
                    case ("pushgossiproom") -> {
                        logger.info("Send pushgossiproom message");
                        if (richNeighbour != null ) {
                            logger.info("Send pushgossiproom message");
                            String serverID = (String) this.gossipMsg.get("serverid");
                            ArrayList<String> roomids = ServerState.getServerStateInstance().globalRoomList;
                            ArrayList<String> roomservers = ServerState.getServerStateInstance().globalRoomServersList;
                            ArrayList<String> roomowners = ServerState.getServerStateInstance().globalRoomOwnersList;
                            ArrayList<ArrayList<String>> clientids = ServerState.getServerStateInstance().globalRoomClientsList;
                            long gossiprounds = (long) this.gossipMsg.get("rounds");

                            JSONObject gossip = this.gossipHandler.pushgossipRoom("pushgossiproom", serverID, roomids, roomservers, roomowners, clientids, (int) (gossiprounds+1));
                            MessageTransferService.sendToServers(gossip, richNeighbour.getServerAddress(), richNeighbour.getCoordinationPort());
                        } else {
                            logger.info("Stop gossiping - null neighbour");
                        }
                    }
                }
            }
            case ("pull") -> {
                switch (request) {
                    case ("pullgossipidentity") -> {
                        logger.info("Send pullgossipidentity message");
                        ArrayList<String> clientIds = ServerState.getServerStateInstance().getGlobalClientsIds();
                        JSONObject gossip = this.gossipHandler.pullUpdate(clientIds, "identity");
                        String port = (String) this.gossipMsg.get("port");
                        MessageTransferService.sendToServers(gossip, (String) this.gossipMsg.get("host"), Integer.parseInt(port));
                    }
                    case ("pullgossiproom") -> {
                        logger.info("Send pullgossiproom message");
                        ArrayList<String> roomids = ServerState.getServerStateInstance().getGlobalRoomList();
                        ArrayList<String> roomservers = ServerState.getServerStateInstance().getGlobalRoomServersList();
                        ArrayList<String> roomowners = ServerState.getServerStateInstance().getGlobalRoomOwnersList();
                        ArrayList<ArrayList<String>> clientids = ServerState.getServerStateInstance().getGlobalRoomClientsList();
                        JSONObject gossip = this.gossipHandler.pullUpdateRooms(roomids, roomservers, roomowners, clientids, "room");
                        String port = (String) this.gossipMsg.get("port");
                        MessageTransferService.sendToServers(gossip, (String) this.gossipMsg.get("host"), Integer.parseInt(port));
                    }
                }
            }
        }
    }

    public static void initializeService() {
        Thread gossipService = new GossipService("wait");
        gossipService.start();
    }
}
