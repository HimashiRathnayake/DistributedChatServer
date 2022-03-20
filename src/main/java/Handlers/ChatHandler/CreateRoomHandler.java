package Handlers.ChatHandler;

import Models.Client;
import Models.Room;
import Models.Server.ServerState;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

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

    public ArrayList<JSONObject> createRoom(Client client, String roomid){

        ArrayList<JSONObject> responses = new ArrayList<>();
        ArrayList<Client> clients = new ArrayList<>();
        if (checkOwnerUnique(client.getIdentity()) && checkRoomIdUnique(roomid)
                && checkRoomIdRules(roomid)){
            Room room = new Room(roomid, System.getProperty("serverID"), client.getIdentity(), clients);
            ServerState.getServerStateInstance().roomList.put(roomid, room);
            logger.info("New room creation accepted");
            responses.add(responseHandler.sendNewRoomResponse(roomid, "true"));
            responses.add(moveToNewRoom(room, client));

        } else {
            logger.info("New room creation rejected");
            responses.add(responseHandler.sendNewRoomResponse(roomid,"false"));
        }
        return responses;
    }
}
