package de.htw.grischa.chess;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.apache.log4j.Logger;
import java.io.FileWriter;
import java.io.BufferedWriter;
import de.htw.grischa.node.task.GTask;
import de.htw.grischa.node.task.Task;
import de.htw.grischa.node.task.TaskDispatcher;
import de.htw.grischa.node.task.TaskReceptor;
import de.htw.grischa.registry.GWorkerNodeRegistry;

/**
 * This class job is the distribution of a given game, this means the current chessboard, and
 * the time nodes get to calculate.
 * This class is used by the GridGameManager, which takes care of the games computed in the grid.
 *
 * @see de.htw.grischa.chess.GridGameManager
 *
 * <h3>Version History</h3>
 * <ul>
 * <li> 05/10 - Daniel Heim - Initial Version </li>
 * <li> 03/17 - Benjamin Troester - Adding documentation</li>
 * <li> 04/17 - Benjamin Troester - Adding </li>
 * </ul>
 *
 * @author Daniel Heim
 *
 * @version 03/17
 */

public class DistributedSearch {
    private final static Logger LOG = Logger.getLogger(DistributedSearch.class);//logging tool
    private IChessGame mNextGame;//the next game
    private TreeMap<String, Integer> mResultset; //red-black tree - containing results and chessboards
    private Player mMaxPlayer;
    private IChessGame mGame;//current game
    private ExecutorService mExecutorService = null;//threading managing for async tasks
    private BufferedWriter write;

    /**
     * Default constructor for creating a thread pool, so that the computation
     * of the game tree could be distributed.
     */
    public DistributedSearch() {
        this.mExecutorService = Executors.newCachedThreadPool();
    }

    /**
     * Getter Method that gets all available worker nodes from GWorkerNodeRegistry instance puts it
     * in an ArrayList of strings called nodes. Creates a ArrayList of IChessGame that contains all game the
     * has to be computed by the splitWork method called games to compute and last but not least creates a list
     * of dispatchers.
     * @param game
     * @param wait
     */
    public void getAlphaBetaTurn(IChessGame game, long wait) {
        mResultset = new TreeMap<>();
        mMaxPlayer = game.getPlayerToMakeTurn();
        mGame = game; // Assign the game it's needed in the collectJobResults() method.

        ArrayList<String> nodes = GWorkerNodeRegistry.getInstance().getOnlineWorkerNodes();//available nodes
        ArrayList<IChessGame> gamesToCompute = this.splitWork(game, nodes.size());  //game tree
        ArrayList<TaskDispatcher> dispatchers = new ArrayList<>(); // list of nodes to dispatch the workload

        // Create a task dispatcher for each game move. If none or not enough nodes available
        // compute move locally.
        ArrayList<String> gamesAsString = new ArrayList<>();
        for (int i = 0; i < gamesToCompute.size(); i++) {
            if (i >= nodes.size()) { //condition: not enough worker nodes available
                this.computeLocally(gamesToCompute.get(i));//do it locally
                continue;
            }
            Task newTask = new GTask(gamesToCompute.get(i), mMaxPlayer); // if there are enough nodes sent task
            dispatchers.add(new TaskDispatcher(newTask, nodes.get(i))); // adding them to the dispatchers
        }
        // Dispatch each task to a node.
        for (TaskDispatcher d : dispatchers) {
            mExecutorService.submit(d); // concurrency safe for dispatchers
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
     * It creates a list of receptors, the grid nodes, and will fill this list with
     * the alls task receptors. <p>
     * After all receptors are enlisted then the games will be stored to the
     * mResultset by casting the boards to string and the quality to integers.
     * @param   dispatchers     The dispatcher
     * @see de.htw.grischa.node.task.TaskDispatcher
     */
    private void collectJobResults(ArrayList<TaskDispatcher> dispatchers) {
        ArrayList<TaskReceptor> receptors = new ArrayList<>();
        CountDownLatch doneSignal = new CountDownLatch(dispatchers.size());
        LOG.info("jumlah dispatchers: " + dispatchers.size());
        for (TaskDispatcher dispatcher : dispatchers) {
            TaskReceptor tr = new TaskReceptor(dispatcher, doneSignal);
            mExecutorService.submit(tr);
            receptors.add(tr);
        }
        LOG.info("jumlah receptor: " + receptors.size());
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
            
            writeInfoToTextFile(receptor.getHostName(), result);
            
            // condition for wich player the value has to be stored concerns only the result value due minimax
            if ((currentGame.getBytes())[64] == mGame.getStringRepresentation().getBytes()[64]) {
                mResultset.put(currentGame, result);
            } else {
                mResultset.put(currentGame, result * (-1));
            }
        }
    }

    // write node info (hostname, value and datetime) to text file
    private void writeInfoToTextFile(String hostName, int value) {
        try {
            String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            String nodeInfo = hostName + ';' + (value / 10000) + ';' + currentDateTime;
            write = new BufferedWriter(new FileWriter("worker.txt", true));
            write.write(nodeInfo);
            write.newLine();
            write.flush();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        } finally {                       
            if (write != null) try {
                write.close();
            } catch (Exception ioe2) {
                LOG.error(ioe2.getMessage());
            }
        } 
    }

    /**
     * Method to split up the workload of the game tree.
     * This is done by taking all available nextTurn from the current game and try to
     * distribute the next games to the available worker nodes. If there are at least as much
     * worker nodes online as games in the tree the method returns the ArrayList with the next games.
     * If there less than that
     *
     * @param   game      The current game as IChessGame/Chessboard object
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
     * Getter method that return the next game from mNextGame variable
     * which is set in the getAlphaBetaTurn method
     * @return the next move in IChessGame format
     */
    public IChessGame getNextGame() {
        return mNextGame;
    }

    /**
     * If there are note enough nodes to distribute the game tree, it hast to be
     * computed locally. So the local nodes will computed a given game, with given depth.
     * @param   game    holds the game list
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
