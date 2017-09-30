package de.htw.grischa.node;

import de.htw.grischa.node.task.GTask;
import org.apache.log4j.Logger;

/**
 * GNode extends the abstract Node class and
 * specifies how to run certain Nodes.
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

public class GNode extends Node {
    private final static Logger LOG = Logger.getLogger(Node.class);
    private GTask mTask = null;

    public GNode() {
        super();
    }

    /**
     * Entry point for grid node
     * @param args
     */
    public static void main(String[] args) {
        Node node = new GNode();
        node.parseArgs(args);
        new Thread(node).start();
    }

    @Override
    public void run() {
        login();
        while (mIsRunning) {
            try {
                Thread.sleep(1000);//sleeps for 1.0 sec
            } catch (InterruptedException e) {
                LOG.error("Shutdown node: " + e.getMessage());
            }
        }
    }

    public synchronized void runTask(String taskString) {
        mTask = new GTask(taskString);

        Thread t = new Thread(mTask);
        t.start();
    }

    public void stopTask() {
        mTask.stop();
    }

    protected Object getResult() {
        return this.mTask.getResult();
    }

    protected String getHostName() {
        return this.mTask.getHostName();
    }
}
