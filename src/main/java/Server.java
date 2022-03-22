import Models.Client;
import Models.Room;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.ConfigFileReaderService;

import java.net.*;
import java.io.*;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Set;

import Services.CoordinationService.CoordinationService;
import Services.CoordinationService.FastBullyService;
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

            Room mainHall = new Room("MainHall-"+serverID, serverID, "", new ArrayList<Client>());
            ServerState.getServerStateInstance().addNewRoom(mainHall);

            // TODO: Refactor following part - Bully Algo.
            // Initialize Bully Algorithm
            FastBullyService.initializeService();
            // ServerData leader = new ServerData("s1", "localhost", 4444, 5555);
            // ServerState.getServerStateInstance().setLeaderServerData(leader);

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
                        SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();
                        if (channel!=null) {
                            Socket socket = channel.socket();
                            int port = socket.getLocalPort();
                            if (server_info.getClientPort() == port) {
                                ChatClientService clientThread = new ChatClientService(socket);
                                clientThread.start();
                            } else {
                                CoordinationService coordinatorThread = new CoordinationService(socket);
                                coordinatorThread.start();
                            }
                        }
                    }
                }
            }

        } catch (CmdLineException e) {
            e.printStackTrace();
        }

    }
}