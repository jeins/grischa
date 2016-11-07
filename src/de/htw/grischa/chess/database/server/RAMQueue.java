package de.htw.grischa.chess.database.server;

import de.htw.grischa.chess.database.client.DatabaseEntry;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Wrapper class for {@link java.util.Queue}
 * <h3>Version History</h3>
 * <ul>
 * <li> 1.0 - 04/14 - Karsten Kochan - Initial Version</li>
 * <li> 1.1 - 06/14 - Karsten Kochan - Replaced RAMQueueElement by DatabaseEntry</li>
 * </ul>
 *
 * @author Karsten Kochan
 * @version 1.1
 */
class RAMQueue {
    /**
     *
     */
    private final Queue<DatabaseEntry> queue;

    /**
     * Constructor
     */
    public RAMQueue() {
        this.queue = new ConcurrentLinkedQueue<DatabaseEntry>();
    }

    /**
     * Return oldest element in queue
     *
     * @return de.htw.grischa.RAMQueueElement
     */
    public DatabaseEntry getElement() {
        return this.queue.poll();
    }

    /**
     * Add element to queue
     *
     * @param element de.htw.grischa.RAMQueueElement
     */
    synchronized public void addElement(DatabaseEntry element) {
        this.queue.add(element);
    }

    /**
     * Returns if queue is empty
     *
     * @return true if no element
     */
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    public int size() {
        return this.queue.size();
    }
}
