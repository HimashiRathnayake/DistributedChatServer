package Handlers.CoordinationHandler;

import Handlers.ChatHandler.ClientListInRoomHandler;
import Handlers.ChatHandler.ClientResponseHandler;
import Models.Client;
import Models.Room;
import Models.Server.LeaderState;
import Models.Server.ServerData;
import Models.Server.ServerState;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CreateRoomHandler {

    private final Logger logger = Logger.getLogger(Handlers.ChatHandler.NewIdentityHandler.class);
    private final RequestHandler serverRequestHandler = new RequestHandler();
    private final ResponseHandler serverResponseHandler = new ResponseHandler();
    private final ClientResponseHandler clientResponseHandler = new ClientResponseHandler();
    private final GossipHandler gossipHandler = new GossipHandler();

    public boolean checkRoomIdUnique(String identity){

        boolean isIdentityUnique = true;
        ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
        ServerData leaderServer = ServerState.getServerStateInstance().getLeaderServerData();
        if (Objects.equals(currentServer.getServerID(), leaderServer.getServerID())) {
            ConcurrentHashMap<String, Room> rooms = LeaderState.getInstance().getGlobalRoomList();
            for (Iterator<String> it = rooms.keys().asIterator(); it.hasNext(); ) {
                String room = it.next();
                if (Objects.equals(room, identity)) {
                    isIdentityUnique = false;
                }
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
        response = clientResponseHandler.moveToRoomResponse(client.getIdentity(), formerID, roomID);
        return response;
    }

    public Map<String, JSONObject> coordinatorNewRoomIdentity(String clientID, String roomid, String serverID) {
        Map<String, JSONObject> responses = new HashMap<>();
        JSONObject response;
        if (checkRoomIdUnique(roomid)) {
            ArrayList<Client> clients = new ArrayList<>();
            /*Client client = new Client();
            client.setServer(serverID);
            client.setIdentity(clientID);
            client.setStatus("active");
            clients.add(client);*/
            Room room = new Room(roomid, serverID, clientID, clients);
            LeaderState.getInstance().addRoomToGlobalList(room);
            logger.info("New room creation accepted");
            response = this.serverResponseHandler.sendCreateRoomServerResponse("true", roomid, clientID);
            responses.put("response", response);
            JSONObject gossipMsg = this.gossipHandler.gossipRoom("gossiproom", System.getProperty("serverID"), LeaderState.getInstance().getGlobalRoomList());
            responses.put("gossip", gossipMsg);
        } else {
            logger.info("New room creation rejected");
            response = this.serverResponseHandler.sendCreateRoomServerResponse("false", roomid, clientID);
            responses.put("response", response);
        }
        return responses;
    }

    public Map<String, JSONObject> leaderApprovedNewRoomIdentity(String isApproved, Client client, String roomid){
        Map<String, JSONObject> responses = new HashMap<>();
        if(isApproved.equals("true")){
            logger.info("New room creation accepted");
            ArrayList<Client> clients = new ArrayList<>();
            Room room = new Room(roomid, System.getProperty("serverID"), client.getIdentity(), clients);
            ServerState.getServerStateInstance().roomList.put(roomid, room);
            responses.put("client-only", clientResponseHandler.sendNewRoomResponse(roomid, "true"));
            responses.put("broadcast", moveToNewRoom(room, client));
        }else if(isApproved.equals("false")){
            logger.info("New room creation rejected");
            JSONObject createRoomResponse = this.clientResponseHandler.sendNewRoomResponse(roomid, "false");
            responses.put("client-only",createRoomResponse);
        }
        return responses;
    }
}