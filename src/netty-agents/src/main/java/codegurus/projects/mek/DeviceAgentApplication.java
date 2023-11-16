package codegurus.projects.mek;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

public class DeviceAgentApplication {
    public static void main(String[] args) throws Exception {
        CommandLine commandLine = parseCommandLine(args);
        if (commandLine == null) {
            return;
        }
        String host = commandLine.getOptionValue("host");
        String _port = commandLine.getOptionValue("port");
        String _dataSize = commandLine.getOptionValue("datasize");
        String _period = commandLine.getOptionValue("period");
        String mode = commandLine.getOptionValue("mode");

        int period = parseInt(_period);
        int port = parseInt(_port);
        int datasize = parseInt(_dataSize);
        if (datasize == 0) {
            datasize = 120;
        }
        if (period == 0) {
            period = 1000;
        }
        if (StringUtils.isEmpty(host)) {
            host = "localhost";
        }
        if (port == 0) {
            port = 8080;
        }
        if (StringUtils.isEmpty(mode)) {
            mode = "notest";
        }


        System.out.println("host="+host);
        System.out.println("port="+port);
        System.out.println("mode="+mode);
        System.out.println("period="+period);
        System.out.println("datasize="+datasize);


        if ("test".equalsIgnoreCase(mode)) {
            DeviceAgentHandler agentHandler = new DeviceAgentHandler(0, period, datasize,true);    // 여러개의 클라이언트 테스트 용도로 사용.
            agentHandler.startup();

            agentHandler.startupAgent(host, port);
            agentHandler.startupAgent(host, port);
            agentHandler.startupAgent(host, port);

            Thread.sleep(100000000);
            agentHandler.shutdownAgent(host, port);
            Thread.sleep(10000);
            agentHandler.shutdown();
            Thread.sleep(10000);
        } else {
            DeviceAgentHandler agentHandler = new DeviceAgentHandler(0, period, datasize);    // 여러개의 클라이언트 테스트 용도로 사용.
            agentHandler.startup();

            agentHandler.startupAgent(host, port);

            Thread.sleep(100000000);
            agentHandler.shutdownAgent(host, port);
            Thread.sleep(1000);
            agentHandler.shutdown();
            Thread.sleep(1000);
        }


    }

    private static int parseInt(String s) {
        if (StringUtils.isEmpty(s)) {
            return 0;
        } else {
            return Integer.valueOf(s);
        }
    }
    private static CommandLine parseCommandLine(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption(Option.builder("h").longOpt("host").desc("device url").required().hasArg().build());
        options.addOption(Option.builder("P").longOpt("port").desc("port").required().hasArg().build());
        options.addOption(Option.builder("d").longOpt("datasize").desc("data Size").hasArg().build());
        options.addOption(Option.builder("p").longOpt("period").desc("period milesecond").hasArg().build());
        options.addOption(Option.builder("m").longOpt("mode").desc("mode type : test , nt(notest)").hasArg().build());

        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("deviceAgent", options);
            return null;
        }

        CommandLine line = parser.parse(options, args);



        return line;
    }
}
