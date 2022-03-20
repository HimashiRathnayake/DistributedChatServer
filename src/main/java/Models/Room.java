package Models;

import java.util.ArrayList;

public class Room {

    private String roomID;
    private String server;
    private String owner;
    private ArrayList<Client> clients;

    public Room(String roomID, String server, String owner, ArrayList<Client> clients) {
        this.roomID = roomID;
        this.server = server;
        this.owner = owner;
        this.clients = clients;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public void setClients(ArrayList<Client> clients) {
        this.clients = clients;
    }

    public void addClient(Client client){
        this.clients.add(client);
    }

    public void removeClient(Client client){
        this.clients.remove(client);

    public void addClientList(ArrayList<Client> client){
        this.clients.addAll(client);
    }

    public synchronized void removeClient1(String clientID){
        for(Client client :this.clients){
            if(client.identity.equals(clientID)){
                this.clients.remove(client);
                break;
            }
        }
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
