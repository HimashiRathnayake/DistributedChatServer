package Handlers.ChatHandler;

import Handlers.CoordinationHandler.GossipHandler;
import Handlers.CoordinationHandler.RequestHandler;
import Handlers.CoordinationHandler.ResponseHandler;
import Models.Client;
import Models.Room;
import Models.Server.LeaderState;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuitHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ClientResponseHandler clientResponseHandler;
    private final ResponseHandler serverResponseHandler = new ResponseHandler();
    private final RequestHandler serverRequestHandler = new RequestHandler();
    private final GossipHandler gossipHandler = new GossipHandler();

    public QuitHandler(ClientResponseHandler clientResponseHandler) {
        this.clientResponseHandler = clientResponseHandler;
    }

    public Map<String, ArrayList<JSONObject>> handleQuit(Client client) {

        Map<String, ArrayList<JSONObject>> responses = new HashMap<>();
        ArrayList<JSONObject> broadcastResponse = new ArrayList<>();
        ArrayList<JSONObject> clientOnlyResponse = new ArrayList<>();
        ArrayList<JSONObject> replyResponse = new ArrayList<>();
        ArrayList<JSONObject> broadcastServerResponse = new ArrayList<>();
        ArrayList<JSONObject> gossipResponse = new ArrayList<>();
        String clientID = client.getIdentity();
        ServerState.getServerStateInstance().clients.remove(clientID); // remove the client from the client list
        if (ServerState.getServerStateInstance().isCurrentServerLeader()) {
            LeaderState.getInstance().globalClients.remove(clientID);
            JSONObject gossipMsg = this.gossipHandler.gossip("gossipidentity", System.getProperty("serverID"), LeaderState.getInstance().globalClients);
            gossipResponse.add(gossipMsg);
            responses.put("gossip", gossipResponse);
        } else {
            ServerData leaderServer = ServerState.getServerStateInstance().getLeaderServerData();
            MessageTransferService.sendToServers(serverRequestHandler.sendQuitClientResponse(clientID), leaderServer.getServerAddress(), leaderServer.getCoordinationPort());
        }
        Room deleteRoom = ServerState.getServerStateInstance().getOwningRoom(clientID);
        if (deleteRoom == null){
            String formerRoom = ServerState.getServerStateInstance().removeClientFromRoomWithFormerRoom(client); // delete client from room
            clientOnlyResponse.add(this.clientResponseHandler.broadCastRoomChange(clientID, formerRoom, ""));
            responses.put("client-only", clientOnlyResponse);
        } else {
            ArrayList<Client> deleteRoomClients = deleteRoom.getClients();
            deleteRoomClients.remove(client);
            //delete room
            ServerState.getServerStateInstance().roomList.remove(deleteRoom.getRoomID());
            JSONObject serverBroadcastResponse = serverResponseHandler.deleteRoomServerRespond(System.getProperty("serverID"), deleteRoom.getRoomID());
            broadcastServerResponse.add(serverBroadcastResponse);
            responses.put("broadcastServers", broadcastServerResponse);
            // Move to MainHall
            ServerState.getServerStateInstance().roomList.get("MainHall-"+System.getProperty("serverID")).addClientList(deleteRoomClients);
            for(Client movingClient: deleteRoomClients){
                JSONObject broadcastRoomChange = this.clientResponseHandler
                        .broadCastRoomChange(movingClient.getIdentity(), "deletedRoom", "MainHall-"+System.getProperty("serverID"));
                broadcastResponse.add(broadcastRoomChange);
            }
            replyResponse.add(this.clientResponseHandler
                    .broadCastRoomChange(client.getIdentity(), "deletedRoom", ""));
            responses.put("reply", replyResponse);
            responses.put("broadcastClients", broadcastResponse);
        }
        return responses;
    }
}
