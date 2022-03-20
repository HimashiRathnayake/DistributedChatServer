package Handlers.ChatHandler;

import Models.Client;
import Models.Room;
import Models.Server.ServerState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ClientListInRoomHandler {

//    RoomsYML roomsYML = new YMLReader().readRoomsYML();

    public String getClientsRoomID(String identity){

        String roomid = null;
        Collection<Room> roomsList = ServerState.getServerStateInstance().roomList.values();
        for (Room room : roomsList) {
            for (int j = 0; j < room.getClients().size(); j++) {
                if (Objects.equals(room.getClients().get(j).getIdentity(), identity)) {
                    roomid = room.getRoomID();
                    break;
                }
            }
        }
        return roomid;
    }

    public String getRoomOwner(String roomid){

        return ServerState.getServerStateInstance().roomList.get(roomid).getOwner();
    }

    public ArrayList<String> getClientsInRoom(String roomid){
        ArrayList<String> clients = new ArrayList<>();
        ArrayList<Client> clientsList = ServerState.getServerStateInstance().roomList.get(roomid).getClients();
        for (Client client : clientsList){
            clients.add(client.getIdentity());
        }
        return clients;
    }
}
