package codegurus.projects.mek;

public interface AgentHandler {
    public void startupAgent(String host, int port) throws Exception;
    public void shutdownAgent(String host, int port);

    public void startup();

    public void shutdown();
}
