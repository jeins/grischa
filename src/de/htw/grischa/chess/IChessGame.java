package de.htw.grischa.chess;

import java.util.ArrayList;

/**
 * Interface for chessboard
 * Provides requirements to the chessboard representation
 * <h3>Version History</h3>
 * <ul>
 * <li> 0.0.1 - 12/09 - Heim - Initial Version</li>
 * <li> 0.0.? - 05/10 - Rossius - ???</li>
 * <li> 0.0.? - 06/10 - Heim - ???</li>
 * <li> 0.0.3 - 06/14 - Karsten Kochan - Added toDatabase method, added getMD5Hash() method,
 * documentation/type,
 * translation, added parent getter and setter</li>
 * </ul>
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
     * @param   board       String to initialize game from
     */
    public void loadFromString(String board);

    /**
     * Returns standard string representation of the current game
     * @return  String      with standard string representation
     */
    public String getStringRepresentation();

    /**
     * Returns player who is on the move
     * @return  Player      to make turn
     */
    public Player getPlayerToMakeTurn();

    /**
     * Calculates all valid next turn
     * @return  ArrayList   ArrayList with all valid next turns
     */
    public ArrayList<IChessGame> getNextTurns();

    /**
     * Getter method for the current game quality with the given player
     * @param   player      to calculated the quality for
     * @return  int         quality of the board
     */
    public int getQuality(Player player);

    /**
     * Returns a human readable representation of the board
     * @return  String      human readable representation
     */
    public String getReadableString();

    /**
     * Return the standard hash of the board
     * @return  String  standard hash
     */
    public String getHash();

    /**
     * Generates a heuristic quality assumption of the board according to last move.
     * Positive values calculated white as better, negative values black
     * @return  int     heuristic quality assumption
     */
    public int getHeuristicValue();

    /**
     * Getter
     * Returns String in turn notation
     * Format d2d4 for last move
     * @return  String      with turn notation
     */
    public String getTurnNotation();

    /**
     * Getter
     * Return the new board if the turn given as string is done by active player
     * @param   turn        String as turn to do
     * @return  IChessGame  resulting board
     * @throws  Exception   if turn is illegal
     */
    public IChessGame makeTurn(String turn) throws Exception;

    /**
     * Getter
     * Returns the number of turns made
     * @return  int     number of turns
     */
    public int getTurnsMade();

    /**
     * Returns if the current player is able to check mate the king
     * @return  true    if possible
     */
    public boolean isLegalBoard();

    /**
     * Getter for the game state
     * legal if all ok, illegal of any problems like missing king,
     * king in check mate, no more following moves
     * @see GameState
     * @return  GameState   state
     */
    public GameState getGameState();

    /**
     * Setter for castling
     * Set which kind of castling is possible on current board
     * @param   k_Castling  boolean if white king side castling (short castling) can be done
     * @param   q_Castling  boolean if white queen side castling (long castling) can be done
     * @param   K_Castling  boolean if black king side castling (short castling) can be done
     * @param   Q_Castling  boolean if black queen side castling (long castling) can be done
     */
    public void setRochade(boolean k_Castling, boolean q_Castling, boolean K_Castling, boolean Q_Castling);

    /**
     * Returns if white has lost the game
     * @return  boolean     true if white has lost
     */
    public boolean hasWhiteLost();

    /**
     * Returns if black has lost the game
     * @return  boolean     true if black has lost
     */
    public boolean hasBlackLost();

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
     * You are able to set a chain of children of a board by setting a parent which having a parent itself

     * @param parent IChessGame parent
     */
    public void setParent(IChessGame parent);
}