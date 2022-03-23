package Handlers.ChatHandler;

import Handlers.CoordinationHandler.GossipHandler;
import Handlers.CoordinationHandler.RequestHandler;
import Models.Client;
import Models.Server.LeaderState;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.ChatService.ChatClientService;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NewIdentityHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ClientResponseHandler clientResponseHandler;
    private final RequestHandler serverRequestHandler = new RequestHandler();
    private final GossipHandler gossipHandler = new GossipHandler();

    public NewIdentityHandler(ClientResponseHandler clientResponseHandler){
        this.clientResponseHandler = clientResponseHandler;
    }

    public boolean checkIdentityRules(String identity){
        boolean isIdentityGood;
        isIdentityGood = identity!= null && identity.matches("^[a-zA-Z][a-zA-Z0-9]*$")
                && identity.length()>=3
                && identity.length()<16;
        return isIdentityGood;
    }

    public String checkIdentityUnique(String identity) {
        String isIdentityUnique = "true";
        ServerData leaderServer = ServerState.getServerStateInstance().getLeaderServerData();
        if (ServerState.getServerStateInstance().isCurrentServerLeader()){
            ConcurrentHashMap<String, Client> clients = LeaderState.getInstance().getGlobalClients();
            for (Iterator<String> it = clients.keys().asIterator(); it.hasNext(); ) {
                String client = it.next();
                if (Objects.equals(client, identity)){
                    isIdentityUnique = "false";
                }
            }
        } else {
            JSONObject request = serverRequestHandler.sendNewIdentityResponse(identity);
            MessageTransferService.sendToServers(request, leaderServer.getServerAddress(), leaderServer.getCoordinationPort());
            isIdentityUnique = "askedFromLeader";
        }
        return isIdentityUnique;
    }

    public JSONObject moveToMainHall(Client client){
        JSONObject response;
        String roomID = "MainHall-" + System.getProperty("serverID");
        ServerState.getServerStateInstance().addClientToRoom(roomID, client);
        response = clientResponseHandler.moveToRoomResponse(client.getIdentity(), "", roomID);
        return response;
    }

    public Map<String, JSONObject> addNewIdentity(ChatClientService service, Client client, String identity){
        Map<String, JSONObject> responses = new HashMap<>();
        ServerState.getServerStateInstance().clientServices.put("1temp-"+identity, service); //
        boolean checkIdentityRules = checkIdentityRules(identity);
        String checkIdentityUnique = checkIdentityUnique(identity);
        if (checkIdentityRules && checkIdentityUnique.equals("true")){
            client.setIdentity(identity);
            client.setServer(System.getProperty("serverID"));
            client.setStatus("active");
            ServerState.getServerStateInstance().clients.put(identity, client);
            LeaderState.getInstance().globalClients.put(identity, client);
            ServerState.getServerStateInstance().clientServices.remove("1temp-"+identity, service); //
            ServerState.getServerStateInstance().clientServices.put(identity, service); //
            logger.info("New identity creation accepted");
            responses.put("client-only", clientResponseHandler.sendNewIdentityResponse("true"));
            responses.put("broadcast", moveToMainHall(client));
            //JSONObject gossipMsg = this.gossipHandler.gossipNewIdentity(System.getProperty("serverID"), identity);
            JSONObject gossipMsg = this.gossipHandler.gossip("gossipidentity", System.getProperty("serverID"), LeaderState.getInstance().globalClients);
            responses.put("gossip", gossipMsg);
        } else if(checkIdentityRules && checkIdentityUnique.equals("askedFromLeader")){
            logger.info("Asked from leader");
            responses.put("askedFromLeader", null);
        } else {
            ServerState.getServerStateInstance().clientServices.remove("1temp-"+identity); //
            logger.info("New identity creation rejected");
            responses.put("client-only", clientResponseHandler.sendNewIdentityResponse("false"));
        }
        return responses;
    }
}
