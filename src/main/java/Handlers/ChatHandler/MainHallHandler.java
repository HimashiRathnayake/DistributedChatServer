package Handlers.ChatHandler;

import Handlers.CoordinationHandler.GossipHandler;
import Handlers.CoordinationHandler.RequestHandler;
import Models.Client;
import Models.Room;
import Models.Server.LeaderState;
import Models.Server.ServerData;
import Models.Server.ServerState;
import Services.CoordinationService.GossipService;
import Services.MessageTransferService;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class MainHallHandler extends Thread{

    Logger logger = Logger.getLogger(MainHallHandler.class);
    private final GossipHandler gossipHandler = new GossipHandler();

    public void run(){
        while (true) {
            if (ServerState.getServerStateInstance().getLeaderServerData() != null) {
                createMainHall();
                break;
            }
        }
    }

    public void createMainHall() {
        ServerData currentServer = ServerState.getServerStateInstance().getCurrentServerData();
        ServerData leaderServer = ServerState.getServerStateInstance().getLeaderServerData();
        String serverID = System.getProperty("serverID");
        String roomID = "MainHall-" + serverID;
        Room mainHall = new Room(roomID, serverID, "", new ArrayList<>());
        ServerState.getServerStateInstance().roomList.put(roomID, mainHall);
        ServerState.getServerStateInstance().getGlobalRoomList().add(roomID);
        ServerState.getServerStateInstance().getGlobalRoomServersList().add(System.getProperty("serverID"));
        ServerState.getServerStateInstance().getGlobalRoomOwnersList().add("");
        ArrayList<String> roomclientids = new ArrayList<String>();
        ServerState.getServerStateInstance().getGlobalRoomClientsList().add(roomclientids);
        if (Objects.equals(currentServer.getServerID(), leaderServer.getServerID())) {
            LeaderState.getInstance().globalRoomList.put(roomID, mainHall);
            logger.info("Main Hall Created");
            // System.out.println(LeaderState.getInstance().getGlobalRoomList());
            JSONObject gossipMsg = this.gossipHandler.gossipRoom("gossiproom", System.getProperty("serverID"), LeaderState.getInstance().getGlobalRoomList());
            Thread gossipService = new GossipService("send", "gossiproom", gossipMsg);
            gossipService.start();
        } else {
            MessageTransferService.sendToServers(new RequestHandler().sendMainHallResponse(mainHall.getRoomID()), leaderServer.getServerAddress(), leaderServer.getCoordinationPort());
        }
    }
}
