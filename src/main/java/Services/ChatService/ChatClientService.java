package Services.ChatService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClientService extends Thread {
    private final Logger logger = Logger.getLogger(ChatClientService.class);
    private final Socket clientSocket;
    private final JSONParser parser = new JSONParser();

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
}
