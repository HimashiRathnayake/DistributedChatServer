package Services.CoordinationService;

import Handlers.CoordinationHandler.*;
import Models.Client;
import Handlers.ChatHandler.ClientResponseHandler;
import Models.Server.LeaderState;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.ChatService.ChatClientService;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;

public class CoordinationService extends Thread {
    private final Socket coordinationSocket;
    Logger logger = Logger.getLogger(CoordinationService.class);
    private final JSONParser parser = new JSONParser();
    private boolean running = true;
    private final ResponseHandler serverResponseHandler = new ResponseHandler();
    private final GossipHandler gossipHandler = new GossipHandler();

    public CoordinationService(Socket coordinationSocket) {
        this.coordinationSocket = coordinationSocket;
    }

    public void stopThread() {
        running = false;
    }

    public void run() {
        while (running) {
            try {
                assert coordinationSocket != null;
                BufferedReader in = new BufferedReader(new InputStreamReader(coordinationSocket.getInputStream(), StandardCharsets.UTF_8));
                JSONObject message = (JSONObject) parser.parse(in.readLine());
                String type = (String) message.get("type");
//                System.out.println("Receiving: " + message);
                switch (type) {
                    case "allrooms" -> {
                        logger.info("Received message type list");
                        JSONObject response = new RoomListHandler().coordinatorRoomList((String) message.get("clientid"));
                        ServerData requestServer = ServerState.getServerStateInstance().getServersList().get((String) message.get("serverid"));
                        MessageTransferService.sendToServers(response, requestServer.getServerAddress(), requestServer.getCoordinationPort());
                    }
                    case "leaderallrooms" -> {
                        System.out.println(message.get("allrooms"));
                        JSONObject response = new ClientResponseHandler().sendRoomList((ArrayList<String>) message.get("allrooms"));
                        logger.info(response);
                        ChatClientService chatClientService = ServerState.getServerStateInstance().clientServices.get(message.get("clientid"));
                        MessageTransferService.send(chatClientService.getClientSocket(), response);
                    }
                    case "createroom" -> {
                        logger.info("Received message type createroom");
                        JSONObject response = new CreateRoomHandler().coordinatorNewRoomIdentity((String) message.get("clientid"), (String) message.get("roomid"), (String) message.get("serverid"));
                        ServerData requestServer = ServerState.getServerStateInstance().getServersList().get((String) message.get("serverid"));
                        MessageTransferService.sendToServers(response, requestServer.getServerAddress(), requestServer.getCoordinationPort());
                    }
                    case "leadercreateroom" -> {
                        logger.info("Received message type leadercreateroom");
                        Client client = ServerState.getServerStateInstance().clients.get((String) message.get("clientid"));
                        ChatClientService chatClientService = ServerState.getServerStateInstance().clientServices.get((String) message.get("clientid"));
                        List<ChatClientService> clientThreads_formerRoom = ServerState.getServerStateInstance().getClientServicesInRoomByClient(client);
                        Map<String, JSONObject> responses = new CreateRoomHandler().leaderApprovedNewRoomIdentity((String) message.get("approved"), client, (String) message.get("roomid"));
                        MessageTransferService.send(chatClientService.getClientSocket(), responses.get("client-only"));
                        if (responses.containsKey("broadcast")) {
                            MessageTransferService.send(chatClientService.getClientSocket(), responses.get("broadcast"));
                            MessageTransferService.sendBroadcast(clientThreads_formerRoom, responses.get("broadcast"));
                        }
                    }
                    case "roomexist" -> {
                        logger.info("Received message type roomexist");
                        JSONObject response = new JoinRoomHandler().coordinatorRoomExist((String) message.get("clientid"), (String) message.get("roomid"));
                        ServerData requestServer = ServerState.getServerStateInstance().getServersList().get((String) message.get("serverid"));
                        MessageTransferService.sendToServers(response, requestServer.getServerAddress(), requestServer.getCoordinationPort());
                    }
                    case "leaderroomexist" -> {
                        logger.info("Received message type leaderroomexist");
                        Client client = ServerState.getServerStateInstance().clients.get((String) message.get("clientid"));
                        ChatClientService chatClientService = ServerState.getServerStateInstance().clientServices.get((String) message.get("clientid"));
                        List<ChatClientService> clientThreads_formerRoom = ServerState.getServerStateInstance().getClientServicesInRoomByClient(client);
                        Map<String, JSONObject> responses = new JoinRoomHandler().leaderApprovedRoomExist((String) message.get("exist"), client, (String) message.get("roomid"));
                        List<ChatClientService> clientThreads_joinedRoom = ServerState.getServerStateInstance().getClientServicesInRoomByClient(client);

                        if (!responses.containsKey("askedFromLeader")) {
                            if (responses.containsKey("client-only")) {
                                MessageTransferService.send(chatClientService.getClientSocket(), responses.get("client-only"));
                            }
                            if (responses.containsKey("broadcast-all")) {
                                MessageTransferService.send(chatClientService.getClientSocket(), responses.get("broadcast-all"));
                                MessageTransferService.sendBroadcast(clientThreads_formerRoom, responses.get("broadcast-all"));
                                MessageTransferService.sendBroadcast(clientThreads_joinedRoom, responses.get("broadcast-all"));
                            }
                            if (responses.containsKey("broadcast-former")) {
                                MessageTransferService.sendBroadcast(clientThreads_formerRoom, responses.get("broadcast-former"));
                                ServerState.getServerStateInstance().clientServices.get(client.getIdentity()).stop();
                                ServerState.getServerStateInstance().clientServices.remove(client.getIdentity());
                            }
                        }

                    }
                    case "getroomroute" -> {
                        logger.info("Received message type getroomroute");
                        JSONObject response = new JoinRoomHandler().coordinatorRoomRoute((String) message.get("clientid"), (String) message.get("roomid"));
                        ServerData requestServer = ServerState.getServerStateInstance().getServersList().get((String) message.get("serverid"));
                        MessageTransferService.sendToServers(response, requestServer.getServerAddress(), requestServer.getCoordinationPort());
                    }
                    case "leaderroomroute" -> {
                        logger.info("Received message type leaderroomroute");
                        Client client = ServerState.getServerStateInstance().clients.get((String) message.get("clientid"));
                        ChatClientService chatClientService = ServerState.getServerStateInstance().clientServices.get((String) message.get("clientid"));
                        List<ChatClientService> clientThreads_formerRoom = ServerState.getServerStateInstance().getClientServicesInRoomByClient(client);
                        Map<String, JSONObject> responses = new JoinRoomHandler().leaderApprovedRoomRoute((String) message.get("exist"), client, (String) message.get("roomid"), (String) message.get("host"), (String) message.get("port"));

                        if (responses.containsKey("client-only")) {
                            MessageTransferService.send(chatClientService.getClientSocket(), responses.get("client-only"));
                        }
                        if (responses.containsKey("broadcast-former")) {
                            MessageTransferService.sendBroadcast(clientThreads_formerRoom, responses.get("broadcast-former"));
                            ServerState.getServerStateInstance().clientServices.get(client.getIdentity()).stop();
                            ServerState.getServerStateInstance().clientServices.remove(client.getIdentity());
                        }
                    }
                    case "joinroom" -> {
                        logger.info("Received message type joinroom");
                    }
                    case "deleteroom" -> {
                        logger.info("Received message type deleteroom");
                        // TODO: have to do something when resceive deleteroom broad cast message
                    }
                    case "quit" -> {
                        logger.info("Received message type quit");
                        LeaderState.getInstance().globalClients.remove((String) message.get("identity"));
                        JSONObject gossipMsg = this.gossipHandler.gossip("gossipidentity", System.getProperty("serverID"), LeaderState.getInstance().globalClients);
                        Thread gossipService = new GossipService("send", "gossipidentity", gossipMsg);
                        gossipService.start();
                    }
                    case "newidentity" -> {
                        logger.info("Received message type newidentity");
                        Client client = new Client();
                        Map<String, JSONObject> responses = new NewIdentityHandler().coordinatorNewClientIdentity(client, (String) message.get("identity"), (String) message.get("serverid"));
                        JSONObject response = responses.get("response");
                        //JSONObject response = new NewIdentityHandler().coordinatorNewClientIdentity(client, (String) message.get("identity"), (String) message.get("serverid"));
                        ServerData requestServer = ServerState.getServerStateInstance().getServersList().get((String) message.get("serverid"));
                        MessageTransferService.sendToServers(response, requestServer.getServerAddress(), requestServer.getCoordinationPort());
                        if (responses.containsKey("gossip")) {
                            Thread gossipService = new GossipService("send", "gossipidentity", responses.get("gossip"));
                            gossipService.start();
                        }

                    }
                    case "leadernewidentity" -> {
                        logger.info("Received message type leadernewidentity");
                        Client client = new Client();
                        String newClientID = (String) message.get("identity");
                        ChatClientService chatClientService = ServerState.getServerStateInstance().clientServices.get("1temp-"+newClientID);
                        Map<String, JSONObject> responses = new NewIdentityHandler().leaderApprovedNewClientIdentity((String) message.get("approved"), client, (String) message.get("identity"));
                        List<ChatClientService> clientThreads_newId = ServerState.getServerStateInstance().getClientServicesInRoomByClient(client);
                        MessageTransferService.send(chatClientService.getClientSocket(), responses.get("client-only"));
                        if (responses.containsKey("broadcast")) {
                            MessageTransferService.send(chatClientService.getClientSocket(), responses.get("broadcast"));
                            MessageTransferService.sendBroadcast(clientThreads_newId, responses.get("broadcast"));
                        }
                    }
                    case "pushgossipidentity" -> {
                        logger.info("Received message type pushgossipidentity");
                        long gossiprounds = (long) message.get("rounds");
                        ServerState.getServerStateInstance().setIsIgnorant(false);
                        ServerState.getServerStateInstance().setGlobalClientIDs((ArrayList<String>) message.get("clientids"));
                        if (gossiprounds < ServerState.getServerStateInstance().getInitialRounds()){
                            Thread gossipService = new GossipService("push", "pushgossipidentity", message);
                            gossipService.start();
                        } else {
                            System.out.println("Gossip rounds exceed");
                            //ServerData richneighbour = ServerState.getServerStateInstance().getRichNeighborData();
                            JSONObject msg = new GossipHandler().roundExceed("identity");

                            for (ConcurrentMap.Entry<String, ServerData> entry : ServerState.getServerStateInstance().getServersList().entrySet()) {
                                if (!entry.getKey().equals(message.get("serverid"))) {
                                    MessageTransferService.sendToServers(msg, entry.getValue().getServerAddress(), entry.getValue().getCoordinationPort());
                                }
                            }
                        }
                    }
                    case "roundexceed" -> {
                        logger.info("Received message type roundexceed");
                        if (ServerState.getServerStateInstance().getIsIgnorant()){
                            String msgtype = (String) message.get("messagetype");
                            String host = ServerState.getServerStateInstance().getCurrentServerData().getServerAddress();
                            String port = Integer.toString(ServerState.getServerStateInstance().getCurrentServerData().getCoordinationPort());
                            switch (msgtype) {
                                case "identity" -> {
                                    JSONObject msg = new GossipHandler().pullGossip("pullgossipidentity", host, port);
                                    ServerData richneighbour = ServerState.getServerStateInstance().getRichNeighborData();
                                    MessageTransferService.sendToServers(msg, richneighbour.getServerAddress(), richneighbour.getCoordinationPort());
                                }
                            }
                        }
                    }
                    case "pullgossip" -> {
                        logger.info("Received message type pullgossip");
                        if (!ServerState.getServerStateInstance().getIsIgnorant()){
                            String pulltype = (String) message.get("pulltype");
                            Thread gossipService = new GossipService("pull", pulltype, message);
                            gossipService.start();
                        } else {
                            logger.info("Rich neighbour is ignorant");
                            JSONObject msg = new GossipHandler().isIgnorant();
                            MessageTransferService.sendToServers(msg, (String) message.get("host"), Integer.parseInt((String) message.get("port")));

                        }
                    }
                    case "isignorant" -> {
                        logger.info("Received message type isignorant");
                        String host = ServerState.getServerStateInstance().getCurrentServerData().getServerAddress();
                        String port = Integer.toString(ServerState.getServerStateInstance().getCurrentServerData().getCoordinationPort());
                        JSONObject msg = new GossipHandler().pullGossip("pullgossipidentity", host, port);
                        ArrayList<String> serverlist = new ArrayList<String>(ServerState.getServerStateInstance().getServersList().keySet());
                        Random rand = new Random();
                        String randomneighbour = serverlist.get(rand.nextInt(serverlist.size()));
                        ServerData richneighbour = ServerState.getServerStateInstance().getServersList().get(randomneighbour);
                        MessageTransferService.sendToServers(msg, richneighbour.getServerAddress(), richneighbour.getCoordinationPort());
                    }
                    case "pullupdate" -> {
                        logger.info("Received message type pullupdate");
                        ServerState.getServerStateInstance().setIsIgnorant(false);
                        ServerState.getServerStateInstance().setGlobalClientIDs((ArrayList<String>) message.get("updatedlist"));
                    }
                    default -> {
                        // Send other cases to FastBully Service to handle
//                        logger.info("Sending to fast bully service");
                        FastBullyService.receiveBullyMessage(message);
                    }
                }
                this.stopThread(); // Finally, stop the thread as there is no need for these connections to remain active throughout the lifetime of the system
            } catch (IOException | ParseException e) {
                logger.error("Exception occurred" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
