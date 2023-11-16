package codegurus.projects.mek;

import codegurus.projects.netty.ClientAgent;
import codegurus.projects.netty.ClientChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.NettyRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * Client를 통하여 받은 데이터를 처리하는 주요 클래스..
 */
public class DeviceAgentHandler implements AgentHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * channel별 데이터를 모으는 큐..
     */
    private ConcurrentHashMap<String, BlockingQueue<Packet>> CHANNEL_DATA_MAP = new ConcurrentHashMap<>();
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
        this(nThreads, period, dataSize);
        this.clientDupOk = clientDupOk;
    }
    public DeviceAgentHandler(int nThreads, long period, int dataSize) {
        this.nThreads = nThreads;
        this.period = period;
        this.dataSize = dataSize;
    }


    public void startup() {
        logger.info("startup ... ");
        if (period == 0) {
            period = 1000;
        }
        if (dataSize == 0) {
            dataSize = 120;
        }
        // NettyClient에서 사용할 EventLoopGroup
        WORKGROUP = new NioEventLoopGroup(nThreads);
        // 주기적으로 데이터를 잘라내는 용도로 사용할 쓰레드풀.
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        //잘라낸 여러개의 packet을 처리하는 쓰레드풀. 모아놓은 데이터를 특정 형태로 변환하여 rabbitmq로 보낼 때 사용함.
        threadPoolExecutor = new ThreadPoolExecutor(NettyRuntime.availableProcessors(), NettyRuntime.availableProcessors() * 2, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        // 주기적으로 유입된 데이터를 적당한 크기로 잘라내고 이후 처리함. (이후 처리는 쓰레드풀로 처리)
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {

                    for (String channelId : Set.copyOf(CHANNEL_DATA_MAP.keySet())) {    // ConcurrentModification Exception 이 발생하지 않게 복사하여 처리.
                        BlockingQueue<Packet> dataQueue = CHANNEL_DATA_MAP.get(channelId);
                        if (!dataQueue.isEmpty()) {
                            List<Packet> packets = new ArrayList<>();
                            dataQueue.drainTo(packets, Math.min(dataQueue.size(), dataSize));
                            threadPoolExecutor.submit(new PacketsHandler(packets));
                            System.out.println(channelId+" "+dataQueue.size());
                        }
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    //FIXME 운영중에는 e를 던지지않음.
                    throw e;
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

    private String key(String host, int port) {
        return host + "_" + port;
    }
    @Override
    public void startupAgent(String host, int port) throws Exception {
        String key = key(host, port);
        logger.info("startupAgent {} {}", host, port);
        if (clientDupOk) {
            // 테스트 모드에서 하나의 서버에 여러개의 클라이언트 기동 처리.
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
            // 일반 모드에서 클라이언트 기동.
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
        String key = key(host, port);
        if (clientDupOk) {  // test 용도인 경우에 종료처리.
            logger.info("shutdownAgent {} {}", host, port);
            if (PROCESS_MAP2.containsKey(key)) {
                List<ClientAgent> clientAgents = PROCESS_MAP2.get(key);
                for (ClientAgent clientAgent : clientAgents) {
                    clientAgent.shutdown();
                }
            }
        }else {
            // 일반 모드에서 클라이언트 종료처리.
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

//    public static void main(String[] args) throws Exception {
//        DeviceAgentHandler agentHandler = new DeviceAgentHandler(0, 1000, 120,true);
//        agentHandler.startup();
//
//        agentHandler.startupAgent("localhost", 8099);
//        agentHandler.startupAgent("localhost", 8099);
//        agentHandler.startupAgent("localhost",8099);
//
//        Thread.sleep(100000000);
//        agentHandler.shutdownAgent("localhost", 8099);
//        Thread.sleep(10000);
//        agentHandler.shutdown();
//        Thread.sleep(10000);
//
//    }
}
