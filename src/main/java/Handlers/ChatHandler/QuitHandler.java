package Handlers.ChatHandler;

import Models.Client;
import Models.Room;
import Models.Server.ServerState;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuitHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ClientResponseHandler clientResponseHandler;

    public QuitHandler(ClientResponseHandler clientResponseHandler) {
        this.clientResponseHandler = clientResponseHandler;
    }

    public Map<String, ArrayList<JSONObject>> handleQuit(Client client) {

        String clientID = client.getIdentity();
        ServerState.getServerStateInstance().clients.remove(clientID); // remove the client from the client list
        Room deleteRoom = ServerState.getServerStateInstance().getOwningRoom(clientID);
        Map<String, ArrayList<JSONObject>> responses = new HashMap<>();
        ArrayList<JSONObject> broadcastResponse = new ArrayList<>();
        ArrayList<JSONObject> clientOnlyResponse = new ArrayList<>();
        ArrayList<JSONObject> replyResponse = new ArrayList<>();

        if (deleteRoom == null){
            String formerRoom = ServerState.getServerStateInstance().removeClientFromRoomWithFormerRoom(client); // delete client from room
            clientOnlyResponse.add(this.clientResponseHandler.broadCastRoomChange(clientID, formerRoom, ""));
            responses.put("client-only", clientOnlyResponse);
        } else {
            ArrayList<Client> deleteRoomClients = deleteRoom.getClients();
            deleteRoomClients.remove(client);
            //delete room
            ServerState.getServerStateInstance().roomList.remove(deleteRoom.getRoomID());
            // TODO: broadcast delete to other servers
            // JSONObject broadcastDelete = this.responseHandler.broadcastServersDeleteRoomResponse(System.getProperty("serverID"), roomID);
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
            responses.put("broadcast", broadcastResponse);
        }
        return responses;
    }
}
