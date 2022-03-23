package Handlers.ChatHandler;

import Models.Server.ServerState;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;

public class RoomListHandler{

    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ClientResponseHandler clientResponseHandler;

    public RoomListHandler(ClientResponseHandler clientResponseHandler) {
        this.clientResponseHandler = clientResponseHandler;
    }

    public ArrayList<String> getRoomList(){
        ArrayList<String> roomList = new ArrayList<>();
        for (Iterator<String> it = ServerState.getServerStateInstance().roomList.keys().asIterator(); it.hasNext();){
            String roomID = it.next();
            roomList.add(roomID);
        }
        logger.info("room List : "+roomList);
        return roomList;
    }
}
