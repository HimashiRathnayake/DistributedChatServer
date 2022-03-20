package Handlers.ChatHandler;

import Models.Client;
import Models.Server.ServerState;

public class QuitHandler {

    public void handleQuit(Client client) {
        // remove the client from the client list. - roomList, clients, clientServices
//        ServerState.getServerStateInstance().clients.remove(client.getIdentity());
        // roomchange message with empty roomid.
        // server closes the connection
        // If the client is the owner of a chat room, delete chatroom
        // Else delete client from the room

//        ServerState.getServerStateInstance().clientServices.remove(client.getIdentity());
    }
}
