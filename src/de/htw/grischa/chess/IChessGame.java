package de.htw.grischa.chess;

import java.util.ArrayList;

/**
 * Interface to chessboards
 * <h3>Version History</h3>
 * <ul>
 * <li> 1.0 - 12/09 - Heim - Initial Version</li>
 * <li> 1.? - 05/10 - Rossius - ???</li>
 * <li> 1.? - 06/10 - Heim - ???</li>
 * <li> 1.3 - 06/14 - Karsten Kochan - Added toDatabase method, added getMD5Hash() method, documentation/type,
 * translation, added parent getter and setter</li>
 * </ul>
 *
 * @author heim
 * @version 1.3
 */
public interface IChessGame extends Comparable<IChessGame> {
    /*
     * Standard String Representation:
	 * String of 65 chars, one char is one field starting from A1, A2, ... , H8.
	 * White figures are lower case, black figures upper case:
	 * Pawn=b/B, Rook=t/T, Knight=s/S, Bishop=l/L, Queen=d/D, King=k/K. empty=x
	 * Last char (65) tells the player who needs to move next:
	 * s=black, w=white
	 */

    /**
     * Init game from standard string representation
     *
     * @param board String to initialize game from
     */
    public void loadFromString(String board);

    /**
     * Returns standard string representation of the current game
     *
     * @return String with standard string representation
     */
    public String getStringRepresentation();

    /**
     * Returns player to make turn
     *
     * @return Player to make turn
     */
    public Player getPlayerToMakeTurn();

    /**
     * Calculates all possible next turn
     *
     * @return ArrayList of IChessGame with valid next turns
     */
    public ArrayList<IChessGame> getNextTurns();

    /**
     * Calculates the current quality of the game for selected player
     *
     * @param player to calculated the quality for
     * @return int quality of the board
     */
    public int getQuality(Player player);

    /**
     * Returns a human readable representation of the board
     *
     * @return String human readable representation
     */
    public String getReadableString();

    /**
     * Return the standard hash of the board
     *
     * @return String standard hash
     */
    public String getHash();

    /**
     * Generates a heuristic quality assumption of the board according to last move.
     * <p>
     * Positive values calculated white as better, negative values black
     * </p>
     *
     * @return int heuristic quality assumption
     */
    public int getHeuristicValue();

    /**
     * Returns String in turn notation
     * <p>
     * Format d2d4 for last move
     * </p>
     *
     * @return String with turn notation
     */
    public String getTurnNotation();

    /**
     * Return the new board if the turn given as string is done by active player
     *
     * @param turn String as turn to do
     * @return IChessGame resulting board
     * @throws Exception if turn is illegal
     */
    public IChessGame makeTurn(String turn) throws Exception;

    /**
     * Returns the number of turns made
     *
     * @return int number of turns
     */
    public int getTurnsMade();

    /**
     * Returns if the current player is able to check mate the king
     *
     * @return true if possible
     */
    public boolean isLegalBoard();

    /**
     * Returns the status of the board:
     * <p>
     * legal if all ok, illegal of any problems like missing king, king in check mate, no more following moves
     * </p>
     *
     * @return GameState state
     */
    public GameState getGameState();

    /**
     * Set which kind of castling is possible on current board
     *
     * @param k_Castling boolean if white king side castling (short castling) can be done
     * @param q_Castling boolean if white queen side castling (long castling) can be done
     * @param K_Castling boolean if black king side castling (short castling) can be done
     * @param Q_Castling boolean if black queen side castling (long castling) can be done
     */
    public void setRochade(boolean k_Castling, boolean q_Castling, boolean K_Castling, boolean Q_Castling);

    /**
     * Returns if white has lost the game
     *
     * @return boolean true if white has lost
     */
    public boolean hasWhiteLost();

    /**
     * Returns if black has lost the game
     *
     * @return boolean true if black has lost
     */
    public boolean hasBlackLost();

    /**
     * Generates database-String for current board including hash, depth and value
     *
     * @param current Player to make turn
     * @param depth   int searching depth
     * @return String hash, length {@link de.htw.grischa.chess.database.client.DatabaseEntry#TARGET_LENGTH}
     */
    public String toDatabase(Player current, int depth);

    /**
     * Generates a MD5-Hash of this board to identify in database
     *
     * @return String hash length 32
     */
    public String getMD5Hash();

    /**
     * Return the parent off the game
     * <p>
     * This method returns null if the parent is not set or does not exists. Beware that not every IChessGame needs
     * to have an parent
     * </p>
     *
     * @return IChessGame parent of the board
     */
    public IChessGame getParent();

    /**
     * Set the parent board to the IChessGame
     * <p>
     * You are able to set a chain of children of a board by setting a parent which having a parent itself
     * </p>
     *
     * @param parent IChessGame parent
     */
    public void setParent(IChessGame parent);
}