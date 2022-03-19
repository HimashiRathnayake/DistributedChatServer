package Services.ChatService;
import Models.Server.ServerData;
import Models.Server.ServerState;

public class ChatClientService implements Runnable {
    private static ChatClientService ChatClientInstance;
    private final int port;
    private static String id;

    private ChatClientService(){
        ServerData server_info = ServerState.getServerStateInstance().getServerData();
        this.port = server_info.getClientPort();
        this.id = server_info.getServerID();
    }
    public static ChatClientService getInstance(){
        if (ChatClientInstance== null){
            ChatClientInstance = new ChatClientService();
        }
        return ChatClientInstance;
    }
    @Override
    public void run() {
        while (true){
            try{Thread.sleep(1500);}catch(InterruptedException e){System.out.println(e);}
                System.out.println("server id = " +this.id);
                System.out.println("server port = " +this.port);
            }
    }
}
