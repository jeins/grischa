package de.htw.grischa.chess;

import org.apache.log4j.Logger;

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
 * @version 2-17
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

