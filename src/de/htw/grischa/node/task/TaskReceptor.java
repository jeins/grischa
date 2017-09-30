package de.htw.grischa.node.task;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import de.htw.grischa.client.GClientConnection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
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

public class TaskReceptor implements Runnable {
    private final static Logger LOG = Logger.getLogger(TaskReceptor.class);
    private final String mJid;
    private final String mUser;
    private final TaskDispatcher mDispatcher;
    private final CountDownLatch mDoneSignal;
    private Object mTaskResult = null;
    private Boolean mIsDone = null;
    private Jedis RedisSubscriber;
    private Jedis RedisPublisher;
    private JedisPubSub jedisPubSub;
    private String mHostName = null;

    /**
     * Constructor
     * @param dispatcher
     * @param doneSignal
     */
    public TaskReceptor(TaskDispatcher dispatcher, CountDownLatch doneSignal) {
        mDispatcher = dispatcher;
        mJid = dispatcher.getJid();
        mUser = dispatcher.getUser();
        mDoneSignal = doneSignal;
        mIsDone = false;
        jedisPubSub = new JedisPubSub() {
            @Override
            public void onMessage(String s, String s2) {
                LOG.debug("task receptor subscriber i get message");

                String[] hostNameWithResult = s2.split(";");
                mHostName = hostNameWithResult[0];
                mTaskResult = hostNameWithResult[1];

                setDone();
            }

            @Override
            public void onPMessage(String s, String s2, String s3) {
            }

            @Override
            public void onSubscribe(String s, int i) {
                LOG.debug("task receptor subscriber i subscribed");
                askForResults();
            }

            @Override
            public void onUnsubscribe(String s, int i) {
                LOG.debug("task receptor subscriber i unsubscribed");
            }

            @Override
            public void onPUnsubscribe(String s, int i) {
            }

            @Override
            public void onPSubscribe(String s, int i) {
            }
        };
    }


    @Override
    public void run() {
        try {
            //Jedis jedis = GClientConnection.getInstance().getRedis();
            LOG.debug("task receptor subscriber i get connection");
            final GClientConnection gcon = GClientConnection.getInstance();
            LOG.debug("task receptor subscriber i get redis");
            RedisSubscriber = gcon.getRedis();
            //waitUntilDone();
            RedisSubscriber.subscribe(jedisPubSub, "result:" + mUser);
        } finally {
            LOG.debug("task receptor subscriber i release redis");
            RedisSubscriber.close();
            GClientConnection.getInstance().releaseRedis(RedisSubscriber);
        }
    }

    private void askForResults() {
        try {
            LOG.debug("task receptor publisher i get connection");
            final GClientConnection gcon = GClientConnection.getInstance();
            LOG.debug("task receptor publisher i get redis");
            RedisPublisher = gcon.getRedis();
            RedisPublisher.publish("move:" + mUser, "results");

        } finally {
            LOG.debug("task receptor publisher i release redis");
            RedisPublisher.close();
            GClientConnection.getInstance().releaseRedis(RedisPublisher);
        }
    }

    /**
     * Sets the thread status to done. This unblocks the thread.
     */
    public synchronized void setDone() {

        mIsDone = true;
        jedisPubSub.unsubscribe();
        mDoneSignal.countDown();
        notifyAll();
    }

    /**
     * blocks a thread until the status is done.
     */
    public synchronized void waitUntilDone() {
        while (mIsDone == false) {
            try {
                wait();
            } catch (InterruptedException e) {
                setDone();
                return;
            }
        }
    }

    /**
     *
     * @return
     */
    public Object getTaskResult() {
        return mTaskResult;
    }

    /**
     *
     * @return
     */
    public Task getTask() {
        return mDispatcher.getTask();
    }

    public String getHostName(){
        return mHostName;
    }
}
