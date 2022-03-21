package Handlers.ChatHandler;

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
    private final ResponseHandler clientResponseHandler;
    private final RequestHandler serverRequestHandler = new RequestHandler();

    public NewIdentityHandler(ResponseHandler responseHandler){
        this.clientResponseHandler = responseHandler;
    }

    public boolean checkIdentityRules(String identity){
        boolean isIdentityGood;
        isIdentityGood = identity!= null && identity.matches("^[a-zA-Z][a-zA-Z0-9]*$")
                && identity.length()>=3
                && identity.length()<16;
        return isIdentityGood;
    }

    public boolean checkIdentityUnique(String identity) {
        boolean isIdentityUnique = true;
        // TODO: Check identity from all servers.
        ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
        ServerData leaderServer = ServerState.getServerStateInstance().getLeaderServerData();
        if (Objects.equals(currentServer.getServerID(), leaderServer.getServerID())){
            ConcurrentHashMap<String, Client> clients = LeaderState.getInstance().getGlobalClients();
            for (Iterator<String> it = clients.keys().asIterator(); it.hasNext(); ) {
                String client = it.next();
                if (Objects.equals(client, identity)){
                    isIdentityUnique = false;
                }
            }
        } else {
            JSONObject request = serverRequestHandler.sendNewIdentityResponse(identity);
            MessageTransferService.sendToServers(request, leaderServer.getServerAddress(), leaderServer.getCoordinationPort());
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
        if (checkIdentityUnique(identity) && checkIdentityRules(identity)){
            client.setIdentity(identity);
            client.setServer(System.getProperty("serverID"));
            client.setStatus("active");
            ServerState.getServerStateInstance().clientServices.put(identity, service);
            ServerState.getServerStateInstance().clients.put(identity, client);
            logger.info("New identity creation accepted");
            responses.put("client-only", clientResponseHandler.sendNewIdentityResponse("true"));
            responses.put("broadcast", moveToMainHall(client));
        } else {
            logger.info("New identity creation rejected");
            responses.put("client-only", clientResponseHandler.sendNewIdentityResponse("false"));
        }
        return responses;
    }
}
