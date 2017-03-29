package de.htw.grischa.node.task;

/**
 * Interface description for task that a node do
 */

public interface Task extends Runnable {

    /**
     * Method that takes care of stopping a task
     */
    void stop();

    /**
     * Getter method that get the calculated results from the nodes
     * and returns them.
     * @return  result double or int value - that represents the quality
     */
    Object getResult();

    /**
     * Setter Method to put a given result to GTask.
     * @param result
     */
    void setResult(Object result);

    /**
     * Gets string representation of the chessboard
     * @return string with all necessary information, that hold a chessboard
     */
    String toString();
}
