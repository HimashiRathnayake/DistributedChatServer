package Handlers.ChatHandler;

import Models.Client;
import Models.Server.ServerState;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.*;

public class JoinRoomHandler {

    ClientListInRoomHandler clientListInRoomHandler= new ClientListInRoomHandler();
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ClientResponseHandler clientResponseHandler;
    String currentChatRoom = "";

    public JoinRoomHandler(ClientResponseHandler clientResponseHandler){
        this.clientResponseHandler = clientResponseHandler;
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

        return currentRoomServer.equals(newRoomServer);
    }

    public JSONObject moveToNewRoom(String former, String roomid, Client client){
        JSONObject response;
        ServerState.getServerStateInstance().addClientToRoom(roomid, client);
        ServerState.getServerStateInstance().removeClientFromRoom(former, client);
        response = clientResponseHandler.moveToRoomResponse(client.getIdentity(), currentChatRoom, roomid);
        return response;
    }

    public Map<String, JSONObject> joinRoom(Client client, String roomid){
        Map<String, JSONObject> responses = new HashMap<>();
        currentChatRoom = clientListInRoomHandler.getClientsRoomID(client.getIdentity());
        String currentRoomServer = ServerState.getServerStateInstance().roomList.get(currentChatRoom).getServer();

        if (checkRoomIdExist(roomid) && checkClientisOwner(client.getIdentity(), currentChatRoom)) {
            String newRoomServer = ServerState.getServerStateInstance().roomList.get(roomid).getServer();
            if (checkRoomsinSameServer(currentRoomServer, newRoomServer)) {
                logger.info("Join room within same server is accepted");
                JSONObject roomChangedResponse = moveToNewRoom(currentChatRoom, roomid, client);
                responses.put("broadcast",roomChangedResponse);
            }
            else {
                logger.info("Redirecting to another server");
                //TODO: get address and listening port of the server containing the new roomid
                //String host = ServerState.getServerStateInstance().getCurrentServerData().getServerAddress();
                //int port = ServerState.getServerStateInstance().getCurrentServerData().getClientPort();
                //responses.add(responseHandler.sendNewRouteMessage(roomid, host, Integer.toString(port)));
                //remove clients from serverlist
                //ServerState.getServerStateInstance().clients.remove(client.getIdentity());
                //responses.add(moveToNewRoom(currentChatRoom, roomid, client));
                JSONObject roomChangedResponse = moveToNewRoom(currentChatRoom, roomid, client);
                responses.put("broadcast",roomChangedResponse);
            }

        } else {
            logger.info("Join room rejected");
            JSONObject roomChangedResponse = this.clientResponseHandler.broadCastRoomChange(client.getIdentity(),roomid,roomid);
            responses.put("client-only",roomChangedResponse);
        }
        return responses;
    }

}
