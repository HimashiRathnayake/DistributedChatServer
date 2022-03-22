package Services.CoordinationService;

import Handlers.CoordinationHandler.FastBullyHandler;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FastBullyService extends Thread{

    private static final Logger logger = Logger.getLogger(FastBullyService.class);
    private final String operation; // wait or send message
    private String request;
    private static final FastBullyHandler messageHandler = new FastBullyHandler();
    private static boolean electionInProgress = false;
    private static ArrayList<JSONObject> viewMessagesReceived;
    private JSONObject reply;

    public FastBullyService(String operation, String request){
        this.operation = operation;
        this.request = request;
    }

    public void setReply(JSONObject reply) {
        this.reply = reply;
    }

    public FastBullyService(String operation) {
        this.operation = operation;
    }

    public String getHighestPriorityServers(String currentServerID, ArrayList<JSONObject> servers){
        String highestPriorityServer = currentServerID;
        for (JSONObject server : servers){
            String serverID = server.get("serverid").toString();
            if (Integer.parseInt(highestPriorityServer.substring(1)) > Integer.parseInt(serverID.substring(1))){
                highestPriorityServer = serverID;
            }
        }
        return highestPriorityServer;
    }

    public void run(){
        // send messages to other servers corresponding Fast Bully Algorithm.
        switch (operation) {
            case ("wait") -> {
                try {
                    Thread.sleep(1000);
                    ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
                    String currentServerID = currentServer.getServerID();
                    if (viewMessagesReceived.isEmpty()){
                        logger.info("No view messages received. Current server become the leader");
                        ServerState.getServerStateInstance().setLeaderServerData(currentServer);
                    } else {
                        logger.info("View messages received.");
                        // TODO: Compare view messages and update
                        // Check highest priority server from views
                        String highestPriorityServerID = getHighestPriorityServers(currentServerID, viewMessagesReceived);
                        if (highestPriorityServerID.equals(currentServerID)){
                            // If Current Server has the highest priority broadcast coordinator message to others
                            logger.info("Current server become the leader");
                            ServerState.getServerStateInstance().setLeaderServerData(currentServer);
                            MessageTransferService.sendToServersBroadcast(messageHandler.coordinatorMessage()); //broadcast coordinator message
                        } else {
                            // Else save the highest priority server as leader
                            logger.info("Set the leader to server : " + highestPriorityServerID);
                            ServerData leaderData = ServerState.getServerStateInstance().getServerDataById(highestPriorityServerID);
                            ServerState.getServerStateInstance().setLeaderServerData(leaderData);
                        }
                    }
                    electionInProgress = false;
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
                        MessageTransferService.sendToServersBroadcast(messageHandler.iamUpMessage());
                        Thread fastBullyService = new FastBullyService("wait");
                        fastBullyService.start();
                    }
                    case "election" -> {
                        logger.info("Sending Election Message");
                    }
                    case "nomination" -> {
                        logger.info("Sending Nomination Message");
                    }
                    case "coordinator" -> {
                        logger.info("Sending Coordinator Message");
                    }
                    case "answer" -> {
                        logger.info("Sending Answer Message");
                    }
                    case "view" -> {
                        logger.info("Sending View Message");

                        ServerData requestServer = ServerState.getServerStateInstance().getServerDataById((String) this.reply.get("serverid"));
                        ArrayList<String> activeServers = new ArrayList<>(); // TODO: get actual active servers.
                        JSONObject response = messageHandler.viewMessage(activeServers);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        MessageTransferService.sendToServers(response, requestServer.getServerAddress(), requestServer.getCoordinationPort());
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
                FastBullyService fastBullyService = new FastBullyService("send", "view");
                fastBullyService.setReply(response);
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
