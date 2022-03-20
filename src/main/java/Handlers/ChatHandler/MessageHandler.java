package Handlers.ChatHandler;

import org.apache.log4j.Logger;

public class MessageHandler {
    private final Logger logger = Logger.getLogger(NewIdentityHandler.class);
    private final ResponseHandler responseHandler;

    public MessageHandler(ResponseHandler responseHandler){
        this.responseHandler = responseHandler;
    }


}
