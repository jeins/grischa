package de.htw.grischa.node;

import de.htw.grischa.client.GClientConnection;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Abstract node class, implementing basic features of a node.
 * Contains everything that is needed for communication with Redis server,
 * push and pull messages, starting tasks for calculation
 * <h3>Abstract Methods</h3>
 * <ul>
 *  <li>runTask(String taskString)</li>
 *  <li>stopTask()</li>
 *  <li>getResult();</li>
 * </ul>
 *
 * <h3>Version History</h3>
 * <ul>
 * <li> 05/10 - Daniel Heim - Initial Version </li>
 * <li> xx/11 - Laurence Bortfeld - Revise and optimize code, adding xmpp protocol</li>
 * <li> 12/14 - Philip Stewart - Adding communication via Redis</li>
 * <li> 02/17 - Benjamin Troester - adding documentation and revise code </li>
 * </ul>
 *
 * @version 02/17
 */

public abstract class Node implements Runnable {
    // logging for nodes
    private final static Logger LOG = Logger.getLogger(Node.class);
    // bool status master node runs
    protected Boolean mIsRunning = true;//master node is running
    // access credentials for com server - Names for Server, Nodes etc
    private String mServer = "grischa.f4.htw-berlin.de";
    private String mUser = "grid-xmpp-user-001";
    private String mPassword = "node-001";
    private Jedis RedisSubscriber;
    private Jedis RedisPublisher;

    // default constructor
    Node() {}

    /**
     * Bootstraps the Redis registration process via Jedis publish-subscribe pattern
     */
    protected void login() {
        RedisSubscriber = GClientConnection.getInstance().getRedis();
        register();
        RedisSubscriber.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String s, String body) {
                LOG.debug("i got message");
                //checking message body
                if (body.compareTo("stop") == 0)
                    stopNode();
                else if (body.compareTo("results") == 0) {
                    stopTask();
                    sendResultsBack();
                }
                else
                    runTask(body);
            }

            @Override
            public void onPMessage(String s, String s2, String s3) { }

            @Override
            public void onSubscribe(String s, int i) {
                LOG.debug("i got subscribed");
            }

            @Override
            public void onUnsubscribe(String s, int i) {
                LOG.debug("i got un-subscribed");
            }

            @Override
            public void onPUnsubscribe(String s, int i) { }

            @Override
            public void onPSubscribe(String s, int i) { }
        }, "move:" + mUser);
    }

    /**
     * Method for stopping node by system exit
     * @see System
     */
    public void stopNode() {
        LOG.debug("i stop node");
        this.mIsRunning = false;
        System.exit(98);
    }

    /**
     * Method for messaging results to redis via result channel
     */
    public synchronized void sendResultsBack() {
        String result = getResult().toString();
        LOG.debug("i send results back");
        RedisPublisher = GClientConnection.getInstance().getRedis();
        RedisPublisher.publish("result:" + mUser, getHostName()+';'+result);
        GClientConnection.getInstance().releaseRedis(RedisPublisher);
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
     * key value for redis-cli monintor is gregistered
     */
    public void register() {
        LOG.debug("i register");
        RedisPublisher = GClientConnection.getInstance().getRedis();
        RedisPublisher.lpush("gregistered", mUser);
        GClientConnection.getInstance().releaseRedis(RedisPublisher);
    }

    /*
    * -------------------------------
    * just the abstract methods here:
    * -------------------------------
    */
    protected abstract void runTask(String taskString);

    protected abstract void stopTask();

    protected abstract Object getResult();

    protected abstract String getHostName();

    private class ShutdownHook extends Thread {
        public void run() {
            stopNode();
        }
    }
}
