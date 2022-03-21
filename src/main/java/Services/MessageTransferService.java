package Services;

import Services.ChatService.ChatClientService;
import org.json.simple.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessageTransferService {

    public MessageTransferService(){}
    public void send(Socket socket, JSONObject message) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write((message.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    public void sendBroadcast(List<ChatClientService> clientThreads, JSONObject message) throws IOException {
        for (ChatClientService service : clientThreads) {
            send(service.getClientSocket(), message);
        }
    }

    public void sendToServers(JSONObject message, String host, int port) {
        try{
            Socket socket = new Socket(host, port);
            DataOutputStream dataOutputStream = null;
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.write((message.toJSONString() + "\n").getBytes(StandardCharsets.UTF_8));
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
