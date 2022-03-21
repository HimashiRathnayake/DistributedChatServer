package Services.CoordinationService;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class CoordinationService extends Thread{
    private final Socket coordinationSocket;
    Logger logger = Logger.getLogger(CoordinationService.class);
    private final JSONParser parser = new JSONParser();
    private boolean running = true;

    public CoordinationService(Socket coordinationSocket){
        this.coordinationSocket = coordinationSocket;
    }

    public void stopThread() {
        running = false;
    }

    public void run() {
        while (running){
            try {
                assert coordinationSocket != null;
                BufferedReader in = new BufferedReader(new InputStreamReader(coordinationSocket.getInputStream(), StandardCharsets.UTF_8));
                JSONObject message = (JSONObject) parser.parse(in.readLine());
                String type = (String) message.get("type");
                System.out.println("Receiving: " + message);
                switch (type) {
                    //TODO: Remove this as this is just to test
                    case "message" -> {
                        logger.info("Received message type test");
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
