package codegurus.projects.mek;

import codegurus.projects.netty.ClientAgent;
import codegurus.projects.netty.ClientChannelHandler;
import codegurus.projects.netty.PacketsHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.NettyRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class DeviceAgentHandler implements AgentHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * channel별 데이터를 모으는 큐..
     */
    private Map<String, BlockingQueue<Packet>> CHANNEL_DATA_MAP = new ConcurrentHashMap<>();
    private Map<String, ClientAgent> PROCESS_MAP = new HashMap<>();
    private Map<String, List<ClientAgent>> PROCESS_MAP2= new HashMap<>();
    private EventLoopGroup WORKGROUP;
    private long period;  // 밀리초단위. 데이터처리 주기.
    private int dataSize = 120;   // 데이터 분리 갯수. 기본 120개

    private int nThreads = 0;   // eventLoopGroup 의 쓰레드 갯수. 0인경우 core갯수로 정해짐.

    private boolean clientDupOk = false;    // 하나의 기기에 둘 이상의 클라이언트가 접속가능한지..

    private ScheduledExecutorService scheduledExecutorService;

    private ThreadPoolExecutor threadPoolExecutor;

    public DeviceAgentHandler(int nThreads, long period, int dataSize,boolean clientDupOk) {
        this.nThreads = nThreads;
        this.period = period;
        this.dataSize = dataSize;
        this.clientDupOk = clientDupOk;
    }



    public void startup() {
        logger.info("startup ... ");
        if (period == 0) {
            period = 1000;
        }
        if (dataSize == 0) {
            dataSize = 120;
        }

        WORKGROUP = new NioEventLoopGroup(nThreads);

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        threadPoolExecutor = new ThreadPoolExecutor(NettyRuntime.availableProcessors(), NettyRuntime.availableProcessors() * 2, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    for (String channelId : CHANNEL_DATA_MAP.keySet()) {
                        BlockingQueue<Packet> dataQueue = CHANNEL_DATA_MAP.get(channelId);
                        if (!dataQueue.isEmpty()) {
                            List<Packet> packets = new ArrayList<>();
                            dataQueue.drainTo(packets, Math.min(dataQueue.size(), dataSize));
                            threadPoolExecutor.submit(new PacketsHandler(packets));
                        }
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

            }
        }, 0, period, TimeUnit.MILLISECONDS);

    }

    @Override
    public void shutdown() {
        logger.info("shutdown ... ");
        WORKGROUP.shutdownGracefully();
        scheduledExecutorService.shutdown();
        threadPoolExecutor.shutdown();
    }

    @Override
    public void startupAgent(String host, int port) throws Exception {
        String key = host + "_" + port;
        if (clientDupOk) {
            logger.info("startupAgent {} {}", host, port);
            ClientAgent clientAgent = new ClientAgent(new ClientChannelHandler(CHANNEL_DATA_MAP), WORKGROUP);
            clientAgent.startup(host, port);
            if (PROCESS_MAP2.containsKey(key)) {
                List<ClientAgent> clientAgents = PROCESS_MAP2.get(key);
                clientAgents.add(clientAgent);
            }else {
                List<ClientAgent> clientAgents = new ArrayList<>();
                clientAgents.add(clientAgent);
                PROCESS_MAP2.put(key,clientAgents);
            }
        }else {

            if (PROCESS_MAP.containsKey(key)) {
                logger.error("already startup {}", key);
                return;
            }
            logger.info("startupAgent {} {}", host, port);
            ClientAgent clientAgent = new ClientAgent(new ClientChannelHandler(CHANNEL_DATA_MAP), WORKGROUP);
            clientAgent.startup(host, port);
            PROCESS_MAP.put(key, clientAgent);
        }
        logger.info("done startupAgent {} {}", host, port);
    }

    @Override
    public void shutdownAgent(String host, int port) {
        String key = host + "_" + port;
        if (clientDupOk) {
            logger.info("shutdownAgent {} {}", host, port);
            if (PROCESS_MAP2.containsKey(key)) {
                List<ClientAgent> clientAgents = PROCESS_MAP2.get(key);
                for (ClientAgent clientAgent : clientAgents) {
                    clientAgent.shutdown();
                }
            }
        }else {
            if (PROCESS_MAP.containsKey(key) == false) {
                logger.error("no Client {}", key);
                return;
            }

            logger.info("shutdownAgent {} {}", host, port);
            ClientAgent clientAgent = PROCESS_MAP.get(key);
            clientAgent.shutdown();
        }

        logger.info("done shutdownAgent {} {}", host, port);
    }

    public static void main(String[] args) throws Exception {
        DeviceAgentHandler agentHandler = new DeviceAgentHandler(0, 1000, 120,false);
        agentHandler.startup();

        agentHandler.startupAgent("localhost", 8099);
        agentHandler.startupAgent("localhost", 8099);
//        agentHandler.startupAgent("localhost",8099);

        Thread.sleep(1000000);
        agentHandler.shutdownAgent("localhost", 8099);
        Thread.sleep(10000);
        agentHandler.shutdown();
        Thread.sleep(10000);

    }
}
