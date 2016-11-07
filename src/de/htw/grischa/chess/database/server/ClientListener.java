package de.htw.grischa.chess.database.server;

import de.htw.grischa.chess.database.client.DatabaseEntry;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * ClientListener is the class to handle the input coming from client
 * <p>
 * ClientListener will transform incoming string messages into {@link de.htw.grischa.chess.database.client.DatabaseEntry}
 * </p>
 * <h3>Version History</h3>
 * <ul>
 * <li> 1.0 - 04/14 - Karsten Kochan - Initial Version</li>
 * <li> 1.1 - 06/14 - Karsten Kochan - Replaced RAMQueueElement by DatabaseEntry, removed MAGIC_LENGTH</li>
 * </ul>
 *
 * @author Karsten Kochan
 * @version 1.1
 */
class ClientListener extends Thread {

    /**
     * Logger
     */
    private final static Logger log = Logger.getLogger(ClientListener.class);

    /**
     * Socket used for connection
     */
    private final Socket server;

    /**
     * de.htw.grischa.RAMQueue to save into
     */
    private final RAMQueue queue;

    /**
     * Constructor, gets Socket from parent
     *
     * @param serverParent Socket, created by parent
     * @param queue        MessageQueue to fill
     */
    public ClientListener(Socket serverParent, RAMQueue queue) {
        this.server = serverParent;
        this.queue = queue;
    }

    /**
     * Start the thread, containing infinity loop
     */
    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                handleInput(this.server);
            } catch (IOException e) {
                e.printStackTrace();
                log.warn("IOException caught while from reading socket");
            }
        }
    }

    /**
     * Handles incoming client message
     * <p>
     * New Syntax: message<br>
     * message: 0ee88f1786fb5a990863a1f6a5ab60c3#0001#0004;
     * </p>
     *
     * @param serverParent Socket for communication
     * @throws java.io.IOException                Reading from socket failed
     * @throws java.lang.IllegalArgumentException Syntax of message was illegal
     */
    private void handleInput(Socket serverParent) throws IOException, IllegalArgumentException {
        InputStream in = serverParent.getInputStream();
        String inputStreamString;
        try {
            inputStreamString = new Scanner(in, "UTF-8").useDelimiter(";").next();
        } catch (Exception e) {
            return;
        }
        log.trace(inputStreamString);
        if (inputStreamString.equals("quit")) {
            System.exit(0);
        }
        DatabaseEntry entry;
        try {
            entry = new DatabaseEntry(inputStreamString);
        } catch (IllegalArgumentException e) {
            log.warn("Incoming message contains invalid object");
            return;
        }
        //This check is truly needed because constructor of DatabaseEntry returning null if throwing exception
        //noinspection ConstantConditions
        if (entry != null) {
            log.trace("Adding " + entry.toString() + " to worker queue");
            this.queue.addElement(entry);
        } else {
            log.warn("Ignored illegal input string, created null object");
        }
    }
}
