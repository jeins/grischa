/**
 * Class that takes care of communication between redis and GClient
 * TODO: catch block in constructor to alternative redis server/ instance
 */

package de.htw.grischa.client;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class GClientConnection {

    public final static String SERVER = "grischa.f4.htw-berlin.de";

    private final static Logger LOG = Logger.getLogger(GClientConnection.class);

    private static GClientConnection mInstance;

    private JedisPool pool;

    private GClientConnection() {
        String redisHost;
        int redisPort;
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(100);
        poolConfig.setMaxIdle(100);
        try {
            redisHost = "localhost";
            redisPort = 6379;
            pool = new JedisPool(poolConfig,redisHost, redisPort);
        } catch (Exception e) {//setting
            System.out.println("Local Redis server not found!");
            pool = new JedisPool(poolConfig,"46.38.241.128", 6379);
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
