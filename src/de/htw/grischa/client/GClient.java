package de.htw.grischa.client;

import de.htw.grischa.registry.GWorkerNodeRegistry;
import de.htw.grischa.xboard.WinboardCommunication;

/**
 * Entry point for communication between Xboard and Comm-Server
 * Class that takes care of bootstrapping worker node registration,
 * connection to the client
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
