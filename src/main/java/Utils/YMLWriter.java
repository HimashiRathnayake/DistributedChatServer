package Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static java.util.logging.LogManager.*;

public class YMLWriter {

//    private static final Logger LOGGER = LogManager.getLogger(YMLWriter.class);

    public void writeAnyFile(String filePath, String content) {
        BufferedWriter bw = null;

        try {
            File file = new File(filePath);
            FileWriter fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(content);
        } catch (IOException var14) {
//            LOGGER.error(var14);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (Exception var13) {
//                LOGGER.error("Error in closing the BufferedWriter" + var13);
            }

        }

    }

    public void writeClientsYML(ClientsYML clientsYML){

        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("clients:").append("\n\n");
        for (int i = 0; i<clientsYML.getClients().size(); i++){
            stringBuffer.append("  ").append("-").append(" ")
                    .append("identity: ").append(clientsYML.getClients().get(i).getIdentity()).append("\n")
                    .append("    ").append("server: ").append(clientsYML.getClients().get(i).getServer()).append("\n")
                    .append("    ").append("status: ").append(clientsYML.getClients().get(i).getStatus()).append("\n\n");
        }

        String clientsYMLContent = stringBuffer.toString();
        writeAnyFile(YMLReader.clientsYMLPath, clientsYMLContent);

    }

    public void writeRoomsYML(RoomsYML roomsYML){

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("rooms: ").append("\n\n");
        for (int i = 0; i<roomsYML.getRooms().size(); i++){
            stringBuilder.append("  ").append("-").append(" ")
                    .append("roomID: ").append(roomsYML.getRooms().get(i).getRoomID()).append("\n")
                    .append("    ").append("server: ").append(roomsYML.getRooms().get(i).getServer()).append("\n")
                    .append("    ").append("clients: ").append("\n");
            for (int j = 0; j<roomsYML.getRooms().get(i).getClients().size(); j++){
                stringBuilder.append("      ").append("-").append(" ").append(roomsYML.getRooms().get(i).getClients().get(j)).append("\n");
            }
            stringBuilder.append("\n\n");
        }

        String roomsYMLContent = stringBuilder.toString();
        writeAnyFile(YMLReader.roomsYMLPath, roomsYMLContent);
    }
}
