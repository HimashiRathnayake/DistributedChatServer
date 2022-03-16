package Handlers.ChatHandler;

import java.util.ArrayList;
import java.util.Objects;

public class ClientListInRoomHandler {

//    RoomsYML roomsYML = new YMLReader().readRoomsYML();

    public String getClientsRoomID(String identity){

        String roomid = null;
//        for (int i = 0; i<roomsYML.getRooms().size(); i++){
//            if (roomsYML.getRooms().get(i).getClients().contains(identity)){
//                roomid = roomsYML.getRooms().get(i).getRoomID();
//            }
//        }
        return roomid;
    }

    public String getRoomOwner(String room){

        String owner = null;
//        for (int i = 0; i<roomsYML.getRooms().size(); i++){
//            if (Objects.equals(roomsYML.getRooms().get(i).getRoomID(), room)){
//                owner = roomsYML.getRooms().get(i).getOwner();
//            }
//        }
        return owner;
    }

    public ArrayList<String> getClientsInRoom(String room_id){

        ArrayList<String> clients = new ArrayList<>();
//        for (int i = 0; i<roomsYML.getRooms().size(); i++){
//            if (Objects.equals(roomsYML.getRooms().get(i).getRoomID(), room_id)){
//                clients = roomsYML.getRooms().get(i).getClients();
//            }
//        }
        return clients;
    }
}
