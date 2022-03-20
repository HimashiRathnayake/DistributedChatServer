package Services.ChatService;

import Handlers.ChatHandler.MessageHandler;
import Handlers.ChatHandler.MoveJoinHandler;
import Handlers.ChatHandler.NewIdentityHandler;
import Handlers.ChatHandler.ResponseHandler;
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

    public ChatClientService(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    public void run() {
        while (true){
            try {
                assert clientSocket != null;
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                JSONObject message = (JSONObject) parser.parse(in.readLine());
                String type = (String) message.get("type");
                System.out.println(message);
                switch(type){
                    case "list":
                        logger.info("Received message type list");
                        break;
                    case "who":
                        logger.info("Received message type who");
                        break;
                    case "createroom":
                        logger.info("Received message type createroom");
                        break;
                    case "joinroom":
                        logger.info("Received message type joinroom");
                        break;
                    case "movejoin":
                        logger.info("Received message type movejoin");
                        Map<String, JSONObject> movejoinResponses = new MoveJoinHandler(this.responseHandler).movejoin((String) message.get("former"), (String) message.get("roomid"), (String) message.get("identity"), this.client);
                        send(movejoinResponses.get("client-only"));

                        break;
                    case "deleteroom":
                        logger.info("Received message type deleteroom");
                        break;
                    case "quit":
                        logger.info("Received message type quit");
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
//                        String clientID = this.client.getIdentity();
//                        String content = (String) message.get("content");
//                        List<ChatClientService> clientThreads = ServerState.getServerStateInstance().getClientServicesInRoomByClient(this.client);
                        break;
                }
            } catch (IOException | ParseException e) {
                logger.error("Exception occurred" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void send(JSONObject obj) throws IOException {
        OutputStream out = clientSocket.getOutputStream();
        out.write((obj.toJSONString() + "\n").getBytes("UTF-8"));
        out.flush();
    }
}
