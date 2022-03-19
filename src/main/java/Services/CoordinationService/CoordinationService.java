package Services.CoordinationService;

import Models.Server.ServerData;
import Models.Server.ServerState;
import org.apache.log4j.Logger;

import java.net.Socket;

public class CoordinationService implements Runnable{
    private static CoordinationService CoordinationInstance;
    private final int port;
    private static String id;
    private Socket coordinationSocket;
    Logger logger = Logger.getLogger(CoordinationService.class);

    private CoordinationService(){
        ServerData server_info = ServerState.getServerStateInstance().getServerData();
        this.port = server_info.getCoordinationPort();
        this.id = server_info.getServerID();
        try {
            this.coordinationSocket = new Socket(server_info.getServerAddress(), this.port);
        }catch (Exception e){
            logger.error("Coordination exception occurred. Reason - "+ e.getMessage());
        }
    }
    public static CoordinationService getInstance(){
        if (CoordinationInstance== null){
            CoordinationInstance = new CoordinationService();
        }
        return CoordinationInstance;
    }
    @Override
    public void run() {
        while (true){
            try{Thread.sleep(1500);}catch(InterruptedException e){System.out.println(e);}
//            System.out.println("server id = " +this.id);
//            System.out.println("server coordination port = " +this.port);
        }
    }
}
