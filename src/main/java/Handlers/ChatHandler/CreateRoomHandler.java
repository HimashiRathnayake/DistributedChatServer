package Handlers.ChatHandler;

import Models.Client;
import Models.Room;
import Models.Server.ServerState;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.*;

public class CreateRoomHandler {

    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ResponseHandler responseHandler;

    public CreateRoomHandler(ResponseHandler responseHandler){
        this.responseHandler = responseHandler;
    }

    public boolean checkOwnerUnique(String owner){
        boolean isUniqueOwner = true;
        for (Iterator<Room> it = ServerState.getServerStateInstance().roomList.elements().asIterator(); it.hasNext(); ) {
            Room room = it.next();
            if (Objects.equals(room.getOwner(), owner)){
                isUniqueOwner = false;
            }
        }
        return isUniqueOwner;
    }

    public boolean checkRoomIdRules(String identity){
        boolean isIdentityGood;
        isIdentityGood = identity!= null && identity.matches("^[a-zA-Z0-9]*$")
                && identity.length()>=3
                && identity.length()<16;
        return isIdentityGood;
    }

    public boolean checkRoomIdUnique(String identity){

        boolean isIdentityUnique = true;
        // TODO: Check identity from all servers.
        for (Iterator<String> it = ServerState.getServerStateInstance().roomList.keys().asIterator(); it.hasNext(); ) {
            String room = it.next();
            if (Objects.equals(room, identity)){
                isIdentityUnique = false;
            }
        }
        return isIdentityUnique;
    }

    public JSONObject moveToNewRoom(Room room, Client client){
        JSONObject response;
        String roomID = room.getRoomID();
        String formerID = new ClientListInRoomHandler().getClientsRoomID(client.getIdentity());
        ServerState.getServerStateInstance().addClientToRoom(roomID, client);
        ServerState.getServerStateInstance().removeClientFromRoom(formerID, client);
        response = responseHandler.moveToRoomResponse(client.getIdentity(), formerID, roomID);
        return response;
    }

    public Map<String, JSONObject> createRoom(Client client, String roomid){
        Map<String, JSONObject> responses = new HashMap<>();
        ArrayList<Client> clients = new ArrayList<>();
        if (checkOwnerUnique(client.getIdentity()) && checkRoomIdUnique(roomid)
                && checkRoomIdRules(roomid)){
            Room room = new Room(roomid, System.getProperty("serverID"), client.getIdentity(), clients);
            ServerState.getServerStateInstance().roomList.put(roomid, room);
            logger.info("New room creation accepted");
            JSONObject createRoomResponse = this.responseHandler.sendNewRoomResponse(roomid, "true");
            JSONObject roomChangedResponse = moveToNewRoom(room, client);
            responses.put("client-only",createRoomResponse);
            responses.put("broadcast",roomChangedResponse);

        } else {
            logger.info("New room creation rejected");
            JSONObject createRoomResponse = this.responseHandler.sendNewRoomResponse(roomid, "false");
            responses.put("client-only",createRoomResponse);
        }
        return responses;
    }
}
