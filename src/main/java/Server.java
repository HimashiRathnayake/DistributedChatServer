import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.ConfigFileReaderService;

import java.net.*;
import java.io.*;
import java.nio.channels.*;
import java.util.Set;

import Services.CoordinationService.CoordinationService;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import Services.ChatService.ChatClientService;

public class Server {
    public static void main(String[] args) throws IOException {

        String serverID;
        String serversConf;
        CmdLineValues values = new CmdLineValues();
        CmdLineParser parser = new CmdLineParser(values);

        try {
            parser.parseArgument(args);
            serverID = values.getServerId();
            serversConf = values.getServerConfig();
            System.setProperty("serverID", serverID);

            Logger logger = Logger.getLogger(Server.class);
            logger.info("Server configuration.");

            new ConfigFileReaderService().readConfigFile(serverID, serversConf);

            ServerData server_info = ServerState.getServerStateInstance().getCurrentServerData();
            Selector selector = Selector.open();
            int[] ports = {server_info.getClientPort(), server_info.getCoordinationPort()};

            for (int port : ports) {
                ServerSocketChannel server = ServerSocketChannel.open();
                server.configureBlocking(false);
                server.socket().bind(new InetSocketAddress(port));
                server.register(selector, SelectionKey.OP_ACCEPT);
            }

            while (selector.isOpen()) {
                selector.select();
                Set<SelectionKey> readyKeys = selector.selectedKeys();
                for (SelectionKey key : readyKeys) {
                    if (key.isAcceptable()) {
                        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
                        Socket socket = client.socket();
                        int port = socket.getLocalPort();
                        if (server_info.getClientPort()==port){
                            ChatClientService chatClient = new ChatClientService(socket);
                            Thread chatClientThread = new Thread(chatClient);
                            chatClientThread.start();
                            System.out.println("Thread ID:" + chatClientThread.getId());
                        } else {
                            CoordinationService coordinator = new CoordinationService(socket);
                            Thread coordinatorThread = new Thread(coordinator);
                            coordinatorThread.start();
                        }
                    }
                }
            }

        } catch (CmdLineException e) {
            e.printStackTrace();
        }

    }
}