package Handlers.ChatHandler;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

public class MessageHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ClientResponseHandler clientResponseHandler;

    public MessageHandler(ClientResponseHandler clientResponseHandler){
        this.clientResponseHandler = clientResponseHandler;
    }

    public JSONObject handleMessage(String clientID, String content){
        JSONObject response;
        logger.info("Broadcasting Message");
        response = clientResponseHandler.broadCastMessage(clientID, content);
        return response;
    }
}
