package Handlers.CoordinationHandler;

import Models.Client;
import Models.Room;
import org.json.simple.JSONObject;

import java.util.ArrayList;
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
    public JSONObject pushgossipRoom(String type, String serverid, ArrayList<String> roomids, ArrayList<String> roomservers, ArrayList<String> roomowners, ArrayList<ArrayList<String>> clientids, int rounds){
        JSONObject gossip = new JSONObject();
        gossip.put("type", type);
        gossip.put("serverid", serverid);
        gossip.put("roomids", roomids);
        gossip.put("roomservers", roomservers);
        gossip.put("roomowners", roomowners);
        gossip.put("clientids", clientids);
        gossip.put("rounds", rounds);
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

    // {"type" : "pullupdate", "updatedlist": []}
    public JSONObject pullUpdateRooms(ArrayList<String> updatedroomids, ArrayList<String> roomservers, ArrayList<String> roomowners, ArrayList<ArrayList<String>> clientids, String updatedType){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "pullupdate");
        gossip.put("updatetype", updatedType);
        gossip.put("updatedroomids", updatedroomids);
        gossip.put("roomservers", roomservers);
        gossip.put("roomowners", roomowners);
        gossip.put("clientids", clientids);
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

    // {"type" : "roundexceed", "messagetype": "type"}
    @SuppressWarnings("unchecked")
    public JSONObject roundExceed(String type){
        JSONObject gossip = new JSONObject();
        gossip.put("type", "roundexceed");
        gossip.put("messagetype", type);
        return gossip;
    }

}
