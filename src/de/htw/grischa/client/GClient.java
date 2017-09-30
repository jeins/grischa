package de.htw.grischa.client;

import de.htw.grischa.registry.GWorkerNodeRegistry;
import de.htw.grischa.xboard.WinboardCommunication;
/**
 * Entry point for communication between Xboard and Comm-Server
 * Class that takes care of bootstrapping worker node registration,
 * connection to the client
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
 * @see java.lang.Runnable
 */

public class GClient implements Runnable {

    public GClient() {
    }

    @Override
    public void run() {
        // Boot registry and client
        GWorkerNodeRegistry.getInstance();
        GClientConnection.getInstance();
        //Command line interface between Xboard and GClient
        WinboardCommunication cli = new WinboardCommunication();
        cli.setTime();
        cli.run();
    }

    /**
     * Entry point for the Client
     * @param   args
     */
    public static void main(String[] args) {
        GClient client = new GClient();
        new Thread(client).start();
    }
}
