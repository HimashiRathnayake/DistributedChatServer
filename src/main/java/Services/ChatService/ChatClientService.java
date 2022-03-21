package Services.ChatService;

import Handlers.ChatHandler.*;
import Models.Client;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ChatClientService extends Thread {
    private final Logger logger = Logger.getLogger(ChatClientService.class);
    private final Socket clientSocket;
    private final JSONParser parser = new JSONParser();
    private Client client;
    private final ResponseHandler responseHandler = new ResponseHandler();
    private boolean running = true;

    public ChatClientService(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    public void stopThread() {
        running = false;
    }

    public void run() {
        while (running){
            try {
                assert clientSocket != null;
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                JSONObject message = (JSONObject) parser.parse(in.readLine());
                String type = (String) message.get("type");
                System.out.println(message);

                switch (type) {
                    case "list" -> {
                        logger.info("Received message type list");
                        //TODO: get room list from other servers
                        JSONObject roomListResponse = new ResponseHandler().sendRoomList(new RoomListHandler(responseHandler).getRoomList());
                        MessageTransferService.send(this.clientSocket, roomListResponse);
                    }
                    case "who" -> {
                        logger.info("Received message type who");
                        ClientListInRoomHandler clientListInRoomHandler = new ClientListInRoomHandler();
                        String roomID = clientListInRoomHandler.getClientsRoomID(client.getIdentity());
                        ArrayList<String> identities = clientListInRoomHandler.getClientsInRoom(roomID);
                        String owner = clientListInRoomHandler.getRoomOwner(roomID);
                        JSONObject clientInRoomListResponse = new ResponseHandler().sendClientListInChatRoom(roomID, identities, owner);
                        MessageTransferService.send(this.clientSocket, clientInRoomListResponse);
                    }
                    case "createroom" -> {
                        logger.info("Received message type createroom");
                        List<ChatClientService> clientThreads_formerRoom = ServerState.getServerStateInstance().getClientServicesInRoomByClient(this.client);
                        Map<String, JSONObject> createRoomResponses = new CreateRoomHandler(this.responseHandler).createRoom(client, (String) message.get("roomid"));
                        MessageTransferService.send(this.clientSocket, createRoomResponses.get("client-only"));
                        if (createRoomResponses.containsKey("broadcast")) {
                            MessageTransferService.send(this.clientSocket, createRoomResponses.get("broadcast"));
                            MessageTransferService.sendBroadcast(clientThreads_formerRoom, createRoomResponses.get("broadcast"));
                        }
                    }
                    case "joinroom" -> {
                        logger.info("Received message type joinroom");
                        List<ChatClientService> clientThreads_formerRoom = ServerState.getServerStateInstance().getClientServicesInRoomByClient(this.client);
                        Map<String, JSONObject> joinRoomResponses = new JoinRoomHandler(this.responseHandler).joinRoom(client, (String) message.get("roomid"));
                        List<ChatClientService> clientThreads_joinedRoom = ServerState.getServerStateInstance().getClientServicesInRoomByClient(this.client);

                        if (joinRoomResponses.containsKey("client-only")) {
                            MessageTransferService.send(this.clientSocket, joinRoomResponses.get("client-only"));
                        }
                        if (joinRoomResponses.containsKey("broadcast")) {
                            MessageTransferService.send(this.clientSocket, joinRoomResponses.get("broadcast"));
                            MessageTransferService.sendBroadcast(clientThreads_formerRoom, joinRoomResponses.get("broadcast"));
                            MessageTransferService.sendBroadcast(clientThreads_joinedRoom, joinRoomResponses.get("broadcast"));
                        }
                    }
                    case "movejoin" -> {
                        // TODO: have to check the functionality
                        logger.info("Received message type movejoin");
                        Map<String, JSONObject> movejoinResponses = new MoveJoinHandler(this.responseHandler).movejoin((String) message.get("former"), (String) message.get("roomid"), (String) message.get("identity"), this.client);
                        MessageTransferService.send(this.clientSocket, movejoinResponses.get("client-only"));
                        List<ChatClientService> clientThreads_movejoin = ServerState.getServerStateInstance().getClientServicesInRoomByClient(this.client);
                        MessageTransferService.sendBroadcast(clientThreads_movejoin, movejoinResponses.get("broadcast"));
                    }
                    case "deleteroom" -> {
                        logger.info("Received message type deleteroom");
                        Map<String, ArrayList<JSONObject>> deleteRoomResponses = new DeleteRoomHandler(this.responseHandler).deleteRoom((String) message.get("roomid"), this.client);
                        if (deleteRoomResponses.containsKey("broadcast")) {
                            List<ChatClientService> clientThreads_deleteRoom = ServerState.getServerStateInstance().getClientServicesInRoomByClient(this.client);
                            for (JSONObject deleteResponse : deleteRoomResponses.get("broadcast")) {
                                MessageTransferService.send(this.clientSocket, deleteResponse);
                                MessageTransferService.sendBroadcast(clientThreads_deleteRoom, deleteResponse);
                            }
                        }
                        MessageTransferService.send(this.clientSocket, deleteRoomResponses.get("client-only").get(0));
                    }
                    case "quit" -> {
                        logger.info("Received message type quit");
                        List<ChatClientService> clientThreads_quit = ServerState.getServerStateInstance().getClientServicesInRoomByClient(this.client);
                        Map<String, ArrayList<JSONObject>> quitResponses = new QuitHandler(this.responseHandler).handleQuit(this.client);
                        if (quitResponses.containsKey("broadcast")) {
                            for (JSONObject response : quitResponses.get("broadcast")) {
                                MessageTransferService.sendBroadcast(clientThreads_quit, response);
                            }
                        }
                        if (quitResponses.containsKey("reply")) {
                            MessageTransferService.send(this.clientSocket, quitResponses.get("reply").get(0));
                        }
                        if (quitResponses.containsKey("client-only")) {
                            MessageTransferService.send(this.clientSocket, quitResponses.get("client-only").get(0));
                        }
                        // server closes the connection
                        ServerState.getServerStateInstance().clientServices.remove(this.client.getIdentity());
                        this.stopThread();
                    }
                    case "newidentity" -> {
                        logger.info("Received message type newidentity");
                        this.client = new Client();
                        Map<String, JSONObject> responses = new NewIdentityHandler(this.responseHandler).addNewIdentity(this, client, (String) message.get("identity"));
                        List<ChatClientService> clientThreads_newId = ServerState.getServerStateInstance().getClientServicesInRoomByClient(this.client);
                        MessageTransferService.send(this.clientSocket, responses.get("client-only"));
                        MessageTransferService.send(this.clientSocket, responses.get("broadcast"));
                        if (responses.containsKey("broadcast")) {
                            MessageTransferService.sendBroadcast(clientThreads_newId, responses.get("broadcast"));
                        }
                    }
                    case "message" -> {
                        logger.info("Received message type message");
                        String clientID = this.client.getIdentity();
                        String content = (String) message.get("content");
                        List<ChatClientService> clientThreads = ServerState.getServerStateInstance().getClientServicesInRoomByClient(this.client);
                        JSONObject messageResponse = new MessageHandler(this.responseHandler).handleMessage(clientID, content);
                        MessageTransferService.sendBroadcast(clientThreads, messageResponse);
                        //TODO: Remove later - Only for testing
                        ServerData server = ServerState.getServerStateInstance().getServersList().get("s1");
                        MessageTransferService.sendToServers(messageResponse, server.getServerAddress(), server.getCoordinationPort());
                    }
                }
            } catch (IOException e) {
                //TODO: Need to handle Abrupt disconnections by clients
                logger.info("Exception occurred" + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            } catch (ParseException e) {
                logger.error("Parse Exception occurred" + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public Socket getClientSocket(){
        return this.clientSocket;
    }

}
