package de.htw.grischa.chess;

import org.apache.log4j.Logger;
import java.util.ArrayList;

/**
 * Abstract alpha-beta-search implementing the most important methods for searching tree
 * Alpha-Beta pruning a game tree to optimize speed, memory, cpu, network usage
 * <h3>Version History</h3>
 * <ul>
 * <li> 0.0.1 - 05/10 - Daniel Heim - Initial Version </li>
 * <li> 0.0.? - 12/10 - Rossius - ??? </li>
 * <li> 0.0.2 - 04/14 - Karsten Kochan - First database implementation</li>
 * <li> 0.0.3 - 07/14 - Karsten Kochan - Cleanup, check for db usage via properties file, documentation</li>
 * <li> 0.0.3 - 02/17 - Benjamin Troester - Cleanup, research chess algorithm</li>
 * <li> 0.0.3 - 05/17 - Benjamin Troester - Monte Carlo methods</li>
 * </ul>
 */

public abstract class AlphaBetaSearch {
     //Logger
    private final static Logger log = Logger.getLogger(AlphaBetaSearch.class);
     //Minimum integer used for check mate 10000000 originally
    private final static int MIN_INT = Integer.MIN_VALUE;
    // Maximum integer used for check mate
    private final static int MAX_INT = Integer.MAX_VALUE;
    // successor
    public IChessGame nextGame;
    // Current player to calculate for
    protected Player maximizingPlayer;
    // Maximum depth to search for
    protected int maxSearchDepth;

    int count = 0;

    private GameList clientGameList;

    /**
     *
     * @param   maxSearchDepth  int, maximum searching depth
     * @param   game            IChessGame to calculate for
     * @return  int             calculated quality of move - casted originally double!
     */
    public int getAlphaBetaTurn(int maxSearchDepth, IChessGame game) {
        clientGameList = new GameList();
        this.maximizingPlayer = game.getPlayerToMakeTurn();
        this.maxSearchDepth = maxSearchDepth;
        return maxValue(game, 0, MIN_INT, MAX_INT);
    }

    /**
     * First check if game is already in list, provide quality from list if positive
     * <p>
     * Game list filled by client until size is not a predefined value, send to server if size reached value
     * </p>
     *
     * @param game    IChessGame to save
     * @param depth   int, current calculating depth
     * @param quality double, game quality
     */
    private void saveGameToList(IChessGame game, int depth, double quality) {
        if (depth > 2) {
            clientGameList.setGame(game, depth, quality);
            log.debug("Saving  " + depth + " " + game.getStringRepresentation() + " Quality= " + quality + " count=" + count++);
        }
    }

    /**
     * Mehtod part of Alpha-Beta
     * This part calculates the maximizing part in a given tree with given depth
     * @param   game    chessboard/check position
     * @param   depth   depth in the tree
     * @param   alpha   upper bound
     * @param   beta    lower bound
     * @return  int     returns the quality of the maximized branch in the tree
     */
    private int maxValue(IChessGame game, int depth, int alpha, int beta) {
        ArrayList<IChessGame> successorList;
        int minimumValueOfSuccessor;
        int v = MIN_INT;

        if (game.hasBlackLost()) {
            if (maximizingPlayer == Player.BLACK) {
                saveGameToList(game, depth, MIN_INT + depth);
                return MIN_INT + depth;
            } else {
                saveGameToList(game, depth, MAX_INT - depth);
                return MAX_INT - depth;
            }
        }
        if (game.hasWhiteLost()) {
            if (maximizingPlayer == Player.BLACK) {
                saveGameToList(game, depth, MAX_INT - depth);
                return MAX_INT - depth;
            } else {
                saveGameToList(game, depth, MIN_INT + depth);
                return MIN_INT + depth;
            }
        }
        if (this.isLeaf(game, depth)) {
            //			saveGameToList(game, depth, this.getPosQuality(game));
            return (int) this.getPosQuality(game);
        } else {
            successorList = game.getNextTurns();
            for (IChessGame successor : successorList) {
                successor.setParent(game);
                //Ignore illegal turns
                if (depth == 0 && !successor.isLegalBoard()) {
                    minimumValueOfSuccessor = MIN_INT;
                } else {
                    minimumValueOfSuccessor = minValue(successor, depth + 1, alpha, beta);
                }

                //Use better successor if available
                if (minimumValueOfSuccessor > v) {
                    v = minimumValueOfSuccessor;
                    if (depth == 0)
                        this.nextGame = successor;
                }
                if (v >= beta) {
                    //					saveGameToList(successor, depth, v);
                    return v;
                }
                alpha = Math.max(v, alpha);
            }
            //			saveGameToList(game, depth, v);
            return v;
        }
    }

    /**
     * Mehtod part of Alpha-Beta
     * This part calculates the minimizing part in a given tree with given depth
     * @param   game    chessboard/check position
     * @param   depth   depth in the tree
     * @param   alpha   upper bound
     * @param   beta    lower bound
     * @return  int     returns the quality of the maximized branch in the tree
     */
    private int minValue(IChessGame game, int depth, int alpha, int beta) {
        ArrayList<IChessGame> successorList;
        int maximumValueOfSuccessor;
        int v = MAX_INT;

        if (game.hasBlackLost()) {
            if (maximizingPlayer == Player.BLACK) {
                saveGameToList(game, depth, MIN_INT + depth);
                return MIN_INT + depth;
            } else {
                saveGameToList(game, depth, MAX_INT - depth);
                return MAX_INT - depth;
            }
        }
        if (game.hasWhiteLost()) {
            if (maximizingPlayer == Player.BLACK) {
                saveGameToList(game, depth, MAX_INT - depth);
                return MAX_INT - depth;
            } else {
                saveGameToList(game, depth, MIN_INT + depth);
                return MIN_INT + depth;
            }
        }
        if (this.isLeaf(game, depth)) {
            //			saveGameToList(game, depth, this.getPosQuality(game));
            return (int) this.getPosQuality(game);
        } else {
            successorList = game.getNextTurns();
            for (IChessGame successor : successorList) {
                maximumValueOfSuccessor = maxValue(successor, depth + 1, alpha, beta);

                if (maximumValueOfSuccessor < v) {
                    v = maximumValueOfSuccessor;
                }
                if (v <= alpha) {
                    //					saveGameToList(successor, depth, v);
                    return v;
                }
                beta = Math.min(beta, v);
            }
            //			saveGameToList(game, depth, v);
            return v;
        }
    }

    /**
     * Checking if this part of the game tree is a leaf - either check mate or depth at maximum
     * @param   game    current chessboard with
     * @param   depth   the depth in the game tree
     * @return boolean
     */
    protected abstract boolean isLeaf(IChessGame game, int depth);

    /**
     * Returns the calculated quality for a given chessboard/check position
     * @param   game    current chessboard to calculate
     * @return  int     value for the given chessboard, originally double
     * @see     Quality
     */
    protected abstract int getQuality(IChessGame game);

    /**
     * Returns the calculated quality for a given chessboard/check position
     * @param   game    current chessboard to calculate
     * @return  double  value for the given chessboard as double
     */
    protected abstract double getPosQuality(IChessGame game);
}
