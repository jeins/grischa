package de.htw.grischa;

import de.htw.grischa.chess.GridGameManager;
import de.htw.grischa.chess.IChessGame;
import de.htw.grischa.client.GClientConnection;
import de.htw.grischa.registry.GWorkerNodeRegistry;

public class GriScha {
    // Version = "xmpp-0.0.1" -> original?
    public final static String VERSION = "Redis.0.0.1";

    public GriScha() {
    }


    public static void main(String[] args) {
        GWorkerNodeRegistry.getInstance();
        GClientConnection.getInstance();
        
        GriScha g = new GriScha();
        g.makeTurn();
        
        GWorkerNodeRegistry.getInstance().stopRegistry();
    }
    
    public void makeTurn() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        GridGameManager gm = new GridGameManager();
        gm.init();
        try {
            //System.out.println(gm.getTurn(5000));
            System.out.println(gm.getTurn(5000));
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
        
        while (true) {
            IChessGame g = gm.getCurrentGame();
            gm.init(g.getStringRepresentation(), false, false, false, false);
            try {
                //System.out.println(gm.getTurn(5000));
                System.out.println(gm.getTurn(5000));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
