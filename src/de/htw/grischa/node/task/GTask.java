package de.htw.grischa.node.task;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;
import de.htw.grischa.chess.ChessBoard;
import de.htw.grischa.chess.IChessGame;
import de.htw.grischa.chess.IterativeAlphaBetaSearch;
import de.htw.grischa.chess.Player;

/**
 * GTask is the implementation of the Task interface.
 * This class
 *
 * @see java.lang.Runnable
 * @see de.htw.grischa.node.task.Task
 */

public class GTask implements Task {
    private final static Logger LOG = Logger.getLogger(GTask.class);
    private final static String JSON_GAME_KEY = "game";//JSON version of
    private final static String JSON_MAXPLAYER_KEY = "maxplayer";//player to maximize
    
    private Boolean mIsRunning = true;
    private IChessGame mChessGame = null;
    private Integer mJobResult = null;
    private Player mMaxPlayer = null;

    /**
     *
     * @param game
     * @param maxPlayer
     */
    public GTask(IChessGame game, Player maxPlayer) {
        this.mChessGame = game;
        this.mMaxPlayer = maxPlayer;
    }

    /**
     *
     * @param taskString
     */
    public GTask(String taskString) {
        JSONTokener t = new JSONTokener(taskString);
        JSONObject o = new JSONObject(t);
        
        IChessGame tmpgame = new ChessBoard();
        String chessString = o.getString(JSON_GAME_KEY);
        tmpgame.loadFromString(chessString);
        this.mChessGame = tmpgame;
        
        String ps = o.getString(JSON_MAXPLAYER_KEY);
        if (ps.compareTo(Player.WHITE.toString()) == 0) {
            mMaxPlayer = Player.WHITE;
        } else {
            mMaxPlayer = Player.BLACK;
        }
    }

    /**
     *
     */
    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        LOG.trace("Running GTask, before creating IterativeAlphaBetaTask");
        IterativeAlphaBetaSearch ab = new IterativeAlphaBetaSearch(this.mChessGame, mMaxPlayer);
        Thread thread = new Thread(ab);
        thread.start();

        while (mIsRunning) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage());
            }

            if (mJobResult == null || ab.getValue() != mJobResult)
                mJobResult = ab.getValue();
        }
        thread.stop();
    }

    /**
     *
     */
    public void stop() {
        this.mIsRunning = false;
    }

    /**
     *
     * @return
     */
    public Integer getResult() {
        return mJobResult;
    }

    /**
     *
     * @param result
     */
    public void setResult(Object result) {
        mJobResult = (Integer) result;
    }

    /**
     *
     * @return
     */
    public IChessGame getChessGame() {
        return mChessGame;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put(JSON_GAME_KEY, mChessGame.getStringRepresentation());
        json.put(JSON_MAXPLAYER_KEY, mMaxPlayer);
        return json.toString();
    }
}
