package de.htw.grischa.node;

import de.htw.grischa.node.task.GTask;
import org.apache.log4j.Logger;

public class GNode extends Node {
    private final static Logger LOG = Logger.getLogger(Node.class);
    private GTask mTask = null;

    public GNode() {
        super();
    }

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
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                LOG.error("Shutdown node: " + e.getMessage());
            }
        }
    }

    public void runTask(String taskString) {
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
}
