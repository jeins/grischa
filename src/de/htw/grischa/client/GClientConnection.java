package de.htw.grischa.client;

import de.htw.grischa.chess.database.GDBRunner;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.FileInputStream;
import java.util.Properties;

public class GClientConnection {

    public final static String SERVER = "grischa.f4.htw-berlin.de";

    private final static Logger LOG = Logger.getLogger(GClientConnection.class);

    private static GClientConnection mInstance;

    private JedisPool pool;

    private GClientConnection() {
        Properties database = new Properties();
        String redisHost;
        int redisPort;
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(100);
        poolConfig.setMaxIdle(100);
        try {
            FileInputStream propertiesFile = new FileInputStream(GDBRunner.properties);
            database.load(propertiesFile);
            propertiesFile.close();
            redisHost = database.getProperty("grischa.redis.host");
            redisPort = Integer.valueOf(database.getProperty("grischa.redis.port"));
            pool = new JedisPool(poolConfig,redisHost, redisPort);
        } catch (Exception e) {
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
        LOG.debug("logging in");
    }

    public Jedis getRedis() {
        return pool.getResource();
    }

    public void realeaseRedis(Jedis j) {
        pool.returnResource(j);
    }
}
