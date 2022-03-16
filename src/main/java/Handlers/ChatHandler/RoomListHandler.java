package Handlers.ChatHandler;

import java.util.ArrayList;

public class RoomListHandler{

    public ArrayList<String> getRoomList(String identity){
        ArrayList<String> roomList = new ArrayList<>();
//        RoomsYML roomsYML = new YMLReader().readRoomsYML();
//        for (int i = 0; i<roomsYML.getRooms().size(); i++){
//            roomList.add(roomsYML.getRooms().get(i).getRoomID());
//        }
        return roomList;
    }
}
