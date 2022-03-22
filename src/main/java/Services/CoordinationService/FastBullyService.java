package Services.CoordinationService;

import Handlers.CoordinationHandler.FastBullyHandler.BullyRequestHandler;
import Handlers.CoordinationHandler.FastBullyHandler.BullyResponseHandler;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

public class FastBullyService extends Thread{

    private static final Logger logger = Logger.getLogger(FastBullyService.class);
    private String operation; // wait or send message
    private String request;
    private static final BullyRequestHandler requestHandler = new BullyRequestHandler();
    private static final BullyResponseHandler responseHandler = new BullyResponseHandler();
    private boolean electionInProgress = false;
    private boolean isViewMessageRecieved = false;

    public FastBullyService(String operation, String request){
        this.operation = operation;
        this.request = request;
    }

    public FastBullyService(String operation) {
        this.operation = operation;
    }

    public void run(){
        // send messages to other servers corresponding Fast Bully Algorithm.
        switch (operation) {
            case ("wait") -> {
                try {
                    Thread.sleep(700);
                    if (!isViewMessageRecieved){
                        electionInProgress = false;
                        // TODO: make this server leader
                    } else {
                        // TODO: Compare view messages
                    }
                } catch (InterruptedException e) {
                    logger.error("Exception occurred in wait thread");
                    e.printStackTrace();
                }
            }
            case ("send") -> {
                switch (request) {
                    case "iamup" -> {
                        logger.info("Send IAM UP message");
                        sendIamUpMessage();
                    }
                    case "election" -> {
                        logger.info("Election message Received");
                    }
                    case "nomination" -> {
                        logger.info("Nomination message Received");
                    }
                    case "coordinator" -> {
                        logger.info("Coordinator message Received");
                    }
                    case "answer" -> {
                        logger.info("Answer message Received");
                    }
                    case "view" -> {
                        logger.info("View message Received");
                    }
                }
            }
        }
    }

    public void sendIamUpMessage(){
        // send iam up message
        this.electionInProgress = true;
        this.isViewMessageRecieved = false;
        JSONObject iamUpRequest = requestHandler.iamUpRequest();
        MessageTransferService.sendToServersBroadcast(iamUpRequest);
        Thread fastBullyService = new FastBullyService("wait");
        fastBullyService.start();
    }

    public static void receiveBullyMessage(JSONObject response){
        String type = (String) response.get("type");
        System.out.println("Receiving To Bully Service: " + response);
        switch (type) {
            case "iamup" -> {
                logger.info("IAM UP message Received");
                // TODO: send view message
            }
            case "election" -> {
                logger.info("Election message Received");
            }
            case "nomination" -> {
                logger.info("Nomination message Received");
            }
            case "coordinator" -> {
                logger.info("Coordinator message Received");
            }
            case "answer" -> {
                logger.info("Answer message Received");
            }
            case "view" -> {
                logger.info("View message Received");
            }
        }
    }

    public static void initializeService(){
        Thread fastBullyService = new FastBullyService("send", "iamup");
        fastBullyService.start();
    }

}
