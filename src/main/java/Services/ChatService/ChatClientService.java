package Services.ChatService;

import Handlers.ChatHandler.NewIdentityHandler;
import Handlers.ChatHandler.ResponseHandler;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ChatClientService extends Thread {
    private final Logger logger = Logger.getLogger(ChatClientService.class);
    private final Socket clientSocket;
    private final JSONParser parser = new JSONParser();
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
                    case "roomchange":
                        logger.info("Received message type roomchange");
                        break;
                    case "roomlist":
                        logger.info("Received message type roomlist");
                        break;
                    case "roomcontents":
                        logger.info("Received message type roomcontents");
                        break;
                    case "movejoin":
                        logger.info("Received message type movejoin");
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
                        ArrayList<JSONObject> responses = new NewIdentityHandler(this.responseHandler).addNewIdentity((String) message.get("identity"));
                        for (JSONObject response: responses){
                            send(response);
                        }
                        break;
                    case "route":
                        logger.info("Received message type route");
                        break;
                    case "serverchange":
                        logger.info("Received message type serverchange");
                        break;
                    case "message":
                        logger.info("Received message type message");
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
