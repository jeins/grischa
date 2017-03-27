/**
 * Iterative Alpha-Beta-search for moving down inside breadth-first search
 * <p>
 * Goes down the Breadth-first search and searches every second ply for best move
 * </p>
 * <h3>Version History</h3>
 * <ul>
 * <li> 0.1 - 04/10 - Heim - Initial Version </li>
 * <li> 0.2 - 06/10 - Neumann - ??? </li>
 * <li> 0.3 - 07/11 - Laurence Bortfeld - ??? </li>
 * <li> 0.4 - 04/14 - Karsten Kochan - Integration of database, typo/language, documentation </li>
 * <li> 0.5 - 07/14 - Karsten Kochan - Added properties based variant to use or not use the database</li>
 * <li> 0.6 - 09/14 - Karsten Kochan - Fallback for missing property or missing db file</li>
 * <li> 0.7 - 02/17 - Benjamin Troester - internship -> cleanup & research</li>
 * <li> 0.8 - 02/17 - Benjamin Troester - research & preparation for Monte Carlo implementation</li>
 * </ul>
 * @version 0.6
 */

package de.htw.grischa.chess;

import org.apache.log4j.Logger;

public class IterativeAlphaBetaSearch implements Runnable {
    //looger
    private final static Logger log = Logger.getLogger(IterativeAlphaBetaSearch.class);
    // Current game (board) to search the best move for
    private final IChessGame game;
    // Player to be maximized
    private final Player maximizingPlayer;
    // Best turn calculated by Alpha-Beta-Search
    public IChessGame bestTurn;
    //Current value of the board calculated
    private int value;

    /**
     * Constructor extracting player to maximize from game
     *
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
            long startTime = System.nanoTime();
            value = abp.getAlphaBetaTurn(depth, game);
            long endTime = System.nanoTime();
            //Mark best move
            bestTurn = abp.nextGame;
            long duration = ((endTime - startTime) / 1000000 );
            //Search two plies deeper
            //depth += 2;
            log.debug("Breadth-first depth: " + depth + " Value: " + value + " calculation duration: " + duration + " ms");
            log.info("Breadth-first depth: " + depth + " Value: " + value + " calculation duration: " + duration + " ms");
            if (depth > 8) {// original value 40
                log.debug("Breadth-first search terminated due to maximum depth of 8");
                break;
            }
            depth += 2;
        }
    }
}

