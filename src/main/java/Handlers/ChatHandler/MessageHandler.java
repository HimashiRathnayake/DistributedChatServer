package Handlers.ChatHandler;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

public class MessageHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ResponseHandler responseHandler;

    public MessageHandler(ResponseHandler responseHandler){
        this.responseHandler = responseHandler;
    }

    public JSONObject handleMessage(String clientID, String content){
        JSONObject response;
        logger.info("Broadcasting Message");
        response = responseHandler.broadCastMessage(clientID, content);
        return response;
    }
}
