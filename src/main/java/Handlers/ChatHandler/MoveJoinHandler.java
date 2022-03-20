package Handlers.ChatHandler;

import Models.Client;
import Models.Room;
import Models.Server.ServerState;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class MoveJoinHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ResponseHandler responseHandler;

    public MoveJoinHandler(ResponseHandler responseHandler){
        this.responseHandler = responseHandler;
    }

    public Map<String, JSONObject> movejoin(String formerRoomID, String joinRoomID, String ClientID, Client client){
        Map<String, JSONObject> responses = new ArrayList<>();
        ConcurrentMap<String, Room> roomList = ServerState.getServerStateInstance().getRoomList();
        if(roomList.containsKey(joinRoomID)){
            Room formerRoom = roomList.get(formerRoomID);
            formerRoom.removeClient(ClientID);    // client is removed from the former room
            Room joinRoom = roomList.get(joinRoomID);
            joinRoom.addClient(client);
            JSONObject serverChangedResponse = this.responseHandler.serverChangedResponse();
            JSONObject roomChangedResponse = this.responseHandler.broadCastRoomChange(ClientID,formerRoomID,joinRoomID);
            responses.put("client-only",serverChangedResponse);
            responses.put("broadcast",roomChangedResponse);
            return responses;
        }else{
            logger.error("former room is not exist");
            // TODO: should check from the global room list and act accordingly
            return null;
        }
    }

}
