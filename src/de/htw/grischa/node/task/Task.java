package de.htw.grischa.node.task;

/**
 * Interface description for Task of a Node
 */

public interface Task extends Runnable {
    public void stop();
    public Object getResult();
    public void setResult(Object result);
    public String toString();
}
