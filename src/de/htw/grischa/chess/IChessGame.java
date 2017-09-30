package de.htw.grischa.chess;

import java.util.ArrayList;

/**
 * Interface for chessboard
 * Provides requirements to the chessboard representation and extends the comparable
 * interface to compare chessboards.
 *
 * <h3>Version History</h3>
 * <ul>
 * <li> 12/09 - Daniel Heim - Initial Version</li>
 * <li> 05/10 - Rossius - ???</li>
 * <li> 06/10 - Daniel Heim - ???</li>
 * <li> 06/14 - Karsten Kochan - Added toDatabase method, added getMD5Hash() method,
 * documentation/type, translation, added parent getter and setter</li>
 * <li> 03/17 - Benjamin Troester - Removed toDatabase method, getMD5Hash() method
 * because shared memory via database isn`t needed nor really working.</li>
 * </ul>
 *
 * @author Danie Heim
 *
 * @version 02/17
 * @see java.lang.Comparable
 */

public interface IChessGame extends Comparable<IChessGame> {
    /*
     * Standard String Representation:
     * s=black, w=white
	 * String of 65 chars, one char is one field starting from A1, A2, ... , H8.
	 * White figures are lower case, black figures upper case:
	 * Pawn=b/B
     * Rook=t/T
	 * Knight=s/S
	 * Bishop=l/L
	 * Queen=d/D
	 * King=k/K
	 * empty=x
	 * Last char (65) tells the player who needs to move next:
	 */

    /**
     * Init game from standard string representation
     * @param   board       String to initialize game from
     */
    void loadFromString(String board);

    /**
     * Returns standard string representation of the current game
     * @return  String      with standard string representation
     */
    String getStringRepresentation();

    /**
     * Returns player who is on the move
     * @return  Player      to make turn
     */
    Player getPlayerToMakeTurn();

    /**
     * Calculates all valid next turn
     * @return  ArrayList   ArrayList with all valid next turns
     */
    ArrayList<IChessGame> getNextTurns();

    /**
     * Getter method for the current game quality with the given player
     * @param   player      to calculated the quality for
     * @return  int         quality of the board
     */
    int getQuality(Player player);

    /**
     * Returns a human readable representation of the board
     * @return  String      human readable representation
     */
    String getReadableString();

    /**
     * Return the standard hash of the board
     * @return  String  standard hash
     */
    String getHash();

    /**
     * Generates a heuristic quality assumption of the board according to last move.
     * Positive values calculated white as better, negative values black
     * @return  int     heuristic quality assumption
     */
    int getHeuristicValue();

    /**
     * Getter
     * Returns String in turn notation
     * Format d2d4 for last move
     * @return  String      with turn notation
     */
    String getTurnNotation();

    /**
     * Getter
     * Return the new board if the turn given as string is done by active player
     * @param   turn        String as turn to do
     * @return  IChessGame  resulting board
     * @throws  Exception   if turn is illegal
     */
    IChessGame makeTurn(String turn) throws Exception;

    /**
     * Getter
     * Returns the number of turns made
     * @return  int     number of turns
     */
    int getTurnsMade();

    /**
     * Returns if the current player is able to check mate the king
     * @return  true    if possible
     */
    boolean isLegalBoard();

    /**
     * Getter for the game state
     * legal if all ok, illegal of any problems like missing king,
     * king in check mate, no more following moves
     * @see GameState
     * @return  GameState   state
     */
    GameState getGameState();

    /**
     * Setter for castling -> name is confusing
     * Set which kind of castling is possible on current board
     * @param   k_Castling  boolean if white king side castling (short castling) can be done
     * @param   q_Castling  boolean if white queen side castling (long castling) can be done
     * @param   K_Castling  boolean if black king side castling (short castling) can be done
     * @param   Q_Castling  boolean if black queen side castling (long castling) can be done
     */
    void setRochade(boolean k_Castling, boolean q_Castling, boolean K_Castling, boolean Q_Castling);

    /**
     * Returns if white has lost the game
     * @return  boolean     true if white has lost
     */
    boolean hasWhiteLost();

    /**
     * Returns if black has lost the game
     * @return  boolean     true if black has lost
     */
    boolean hasBlackLost();

    /**
     * Return the parent off the game
     * <p>
     * This method returns null if the parent is not set or does not exists. Beware that not every IChessGame needs
     * to have an parent
     * </p>
     *
     * @return IChessGame parent of the board
     */
    IChessGame getParent();

    /**
     * Set the parent board to the IChessGame
     * You are able to set a chain of children of a board by setting a parent which having a parent itself

     * @param parent IChessGame parent
     */
    void setParent(IChessGame parent);
}