import org.kohsuke.args4j.Option;

public class CmdLineValues {
    @Option(required = true, name = "-sid", aliases="--serverID", usage = "s=Server ID")
    private String serverID = "1";

    @Option(required = true, name = "-conf", aliases="--serversConf", usage = "l=Server Config File Path")
    private String serversConf = "./servers_config.txt";

    public String getServerId() {
        return serverID;
    }

    public void setServerId(String serverID) {
        this.serverID = serverID;
    }

    public String getServerConfig() {
        return serversConf;
    }

    public void setServerConfig(String serversConf) {
        this.serversConf = serversConf;
    }
}