package Handlers.ChatHandler;

import Models.Client;
import Models.Room;
import Models.Server.ServerState;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.concurrent.ConcurrentMap;

public class MoveJoinHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);

    public MoveJoinHandler(){}

    public JSONObject movejoin(String formerRoomID, String joinRoomID, String ClientID, Client client){
        JSONObject response;
        ConcurrentMap<String, Room> roomList = ServerState.getServerStateInstance().getRoomList();
        if(roomList.containsKey(joinRoomID)){
            Room formerRoom = roomList.get(formerRoomID);
            formerRoom.removeClient(ClientID);    // client is removed from the former room
            Room joinRoom = roomList.get(joinRoomID);
            joinRoom.addClient(client);


        }else{
            logger.error("former room is not exist");
            // TODO: should check from the global room list and act accordingly
        }
        return null;
    }

}
