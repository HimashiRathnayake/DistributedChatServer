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
        JSONObject moveRoom = new JSONObject();
        moveRoom.put("type", "roomchange");
        moveRoom.put("identity", clientID);
        moveRoom.put("former", formerID);
        moveRoom.put("roomid", roomID);
        return moveRoom;
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

    // {"type" : "serverchange", "approved" : "true", "serverid" : "s2"}
    @SuppressWarnings("unchecked")
    public JSONObject serverChangedResponse(){
        JSONObject response= new JSONObject();
        response.put("type", "serverchange");
        response.put("approved", "true");
        response.put("serverid", System.getProperty("serverID"));
        return  response;
    }

    // {"type" : "roomchange", "identity" : "Adel", "former" : "", "roomid" : "MainHall-s1"}
    @SuppressWarnings("unchecked")
    public JSONObject broadCastRoomChange(String clientID, String formerID, String roomID){
        JSONObject response = new JSONObject();
        response.put("type", "roomchange");
        response.put("identity", clientID);
        response.put("former", formerID);
        response.put("roomid", roomID);
        return response;
    }

    // {"type" : "deleteroom", "roomid" : "jokes", "approved" : "false"}
    @SuppressWarnings("unchecked")
    public JSONObject deleteRoomResponse(String roomID, String isApprove){
        JSONObject response = new JSONObject();
        response.put("type" , "deleteroom");
        response.put("roomid", roomID);
        response.put("approved", isApprove);
        return  response;
    }

    // {"type" : "deleteroom", "serverid" : "s1", "roomid" : "jokes"}
    @SuppressWarnings("unchecked")
    public JSONObject broadcastServersDeleteRoomResponse(String serverID, String roomID){
        JSONObject response = new JSONObject();
        response.put("type", "deleteroom");
        response.put("serverid", serverID);
        response.put("roomid", roomID);
        return response;
    }

    // {"type" : "createroom", "roomid" : "jokes", "approved" : "true"}
    @SuppressWarnings("unchecked")
    public JSONObject sendNewRoomResponse(String roomid, String isAccepted){
        JSONObject newRoom = new JSONObject();
        newRoom.put("type", "createroom");
        newRoom.put("roomid", roomid);
        newRoom.put("approved", isAccepted);
        return newRoom;
    }

    //{"type" : "route", "roomid" : "jokes", "host" : "122.134.2.4", "port" : "4445"}
    @SuppressWarnings("unchecked")
    public JSONObject sendNewRouteMessage(String roomid, String host, String port){
        JSONObject route = new JSONObject();
        route.put("type", "route");
        route.put("roomid", roomid);
        route.put("host", host);
        route.put("port", port);
        return route;
    }

}
