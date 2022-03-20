package Handlers.ChatHandler;

import Models.Client;
import Models.Server.ServerState;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class JoinRoomHandler {

    ClientListInRoomHandler clientListInRoomHandler= new ClientListInRoomHandler();
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ResponseHandler responseHandler;
    String currentChatRoom = "";

    public JoinRoomHandler(ResponseHandler responseHandler){
        this.responseHandler = responseHandler;
    }

    public boolean checkRoomIdExist(String identity){

        boolean isIdentityExist = false;
        // TODO: Check identity from all servers.
        for (Iterator<String> it = ServerState.getServerStateInstance().roomList.keys().asIterator(); it.hasNext(); ) {
            String room = it.next();
            if (Objects.equals(room, identity)){
                isIdentityExist = true;
                break;
            }
        }
        return isIdentityExist;
    }

    public boolean checkClientisOwner(String clientid, String roomid){

        boolean isNotowner = true;
        String roomOwner = clientListInRoomHandler.getRoomOwner(roomid);

        if (roomOwner.equals(clientid)) {
            isNotowner = false;
        }
        return isNotowner;
    }

    public boolean checkRoomsinSameServer(String currentRoomServer, String newRoomServer){

        boolean isInSameServer = false;

        if (currentRoomServer.equals(newRoomServer)) {
            isInSameServer = true;
        }
        return isInSameServer;
    }

    public JSONObject moveToNewRoom(String former, String roomid, Client client){
        JSONObject response;
        ServerState.getServerStateInstance().addClientToRoom(roomid, client);
        ServerState.getServerStateInstance().removeClientFromRoom(former, client);
        response = responseHandler.moveToRoomResponse(client.getIdentity(), currentChatRoom, roomid);
        return response;
    }

    public ArrayList<JSONObject> joinRoom(Client client, String roomid){

        ArrayList<JSONObject> responses = new ArrayList<>();
        currentChatRoom = clientListInRoomHandler.getClientsRoomID(client.getIdentity());
        String currentRoomServer = ServerState.getServerStateInstance().roomList.get(currentChatRoom).getServer();
        String newRoomServer = ServerState.getServerStateInstance().roomList.get(roomid).getServer();

        if (checkRoomIdExist(roomid) && checkClientisOwner(client.getIdentity(), currentChatRoom)) {
            if (checkRoomsinSameServer(currentRoomServer, newRoomServer)) {
                logger.info("Join room within same server is accepted");
                responses.add(moveToNewRoom(currentChatRoom, roomid, client));
            }
            else {
                logger.info("Redirecting to another server");
                //TODO: get address and listening port of the server containing the new roomid
                //String host = ServerState.getServerStateInstance().getCurrentServerData().getServerAddress();
                //int port = ServerState.getServerStateInstance().getCurrentServerData().getClientPort();
                //responses.add(responseHandler.sendNewRouteMessage(roomid, host, Integer.toString(port)));
                //remove clients from serverlist
                ServerState.getServerStateInstance().clients.remove(client.getIdentity());
                responses.add(moveToNewRoom(currentChatRoom, roomid, client));
            }

        } else {
            logger.info("Join room rejected");
            responses.add(moveToNewRoom(currentChatRoom, currentChatRoom, client));
        }
        return responses;
    }


}
