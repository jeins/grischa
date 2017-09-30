package de.htw.grischa.registry;

import java.util.*;
import de.htw.grischa.client.GClientConnection;
import org.apache.log4j.Logger;
import de.htw.grischa.node.GNode;
import redis.clients.jedis.Jedis;

/**
 * The GWorkerNodeRegistry is the class that handles the communication between the
 * worker nodes in the grid and the master node that creates the Object GWorkerNodeRegistry.
 * If this is called from the master ndoe, it will either return a new GWorkerNodeRegistry or
 * return a already existing registry. In case that the registry fails it will return null!
 * This class holds an ArrayList in which the Redis/ Jedis server puts all available worker nodes.
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

public class GWorkerNodeRegistry {
    private static GWorkerNodeRegistry mInstance = null;
    private final static Logger LOG = Logger.getLogger(GNode.class);

    /**
     * Getter method to deliver a GWorkerNodeRegistry object.
     *
     * @return mInstance the current Object that holds a GWorkerNodeRegistry or null reference
     */
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
        return onlineNodes;
    }

    public void stopRegistry() {

    }
}
