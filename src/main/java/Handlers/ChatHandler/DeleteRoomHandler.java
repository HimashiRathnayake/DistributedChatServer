package Handlers.ChatHandler;

import Models.Client;
import Models.Room;
import Models.Server.ServerState;
import Services.ChatService.ChatClientService;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import java.util.List;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class DeleteRoomHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ResponseHandler responseHandler;

    public DeleteRoomHandler(ResponseHandler responseHandler){
        this.responseHandler = responseHandler;
    }

    public Map<String, ArrayList<JSONObject>> deleteRoom(String roomID, Client client){
        Map<String, ArrayList<JSONObject>> responses = new HashMap<>();
        ConcurrentMap<String, Room> roomList = ServerState.getServerStateInstance().getRoomList();
        ArrayList<JSONObject> broadcastResponse = new ArrayList<>();
        ArrayList<JSONObject> clientOnlyResponse = new ArrayList<>();
        if(roomList.containsKey(roomID)){
            Room deleteRoom = roomList.get(roomID);
            if(deleteRoom.getOwner().equals(client.getIdentity())){
                ArrayList<Client> deleteRoomClients = deleteRoom.getClients();
                //delete room
                ServerState.getServerStateInstance().roomList.remove(deleteRoom.getRoomID());
                // TODO: broadcast delete to other servers
                // JSONObject broadcastDelete = this.responseHandler.broadcastServersDeleteRoomResponse(System.getProperty("serverID"), roomID);
                // Move to MainHall
                ServerState.getServerStateInstance().roomList.get("MainHall-"+System.getProperty("serverID")).addClientList(deleteRoomClients);

                broadcastResponse.add( this.responseHandler
                        .broadCastRoomChange(client.getIdentity(), "deletedRoom", "MainHall-"+System.getProperty("serverID")));
                for(Client movingClient: deleteRoomClients){
                    JSONObject broadcastRoomChange = this.responseHandler
                            .broadCastRoomChange(movingClient.getIdentity(), "deletedRoom", "MainHall-"+System.getProperty("serverID"));
                    broadcastResponse.add(broadcastRoomChange);
                }
                // Response to approve
                JSONObject approveResponse = this.responseHandler.deleteRoomResponse(roomID, "true");
                responses.put("broadcast", broadcastResponse);
                clientOnlyResponse.add(approveResponse);
                responses.put("client-only", clientOnlyResponse);
                return responses;
            }
        }
        // Response to reject
        JSONObject rejectResponse = this.responseHandler.deleteRoomResponse(roomID, "false");
        clientOnlyResponse.add(rejectResponse);
        responses.put("client-only", clientOnlyResponse);
        return responses;

    }
}
