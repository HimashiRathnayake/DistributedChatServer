package Handlers.CoordinationHandler;

import org.json.simple.JSONObject;

public class RequestHandler {
    // Handle Requests To leader

    // list - {“type” : “allrooms”}
    // who

    // createroom - {“type”: createroom, “roomid”: “jokes”}
    // joinroom - {"type" : roomroute, "roomid" : ”jokes”}

    // movejoin
    // deleteroom - {"type" : "deleteroom", "serverid" : "s1", "roomid" : "jokes"}

    // quit - {"type": "quit", "identity": "Adel"}

    // newidentity - {"type": "newidentity", "identity": "Adel"}
    @SuppressWarnings("unchecked")
    public JSONObject sendNewIdentityResponse(String clientID){
        JSONObject request = new JSONObject();
        request.put("type", "newidentity");
        request.put("identity", clientID);
        return request;
    }

    // message

}
