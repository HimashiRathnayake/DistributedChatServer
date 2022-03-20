package Handlers.ChatHandler;

import Models.Client;
import Models.Room;
import Models.Server.ServerState;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class QuitHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ResponseHandler responseHandler;

    public QuitHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    public Map<String, JSONObject> handleQuit(Client client) {
        Map<String, JSONObject> responses = new HashMap<>();
        String clientID = client.getIdentity();
        ServerState.getServerStateInstance().clients.remove(clientID); // remove the client from the client list
        Room deleteRoom = ServerState.getServerStateInstance().getOwningRoom(clientID);
        if (deleteRoom == null){
            String formerRoom = ServerState.getServerStateInstance().removeClientFromRoom(client); // delete client from room
            responses.put("reply", this.responseHandler.broadCastRoomChange(clientID, formerRoom, ""));
        } else {
//            ArrayList<Client> deleteRoomClients = deleteRoom.getClients();
//            //delete room
//            ServerState.getServerStateInstance().roomList.remove(deleteRoom.getRoomID());
//            // TODO: broadcast delete to other servers
//            // JSONObject broadcastDelete = this.responseHandler.broadcastServersDeleteRoomResponse(System.getProperty("serverID"), roomID);
//            // Move to MainHall
//            ServerState.getServerStateInstance().roomList.get(deleteRoom.getRoomID()).addClientList(deleteRoomClients);
//            responses.put("broadcast", this.responseHandler
//                    .broadCastRoomChange(client.getIdentity(), "deletedRoom", "MainHall-"+System.getProperty("serverID")));
//            responses.put("reply", this.responseHandler.broadCastRoomChange(clientID, "", ""));
        }
        return responses;
    }
}
