package Handlers.ChatHandler;

import Handlers.CoordinationHandler.RequestHandler;
import Models.Client;
import Models.Room;
import Models.Server.LeaderState;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JoinRoomHandler {

    ClientListInRoomHandler clientListInRoomHandler= new ClientListInRoomHandler();
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ClientResponseHandler clientResponseHandler;
    String currentChatRoom = "";
    private final RequestHandler serverRequestHandler = new RequestHandler();
    String serverid = "";

    public JoinRoomHandler(ClientResponseHandler clientResponseHandler){
        this.clientResponseHandler = clientResponseHandler;
    }

    public String checkRoomIdExist(String identity, String clientID){

        String isIdentityExist = "false";
        ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
        ServerData leaderServer = ServerState.getServerStateInstance().getLeaderServerData();
        if (Objects.equals(currentServer.getServerID(), leaderServer.getServerID())){
            ConcurrentHashMap<String, Room> rooms = LeaderState.getInstance().getGlobalRoomList();
            for (Iterator<String> it = rooms.keys().asIterator(); it.hasNext(); ) {
                String room = it.next();
                if (Objects.equals(room, identity)){
                    isIdentityExist = "true";
                }
            }
        } else {
            JSONObject request = serverRequestHandler.sendRoomExistResponse(identity, clientID);
            MessageTransferService.sendToServers(request, leaderServer.getServerAddress(), leaderServer.getCoordinationPort());
            isIdentityExist = "askedFromLeader";
        }
        return isIdentityExist;
    }

    public String getJoinRoomServerData(String roomid, String clientID) {
        String isJoinRoomServerDataExist = "false";
        ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
        ServerData leaderServer = ServerState.getServerStateInstance().getLeaderServerData();
        if (Objects.equals(currentServer.getServerID(), leaderServer.getServerID())){
            serverid = LeaderState.getInstance().getGlobalRoomList().get(roomid).getServer();
            isJoinRoomServerDataExist = "true";
        } else {
            JSONObject request = serverRequestHandler.sendJoinRoomResponse(roomid, clientID);
            MessageTransferService.sendToServers(request, leaderServer.getServerAddress(), leaderServer.getCoordinationPort());
            isJoinRoomServerDataExist = "askedFromLeader";
        }
        return isJoinRoomServerDataExist;
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
        response = clientResponseHandler.moveToRoomResponse(client.getIdentity(), former, roomid);
        return response;
    }

    public Map<String, JSONObject> joinRoom(Client client, String roomid){
        Map<String, JSONObject> responses = new HashMap<>();
        currentChatRoom = clientListInRoomHandler.getClientsRoomID(client.getIdentity());
        String currentRoomOwner = clientListInRoomHandler.getRoomOwner(currentChatRoom);
        String currentRoomServer = ServerState.getServerStateInstance().roomList.get(currentChatRoom).getServer();

        if (!currentRoomOwner.equals(client.getIdentity())) {
            String isRoomExist = checkRoomIdExist(roomid, client.getIdentity());
            if (isRoomExist.equals("true")) {
                //String newRoomServer = ServerState.getServerStateInstance().roomList.get(roomid).getServer();
                Room newRoom = ServerState.getServerStateInstance().roomList.get(roomid);
                if (newRoom !=null) {
                    logger.info("Join room within same server is accepted");
                    JSONObject roomChangedResponse = moveToNewRoom(currentChatRoom, roomid, client);
                    responses.put("broadcast-all",roomChangedResponse);
                }
                else {
                    String getJoinRoomServerData = getJoinRoomServerData(roomid, client.getIdentity());
                    if (getJoinRoomServerData.equals("true")) {
                        ServerData serverData = ServerState.getServerStateInstance().getServersList().get(serverid);
                        String host = serverData.getServerAddress();
                        String port = Integer.toString(serverData.getClientPort());
                        JSONObject routeResponse = this.clientResponseHandler.sendNewRouteMessage(roomid, host, port);
                        JSONObject roomChangeResponse = this.clientResponseHandler.broadCastRoomChange(client.getIdentity(), currentChatRoom, roomid);
                        ServerState.getServerStateInstance().removeClientFromRoom(currentChatRoom, client);
                        ServerState.getServerStateInstance().clients.remove(client.getIdentity());
                        responses.put("client-only",routeResponse);
                        responses.put("broadcast-former",roomChangeResponse);
                    } else if (getJoinRoomServerData.equals("askedFromLeader")) {
                        logger.info("Asked from leader");
                        responses.put("askedFromLeader", null);
                    }
                }
            } else if (isRoomExist.equals("askedFromLeader")) {
                logger.info("Asked from leader");
                responses.put("askedFromLeader", null);
            } else {
                logger.info("Join room rejected - room not exist");
                JSONObject roomChangedResponse = this.clientResponseHandler.broadCastRoomChange(client.getIdentity(),roomid,roomid);
                responses.put("client-only",roomChangedResponse);
            }
        } else {
            logger.info("Join room rejected - Current room owner");
            JSONObject roomChangedResponse = this.clientResponseHandler.broadCastRoomChange(client.getIdentity(),roomid,roomid);
            responses.put("client-only",roomChangedResponse);
        }
        return responses;
    }

}
