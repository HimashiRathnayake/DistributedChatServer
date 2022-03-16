package Models.Server;

public class ServerData {

    private String serverID;
    private String serverAddress;
    private int clientPort;
    private int coordinationPort;

    public ServerData(String serverID, String serverAddress, int clientPort, int coordinationPort) {
        this.serverID = serverID;
        this.serverAddress = serverAddress;
        this.clientPort = clientPort;
        this.coordinationPort = coordinationPort;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getClientPort() {
        return clientPort;
    }

    public void setClientPort(int clientPort) {
        this.clientPort = clientPort;
    }

    public int getCoordinationPort() {
        return coordinationPort;
    }

    public void setCoordinationPort(int coordinationPort) {
        this.coordinationPort = coordinationPort;
    }

}
