package Handlers.ChatHandler;

import Handlers.CoordinationHandler.GossipHandler;
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

public class CreateRoomHandler {

    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ClientResponseHandler clientResponseHandler;
    private final RequestHandler serverRequestHandler = new RequestHandler();
    private final GossipHandler gossipHandler = new GossipHandler();

    public CreateRoomHandler(ClientResponseHandler clientResponseHandler){
        this.clientResponseHandler = clientResponseHandler;
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
        isIdentityGood = identity!= null && identity.matches("^[a-zA-Z][a-zA-Z0-9]*$")
                && identity.length()>=3
                && identity.length()<16;
        return isIdentityGood;
    }

    public String checkRoomIdUnique(String identity, String clientID){

        String isIdentityUnique = "true";
        ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
        ServerData leaderServer = ServerState.getServerStateInstance().getLeaderServerData();
        if (Objects.equals(currentServer.getServerID(), leaderServer.getServerID())){
            ConcurrentHashMap<String, Room> rooms = LeaderState.getInstance().getGlobalRoomList();
            for (Iterator<String> it = rooms.keys().asIterator(); it.hasNext(); ) {
                String room = it.next();
                if (Objects.equals(room, identity)){
                    isIdentityUnique = "false";
                }
            }
        } else {
            JSONObject request = serverRequestHandler.sendCreateRoomResponse(identity, clientID);
            MessageTransferService.sendToServers(request, leaderServer.getServerAddress(), leaderServer.getCoordinationPort());
            isIdentityUnique = "askedFromLeader";
        }
        /*for (Iterator<String> it = ServerState.getServerStateInstance().roomList.keys().asIterator(); it.hasNext(); ) {
            String room = it.next();
            if (Objects.equals(room, identity)){
                isIdentityUnique = false;
            }
        }*/
        return isIdentityUnique;
    }

    public JSONObject moveToNewRoom(Room room, Client client){
        JSONObject response;
        String roomID = room.getRoomID();
        String formerID = new ClientListInRoomHandler().getClientsRoomID(client.getIdentity());
        ServerState.getServerStateInstance().addClientToRoom(roomID, client);
        ServerState.getServerStateInstance().removeClientFromRoom(formerID, client);
        LeaderState.getInstance().globalRoomList.put(roomID, room);
        response = clientResponseHandler.moveToRoomResponse(client.getIdentity(), formerID, roomID);
        return response;
    }

    public Map<String, JSONObject> createRoom(Client client, String roomid){
        Map<String, JSONObject> responses = new HashMap<>();
        boolean checkRoomIdRules = checkRoomIdRules(roomid);
        boolean checkOwnerUnique = checkOwnerUnique(client.getIdentity());
        ArrayList<Client> clients = new ArrayList<>();
        if (checkRoomIdRules && checkOwnerUnique) {
            String checkRoomIdUnique = checkRoomIdUnique(roomid, client.getIdentity());
            if (checkRoomIdUnique.equals("true")){
//                clients.add(client);
                Room room = new Room(roomid, System.getProperty("serverID"), client.getIdentity(), clients);
                ServerState.getServerStateInstance().roomList.put(roomid, room);
//                LeaderState.getInstance().globalRoomList.put(roomid, room);
                logger.info("New room creation accepted");
                responses.put("client-only", clientResponseHandler.sendNewRoomResponse(roomid, "true"));
                responses.put("broadcast", moveToNewRoom(room, client));
                System.out.println(LeaderState.getInstance().globalRoomList.get("room1").getClients());
                JSONObject gossipMsg = this.gossipHandler.gossipRoom("gossiproom", System.getProperty("serverID"), LeaderState.getInstance().getGlobalRoomList());
                responses.put("gossip", gossipMsg);
            } else if(checkRoomIdUnique.equals("askedFromLeader")){
                logger.info("Asked from leader");
                responses.put("askedFromLeader", null);
            } else {
                logger.info("New room creation rejected");
                responses.put("client-only", clientResponseHandler.sendNewRoomResponse(roomid, "false"));
            }
        } else {
            logger.info("New room creation rejected");
            responses.put("client-only", clientResponseHandler.sendNewRoomResponse(roomid, "false"));
        }
        return responses;
    }
}
