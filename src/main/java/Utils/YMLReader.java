package Utils;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;


public class YMLReader {

    static String clientsYMLPath = "ChatData\\clients.yml";
    static String roomsYMLPath = "ChatData\\room.yml";

    public ClientsYML readClientsYML() {
        Yaml yaml = new Yaml();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(clientsYMLPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return (ClientsYML)yaml.loadAs(inputStream, ClientsYML.class);
    }

    public RoomsYML readRoomsYML(){
        Yaml yaml = new Yaml();
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(clientsYMLPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return yaml.loadAs(inputStream, RoomsYML.class);
    }

    public ArrayList<String> getActiveClientList(){
        ArrayList<String> activeClients = new ArrayList<>();
        ClientsYML clientsYML = readClientsYML();
        for (int i = 0; i<clientsYML.getClients().size(); i++){
            activeClients.add(clientsYML.getClients().get(i).getIdentity());
        }
        return activeClients;
    }
}
