package Services.CoordinationService;

import Models.Server.ServerData;
import Models.Server.ServerState;

public class CoordinationService implements Runnable{
    private static CoordinationService CoordinationInstance;
    private final int port;
    private static String id;

    private CoordinationService(){
        ServerData server_info = ServerState.getServerStateInstance().getServerData();
        this.port = server_info.getCoordinationPort();
        this.id = server_info.getServerID();
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
            System.out.println("server id = " +this.id);
            System.out.println("server coordination port = " +this.port);
        }
    }
}
