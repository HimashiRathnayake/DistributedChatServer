package Services;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import Models.Server.ServerData;
import Models.Server.ServerState;
import org.apache.log4j.Logger;

public class ConfigFileReaderService {

    private Logger logger = Logger.getLogger(ConfigFileReaderService.class);

    public void readConfigFile(String currentServerID, String serversConfig){
        try{
            BufferedReader buf = new BufferedReader(new FileReader(serversConfig));
            List<ServerData> serverDataList = new ArrayList<ServerData>();
            String lineJustFetched = null;
            String[] wordsArray;

            while(true){
                lineJustFetched = buf.readLine();
                if(lineJustFetched == null){
                    break;
                }else{
                    wordsArray = lineJustFetched.split("\t");
                    String serverID = wordsArray[0];
                    String serverAddress = wordsArray[1];
                    int clientPort = Integer.parseInt(wordsArray[2]);
                    int coordinationPort = Integer.parseInt(wordsArray[3]);
                    ServerData severData = new ServerData(serverID, serverAddress, clientPort, coordinationPort);
                    serverDataList.add(severData);
                }
            }

            buf.close();
            ServerState.getServerStateInstance().setServersList(serverDataList, currentServerID);
            logger.info("Server Configuration Added");

        }catch(Exception e){
            logger.error("Exception while reading configuration due to: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
