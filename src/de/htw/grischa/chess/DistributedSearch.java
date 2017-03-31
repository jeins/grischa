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
 * This class job is the distrubtion of a given game, this means the current chessboard, and
 * the time nodes get to calculate.
 * This class is used by the GridGameManager, which takes care of the games computed in the grid.
 *
 * @see de.htw.grischa.chess.GridGameManager
 */

public class DistributedSearch {
    private final static Logger LOG = Logger.getLogger(DistributedSearch.class);//logging tool
    private IChessGame mNextGame;//the next game
    private TreeMap<String, Integer> mResultset; //red-black tree - containing results and chessboards
    private Player mMaxPlayer;
    private IChessGame mGame;//current game
    private ExecutorService mExecutorService = null;//threading managing for async tasks

    /**
     * Default contructor for creating a thread pool, so that the computation
     * of the game tree could be distributed.
     */
    public DistributedSearch() {
        this.mExecutorService = Executors.newCachedThreadPool();
    }

    /**
     * Getter method
     * @param game
     * @param wait
     */
    public void getAlphaBetaTurn(IChessGame game, long wait) {
        mResultset = new TreeMap<>();
        mMaxPlayer = game.getPlayerToMakeTurn();
        mGame = game; // Assign the game it's needed in the collectJobResults() method.

        ArrayList<String> nodes = GWorkerNodeRegistry.getInstance().getOnlineWorkerNodes();
        ArrayList<IChessGame> gamesToCompute = this.splitWork(game, nodes.size());
        ArrayList<TaskDispatcher> dispatchers = new ArrayList<>();

        // Create a task dispatcher for each game move. If none or not enough nodes available
        // compute move locally.
        ArrayList<String> gamesAsString = new ArrayList<>();
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
        // Give the nodes some time to compute their tasks
        // if any node is available else use local
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

    /**
     * Method to collect the calculated boards that are computed in the grid.
     * @param   dispatchers     The dispatcher
     * @see de.htw.grischa.node.task.TaskDispatcher
     */
    private void collectJobResults(ArrayList<TaskDispatcher> dispatchers) {
        ArrayList<TaskReceptor> receptors = new ArrayList<>();
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
        //iterate trough TaskReceptors to get results
        for (TaskReceptor receptor : receptors) {
            Object tmp = receptor.getTaskResult();//actual quality
            if (tmp == null) {
                LOG.error("The job of an worker nodes could not return a result!");
                continue;
            }
            //cast to integer
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
     * @param   game      The current game
     * @param   nodeCount The number of available worker nodes
     * @return  ArrayList of games base on one game
     */
    private ArrayList<IChessGame> splitWork(IChessGame game, int nodeCount) {
        ArrayList<IChessGame> nextGames = game.getNextTurns();

        if (nextGames.size() >= nodeCount) {
            return nextGames;
        }
        //Checks size of the game tree vs available nodes
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

    /**
     *
     * @return
     */
    public IChessGame getNextGame() {
        return mNextGame;
    }

    /**
     * If there are note enough nodes to distribute the game tree, it hast to be
     * computed locally. So the local nodes will computed a given game, with given depth.
     * @param   game    holds the gamelist
     */
    private void computeLocally(IChessGame game) {
        AlphaBetaSearchFixedDepth abs = new AlphaBetaSearchFixedDepth();
        int value;
        if (game.getPlayerToMakeTurn() == mMaxPlayer) { // conditional if player whom to compute
            value = abs.getAlphaBetaTurn(0, game);
            this.mResultset.put(game.getStringRepresentation(), value);
        } else { // cond. opponent is on the turn
            value = abs.getAlphaBetaTurn(1, game);
            value = value * (-1); //-> value has to be negated
            this.mResultset.put(game.getStringRepresentation(), value);
        }
    }
}
