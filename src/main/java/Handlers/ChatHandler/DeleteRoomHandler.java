package Handlers.ChatHandler;

import Handlers.CoordinationHandler.GossipHandler;
import Handlers.CoordinationHandler.ResponseHandler;
import Models.Client;
import Models.Room;
import Models.Server.LeaderState;
import Models.Server.ServerState;
import Services.CoordinationService.GossipService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class DeleteRoomHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ClientResponseHandler clientResponseHandler;
    private final ResponseHandler serverResponseHandler = new ResponseHandler();
    private final GossipHandler gossipHandler = new GossipHandler();

    public DeleteRoomHandler(ClientResponseHandler clientResponseHandler){
        this.clientResponseHandler = clientResponseHandler;
    }

    public Map<String, ArrayList<JSONObject>> deleteRoom(String roomID, Client client){
        Map<String, ArrayList<JSONObject>> responses = new HashMap<>();
        ConcurrentMap<String, Room> roomList = ServerState.getServerStateInstance().getRoomList();
        ArrayList<JSONObject> broadcastClientResponse = new ArrayList<>();
        ArrayList<JSONObject> clientOnlyResponse = new ArrayList<>();
        ArrayList<JSONObject> broadcastServerResponse = new ArrayList<>();
        ArrayList<JSONObject> gossipResponse = new ArrayList<>();
        if(roomList.containsKey(roomID)){
            Room deleteRoom = roomList.get(roomID);
            if(deleteRoom.getOwner().equals(client.getIdentity())){
                ArrayList<Client> deleteRoomClients = deleteRoom.getClients();
                //delete room
                ServerState.getServerStateInstance().roomList.remove(deleteRoom.getRoomID());
                JSONObject serverBroadcastRespond = serverResponseHandler.deleteRoomServerRespond(System.getProperty("serverID"), roomID);
                broadcastServerResponse.add(serverBroadcastRespond);
                responses.put("broadcastServers", broadcastServerResponse);

                // Move to MainHall
                ServerState.getServerStateInstance().roomList.get("MainHall-"+System.getProperty("serverID")).addClientList(deleteRoomClients);
                for(Client movingClient: deleteRoomClients){
                    JSONObject broadcastRoomChange = this.clientResponseHandler
                            .broadCastRoomChange(movingClient.getIdentity(), "deletedRoom", "MainHall-"+System.getProperty("serverID"));
                    broadcastClientResponse.add(broadcastRoomChange);
                }
                // Response to approve
                JSONObject approveResponse = this.clientResponseHandler.deleteRoomResponse(roomID, "true");
                responses.put("broadcastClients", broadcastClientResponse);
                clientOnlyResponse.add(approveResponse);
                responses.put("client-only", clientOnlyResponse);

                String currentServer = ServerState.getServerStateInstance().getCurrentServerData().getServerID();
                String leaderserver = ServerState.getServerStateInstance().getLeaderServerData().getServerID();
                if (currentServer.equals(leaderserver)) {
                    LeaderState.getInstance().getGlobalRoomList().remove(roomID);
                    JSONObject gossipMsg = this.gossipHandler.gossipRoom("gossiproom", System.getProperty("serverID"), LeaderState.getInstance().globalRoomList);
                    gossipResponse.add(gossipMsg);
                    responses.put("gossip", gossipResponse);
                }
                return responses;
            }
        }
        // Response to reject
        JSONObject rejectResponse = this.clientResponseHandler.deleteRoomResponse(roomID, "false");
        clientOnlyResponse.add(rejectResponse);
        responses.put("client-only", clientOnlyResponse);
        return responses;

    }
}
