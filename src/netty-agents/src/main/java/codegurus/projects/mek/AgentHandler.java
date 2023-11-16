package codegurus.projects.mek;

public interface AgentHandler {
    /**
     * client 기동
     * @param host
     * @param port
     * @throws Exception
     */
    public void startupAgent(String host, int port) throws Exception;

    /**
     * client 종료
     * @param host
     * @param port
     */
    public void shutdownAgent(String host, int port);

    /**
     * 데이터를 처리하는 Agent 기동
     */
    public void startup();

    /**
     * 데이터를 처리하는 Agent 종료.
     */
    public void shutdown();
}
