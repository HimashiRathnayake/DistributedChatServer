package Services.CoordinationService;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class CoordinationService implements Runnable{
    private static CoordinationService CoordinationInstance;
    private final ServerSocket coordinationServerSocket;
    Logger logger = Logger.getLogger(CoordinationService.class);
    private final JSONParser parser = new JSONParser();

    private CoordinationService(ServerSocket coordinationServerSocket){
        this.coordinationServerSocket = coordinationServerSocket;
    }

    public static CoordinationService getInstance(ServerSocket coordinationServerSocket){
        if (CoordinationInstance== null){
            CoordinationInstance = new CoordinationService(coordinationServerSocket);
        }
        return CoordinationInstance;
    }

    @Override
    public void run() {
        Socket coordinationSocket = null;
        try {
            coordinationSocket = coordinationServerSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true){
            try {
                assert coordinationSocket != null;
                BufferedReader in = new BufferedReader(new InputStreamReader(coordinationSocket.getInputStream(), "UTF-8"));
                JSONObject message = (JSONObject) parser.parse(in.readLine());
                System.out.println("Receiving: " + message.toJSONString());
            } catch (IOException | ParseException e) {
                logger.error("Exception occurred" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
