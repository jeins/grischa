package de.htw.grischa.node.task;

import org.apache.log4j.Logger;
import de.htw.grischa.client.GClientConnection;
import redis.clients.jedis.Jedis;

/**
 *
 *
 * @see java.lang.Runnable
 */

public class TaskDispatcher implements Runnable {
    private final static Logger LOG = Logger.getLogger(TaskDispatcher.class);
    private Task mTask = null;
    private String mJid = null;
    private String mUser = null;

    public TaskDispatcher(Task task, String user) {
        mTask = task;
        mJid = new StringBuilder(user).append("@").append(GClientConnection.SERVER).toString();
        mUser = user;

    }

    @Override
    public void run() {
        LOG.debug("task dispatcher i get redis");
        Jedis j = GClientConnection.getInstance().getRedis();

        try {
            LOG.debug("task dispatcher i publish");
            j.publish("move:"+ mUser, mTask.toString());
        } catch (Exception e) {
            LOG.error(e.getMessage());
        } finally {
            LOG.debug("task dispatcher i release redis");
            j.close();
            GClientConnection.getInstance().releaseRedis(j);
        }
    }
    
    public String getJid() {
        return mJid;
    }
    public String getUser() {
        return mUser;
    }
    
    public Task getTask() {
        return mTask;
    }
}
