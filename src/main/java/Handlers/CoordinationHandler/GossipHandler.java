package Handlers.CoordinationHandler;

import Models.Client;
import Models.Room;
import Models.Server.ServerState;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GossipHandler {

    // {"type" : "gossipnewidentity", "serverid" : "ServerID", "updatedlist": []}
    public JSONObject gossip(String type, String serverid, ConcurrentHashMap<String, Client> globalClients){
        JSONObject gossip = new JSONObject();
        gossip.put("type", type);
        gossip.put("serverid", serverid);
        gossip.put("updatedlist", globalClients);
        return gossip;
    }

    // {"type" : "gossipnewidentity", "serverid" : "ServerID", "updatedlist": []}
    public JSONObject gossipRoom(String type, String serverid, ConcurrentHashMap<String, Room> globalRooms){
        JSONObject gossip = new JSONObject();
        gossip.put("type", type);
        gossip.put("serverid", serverid);
        gossip.put("updatedlist", globalRooms);
        return gossip;
    }

    // {"type" : "pushgossipnewidentity", "serverid" : "ServerID", "updatedlist": "", "rounds": 1}
    public JSONObject pushgossip(String type, String serverid, ArrayList<String> clientids, int rounds){
        JSONObject gossip = new JSONObject();
        gossip.put("type", type);
        gossip.put("serverid", serverid);
        gossip.put("clientids", clientids);
        gossip.put("rounds", rounds);
        return gossip;
    }

    // {"type" : "pushgossipnewidentity", "serverid" : "ServerID", "updatedlist": "", "rounds": 1}
    public JSONObject pushgossipRoom(String type, String serverid, ArrayList<String> roomids, int rounds){
        JSONObject gossip = new JSONObject();
        gossip.put("type", type);
        gossip.put("serverid", serverid);
        gossip.put("roomids", roomids);
        gossip.put("rounds", rounds);
        return gossip;
    }

    // {"type" : "gossipnewidentity", "serverid" : "ServerID", "identity": "identity"}
    public JSONObject gossipNewIdentity(String serverid, String identity){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "gossipnewidentity");
        gossip.put("serverid", serverid);
        gossip.put("identity", identity);
        return gossip;
    }

    // {"type" : "pullupdate", "updatedlist": []}
    public JSONObject pullUpdate(ArrayList<String> updatedlist, String updatedType){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "pullupdate");
        gossip.put("updatetype", updatedType);
        gossip.put("updatedlist", updatedlist);
        return gossip;
    }

    // {"type" : "pushgossipnewidentity", "sender": "senderid", "serverid" : "ServerID", "identity": "identity", "rounds": "rounds"}
    @SuppressWarnings("unchecked")
    public JSONObject pushgossipnewidentity(String type, String serverid, String identity, String rounds){
        JSONObject gossip = new JSONObject();
        gossip.put("type", type);
        gossip.put("sender", System.getProperty("serverID"));
        gossip.put("serverid", serverid);
        gossip.put("identity", identity);
        gossip.put("rounds", rounds);
        return gossip;
    }

    // {"type" : "gossipcreateroom", "serverid" : "ServerID", "roomid": "roomid"}
    @SuppressWarnings("unchecked")
    public JSONObject gossipCreateRoom(String serverid, String roomid){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "gossipcreateroom");
        gossip.put("serverid", serverid);
        gossip.put("roomid", roomid);
        return gossip;
    }

    // {"type" : "pushgossipcreateroom", "sender": "senderid", "serverid" : "ServerID", "roomid": "roomid"}
    @SuppressWarnings("unchecked")
    public JSONObject pushgossipcreateroom(String type, String serverid, String roomid){
        JSONObject gossip = new JSONObject();
        gossip.put("type", type);
        gossip.put("sender", System.getProperty("serverID"));
        gossip.put("serverid", serverid);
        gossip.put("roomid", roomid);
        return gossip;
    }

    // {"type" : "gossipdeleteroom", "sender": "senderid", "serverid" : "ServerID", "roomid": "roomid"}
    @SuppressWarnings("unchecked")
    public JSONObject gossipDeleteRoom(String serverid, String roomid){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "gossipdeleteroom");
        gossip.put("serverid", serverid);
        gossip.put("roomid", roomid);
        return gossip;
    }

    // {"type" : "pushgossipdeleteroom", "sender": "senderid", "serverid" : "ServerID", "roomid": "roomid"}
    @SuppressWarnings("unchecked")
    public JSONObject pushgossipdeleteroom(String type, String serverid, String roomid){
        JSONObject gossip = new JSONObject();
        gossip.put("type", type);
        gossip.put("sender", System.getProperty("serverID"));
        gossip.put("serverid", serverid);
        gossip.put("roomid", roomid);
        return gossip;
    }

    // {"type" : "gossipquit", "serverid" : "ServerID", "identity": "identity"}
    @SuppressWarnings("unchecked")
    public JSONObject gossipQuit(String serverid, String identity){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "gossipquit");
        gossip.put("serverid", serverid);
        gossip.put("identity", identity);
        return gossip;
    }

    // {"type" : "pushgossipquit", "sender": "senderid", "serverid" : "ServerID", "identity": "identity"}
    @SuppressWarnings("unchecked")
    public JSONObject pushgossipquit(String type, String serverid, String identity){
        JSONObject gossip = new JSONObject();
        gossip.put("type", type);
        gossip.put("sender", System.getProperty("serverID"));
        gossip.put("serverid", serverid);
        gossip.put("identity", identity);
        return gossip;
    }

    // {"type" : "pullgossipnewidentity", "sender": "senderid"}
    @SuppressWarnings("unchecked")
    public JSONObject pullgossipnewidentity(){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "pullgossipnewidentity");
        gossip.put("sender", System.getProperty("serverID"));
        return gossip;
    }

    // {"type" : "pullgossip", "sender": "senderid", "host": "host", "port": "port"}
    @SuppressWarnings("unchecked")
    public JSONObject pullGossip(String pulltype, String host, String port){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "pullgossip");
        gossip.put("pulltype", pulltype);
        gossip.put("sender", System.getProperty("serverID"));
        gossip.put("host", host);
        gossip.put("port", port);
        return gossip;
    }

    // {"type" : "isignorant", "sender": "senderid"}
    @SuppressWarnings("unchecked")
    public JSONObject isIgnorant(String pulltype){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "isignorant");
        gossip.put("sender", System.getProperty("serverID"));
        gossip.put("pulltype", pulltype);
        return gossip;
    }

    // {"type" : "pullgossipcreateroom", "sender": "senderid"}
    @SuppressWarnings("unchecked")
    public JSONObject pullgossipcreateroom(){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "pullgossipcreateroom");
        gossip.put("sender", System.getProperty("serverID"));

        return gossip;
    }

    // {"type" : "pullgossipdeleteroom", "sender": "senderid"}
    @SuppressWarnings("unchecked")
    public JSONObject pullgossipdeleteroom(){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "pullgossipdeleteroom");
        gossip.put("sender", System.getProperty("serverID"));
        return gossip;
    }

    // {"type" : "pullgossipquit", "sender": "senderid"}
    @SuppressWarnings("unchecked")
    public JSONObject pullgossipQuit(){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "pushgossipquit");
        gossip.put("sender", System.getProperty("serverID"));
        return gossip;
    }

    // {"type" : "roundexceed", "messagetype": "type"}
    @SuppressWarnings("unchecked")
    public JSONObject roundExceed(String type){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "roundexceed");
        gossip.put("messagetype", type);
        return gossip;
    }

}
