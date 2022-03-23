package Handlers.ChatHandler;

import Handlers.CoordinationHandler.RequestHandler;

import Models.Room;
import Models.Server.LeaderState;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class RoomListHandler {

    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ClientResponseHandler clientResponseHandler;
    private final RequestHandler serverRequestHandler = new RequestHandler();

    public RoomListHandler(ClientResponseHandler clientResponseHandler) {
        this.clientResponseHandler = clientResponseHandler;

    }

    @SuppressWarnings("unchecked")
    public JSONObject getRoomList(String clientID) {
        JSONObject roomListResponse = new JSONObject();
        ArrayList<String> roomList = new ArrayList<>();
        ServerData leaderServer = ServerState.getServerStateInstance().getLeaderServerData();
        if (ServerState.getServerStateInstance().isCurrentServerLeader()) {
            ConcurrentHashMap<String, Room> rooms = LeaderState.getInstance().getGlobalRoomList();

            for (Iterator<String> it = rooms.keys().asIterator(); it.hasNext(); ) {
                String roomID = it.next();
                logger.info(roomID);
                roomList.add(roomID);
            }
            roomListResponse.put("type", "roomlist");
            roomListResponse.put("rooms", roomList);

        } else {
            JSONObject request = serverRequestHandler.createAllRoomsRequest(clientID);
            MessageTransferService.sendToServers(request, leaderServer.getServerAddress(), leaderServer.getCoordinationPort());
            logger.info("send to leader");
        }

        logger.info("room List : " + roomList);
        return roomListResponse;
    }


}
