package de.htw.grischa.chess;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import de.htw.grischa.node.task.GTask;
import de.htw.grischa.node.task.Task;
import de.htw.grischa.node.task.TaskDispatcher;
import de.htw.grischa.node.task.TaskReceptor;
import de.htw.grischa.registry.GWorkerNodeRegistry;

/**
 *
 */

public class DistributedSearch {
    private final static Logger LOG = Logger.getLogger(DistributedSearch.class);
    private IChessGame mNextGame;
    private TreeMap<String, Integer> mResultset;
    private Player mMaxPlayer;
    private IChessGame mGame;
    private ExecutorService mExecutorService = null;

    public DistributedSearch() {
        this.mExecutorService = Executors.newCachedThreadPool();
    }

    public void getAlphaBetaTurn(IChessGame game, long wait) {
        mResultset = new TreeMap<String, Integer>();
        mMaxPlayer = game.getPlayerToMakeTurn();
        mGame = game; // Assign the game it's needed in the collectJobResults() method.

        ArrayList<String> nodes = GWorkerNodeRegistry.getInstance().getOnlineWorkerNodes();
        ArrayList<IChessGame> gamesToCompute = this.splitWork(game, nodes.size());
        ArrayList<TaskDispatcher> dispatchers = new ArrayList<TaskDispatcher>();

        // Create a task dispatcher for each game move. If none or not enough nodes available
        // compute move locally.
        ArrayList<String> gamesAsString = new ArrayList<String>();
        for (int i = 0; i < gamesToCompute.size(); i++) {
            if (i >= nodes.size()) {
                this.computeLocally(gamesToCompute.get(i));
                continue;
            }

            Task newTask = new GTask(gamesToCompute.get(i), mMaxPlayer);
            dispatchers.add(new TaskDispatcher(newTask, nodes.get(i)));
        }

        // Dispatch each task to a node.
        for (TaskDispatcher d : dispatchers) {
            mExecutorService.submit(d);
        }

        // TODO - laurence: maybe it is a better idea to put this into AlphaBetaSearchGridResults?
        for (int i = 0; i < gamesToCompute.size(); i++) {
            gamesAsString.add(gamesToCompute.get(i).getStringRepresentation());
        }

        // Give the nodes some time to compute their tasks if any node is available else use local
        // computed results.
        try {
            if (nodes.size() > 0)
                Thread.sleep(wait);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }

        // Get back the results from the nodes
        this.collectJobResults(dispatchers);

        // Do the final search for the best move using the results form the nodes (or if no or not
        // nodes available local computed results) to get the best move.
        AlphaBetaSearchGridResults abs = new AlphaBetaSearchGridResults(gamesAsString, mResultset);
        abs.getAlphaBetaTurn(0, game);
        this.mNextGame = abs.nextGame;
    }

    private void collectJobResults(ArrayList<TaskDispatcher> dispatchers) {
        ArrayList<TaskReceptor> receptors = new ArrayList<TaskReceptor>();
        CountDownLatch doneSignal = new CountDownLatch(dispatchers.size());

        for (TaskDispatcher dispatcher : dispatchers) {
            TaskReceptor tr = new TaskReceptor(dispatcher, doneSignal);
            mExecutorService.submit(tr);
            receptors.add(tr);
        }
        
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
        }
        
        for (TaskReceptor receptor : receptors) {
            Object tmp = receptor.getTaskResult();

            if (tmp == null) {
                LOG.error("Der Job eines WorkerNodes konnte kein Ergebnis lieferen.");
                continue;
            }

            Integer result = Integer.valueOf((String) tmp);

            GTask task = (GTask) receptor.getTask();
            String currentGame = task.getChessGame().getStringRepresentation();
            if ((currentGame.getBytes())[64] == mGame.getStringRepresentation().getBytes()[64]) {
                mResultset.put(currentGame, result);
            } else {
                mResultset.put(currentGame, result * (-1));
            }
        }
    }

    /**
     * Computes a list of following chess games
     * 
     * @param game The current game
     * @param nodeCount The number of availabel worker nodes
     * @return List of games base on one game
     */
    private ArrayList<IChessGame> splitWork(IChessGame game, int nodeCount) {
        ArrayList<IChessGame> nextGames = game.getNextTurns();

        if (nextGames.size() >= nodeCount) {
            return nextGames;
        }

        while (nextGames.size() < nodeCount) {
            for (int i = 0; i < nextGames.size(); i++) {
                IChessGame tmpGame = nextGames.get(i);
                ArrayList<IChessGame> tmpGames = tmpGame.getNextTurns();

                if ((nextGames.size() + tmpGames.size() - 1) >= nodeCount) {
                    return nextGames;
                }

                nextGames.remove(i);
                nextGames.addAll(tmpGames);
            }
        }

        return nextGames;
    }

    public IChessGame getNextGame() {
        return mNextGame;
    }

    private void computeLocally(IChessGame game) {
        AlphaBetaSearchFixedDepth abs = new AlphaBetaSearchFixedDepth();
        int value;

        // Wenn spieler für den gerechnet wird dran ist
        if (game.getPlayerToMakeTurn() == mMaxPlayer) {
            value = abs.getAlphaBetaTurn(0, game);
            this.mResultset.put(game.getStringRepresentation(), value);
        } else { // Wenn Gegner dran ist
            value = abs.getAlphaBetaTurn(1, game);
            // Bewertung -> Vorzeichen vertauschen
            value = value * (-1);
            this.mResultset.put(game.getStringRepresentation(), value);
        }
    }
}
