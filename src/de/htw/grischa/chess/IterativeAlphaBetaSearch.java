package de.htw.grischa.chess;

import org.apache.log4j.Logger;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Iterative Alpha-Beta-search for moving down inside breadth-first search
 * <p>
 * Goes down the Breadth-first search and searches every second ply for best move
 * </p>
 * <h3>Version History</h3>
 * <ul>
 * <li> 04-10 - Daniel Heim - Initial Version </li>
 * <li> 06-10 - Neumann - ??? </li>
 * <li> 07-11 - Laurence Bortfeld - Optimizing the worker nodes </li>
 * <li> 04-14 - Karsten Kochan - Integration of database, typo/language, documentation </li>
 * <li> 07-14 - Karsten Kochan - Added properties based variant to use or not use the database</li>
 * <li> 09-14 - Karsten Kochan - Fallback for missing property or missing db file</li>
 * <li> 02-17 - Benjamin Troester - Internship, cleanup & research, removing everything
 * related to sharede memory, because shared memory via database isn`t needed nor really working, due to
 * memory problems</li>
 * <li> 04-17 - Benjamin Troester - research & preparation for Monte Carlo implementation</li>
 * </ul>
 *
 * @author Daniel Heim
 *
 * @version 2/17
 *
 * @see java.lang.Runnable
 */

public class IterativeAlphaBetaSearch implements Runnable {
    private final static Logger log = Logger.getLogger(IterativeAlphaBetaSearch.class);//looger
    private final IChessGame game; // Current game (board) to search the best move for
    private final Player maximizingPlayer;// Player to be maximized
    public IChessGame bestTurn;// Best turn calculated by Alpha-Beta-Search
    private int value;//Current value of the board calculated

    /**
     * Constructor extracting player to maximize from game
     * @param game IChessGame board to calculate the best move
     */
    public IterativeAlphaBetaSearch(IChessGame game) {
        this.game = game;
        this.maximizingPlayer = game.getPlayerToMakeTurn();
    }

    /**
     * Constructor given explicit player to maximize
     *
     * @param game             IChessGame board to calculate the best move
     * @param maximizingPlayer Player to maximize
     */
    public IterativeAlphaBetaSearch(IChessGame game, Player maximizingPlayer) {
        this.game = game;
        this.maximizingPlayer = maximizingPlayer;
    }

    /**
     * Getter value
     *
     * @return int value
     */
    public int getValue() {
        return value;
    }

    public String getHostName(){
        String hostname = "unknown";

        try
        {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        }
        catch (UnknownHostException ex)
        {
            log.info("Hostname can not be resolved");
        }

        return hostname;
    }

    /**
     * threading through runnable interface
     * run method containing breadth-first-search
     *
     */
    public void run() {
        int depth;
        AlphaBetaSearchFixedDepth abp;
        if (this.maximizingPlayer == game.getPlayerToMakeTurn()) {
            depth = 0;
        }
        else {
            depth = 1;
        }
        while (true) {
            abp = new AlphaBetaSearchFixedDepth();
            // measuring time consumption in alpha beta algorithm
            long startTime = System.nanoTime();
            value = abp.getAlphaBetaTurn(depth, game);
            long endTime = System.nanoTime();
            //Mark best move
            bestTurn = abp.nextGame;
            long duration = ((endTime - startTime) / 1000000 );
            log.info("Breadth-first depth: " + depth + " Hostname: " + hostname + " Value: " + value + " calculation duration: " + duration + " ms");
            if (depth > 10) {// original value 40
                log.debug("Breadth-first search terminated due to maximum depth of 10");
                break;
            }
            //Search two plies deeper -- due to alternating in game tree
            depth += 2;
        }
    }
}

