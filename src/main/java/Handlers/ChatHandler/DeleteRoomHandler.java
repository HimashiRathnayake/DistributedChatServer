package Handlers.ChatHandler;

import Models.Client;
import Models.Room;
import Models.Server.ServerState;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.concurrent.ConcurrentMap;

public class DeleteRoomHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ResponseHandler responseHandler;

    public DeleteRoomHandler(ResponseHandler responseHandler){
        this.responseHandler = responseHandler;
    }

    public JSONObject deleteRoom(String roomID, Client client){
        ConcurrentMap<String, Room> roomList = ServerState.getServerStateInstance().getRoomList();
        if(roomList.containsKey(roomID)){
            Room deleteRoom = roomList.get(roomID);
            if(deleteRoom.getOwner().equals(client.getIdentity())){
                //delete
            }
        }
        JSONObject deleteRoomResponse = responseHandler.deleteRoomResponse(roomID, false);
        //reject
        return deleteRoomResponse;

    }
}
