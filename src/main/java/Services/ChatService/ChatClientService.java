package Services.ChatService;

import Handlers.ChatHandler.*;
import Models.Client;
import Models.Server.ServerState;
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

public class ChatClientService extends Thread {
    private final Logger logger = Logger.getLogger(ChatClientService.class);
    private final Socket clientSocket;
    private final JSONParser parser = new JSONParser();
    private Client client;
    private final ResponseHandler responseHandler = new ResponseHandler();
    private boolean blinker = true;

    public ChatClientService(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    public void stopThread() {
        blinker = false;
    }

    public void run() {
        while (blinker){
            try {
                assert clientSocket != null;
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                JSONObject message = (JSONObject) parser.parse(in.readLine());
                String type = (String) message.get("type");
                System.out.println(message);

                switch(type){
                    case "list":
                        logger.info("Received message type list");
                        //TODO: get room list from other servers
                        JSONObject roomListResponse = new ResponseHandler().sendRoomList(new RoomListHandler(responseHandler).getRoomList());
                        send(roomListResponse);
                        break;
                    case "who":
                        logger.info("Received message type who");
                        ClientListInRoomHandler clientListInRoomHandler = new ClientListInRoomHandler();
                        String roomID = clientListInRoomHandler.getClientsRoomID(client.getIdentity());
                        ArrayList<String> identities = clientListInRoomHandler.getClientsInRoom(roomID);
                        String owner = clientListInRoomHandler.getRoomOwner(roomID);
                        JSONObject clientInRoomListResponse = new ResponseHandler().sendClientListInChatRoom(roomID, identities, owner);
                        send(clientInRoomListResponse);
                        break;
                    case "createroom":
                        logger.info("Received message type createroom");
                        ArrayList<JSONObject> roomResponses = new CreateRoomHandler(this.responseHandler).createRoom(client, (String) message.get("roomid"));
                        for (JSONObject response : roomResponses) {
                            send(response);
                        }
                        break;
                    case "joinroom":
                        logger.info("Received message type joinroom");
                        ArrayList<JSONObject> joinResponses = new JoinRoomHandler(this.responseHandler).joinRoom(client, (String) message.get("roomid"));
                        for (JSONObject response : joinResponses) {
                            send(response);
                        }
                        break;
                    case "movejoin":
                        logger.info("Received message type movejoin");
                        Map<String, JSONObject> movejoinResponses = new MoveJoinHandler(this.responseHandler).movejoin((String) message.get("former"), (String) message.get("roomid"), (String) message.get("identity"), this.client);
                        send(movejoinResponses.get("client-only"));
                        List<ChatClientService> clientThreads_movejoin = ServerState.getServerStateInstance().getClientServicesInRoomByClient(this.client);
                        for (ChatClientService service: clientThreads_movejoin){
                            sendBroadcast(service.clientSocket, movejoinResponses.get("broadcast"));
                        }
                        break;
                    case "deleteroom":
                        logger.info("Received message type deleteroom");
                        Map<String, ArrayList<JSONObject>> deleteRoomResponses = new DeleteRoomHandler(this.responseHandler).deleteRoom((String) message.get("roomid"), this.client);
                        if (deleteRoomResponses.containsKey("broadcast")) {
                            List<ChatClientService> clientThreads_deleteRoom = ServerState.getServerStateInstance().getClientServicesInRoomByClient(this.client);
                            for (JSONObject deleteResponse : deleteRoomResponses.get("broadcast")) {
                                for (ChatClientService service : clientThreads_deleteRoom) {
                                    sendBroadcast(service.clientSocket, deleteResponse);
                                }
                            }
                        }
                        for (JSONObject deleteResponse : deleteRoomResponses.get("broadcast")){
                            sendBroadcast(this.clientSocket, deleteResponse);
                        }
                        send(deleteRoomResponses.get("client-only").get(0));
                        break;
                    case "quit":
                        logger.info("Received message type quit");
                        Map<String, JSONObject> quitResponses = new QuitHandler(this.responseHandler).handleQuit(this.client);
                        send(quitResponses.get("reply"));
                        // server closes the connection
                        ServerState.getServerStateInstance().clientServices.remove(this.client.getIdentity());
                        this.stopThread();
                        break;
                    case "newidentity":
                        logger.info("Received message type newidentity");
                        // TODO: Send to coordinator for approving new identity
                        this.client = new Client();
                        ArrayList<JSONObject> responses = new NewIdentityHandler(this.responseHandler).addNewIdentity(this, client, (String) message.get("identity"));
                        for (JSONObject response : responses) {
                            send(response);
                        }
                        break;
                    case "message":
                        logger.info("Received message type message");
                        String clientID = this.client.getIdentity();
                        String content = (String) message.get("content");
                        List<ChatClientService> clientThreads = ServerState.getServerStateInstance().getClientServicesInRoomByClient(this.client);
                        JSONObject messageResponse = new MessageHandler(this.responseHandler).handleMessage(clientID, content);
                        for (ChatClientService service: clientThreads){
                            sendBroadcast(service.clientSocket, messageResponse);
                        }
                        break;
                }
            } catch (IOException | ParseException e) {
                logger.error("Exception occurred" + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    //TODO: Remove this and merge to following one
    public void send(JSONObject obj) throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        out.write((obj.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public void sendBroadcast(Socket socket, JSONObject obj) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write((obj.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

}
