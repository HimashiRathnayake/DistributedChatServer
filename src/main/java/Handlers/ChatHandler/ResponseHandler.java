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
        JSONObject newIdentity = new JSONObject();
        newIdentity.put("type", "roomchange");
        newIdentity.put("identity", clientID);
        newIdentity.put("former", formerID);
        newIdentity.put("roomid", roomID);
        return newIdentity;
    }

}