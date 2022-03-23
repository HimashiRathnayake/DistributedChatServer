package Handlers.CoordinationHandler;

import Models.Client;
import Models.Room;
import Models.Server.LeaderState;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.MessageTransferService;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RoomListHandler {

    private final ResponseHandler serverResponseHandler = new ResponseHandler();

    public ArrayList<String> getRoomList () {
        ArrayList<String> roomList = new ArrayList<>();
//        ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
//        ServerData leaderServer = ServerState.getServerStateInstance().getLeaderServerData();

        ConcurrentHashMap<String, Room> rooms = LeaderState.getInstance().getGlobalRoomList();
        for (Iterator<String> it = rooms.keys().asIterator(); it.hasNext(); ) {
            String room = it.next();
            roomList.add(room);
        }
        return roomList;
    }

    public JSONObject coordinatorRoomList(String clientID) {
        ArrayList<String> roomList = getRoomList();
        JSONObject response;
        response = this.serverResponseHandler.createAllRoomsListResponseFromLeader(roomList, clientID);
        return response;
    }
}