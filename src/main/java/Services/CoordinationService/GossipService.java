package Services.CoordinationService;

import Handlers.CoordinationHandler.FastBullyHandler;
import Handlers.CoordinationHandler.GossipHandler;
import Models.Client;
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
    private final String operation; // wait, push or pull
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
                    case ("gossipcreateroom") -> {
                        logger.info("Send gossipnewroom message");
                    }
                    case ("gossipdeleteroom") -> {
                        logger.info("Send gossipdeleteroom message");
                    }
                    case ("gossipquit") -> {
                        logger.info("Send gossipquit message");
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
                    case ("pushgossipcreateroom") -> {
                        logger.info("Send pushgossipnewroom message");
                    }
                    case ("pushgossipdeleteroom") -> {
                        logger.info("Send pushgossipdeleteroom message");
                    }
                    case ("pushgossipquit") -> {
                        logger.info("Send pushgossipquit message");
                    }
                }
            }
            case ("pull") -> {
                switch (request) {
                    case ("pullgossipidentity") -> {
                        logger.info("Send pullgossipnewidentity message");
                        ArrayList<String> clientIds = ServerState.getServerStateInstance().getGlobalClientsIds();
                        JSONObject gossip = this.gossipHandler.pullUpdate(clientIds);
                        String port = (String) this.gossipMsg.get("port");
                        MessageTransferService.sendToServers(gossip, (String) this.gossipMsg.get("host"), Integer.parseInt(port));

                    }
                    case ("pullgossipcreateroom") -> {
                        logger.info("Send pullgossipnewroom message");
                    }
                    case ("pullgossipdeleteroom") -> {
                        logger.info("Send pullgossipdeleteroom message");
                    }
                    case ("pullgossipquit") -> {
                        logger.info("Send pullgossipquit message");
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
