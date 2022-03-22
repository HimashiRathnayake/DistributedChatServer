package Services.CoordinationService;

import Handlers.CoordinationHandler.FastBullyHandler;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FastBullyService extends Thread {

    private static final Logger logger = Logger.getLogger(FastBullyService.class);
    private final String operation; // wait or send message
    private String request;
    private static final FastBullyHandler messageHandler = new FastBullyHandler();
    private static boolean electionInProgress = false;
    private static ArrayList<JSONObject> viewMessagesReceived;
    private static ArrayList<JSONObject> answerMessageReceived;
    private static JSONObject coordinationMessageReceived = null;
    private static JSONObject nominationMessageReceived = null;
    private JSONObject reply;
    private static boolean nominationStart = false;

    public FastBullyService(String operation, String request) {
        this.operation = operation;
        this.request = request;
    }

    public void setReply(JSONObject reply) {
        this.reply = reply;
    }

    public FastBullyService(String operation) {
        this.operation = operation;
    }

    public String getHighestPriorityServers(String currentServerID, ArrayList<JSONObject> responses) {
        String highestPriorityServerID = currentServerID;
        for (JSONObject response : responses) {
            String serverID = response.get("serverid").toString();
            if (Integer.parseInt(highestPriorityServerID.substring(1)) > Integer.parseInt(serverID.substring(1))) {
                highestPriorityServerID = serverID;
            }
        }
        return highestPriorityServerID;
    }

    public ArrayList<ServerData> getHigherPriorityServers(ServerData currentServerData) {
        ConcurrentMap<String, ServerData> serverList = ServerState.getServerStateInstance().getServersList();
        ArrayList<ServerData> higherPriorityServers = new ArrayList<>();
        for (ConcurrentMap.Entry<String, ServerData> entry : serverList.entrySet()) {
            if (Integer.parseInt(entry.getKey().substring(1)) < Integer.parseInt(currentServerData.getServerID().substring(1))) {
                higherPriorityServers.add(entry.getValue());
            }
        }
        return higherPriorityServers;
    }

    public ArrayList<ServerData> getLowPriorityServers(ServerData currentServerData) {
        ConcurrentMap<String, ServerData> serverList = ServerState.getServerStateInstance().getServersList();
        ArrayList<ServerData> lowPriorityServers = new ArrayList<>();
        for (ConcurrentMap.Entry<String, ServerData> entry : serverList.entrySet()) {
            if (Integer.parseInt(entry.getKey().substring(1)) > Integer.parseInt(currentServerData.getServerID().substring(1))) {
                lowPriorityServers.add(entry.getValue());
            }
        }
        return lowPriorityServers;
    }

    public void removeNominationSend(String ServerID) {
        for (JSONObject response : answerMessageReceived) {
            if (response.get("serverid").equals(ServerID)) {
                answerMessageReceived.remove(response);
                break;
            }
        }
    }

    public void run() {
        // send messages to other servers corresponding Fast Bully Algorithm.
        switch (operation) {
            case ("heartbeat") -> {
                while (true) {
                    try {
                        ServerState serverState = ServerState.getServerStateInstance();
                        if (!serverState.getCurrentServerData().getServerID().equals(serverState.getLeaderServerData().getServerID())) {
                            Thread.sleep(3000);
                            ServerState newServerState = ServerState.getServerStateInstance();
                            MessageTransferService.heartbeatToLeader(messageHandler.heartBeatMessage(), newServerState.getLeaderServerData().getServerAddress(), newServerState.getLeaderServerData().getCoordinationPort());
                        }
                    } catch (IOException e) {
                        electionInProgress = true;
                        answerMessageReceived = new ArrayList<JSONObject>();
                        ServerState newServerState = ServerState.getServerStateInstance();
                        logger.error("Heartbeat of the leader is stopped - " + e.getMessage());
                        ArrayList<ServerData> higherPriorityServers = getHigherPriorityServers(newServerState.getCurrentServerData());
                        MessageTransferService.sendToSelectedServersBroadcast(higherPriorityServers, messageHandler.electionMessage());
                        FastBullyService fastBullyService = new FastBullyService("wait", "electionWait");
                        fastBullyService.start();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            case ("wait") -> {
                switch (request) {
                    case ("viewMessageWait") -> {
                        try {
                            Thread.sleep(1000);
                            ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
                            String currentServerID = currentServer.getServerID();
                            if (viewMessagesReceived.isEmpty()) {
                                logger.info("No view messages received. Current server become the leader");
                                ServerState.getServerStateInstance().setLeaderServerData(currentServer);
                            } else {
                                logger.info("View messages received.");
                                // TODO: Compare view messages and update
                                // Check highest priority server from views
                                String highestPriorityServerID = getHighestPriorityServers(currentServerID, viewMessagesReceived);
                                if (highestPriorityServerID.equals(currentServerID)) {
                                    // If Current Server has the highest priority broadcast coordinator message to others
                                    logger.info("Current server become the leader");
                                    ServerState.getServerStateInstance().setLeaderServerData(currentServer);
                                    //TODO: check this coordinator message sending part
                                    MessageTransferService.sendToServersBroadcast(messageHandler.coordinatorMessage()); //broadcast coordinator message
                                } else {
                                    // Else save the highest priority server as leader
                                    logger.info("Set the leader to server : " + highestPriorityServerID);
                                    ServerData leaderData = ServerState.getServerStateInstance().getServerDataById(highestPriorityServerID);
                                    ServerState.getServerStateInstance().setLeaderServerData(leaderData);
                                }
                            }
                            FastBullyService heartBeat = new FastBullyService("heartbeat");
                            heartBeat.start();
                            electionInProgress = false;
                        } catch (InterruptedException e) {
                            logger.error("Exception occurred in wait thread");
                            e.printStackTrace();
                        }
                    }
                    case ("electionWait") -> {
                        try {
                            Thread.sleep(1500);
                            ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
                            String currentServerID = currentServer.getServerID();
                            if (answerMessageReceived.isEmpty()) {
                                logger.info("No answer messages received. Current server become the leader");
                                ServerState.getServerStateInstance().setLeaderServerData(currentServer);
                                ArrayList<ServerData> lowPriorityServes = getLowPriorityServers(currentServer);
                                MessageTransferService.sendToSelectedServersBroadcast(lowPriorityServes, messageHandler.coordinatorMessage());
                                electionInProgress = false;
                                nominationStart = false;
                            } else {
                                String highestPriorityServerID = getHighestPriorityServers(currentServerID, answerMessageReceived);
                                // Else save the highest priority server as leader
                                logger.info("<Election> Set the leader to server : " + highestPriorityServerID);
                                ServerData highestPriorityServerData = ServerState.getServerStateInstance().getServerDataById(highestPriorityServerID);
                                logger.info("Sending Nomination message");
                                nominationStart = true;
                                MessageTransferService.sendToServers(messageHandler.nominationMessage(), highestPriorityServerData.getServerAddress(), highestPriorityServerData.getCoordinationPort());
                                removeNominationSend(highestPriorityServerID);
                                FastBullyService fastBullyService = new FastBullyService("wait", "coordinationMessageWait");
                                fastBullyService.start();

                            }
                        } catch (InterruptedException e) {
                            logger.error("Exception occurred in wait thread");
                            e.printStackTrace();
                        }
                    }
                    case ("coordinationMessageWait") -> {
                        try {
                            Thread.sleep(1500);
                            if (coordinationMessageReceived == null) {
                                FastBullyService fastBullyService = new FastBullyService("wait", "electionWait");
                                fastBullyService.start();
                            } else {
                                electionInProgress = false;
                                nominationStart = false;
                                String leaderServerID = (String) coordinationMessageReceived.get("serverid");
                                ServerData leaderServer = ServerState.getServerStateInstance().getServerDataById(leaderServerID);
                                ServerState.getServerStateInstance().setLeaderServerData(leaderServer);
                                coordinationMessageReceived = null;
                                answerMessageReceived = new ArrayList<>();
                            }

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    case ("nominationCoordinationWait") -> {
                        if (!(nominationMessageReceived == null)) {
                            logger.info("Sending Coordinator Message");
                            ServerData leaderServer = ServerState.getServerStateInstance().getCurrentServerData();
                            ServerState.getServerStateInstance().setLeaderServerData(leaderServer);
                            MessageTransferService.sendToServersBroadcast(messageHandler.coordinatorMessage());
                            nominationMessageReceived = null;
                            nominationStart = false;
                            electionInProgress = false;
                        }else if (!(coordinationMessageReceived == null)){
                            String leaderServerID = (String) coordinationMessageReceived.get("serverid");
                            ServerData leaderServer = ServerState.getServerStateInstance().getServerDataById(leaderServerID);
                            ServerState.getServerStateInstance().setLeaderServerData(leaderServer);
                            coordinationMessageReceived = null;
                            nominationStart = false;
                            electionInProgress = false;
                        }else{
                            electionInProgress = true;
                            answerMessageReceived = new ArrayList<JSONObject>();
                            ServerState newServerState = ServerState.getServerStateInstance();
                            ArrayList<ServerData> higherPriorityServers = getHigherPriorityServers(newServerState.getCurrentServerData());
                            MessageTransferService.sendToSelectedServersBroadcast(higherPriorityServers, messageHandler.electionMessage());
                            FastBullyService fastBullyService = new FastBullyService("wait", "electionWait");
                            fastBullyService.start();
                        }

                    }
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
                        FastBullyService fastBullyService = new FastBullyService("wait", "viewMessageWait");
                        fastBullyService.start();
                    }
                    case "election" -> {  // done!
                        logger.info("Sending Election Message");
                    }
                    case "nomination" -> {
                        logger.info("Sending Nomination Message");
                    }
                    case "coordinator" -> {  // done!
                        logger.info("Sending Coordinator Message");
                    }
                    case "answer" -> {
                        logger.info("Sending Answer Message");
                        ServerData requestServer = ServerState.getServerStateInstance().getServerDataById((String) this.reply.get("serverid"));
                        MessageTransferService.sendToServers(messageHandler.answerMessage(), requestServer.getServerAddress(), requestServer.getCoordinationPort());
                        FastBullyService fastBullyService = new FastBullyService("wait", "nominationCoordinationWait");
                        start();
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

    public static void receiveBullyMessage(JSONObject response) {
        String type = (String) response.get("type");
//        System.out.println("Receiving To Bully Service: " + response);
        switch (type) {
            case ("heartbeat") -> {
                // heartbeat received to the leader server - don't do anything
            }
            case "iamup" -> {
                logger.info("IAM UP message Received");
                FastBullyService fastBullyService = new FastBullyService("send", "view");
                fastBullyService.setReply(response);
                fastBullyService.start();
            }
            case "election" -> {
                logger.info("Election message Received");
                FastBullyService fastBullyService = new FastBullyService("send", "answer");
                fastBullyService.setReply(response);
                fastBullyService.start();
            }
            case "nomination" -> {
                logger.info("Nomination message Received");
                nominationMessageReceived = response;
            }
            case "coordinator" -> {
                logger.info("Coordinator message Received");
                if (nominationStart) {
                    coordinationMessageReceived = response;
                } else {
                    String leaderServerID = (String) response.get("serverid");
                    ServerData leaderServer = ServerState.getServerStateInstance().getServerDataById(leaderServerID);
                    ServerState.getServerStateInstance().setLeaderServerData(leaderServer);
                }
            }
            case "answer" -> {
                logger.info("Answer message Received");
                answerMessageReceived.add(response);
            }
            case "view" -> {
                logger.info("View message Received");
                viewMessagesReceived.add(response);
            }
        }
    }

    public static void initializeService() {
        Thread fastBullyService = new FastBullyService("send", "iamup");
        fastBullyService.start();
    }

}
