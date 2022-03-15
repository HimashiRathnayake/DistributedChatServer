package Handlers;

import Utils.Client;
import Utils.ClientsYML;
import Utils.YMLReader;
import Utils.YMLWriter;

import java.util.ArrayList;

public class NewIdentityHandler {

    public boolean checkIdentityRules(String identity){
        boolean isIdentityGood;
        isIdentityGood = identity!= null && identity.matches("^[a-zA-Z0-9]*$")
                && identity.length()>=3
                && identity.length()<16;
        return isIdentityGood;
    }

    public boolean checkIdentityUnique(String identity){

        boolean isIdentityUnique = false;
        ArrayList<String> activeClients = new YMLReader().getActiveClientList();
        if (! activeClients.contains(identity)){
            isIdentityUnique = true;
        }
        return isIdentityUnique;
    }

    public void addNewIdentity(String identity, String server){
        Client client = new Client();
        if (checkIdentityUnique(identity) && checkIdentityRules(identity)){
            client.setIdentity(identity);
            client.setServer(server);
            client.setStatus("active");
            ClientsYML clientsYML = new YMLReader().readClientsYML();
            clientsYML.getClients().add(client);
            new YMLWriter().writeClientsYML(clientsYML);
        }
    }
}
