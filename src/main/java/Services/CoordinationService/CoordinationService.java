package Services.CoordinationService;

import Handlers.CoordinationHandler.CreateRoomHandler;
import Handlers.CoordinationHandler.JoinRoomHandler;
import Handlers.CoordinationHandler.NewIdentityHandler;
import Handlers.CoordinationHandler.ResponseHandler;
import Models.Client;
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
import java.util.List;
import java.util.Map;

public class CoordinationService extends Thread {
    private final Socket coordinationSocket;
    Logger logger = Logger.getLogger(CoordinationService.class);
    private final JSONParser parser = new JSONParser();
    private boolean running = true;
    private final ResponseHandler serverResponseHandler = new ResponseHandler();

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
                System.out.println("Receiving: " + message);
                switch (type) {
                    case "list" -> {
                        logger.info("Received message type list");
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
                    case "newidentity" -> {
                        logger.info("Received message type newidentity");
                        Client client = new Client();
                        JSONObject response = new NewIdentityHandler().coordinatorNewClientIdentity(client, (String) message.get("identity"), (String) message.get("serverid"));
                        ServerData requestServer = ServerState.getServerStateInstance().getServersList().get((String) message.get("serverid"));
                        MessageTransferService.sendToServers(response, requestServer.getServerAddress(), requestServer.getCoordinationPort());
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
                }
                this.stopThread(); // Finally, stop the thread as there is no need for these connections to remain active throughout the lifetime of the system
            } catch (IOException | ParseException e) {
                logger.error("Exception occurred" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
