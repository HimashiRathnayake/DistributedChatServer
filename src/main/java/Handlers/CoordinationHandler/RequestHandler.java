package Handlers.CoordinationHandler;

import org.json.simple.JSONObject;

public class RequestHandler {
    // Handle Requests To leader

    // list - {“type” : “allrooms”}
    // who

    // createroom - {“type”: createroom, “roomid”: “jokes”, "clientid": clientID, "serverid": "serverid"}
    @SuppressWarnings("unchecked")
    public JSONObject sendCreateRoomResponse(String roomid, String clientID){
        JSONObject request = new JSONObject();
        request.put("type", "createroom");
        request.put("roomid", roomid);
        request.put("clientid", clientID);
        request.put("serverid", System.getProperty("serverID"));
        return request;
    }
    // joinroom - {"type" : roomroute, "roomid" : ”jokes”}
    public JSONObject sendJoinRoomResponse(String roomid){
        JSONObject request = new JSONObject();
        request.put("type", "roomroute");
        request.put("roomid", roomid);
        return request;
    }

    // movejoin
    // deleteroom - {"type" : "deleteroom", "serverid" : "s1", "roomid" : "jokes"}

    // newidentity - {"type": "newidentity", "identity": "Adel"}
    @SuppressWarnings("unchecked")
    public JSONObject sendNewIdentityResponse(String clientID){
        JSONObject request = new JSONObject();
        request.put("type", "newidentity");
        request.put("identity", clientID);
        request.put("serverid", System.getProperty("serverID"));
        return request;
    }

}
