package Handlers.CoordinationHandler;

import Handlers.ChatHandler.ClientResponseHandler;
import Models.Client;
import Models.Server.LeaderState;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.ChatService.ChatClientService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class NewIdentityHandler {
    private final Logger logger = Logger.getLogger(Handlers.ChatHandler.NewIdentityHandler.class);
    private final RequestHandler serverRequestHandler = new RequestHandler();
    private final ResponseHandler serverResponseHandler = new ResponseHandler();
    private final ClientResponseHandler clientResponseHandler = new ClientResponseHandler();
    private final GossipHandler gossipHandler = new GossipHandler();

    public NewIdentityHandler() {
    }

    public boolean checkIdentityUnique(String identity) {
        boolean isIdentityUnique = true;
        ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
        ServerData leaderServer = ServerState.getServerStateInstance().getLeaderServerData();
        if (Objects.equals(currentServer.getServerID(), leaderServer.getServerID())) {
            ConcurrentHashMap<String, Client> clients = LeaderState.getInstance().getGlobalClients();
            for (Iterator<String> it = clients.keys().asIterator(); it.hasNext(); ) {
                String client = it.next();
                if (Objects.equals(client, identity)) {
                    isIdentityUnique = false;
                }
            }
        }
        return isIdentityUnique;
    }

    //public JSONObject coordinatorNewClientIdentity(Client client, String identity, String serverID) {
    public Map<String, JSONObject> coordinatorNewClientIdentity(Client client, String identity, String serverID) {
        Map<String, JSONObject> responses = new HashMap<>();
        JSONObject response;
        if (checkIdentityUnique(identity)) {
            client.setIdentity(identity);
            client.setServer(serverID);
            client.setStatus("active");
            LeaderState.getInstance().addClientToGlobalList(client);
            logger.info("New identity creation accepted");
            response = this.serverResponseHandler.sendNewIdentityServerResponse("true", identity);
            //JSONObject gossipMsg = this.gossipHandler.gossipNewIdentity(serverID, identity);
            JSONObject gossipMsg = this.gossipHandler.gossip("gossipidentity", System.getProperty("serverID"), LeaderState.getInstance().globalClients);
            responses.put("response", response);
            responses.put("gossip", gossipMsg);
        } else {
            logger.info("New identity creation rejected");
            response = this.serverResponseHandler.sendNewIdentityServerResponse("false", identity);
            responses.put("response", response);
        }
        return responses;
    }
    public JSONObject moveToMainHall(Client client){
        JSONObject response;
        String roomID = "MainHall-" + System.getProperty("serverID");
        ServerState.getServerStateInstance().addClientToRoom(roomID, client);
        response = clientResponseHandler.moveToRoomResponse(client.getIdentity(), "", roomID);
        return response;
    }
    public Map<String, JSONObject> leaderApprovedNewClientIdentity(String isApproved, Client client, String identity){
        Map<String, JSONObject> responses = new HashMap<>();
        if(isApproved.equals("true")){
            logger.info("New identity creation accepted");
            client.setIdentity(identity);
            client.setServer(System.getProperty("serverID"));
            client.setStatus("active");
            ChatClientService service = ServerState.getServerStateInstance().clientServices.get("1temp-"+identity); //
            service.setClient(client);
            ServerState.getServerStateInstance().clients.put(client.getIdentity(), client);
            ServerState.getServerStateInstance().clientServices.remove("1temp-"+identity, service); //
            ServerState.getServerStateInstance().clientServices.put(identity, service); //
            responses.put("client-only", clientResponseHandler.sendNewIdentityResponse("true"));
            responses.put("broadcast", moveToMainHall(client));
        }else if(isApproved.equals("false")){
            ServerState.getServerStateInstance().clientServices.get("1temp-"+identity).stop();
            ServerState.getServerStateInstance().clientServices.remove("1temp-"+identity); //
            logger.info("New identity creation rejected");
            ServerState.getServerStateInstance().clientServices.remove("1temp-"+identity); //
            responses.put("client-only", clientResponseHandler.sendNewIdentityResponse("false"));
        }
        return responses;
    }
}
