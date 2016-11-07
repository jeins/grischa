package de.htw.grischa.chess.database.client;

import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Central Worker on client side transferring entries to server
 * <h3>Version History</h3>
 * <ul>
 * <li> 1.0 - 04/14 - Karsten Kochan - Initial Version</li>
 * </ul>
 *
 * @author Karsten Kochan
 * @version 1.0
 * @see java.lang.Thread
 */
public class CommitThread extends Thread {

    /**
     * Minimum allowed dynamic port
     */
    public static final int PORT_MIN = 1024;

    /**
     * Maximum allowed dynamic port
     */
    public static final int PORT_MAX = 65535;

    /**
     * Logger
     */
    private final static Logger log = Logger.getLogger(CommitThread.class);

    /**
     * Maximum runtime of thread in ms
     */
    private static final int RUNTIME = 60000;

    /**
     * RegEx for validation of IP addresses
     *
     * @see <a href="http://www.mkyong.com/regular-expressions/how-to-validate-ip-address-with-regular-expression/">
     * http://www.mkyong.com/regular-expressions/how-to-validate-ip-address-with-regular-expression/</a>
     */
    private static final String IP_ADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    /**
     * Socket connection
     */
    private Socket clientSocket;

    /**
     * Queue to read from
     */
    private BlockingQueue<DatabaseEntry> queue;

    /**
     * Start time in ms
     */
    private long startTime;

    private int port;

    private String host;

    /**
     * Constructor for worker thread
     *
     * @param queue BlockingQueue of DatabaseEntries to read Entries from
     * @param port  int, Host port to communicate with, only dynamic ports allowed
     * @param host  String, Host address (IPv4) to communicate with
     * @throws IllegalArgumentException if host or port are illegal or queue is null
     */
    public CommitThread(BlockingQueue<DatabaseEntry> queue, int port, String host)
        throws IllegalArgumentException {
        if (port < PORT_MIN) {
            throw new IllegalArgumentException("Port to small");
        }
        if (port > PORT_MAX) {
            throw new IllegalArgumentException("Port to large");
        }
        this.startTime = System.currentTimeMillis();
        if (queue != null) {
            this.queue = queue;
        } else {
            throw new IllegalArgumentException("Queue must not be null");
        }
        if (host.matches(IP_ADDRESS_PATTERN)) {
            this.host = host;
            this.port = port;
        } else {
            throw new IllegalArgumentException("Host IP-Address is not valid");
        }
    }

    /**
     * Run method for Thread
     * <p>
     * The thread does not throw any exceptions in case of error, if possible thread will try to proceed in case
     * of error.
     * </p>
     */
    @Override
    public void run() {
        long endTime = this.startTime + RUNTIME;
        log.trace("Commit Thread running");
        int counter = 0;
        DatabaseEntry temp;
        BufferedOutputStream out = null;
        while (System.currentTimeMillis() < endTime) {
            if(counter % 100 == 0){
                if(counter > 0){
                    try {
                        log.debug("Closing OutputStream");
                        out.close();
                    } catch (IOException e) {
                        log.warn("Could not close the output stream to database");
                    }
                }
                log.debug("Trying to connect the Socket");
                try {
                    this.clientSocket = new Socket(host, port);
                } catch (IOException e) {
                    log.error("Host can not be contacted on given IP-Address and Port");
                    return;
                }
                try {
                    out = new BufferedOutputStream(this.clientSocket.getOutputStream());
                } catch (IOException e) {
                    log.error("Could not create OutputStream");
                    return;
                }
            }
            try {
                temp = this.queue.take();
                if (temp == null) {
                    log.error("Null object injected into working queue");
                    continue;
                }
                log.trace("Handling Queue-Element " + temp.toString() + " as number " + (counter + 1));
                String message = temp.toString() + ";";
                byte[] messageBytes = message.getBytes(Charset.forName("UTF-8"));
                out.write(messageBytes);
                out.flush();
                TimeUnit.MILLISECONDS.sleep(40);
            } catch (InterruptedException e) {
                log.info("Thread was interrupted");
                return;
            } catch (IOException e) {
                log.error("Error reading from queue or writing to server socket:", e);
                return;
            }
            counter++;
        }
        log.debug("Thread handled " + counter + " elements in queue");
        if (!this.queue.isEmpty()) {
            log.warn("Thread could not handle all elements in the queue, " + this.queue.size() + " elements left");
        }
    }
}
