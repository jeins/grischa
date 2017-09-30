package de.htw.grischa;

import de.htw.grischa.chess.GridGameManager;
import de.htw.grischa.chess.IChessGame;
import de.htw.grischa.client.GClientConnection;
import de.htw.grischa.registry.GWorkerNodeRegistry;

/**
 * Class GriScha
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
 */

public class GriScha {
    // Version shown in Xboard
    public final static String VERSION = "Redis.0.0.1";

    public GriScha() {
    }

    /**
     * Main Method with entry point
     * @param args
     */
    public static void main(String[] args) {
        GWorkerNodeRegistry.getInstance();
        GClientConnection.getInstance();
        
        GriScha g = new GriScha();
        g.makeTurn();
        
        GWorkerNodeRegistry.getInstance().stopRegistry();
    }

    /**
     *
     */
    public void makeTurn() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        
        GridGameManager gm = new GridGameManager();
        gm.init();
        try {
            System.out.println(gm.getTurn(5000));
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
        
        while (true) {
            IChessGame g = gm.getCurrentGame();
            gm.init(g.getStringRepresentation(), false, false, false, false);
            try {
                System.out.println(gm.getTurn(5000));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
