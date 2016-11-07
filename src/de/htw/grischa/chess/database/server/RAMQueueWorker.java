package de.htw.grischa.chess.database.server;

import de.htw.grischa.chess.database.client.DatabaseEntry;
import de.htw.grischa.chess.database.client.FileSearch;

import java.util.concurrent.TimeUnit;

/**
 * Central class working on the {@link de.htw.grischa.chess.database.server.RAMQueue}
 * <p>
 * This runnable thread is waiting for any {@link de.htw.grischa.chess.database.client.DatabaseEntry} in the queue and
 * will transmit this as message to the database server
 * </p>
 * <p>
 * The RAMQueueWorker is not thread-safe. You can not create more than one worker for same file. This will cause
 * problems while inserting new entries. {@link de.htw.grischa.chess.database.server.RAMQueueWorker#appendCount} is local
 * and can currently not be shared with other threads.
 * </p>
 *
 * @author Karsten Kochan
 * @version 1.1
 * @see java.lang.Thread
 */
class RAMQueueWorker extends Thread {

    /**
     * de.htw.grischa.RAMQueue to save commands in
     */
    private final RAMQueue queue;

    /**
     * de.htw.grischa.RAMFile to store on disk
     */
    private final RAMFile file;

    private final FileSearch fileSearch;

    /**
     * Counter for append commands
     */
    private int appendCount = 0;

    /**
     * Constructor for worker
     *
     * @param queue de.htw.grischa.RAMQueue to save commands in
     * @param file  de.htw.grischa.RAMFile to store on disk
     */
    public RAMQueueWorker(RAMQueue queue, RAMFile file) {
        this.queue = queue;
        this.file = file;
        this.fileSearch = new FileSearch(this.file.getMemoryMappedFile(), false);
    }

    /**
     * Start this thread
     */
    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            if (!this.queue.isEmpty()) {
                DatabaseEntry temp = this.queue.getElement();
                DatabaseEntry existing = this.fileSearch.search(temp.getHash());
                if (existing == null) {
                    //New Entry
                    this.file.write(
                        this.appendCount * (DatabaseEntry.TARGET_LENGTH + 1),
                        DatabaseEntry.TARGET_LENGTH + 1,
                        temp.toString().concat("\n")
                    );
                    this.appendCount++;
                    //Implement circular buffer
                    if (this.appendCount == (this.file.getSize() / (DatabaseEntry.TARGET_LENGTH + 1))) {
                        this.appendCount = 0;
                    }
                } else {
                    //Existing Entry, only process if better depth and not identical
                    if (temp.getDepth() > existing.getDepth() && temp.getValue() != existing.getValue()) {
                        this.file.write(temp.getIndex(), DatabaseEntry.TARGET_LENGTH, temp.toString());
                    }
                }
            }else{
                try {
                    TimeUnit.MICROSECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
