package de.htw.grischa.chess;

import de.htw.grischa.chess.database.client.DatabaseEntry;
import de.htw.grischa.chess.database.client.FileSearch;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

/**
 * Abstract alpha-beta-search implementing the most important methods
 * <h3>Version History</h3>
 * <ul>
 * <li> 1.0 - 05/10 - Heim - Initial Version </li>
 * <li> 1.? - 12/10 - Rossius - ??? </li>
 * <li> 1.2 - 04/14 - Karsten Kochan - First database implementation</li>
 * <li> 1.3 - 07/14 - Karsten Kochan - Cleanup, check for db usage via properties file, documentation</li>
 * </ul>
 *
 * @author Heim
 * @version 1.3
 */
public abstract class AlphaBetaSearch {

    /**
     * Logger
     */
    private final static Logger log = Logger.getLogger(AlphaBetaSearch.class);

    /**
     * Minimum integer used for check mate
     */
    private final static int MIN_INT = -10000000;

    /**
     * Maximum integer used for check mate
     */
    private final static int MAX_INT = +10000000;

    /**
     * Successor
     */
    public IChessGame nextGame;

    /**
     * Current player to calculate for
     */
    protected Player maximizingPlayer;

    /**
     * Maximum depth to search for
     */
    protected int maxSearchDepth;

    int count = 0;

    private GameList clientGameList;

    private BlockingQueue<DatabaseEntry> queue;

    private FileSearch fileSearch;

    private boolean useDB = false;

    public void setQueue(BlockingQueue<DatabaseEntry> queue) {
        this.queue = queue;
    }

    public void setFileSearch(FileSearch fileSearch) {
        this.fileSearch = fileSearch;
    }

    public void setUseDB(boolean useDB) {
        this.useDB = useDB;
    }

    /**
     * Use {@link #getAlphaBetaTurn(int, IChessGame, de.htw.grischa.chess.database.client.FileSearch)} instead
     *
     * @param maxSearchDepth int, maximum searching depth
     * @param game           IChessGame to calculate for
     * @return int, quality
     */
    @Deprecated
    public int getAlphaBetaTurn(int maxSearchDepth, IChessGame game) {
        clientGameList = new GameList();
        this.maximizingPlayer = game.getPlayerToMakeTurn();
        this.maxSearchDepth = maxSearchDepth;
        return maxValue(game, 0, MIN_INT, MAX_INT);
    }

    /**
     * If database is in use, this method will check the database and calculates the result itself if not available
     *
     * @param maxSearchDepth How deep to search
     * @param game           Current board
     * @param fileSearch     FileSearch to use for file access
     * @return Game value
     */
    public int getAlphaBetaTurn(int maxSearchDepth, IChessGame game, FileSearch fileSearch) {
        clientGameList = new GameList();
        this.maximizingPlayer = game.getPlayerToMakeTurn();
        this.maxSearchDepth = maxSearchDepth;
        if (this.useDB) {
            DatabaseEntry existing = fileSearch.search(game.getMD5Hash());
            if (existing != null && existing.getDepth() >= maxSearchDepth) {
                return existing.getValue();
            }
        }
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
        if (depth > 5) {
            clientGameList.setGame(game, depth, quality);
            log.debug("Depth  " + depth + " " + game.getStringRepresentation() + " count=" + count++);
        }
    }

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
        if (this.isLeave(game, depth)) {
            //			saveGameToList(game, depth, this.getPosQuality(game));
            return (int) this.getPosQuality(game);
        } else {
            successorList = game.getNextTurns();
            for (IChessGame successor : successorList) {
                successor.setParent(game);
                if (this.useDB) {
                    if (depth >= 3) {
                        DatabaseEntry temp = fileSearch.search(successor.getMD5Hash());
                        if (temp != null && temp.getDepth() > depth) {
                            return temp.getValue();
                        } else {
                            this.familyPublish(successor, depth, successor.getQuality(maximizingPlayer));
                        }
                    }
                }
                //Ignore illegal turns
                if (depth == 0 && !successor.isLegalBoard()) {
                    minimumValueOfSuccessor = MIN_INT;
                    //this.log.debug("Brett wurde als ungültig erkannt:\n" + successor.getReadableString());
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
     * Publish calculating result to parent objects
     *
     * @param child IChessGame, should have parents to publish
     * @param depth int, current calculating depth
     * @param value int, calculated value to publish
     */
    private void familyPublish(IChessGame child, int depth, int value) {
        if (child != null && child.getParent() != null) {
            DatabaseEntry parent = new DatabaseEntry(child.getMD5Hash(), depth, value);
            DatabaseEntry existing = this.fileSearch.search(child.getMD5Hash());
            if (existing == null || existing.getDepth() < depth) {
                this.queue.add(parent);
            }
            familyPublish(child.getParent(), depth - 2, value);
        }
    }

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
        if (this.isLeave(game, depth)) {
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

    protected abstract boolean isLeave(IChessGame game, int depth);

    protected abstract int getQuality(IChessGame game);

    protected abstract double getPosQuality(IChessGame game);
}
