package de.htw.grischa.chess.database.server;

import de.htw.grischa.chess.database.client.CommitThread;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Central class listening to client connections and waiting for write requests
 * <p>
 * The WriteManager is transferring the incoming write request to a
 * {@link de.htw.grischa.chess.database.client.DatabaseEntry} which is stored in a
 * {@link de.htw.grischa.chess.database.server.RAMQueue}
 * </p>
 * <h3>Version History</h3>
 * <ul>
 * <li> 1.0 - 11/13 - Karsten Kochan - Initial Version</li>
 * <li> 1.1 - 06/14 - Karsten Kochan - Clean up, typo, documentation</li>
 * </ul>
 *
 * @author Karsten Kochan
 * @version 1.1
 */
public class WriteManager {
    /**
     * Logger
     */
    private final static Logger log = Logger.getLogger(WriteManager.class);

    /**
     * Integer Unix port to listen on
     */
    private final int port;

    /**
     * ServerSocket used for connections
     */
    private ServerSocket writeSocket;

    /**
     * de.htw.grischa.database.RAMFile used to write data
     */
    private RAMFile file;

    /**
     * de.htw.grischa.RAMQueue to save requests in
     */
    private RAMQueue queue;

    /**
     * Constructor for writeManager
     *
     * @param port     Integer port to listen on, must be greater 1024
     * @param fileSize Integer file size for file
     * @param fileName Filename of the database
     * @throws java.lang.IllegalAccessError if the fileName is not a valid database file
     * @throws java.io.IOException          if the database could not been created or the port is already in use
     * @throws IllegalArgumentException     if the port is out of dynamic port range
     */
    public WriteManager(int port, int fileSize, String fileName) throws IOException, IllegalAccessError,
            IllegalArgumentException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalAccessError("Database file needs to be specified");
        }
        if (port < CommitThread.PORT_MIN || port > CommitThread.PORT_MAX) {
            throw new IllegalArgumentException("Port out of range");
        }
        this.port = port;
        this.file = new RAMFile(fileName, fileSize);
        this.writeSocket = new ServerSocket(this.port);
    }

    /**
     * Start working on queue and listening to clients
     */
    public void start() {
        this.queue = new RAMQueue();
        Thread w = new RAMQueueWorker(this.queue, this.file);
        w.start();
        log.info("Listening on port " + this.port);
        this.listen();
    }

    /**
     * Waits for connections and forks for every client.
     */
    private void listen() {
        //noinspection InfiniteLoopStatement
        while (true) {
            Socket server = null;
            try {
                server = writeSocket.accept();
            } catch (IOException e) {
                log.warn("Socket is not able to accept connections");
            }
            Thread t = new ClientListener(server, this.queue);
            t.start();
        }
    }
}
