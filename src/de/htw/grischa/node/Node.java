package de.htw.grischa.node;

import de.htw.grischa.client.GClientConnection;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public abstract class Node implements Runnable {
    private final static Logger LOG = Logger.getLogger(Node.class);
    protected Boolean mIsRunning = true;
    private String mServer = "grischa.f4.htw-berlin.de";
    private String mUser = "grid-xmpp-user-001";
    private String mPassword = "node-001";
    private Jedis RedisSubscriber;
    private Jedis RedisPublisher;


    Node() {
//        mXmppConnection = new XMPPConnection(mServer);
//        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }

    protected void login() {
//        try {
        // TODO - laurence: check if the jid is already in user

        RedisSubscriber = GClientConnection.getInstance().getRedis();
        register();
        RedisSubscriber.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String s, String body) {
                LOG.debug("i got message");

                if (body.compareTo("stop") == 0) {
                    stopNode();
                } else if (body.compareTo("results") == 0) {
                    stopTask();
                    sendResulstBack();
                } else {
                    runTask(body);
                }

            }

            @Override
            public void onPMessage(String s, String s2, String s3) {

            }

            @Override
            public void onSubscribe(String s, int i) {

                LOG.debug("i got subscribed");

            }

            @Override
            public void onUnsubscribe(String s, int i) {


                LOG.debug("i got unsubscribed");

            }

            @Override
            public void onPUnsubscribe(String s, int i) {

            }

            @Override
            public void onPSubscribe(String s, int i) {

            }
        }, "move:" + mUser);
    }

    public void stopNode() {


        LOG.debug("i stop node");
        this.mIsRunning = false;
        System.exit(98);
    }

    public void sendResulstBack() {
        LOG.debug("i send results back");
        String result = getResult().toString();
        RedisPublisher = GClientConnection.getInstance().getRedis();
        RedisPublisher.publish("result:" + mUser, result);
        GClientConnection.getInstance().realeaseRedis(RedisPublisher);
    }

    @SuppressWarnings("static-access")
    public void parseArgs(String[] args) {

        Options options = new Options();
        CommandLineParser parser = new BasicParser();

        // Setup option for the help menu
        options.addOption("h", "help", false, "Print the help message");

        // Setup option for the xmpp server
        options.addOption(OptionBuilder.withLongOpt("server")
                .withDescription("Set the xmpp server address").hasArg().withArgName("SERVER")
                .create());

        // Setup option for xmpp username
        options.addOption(OptionBuilder.withLongOpt("user").withDescription("Set the xmpp user")
                .hasArg().withArgName("USER").create());

        // Setup option for xmpp password
        options.addOption(OptionBuilder.withLongOpt("password")
                .withDescription("Set the xmpp authentication password").hasArg()
                .withArgName("PASSWORD").create());

        try {
            CommandLine line = parser.parse(options, args);

            // Print out help messege
            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("ant", options);
                System.exit(0);
            }

            // Read xmpp server
            if (line.hasOption("server")) {
                mServer = line.getOptionValue("server");
            }

            // Read jid
            if (line.hasOption("user")) {
                mUser = line.getOptionValue("user");
            }

            // Read auth passowrd
            if (line.hasOption("password")) {
                mPassword = line.getOptionValue("password");
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Register the node to the registry roster.
     */
    public void register() {
        LOG.debug("i register");
        RedisPublisher = GClientConnection.getInstance().getRedis();
        RedisPublisher.lpush("gregistered", mUser);
        GClientConnection.getInstance().realeaseRedis(RedisPublisher);
//        String registryUser = GWorkerNodeRegistry.USERNAME;
//        String jid = new StringBuilder(registryUser).append('@').append(mServer).toString();
//
//        Chat chat = mChatManager.createChat(jid, "Reistration", mMessageListener);
//
//        try {
//            chat.sendMessage(new StringBuilder("registration:").append(mUser).toString());
//        } catch (XMPPException e) {
//            LOG.error(new StringBuilder("Can not register: ").append(e.getMessage()).toString());
//        }
//
//        // Remove listener and remove reference to chat. Until now both is not needed here any more
//        chat.removeMessageListener(mMessageListener);
//        chat = null;
    }

    protected abstract void runTask(String taskString);

    protected abstract void stopTask();

    protected abstract Object getResult();

    private class ShutdownHook extends Thread {
        public void run() {
            stopNode();
        }
    }
}
