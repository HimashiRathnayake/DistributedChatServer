package Handlers.ChatHandler;

import Models.Client;
import Models.Room;
import Models.Server.ServerState;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class DeleteRoomHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ResponseHandler responseHandler;

    public DeleteRoomHandler(ResponseHandler responseHandler){
        this.responseHandler = responseHandler;
    }

    public Map<String, JSONObject> deleteRoom(String roomID, Client client){
        Map<String, JSONObject> responses = new HashMap<>();
        ConcurrentMap<String, Room> roomList = ServerState.getServerStateInstance().getRoomList();
        if(roomList.containsKey(roomID)){
            Room deleteRoom = roomList.get(roomID);
            if(deleteRoom.getOwner().equals(client.getIdentity())){
                ArrayList<Client> deleteRoomClients = deleteRoom.getClients();
                //delete room
                ServerState.getServerStateInstance().roomList.remove(deleteRoom.getRoomID());
                // TODO: broadcast delete to other servers
                // JSONObject broadcastDelete = this.responseHandler.broadcastServersDeleteRoomResponse(System.getProperty("serverID"), roomID);
                // Move to MainHall
                ServerState.getServerStateInstance().roomList.get(deleteRoom.getRoomID()).addClientList(deleteRoomClients);
                JSONObject broadcastRoomChange = this.responseHandler
                        .broadCastRoomChange(client.getIdentity(), "deletedRoom", "MainHall-"+System.getProperty("serverID"));
                // Response to approve
                JSONObject approveResponse = this.responseHandler.deleteRoomResponse(roomID, "true");
                responses.put("broadcastRoomChange", broadcastRoomChange);
                responses.put("approve", approveResponse);
                return responses;
            }
        }
        // Response to reject
        JSONObject rejectResponse = this.responseHandler.deleteRoomResponse(roomID, "false");
        responses.put("reject", rejectResponse);
        return responses;

    }
}
