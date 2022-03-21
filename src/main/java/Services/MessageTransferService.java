package Services;

import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.ChatService.ChatClientService;
import org.json.simple.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class MessageTransferService {

    private MessageTransferService(){}

    public static void send(Socket socket, JSONObject message) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write((message.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public static void sendBroadcast(List<ChatClientService> clientThreads, JSONObject message) throws IOException {
        for (ChatClientService service : clientThreads) {
            send(service.getClientSocket(), message);
        }
    }

    public static void sendToServers(JSONObject message, String host, int port) {
        try{
            Socket socket = new Socket(host, port);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((message.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();
//            while (true){
//                if(socket.isClosed()){
//                    socket.close();
//                    break;
//                }
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendToServersBroadcast(JSONObject message) {
        ConcurrentMap<String, ServerData> serverList = ServerState.getServerStateInstance().getServersList();
        ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
        for (ConcurrentMap.Entry<String, ServerData> entry : serverList.entrySet()) {
            if (!currentServer.getServerID().equals(entry.getKey())) {
                sendToServers(message, entry.getValue().getServerAddress(), entry.getValue().getCoordinationPort());
            }
        }
    }
}
