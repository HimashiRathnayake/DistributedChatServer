package Handlers.ChatHandler;

import Models.Client;
import Models.Room;
import Models.Server.ServerState;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class MoveJoinHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ClientResponseHandler clientResponseHandler;

    public MoveJoinHandler(ClientResponseHandler clientResponseHandler){
        this.clientResponseHandler = clientResponseHandler;
    }

    public Map<String, JSONObject> movejoin(String formerRoomID, String joinRoomID, String ClientID, Client client){
        Map<String, JSONObject> responses = new HashMap<>();
        ConcurrentMap<String, Room> roomList = ServerState.getServerStateInstance().getRoomList();
        Room joinRoom;
        if(roomList.containsKey(joinRoomID)){
            joinRoom= roomList.get(joinRoomID);
        }else{
            logger.error("Join room is not exist and move client to the main hall");
            joinRoom = roomList.get("MainHall-"+System.getProperty("serverID"));
        }
        joinRoom.addClient(client);
        JSONObject serverChangedResponse = this.clientResponseHandler.serverChangedResponse();
        JSONObject roomChangedResponse = this.clientResponseHandler.broadCastRoomChange(ClientID,formerRoomID,joinRoom.getRoomID());
        logger.info("Server Change");
        responses.put("client-only",serverChangedResponse);
        responses.put("broadcast",roomChangedResponse);
        return responses;
    }

}
