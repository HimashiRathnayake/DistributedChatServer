package Services.CoordinationService;

import Handlers.CoordinationHandler.FastBullyHandler;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class FastBullyService extends Thread {

    private static final Logger logger = Logger.getLogger(FastBullyService.class);
    private final String operation; // wait or send message
    private String request;
    private JSONObject reply;
    private static final FastBullyHandler messageHandler = new FastBullyHandler();
    /**
     * Following variables are visible to all threads (since static) and
     * any read or write operation on them will be visible to all other threads in the class atomically (since volatile).
     **/
    private static volatile boolean electionInProgress = false;
    private static volatile List<JSONObject> viewMessagesReceived;
    private static volatile List<JSONObject> answerMessageReceived;
    private static volatile JSONObject coordinationMessageReceived = null;
    private static volatile JSONObject nominationMessageReceived = null;
    private static volatile boolean nominationStart = false;

    public FastBullyService(String operation, String request) {
        this.operation = operation;
        this.request = request;
    }

    public FastBullyService(String operation) {
        this.operation = operation;
    }

    public void setReply(JSONObject reply) {
        this.reply = reply;
    }

    public String getHighestPriorityServersByID(String currentServerID, List<JSONObject> responses) {
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
                            Thread.sleep(1500);
                            ServerState newServerState = ServerState.getServerStateInstance();
                            MessageTransferService.heartbeatToLeader(messageHandler.heartBeatMessage(), newServerState.getLeaderServerData().getServerAddress(), newServerState.getLeaderServerData().getCoordinationPort());
                        }
                    } catch (IOException e) {
                        if (!electionInProgress) {
                            electionInProgress = true;
                            answerMessageReceived = Collections.synchronizedList(new ArrayList<>());
                            ServerState newServerState = ServerState.getServerStateInstance();
                            logger.info("Heartbeat of the leader is stopped and Sending Election message");
                            ArrayList<ServerData> higherPriorityServers = getHigherPriorityServers(newServerState.getCurrentServerData());
                            MessageTransferService.sendToSelectedServersBroadcast(higherPriorityServers, messageHandler.electionMessage());
                            FastBullyService fastBullyService = new FastBullyService("wait", "answerMessageWait");
                            fastBullyService.start();
                        }
                    } catch (InterruptedException e) {
                        logger.error("Interrupt Exception occurred. Error Message: " + e.getMessage());
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
                                String highestPriorityServerID = getHighestPriorityServersByID(currentServerID, viewMessagesReceived);
                                if (highestPriorityServerID.equals(currentServerID)) {
                                    // If Current Server has the highest priority broadcast coordinator message to others
                                    logger.info("Current server become the leader and sending coordinator message");
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
                    case ("answerMessageWait") -> {
                        try {
                            Thread.sleep(500);
                            if (electionInProgress) {
                                ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
                                String currentServerID = currentServer.getServerID();
                                if (answerMessageReceived.isEmpty()) {
                                    logger.info("No answer messages received. Current server become the leader and sending coordinator message");
                                    ServerState.getServerStateInstance().setLeaderServerData(currentServer);
                                    ArrayList<ServerData> lowPriorityServes = getLowPriorityServers(currentServer);
                                    MessageTransferService.sendToSelectedServersBroadcast(lowPriorityServes, messageHandler.coordinatorMessage());
                                    electionInProgress = false;
                                    nominationStart = false;
                                } else {
                                    String highestPriorityServerID = getHighestPriorityServersByID(currentServerID, answerMessageReceived);
                                    // Else save the highest priority server as leader
                                    ServerData highestPriorityServerData = ServerState.getServerStateInstance().getServerDataById(highestPriorityServerID);
                                    logger.info("Sending Nomination message");
                                    nominationStart = true;
                                    MessageTransferService.sendToServers(messageHandler.nominationMessage(), highestPriorityServerData.getServerAddress(), highestPriorityServerData.getCoordinationPort());
                                    removeNominationSend(highestPriorityServerID);
                                    FastBullyService fastBullyService = new FastBullyService("wait", "coordinationMessageWait");
                                    fastBullyService.start();
                                }
                            }
                        } catch (InterruptedException e) {
                            logger.error("Exception occurred in wait thread");
                            e.printStackTrace();
                        }
                    }
                    case ("coordinationMessageWait") -> {
                        try {
                            Thread.sleep(2000);
                            if (electionInProgress) {
                                if (coordinationMessageReceived == null) {
                                    FastBullyService fastBullyService = new FastBullyService("wait", "answerMessageWait");
                                    fastBullyService.start();
                                } else {
                                    logger.info("Coordinator message Received");
                                    electionInProgress = false;
                                    nominationStart = false;
                                    String leaderServerID = (String) coordinationMessageReceived.get("serverid");
                                    logger.info("Set the leader to server : " + leaderServerID);
                                    ServerData leaderServer = ServerState.getServerStateInstance().getServerDataById(leaderServerID);
                                    ServerState.getServerStateInstance().setLeaderServerData(leaderServer);
                                    coordinationMessageReceived = null;
                                    answerMessageReceived = Collections.synchronizedList(new ArrayList<>());
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    case ("nominationCoordinationWait") -> {
                        try {
                            // busy wait for 1500 milliseconds
                            int counter = 0;
                            while (true) {
                                counter+=1;
                                Thread.sleep(50);
                                if(!(nominationMessageReceived == null) && counter == 30){
                                    break;
                                }
                            }
                            if (electionInProgress) {
                                if (!(nominationMessageReceived == null)) {
                                    logger.info("Nomination message Received");
                                    ServerData leaderServer = ServerState.getServerStateInstance().getCurrentServerData();
                                    ServerState.getServerStateInstance().setLeaderServerData(leaderServer);
                                    logger.info("Current server become leader and Sending Coordinator Message");
                                    MessageTransferService.sendToServersBroadcast(messageHandler.coordinatorMessage());
                                    nominationMessageReceived = null;
                                    nominationStart = false;
                                    electionInProgress = false;
                                } else if (!(coordinationMessageReceived == null)) {
                                    logger.info("Coordinator message Received");
                                    logger.info("Set the leader to server : " + coordinationMessageReceived.get("serverid"));
                                    String leaderServerID = (String) coordinationMessageReceived.get("serverid");
                                    ServerData leaderServer = ServerState.getServerStateInstance().getServerDataById(leaderServerID);
                                    ServerState.getServerStateInstance().setLeaderServerData(leaderServer);
                                    coordinationMessageReceived = null;
                                    nominationStart = false;
                                    electionInProgress = false;
                                } else {
                                    electionInProgress = true;
                                    logger.info("Coordination message or nomination message did not received and restart election procedure");
                                    answerMessageReceived = Collections.synchronizedList(new ArrayList<>());
                                    ServerState newServerState = ServerState.getServerStateInstance();
                                    ArrayList<ServerData> higherPriorityServers = getHigherPriorityServers(newServerState.getCurrentServerData());
                                    MessageTransferService.sendToSelectedServersBroadcast(higherPriorityServers, messageHandler.electionMessage());
                                    FastBullyService fastBullyService = new FastBullyService("wait", "answerMessageWait");
                                    fastBullyService.start();
                                }
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            case ("send") -> {
                switch (request) {
                    case "iamup" -> {
                        // send iam up message
                        if (!electionInProgress) {
                            logger.info("Sending IAM UP message");
                            electionInProgress = true;
                            viewMessagesReceived = Collections.synchronizedList(new ArrayList<>());
                            MessageTransferService.sendToServersBroadcast(messageHandler.iamUpMessage());
                            FastBullyService fastBullyService = new FastBullyService("wait", "viewMessageWait");
                            fastBullyService.start();
                        }
                    }
                    case "election" -> logger.info("Sending Election Message");
                    case "nomination" -> logger.info("Sending Nomination Message");
                    case "coordinator" -> logger.info("Sending Coordinator Message");
                    case "answer" -> {
                        logger.info("Sending Answer Message");
                        ServerData requestServer = ServerState.getServerStateInstance().getServerDataById((String) this.reply.get("serverid"));
                        MessageTransferService.sendToServers(messageHandler.answerMessage(), requestServer.getServerAddress(), requestServer.getCoordinationPort());
                        FastBullyService fastBullyService = new FastBullyService("wait", "nominationCoordinationWait");
                        fastBullyService.start();
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
                electionInProgress = true;
                logger.info("Election message Received");
                FastBullyService fastBullyService = new FastBullyService("send", "answer");
                fastBullyService.setReply(response);
                fastBullyService.start();
            }
            case "nomination" -> nominationMessageReceived = response;
            case "coordinator" -> {
                if (nominationStart) {
                    coordinationMessageReceived = response;
                } else {
                    electionInProgress = false;
                    logger.info("Coordinator message Received");
                    String leaderServerID = (String) response.get("serverid");
                    logger.info("Set the leader to server :  " + leaderServerID);
                    ServerData leaderServer = ServerState.getServerStateInstance().getServerDataById(leaderServerID);
                    ServerState.getServerStateInstance().setLeaderServerData(leaderServer);
                }
            }
            case "answer" -> {
                logger.info("Answer message Received");
                answerMessageReceived.add(response);
            }
            case "view" -> viewMessagesReceived.add(response);
        }
    }

    public static void initializeService() {
        Thread fastBullyService = new FastBullyService("send", "iamup");
        fastBullyService.start();
    }

}
