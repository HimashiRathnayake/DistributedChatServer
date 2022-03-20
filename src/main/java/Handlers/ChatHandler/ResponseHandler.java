package Handlers.ChatHandler;

import org.json.simple.JSONObject;

import java.util.ArrayList;

public class ResponseHandler {


    // {type" : "roomlist", "rooms" : ["MainHall-s1", "MainHall-s2", "jokes"]}
    @SuppressWarnings("unchecked")
    public JSONObject sendRoomList(ArrayList<String> rooms){
        JSONObject roomList = new JSONObject();
        roomList.put("type", "roomlist");
        roomList.put("rooms", rooms);
        return roomList;
    }

    @SuppressWarnings("unchecked")
    public JSONObject sendClientListInChatRoom(String roomid, ArrayList<String> identities, String owner){
        JSONObject clientListInRoom = new JSONObject();
        clientListInRoom.put("type", "roomcontents");
        clientListInRoom.put("roomid", roomid);
        clientListInRoom.put("identities", identities);
        clientListInRoom.put("owner", owner);
        return clientListInRoom;
    }

    // {"type" : "newidentity", "approved" : "true"}
    @SuppressWarnings("unchecked")
    public JSONObject sendNewIdentityResponse(String isAccepted){
        JSONObject newIdentity = new JSONObject();
        newIdentity.put("type", "newidentity");
        newIdentity.put("approved", isAccepted);
        return newIdentity;
    }

    // {"type" : "roomchange", "identity" : "Adel", "former" : "", "roomid" : "MainHall-s1"}
    @SuppressWarnings("unchecked")
    public JSONObject moveToRoomResponse(String clientID, String formerID, String roomID){
        JSONObject newIdentity = new JSONObject();
        newIdentity.put("type", "roomchange");
        newIdentity.put("identity", clientID);
        newIdentity.put("former", formerID);
        newIdentity.put("roomid", roomID);
        return newIdentity;
    }

    // {"type" : "message", "identity" : "Adel", "content" : "Hi there!"}
    @SuppressWarnings("unchecked")
    public JSONObject broadCastMessage(String clientID, String content){
        JSONObject response = new JSONObject();
        response.put("type", "message");
        response.put("identity", clientID);
        response.put("content", content);
        return response;
    }



}
