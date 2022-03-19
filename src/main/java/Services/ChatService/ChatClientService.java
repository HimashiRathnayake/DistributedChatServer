package Services.ChatService;
import Models.Server.ServerData;
import Models.Server.ServerState;
import org.apache.log4j.Logger;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatClientService implements Runnable {
    private static ChatClientService ChatClientInstance;
    private Socket clientSocket;
    private final int port;
    private static String id;
    private  Logger logger = Logger.getLogger(ChatClientService.class);
    private DataInputStream ournewDataInputstream;

    private ChatClientService(){
        ServerData server_info = ServerState.getServerStateInstance().getServerData();
        this.port = server_info.getClientPort();
        this.id = server_info.getServerID();
        try {
            InetAddress inetadress = InetAddress.getByName(server_info.getServerAddress());
            ServerSocket serverClientSocket = new ServerSocket(this.port);
            this.clientSocket = serverClientSocket.accept();
            ournewDataInputstream = new DataInputStream(clientSocket.getInputStream());
            System.out.println(clientSocket);
        }catch (Exception e){
            logger.error("ChatClientService exception occurred. Reason - "+ e.getMessage());
        }
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
            try {
                String receivedString = this.ournewDataInputstream.readUTF();
                System.out.println("Received message = " + receivedString);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            try{Thread.sleep(1500);}catch(InterruptedException e){System.out.println(e);}
//                System.out.println("server id = " +this.id);
//                System.out.println("server port = " +this.port);
            }
    }
}
