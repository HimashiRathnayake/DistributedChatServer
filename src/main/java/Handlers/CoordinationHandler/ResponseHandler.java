package Handlers.CoordinationHandler;

import org.json.simple.JSONObject;

import java.util.ArrayList;

public class ResponseHandler {
    // Handle Responses From Leader

    // list - {"type" : allrooms, "allrooms": ["jokes", "room1"}
    @SuppressWarnings("unchecked")
    public JSONObject createAllRoomsListResponse(Object allrooms){
        JSONObject allRoomsRequest = new JSONObject();
        allRoomsRequest.put("type", "allrooms");
        allRoomsRequest.put("allrooms", allrooms);
        return allRoomsRequest;
    }

    @SuppressWarnings("unchecked")
    public JSONObject createAllRoomsListResponseFromLeader(ArrayList<String> allrooms, String clientID){
        JSONObject allRoomsRequest = new JSONObject();
        allRoomsRequest.put("type", "leaderallrooms");
        allRoomsRequest.put("allrooms", allrooms);
        allRoomsRequest.put("clientid", clientID);
        return allRoomsRequest;
    }
    // who

    // createroom - {"type": "leadercreateroom", "approved": "true", "roomid": "roomid", "clientid", "clientid"}
    @SuppressWarnings("unchecked")
    public JSONObject sendCreateRoomServerResponse(String isAccepted, String roomid, String clientid){
        JSONObject newRoom = new JSONObject();
        newRoom.put("type", "leadercreateroom");
        newRoom.put("approved", isAccepted);
        newRoom.put("roomid",roomid);
        newRoom.put("clientid",clientid);
        return newRoom;
    }

    // joinroom - {"type" : "leaderroomexist", "roomid" : ”jokes”, "exist": "true", "clientid", "clientid"}
    public JSONObject sendRoomExistResponse(String roomid, String isExist, String clientid){
        JSONObject request = new JSONObject();
        request.put("type", "leaderroomexist");
        request.put("roomid", roomid);
        request.put("exist", isExist);
        request.put("clientid",clientid);
        return request;
    }

    // joinroom - {"type" : "leaderroomroute", "roomid": "jokes", "exist": "true", "host" : "122.134.2.4", "port" : "4445", "clientid", "clientid"}
    public JSONObject sendGetRoomRouteResponse(String exist, String roomid, String host, String port, String clientid){
        JSONObject roomroute = new JSONObject();
        roomroute.put("type", "leaderroomroute");
        roomroute.put("roomid", roomid);
        roomroute.put("exist", exist);
        roomroute.put("host",host);
        roomroute.put("port",port);
        roomroute.put("clientid",clientid);
        return roomroute;
    }

    // movejoin
    // deleteroom - {"type" : "deleteroom", "serverid" : "s1", "roomid" : "jokes"}
    @SuppressWarnings("unchecked")
    public JSONObject deleteRoomServerRespond(String serverID, String roomID){
        JSONObject newIdentity = new JSONObject();
        newIdentity.put("type", "deleteroom");
        newIdentity.put("serverid", serverID);
        newIdentity.put("roomid",roomID);
        return newIdentity;
    }

    // newidentity - {"type": "leadernewidentity",  “approved”: ”true”,  "identity": "Adel"}
    @SuppressWarnings("unchecked")
    public JSONObject sendNewIdentityServerResponse(String isAccepted, String clientIdentity){
        JSONObject newIdentity = new JSONObject();
        newIdentity.put("type", "leadernewidentity");
        newIdentity.put("approved", isAccepted);
        newIdentity.put("identity",clientIdentity);
        return newIdentity;
    }

}
