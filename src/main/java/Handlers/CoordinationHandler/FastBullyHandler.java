package Handlers.CoordinationHandler;

import org.json.simple.JSONObject;

import java.util.ArrayList;

public class FastBullyHandler {

    // {"type" : "iamup", "serverid" : "ServerID"}
    @SuppressWarnings("unchecked")
    public JSONObject iamUpMessage(){
        JSONObject message = new JSONObject();
        message.put("type", "iamup");
        message.put("serverid", System.getProperty("serverID"));
        return message;
    }

    // {"type" : "election", "serverid" : "ServerID"}
    @SuppressWarnings("unchecked")
    public JSONObject electionMessage(){
        JSONObject message = new JSONObject();
        message.put("type", "election");
        message.put("serverid", System.getProperty("serverID"));
        return message;
    }

    // {"type" : "view", "serverid" : "ServerID", "serverlist" : ["s1", "s2"]}
    @SuppressWarnings("unchecked")
    public JSONObject viewMessage(ArrayList<String> serverList){
        JSONObject message = new JSONObject();
        message.put("type", "view");
        message.put("serverid", System.getProperty("serverID"));
        message.put("serverlist", serverList);
        return message;
    }

    // {"type" : "answer", "serverid" : "ServerID"}
    @SuppressWarnings("unchecked")
    public JSONObject answerMessage(){
        JSONObject message = new JSONObject();
        message.put("type", "answer");
        message.put("serverid", System.getProperty("serverID"));
        return message;
    }

    // {"type" : "nomination"}
    @SuppressWarnings("unchecked")
    public JSONObject nominationMessage(){
        JSONObject message = new JSONObject();
        message.put("type", "nomination");
        return message;
    }

    // {"type" : "coordinator", "serverid" : "ServerID"}
    @SuppressWarnings("unchecked")
    public JSONObject coordinatorMessage(){
        JSONObject message = new JSONObject();
        message.put("type", "coordinator");
        message.put("serverid", System.getProperty("serverID"));
        return message;
    }

    //   {"type" : "heartbeat", "serverid" : "ServerID"}
    @SuppressWarnings("unchecked")
    public JSONObject heartBeatMessage(){
        JSONObject message = new JSONObject();
        message.put("type", "heartbeat");
        message.put("serverid", System.getProperty("serverID"));
        return message;
    }

}
