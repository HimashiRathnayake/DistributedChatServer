package Handlers.ChatHandler;

import org.json.simple.JSONObject;

public class ResponseHandler {

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
