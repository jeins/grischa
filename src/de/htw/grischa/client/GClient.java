package de.htw.grischa.client;

import de.htw.grischa.registry.GWorkerNodeRegistry;
import de.htw.grischa.xboard.WinboardCommunication;


public class GClient implements Runnable {

    public GClient() {
    }

    @Override
    public void run() {
        // Boot registry and client
        GWorkerNodeRegistry.getInstance();
        GClientConnection.getInstance();
        
        WinboardCommunication cli = new WinboardCommunication();
        cli.run();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        GClient client = new GClient();
        new Thread(client).start();
    }
}
