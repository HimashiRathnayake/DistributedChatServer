package Handlers.CoordinationHandler.FastBullyHandler;

import org.json.simple.JSONObject;

public class BullyRequestHandler {

    // {"type" : "iamup", "serverid" : "ServerID"}
    @SuppressWarnings("unchecked")
    public JSONObject iamUpRequest(){
        JSONObject request = new JSONObject();
        request.put("type", "iamup");
        request.put("serverid", System.getProperty("serverID"));
        return request;
    }

}
