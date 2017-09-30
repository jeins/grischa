package de.htw.grischa.client;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Class that takes care of communication between redis and GClient
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
 *
 * TODO: catch block in constructor to alternative redis server/ instance
 */

public class GClientConnection {

    public final static String SERVER = "grischa.f4.htw-berlin.de";

    private final static Logger LOG = Logger.getLogger(GClientConnection.class);

    private static GClientConnection mInstance;

    private JedisPool pool;

    private GClientConnection() {
        Properties prop = new Properties();
        InputStream input = null;
        String redisHost;
        int redisPort;
        try{
            input = new FileInputStream("grischa.conf");
            prop.load(input);
        } catch (IOException ex){
            ex.printStackTrace();
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (IOException e) {
                e.printStackTrace();
                }
        }
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(100);
        poolConfig.setMaxIdle(100);
        try {
            redisHost = prop.getProperty("redisHost");
            redisPort = Integer.valueOf(prop.getProperty("redisPort"));
            pool = new JedisPool(poolConfig,redisHost, redisPort);
        } catch (Exception e) {//setting
            LOG.error("Local Redis server not found!");
            System.out.println("Local Redis server not found!");
            pool = new JedisPool(poolConfig,"localhost", 6379);
        }
    }

    public static GClientConnection getInstance() {
        LOG.debug("get instance");
        if (mInstance == null) {
            mInstance = new GClientConnection();
            mInstance.login();
        }

        return mInstance;
    }

    private void login() {
        LOG.debug("logging in to: ");
    }

    public Jedis getRedis() {
        return pool.getResource();
    }

    public void releaseRedis(Jedis j) {
        pool.returnResource(j);
    }
}
