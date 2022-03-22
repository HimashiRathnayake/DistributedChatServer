package Handlers.CoordinationHandler.FastBullyHandler;

import org.json.simple.JSONObject;

public class BullyResponseHandler {

    // {"type" : "view", "serverid" : "ServerID", "serverlist" : ["s1", "s2"]}
    @SuppressWarnings("unchecked")
    public JSONObject viewResponse(){
        JSONObject request = new JSONObject();
        request.put("type", "iamup");
        request.put("serverid", System.getProperty("serverID"));
        return request;
    }

}
