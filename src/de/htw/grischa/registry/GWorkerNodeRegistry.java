package de.htw.grischa.registry;

import java.util.*;

import de.htw.grischa.client.GClientConnection;
import org.apache.log4j.Logger;
import de.htw.grischa.node.GNode;
import redis.clients.jedis.Jedis;

/**
 * Returns an started instance of the registry
 * @return Running registry or null if can not be started
 */

public class GWorkerNodeRegistry {
    private static GWorkerNodeRegistry mInstance = null;
    private final static Logger LOG = Logger.getLogger(GNode.class);

    public static GWorkerNodeRegistry getInstance() {
        if (mInstance == null) {
            mInstance = new GWorkerNodeRegistry();
            try {
                mInstance.startRegistry();
            } catch (Exception e) {
               return null;
            }
        }
        
        return mInstance;
    }

    /**
     * Start the registry service
     *
     */
    private void startRegistry() {
    }

    /**
     * Get the jid of all online worker nodes
     * 
     * @return The worker nodes jid
     */
    public ArrayList<String> getOnlineWorkerNodes() {
        Jedis j = GClientConnection.getInstance().getRedis();
        List<String> res = j.lrange("gregistered", 0, -1);
        j.close();

        ArrayList<String> onlineNodes = new ArrayList<String>();

        for (String entry : res) {
            LOG.debug(res);

            onlineNodes.add(entry);
        }
//        Collections.sort(onlineNodes, new CustomComparator());
        return onlineNodes;
    }

    public void stopRegistry() {

    }


    // TODO Remove this in final version
    public class CustomComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }
}
