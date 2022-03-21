package Services.CoordinationService;

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
                        new MessageTransferService().sendToServers(response, requestServer.getServerAddress(), requestServer.getCoordinationPort());
                    }
                    case "leadernewidentity" -> {
                        logger.info("Received message type leadernewidentity");
                        Client client = new Client();
                        ChatClientService chatClientService = ServerState.getServerStateInstance().clientServices.get("1temp-" + (String) message.get("identity"));
                        Map<String, JSONObject> responses = new NewIdentityHandler().leaderApprovedNewClientIdentity((String) message.get("approved"), client, (String) message.get("identity"));
                        List<ChatClientService> clientThreads_newId = ServerState.getServerStateInstance().getClientServicesInRoomByClient(client);
                        new MessageTransferService().send(chatClientService.getClientSocket(), responses.get("client-only"));
                        if (responses.containsKey("broadcast")) {
                            new MessageTransferService().send(chatClientService.getClientSocket(), responses.get("broadcast"));
                            new MessageTransferService().sendBroadcast(clientThreads_newId, responses.get("broadcast"));
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
