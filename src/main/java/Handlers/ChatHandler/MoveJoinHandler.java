package Handlers.ChatHandler;

import Models.Client;
import Models.Room;
import Models.Server.ServerState;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class MoveJoinHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ResponseHandler responseHandler;

    public MoveJoinHandler(ResponseHandler responseHandler){
        this.responseHandler = responseHandler;
    }

    public Map<String, JSONObject> movejoin(String formerRoomID, String joinRoomID, String ClientID, Client client){
        Map<String, JSONObject> responses = new HashMap<>();
        ConcurrentMap<String, Room> roomList = ServerState.getServerStateInstance().getRoomList();
        Room joinRoom;
        if(roomList.containsKey(joinRoomID)){
//            Room formerRoom = roomList.get(formerRoomID);
//            formerRoom.removeClient(ClientID);    // client is removed from the former room
            joinRoom= roomList.get(joinRoomID);
        }else{
            logger.error("Join room is not exist and move client to the main hall");
            joinRoom = roomList.get("MainHall-"+System.getProperty("serverID"));
        }
        joinRoom.addClient(client);
        JSONObject serverChangedResponse = this.responseHandler.serverChangedResponse();
        JSONObject roomChangedResponse = this.responseHandler.broadCastRoomChange(ClientID,formerRoomID,joinRoom.getRoomID());
        responses.put("client-only",serverChangedResponse);
        responses.put("broadcast",roomChangedResponse);
        return responses;
    }

}
