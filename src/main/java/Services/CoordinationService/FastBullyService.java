package Services.CoordinationService;

import Handlers.CoordinationHandler.FastBullyHandler;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class FastBullyService extends Thread{

    private static final Logger logger = Logger.getLogger(FastBullyService.class);
    private String operation; // wait or send message
    private String request;
    private static final FastBullyHandler messageHandler = new FastBullyHandler();
    private static boolean electionInProgress = false;
    private static ArrayList<JSONObject> viewMessagesReceived;
    private static JSONObject reply;

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
                    Thread.sleep(4500);
                    if (viewMessagesReceived.isEmpty()){
                        logger.info("No view messages received. Server can become the leader");
                        electionInProgress = false;
                        // TODO: make this server leader
                    } else {
                        logger.info("View messages received.");
                        // TODO: Compare view messages and update
                        // Check highest priority server from views
                        // If Current Server has the highest priority
                        // broadcast coordinator message to others
                        // Else
                        // Save the highest priority server as leader
                        // Stop procedure
                    }
                } catch (InterruptedException e) {
                    logger.error("Exception occurred in wait thread");
                    e.printStackTrace();
                }
            }
            case ("send") -> {
                switch (request) {
                    case "iamup" -> {
                        // send iam up message
                        logger.info("Sending IAM UP message");
                        electionInProgress = true;
                        viewMessagesReceived = new ArrayList<JSONObject>();
//                        MessageTransferService.sendToServersBroadcast(messageHandler.iamUpMessage());
//                        Thread fastBullyService = new FastBullyService("wait");
//                        fastBullyService.start();
                    }
                    case "election" -> {
                        logger.info("Sending Election message");
                    }
                    case "nomination" -> {
                        logger.info("Sending Nomination message");
                    }
                    case "coordinator" -> {
                        logger.info("Sending Coordinator message");
                    }
                    case "answer" -> {
                        logger.info("Sending Answer message");
                    }
                    case "view" -> {
                        ServerData sender = ServerState.getServerStateInstance().getServerDataById((String) reply.get("serverid"));
                        ArrayList<String> activeServers = new ArrayList<>(); // TODO: get actual active servers.
                        System.out.println(sender.getServerAddress() + " " + sender.getCoordinationPort());
//                        MessageTransferService.sendToServers(messageHandler.viewMessage(activeServers), sender.getServerAddress(), sender.getCoordinationPort());
                    }
                }
            }
        }
    }

    public static void receiveBullyMessage(JSONObject response){
        String type = (String) response.get("type");
        System.out.println("Receiving To Bully Service: " + response);
        switch (type) {
            case "iamup" -> {
                logger.info("IAM UP message Received");
                reply = response;
                FastBullyService fastBullyService = new FastBullyService("send", "view");
                fastBullyService.start();
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
                viewMessagesReceived.add(response);
            }
        }
    }

    public static void initializeService(){
        Thread fastBullyService = new FastBullyService("send", "iamup");
        fastBullyService.start();
    }

}
