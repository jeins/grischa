package de.htw.grischa.chess;

import java.util.ArrayList;

/**
 * This class holds the already calculated boards, with their given depth.
 * Available through getter/setter methods.
 * Boards are stored with possible castling, which colours move/turn it is.
 * If there is positive request, the calculated value will be returned,
 * so that the node don`t have to calculated this chessboard in the game tree.
 *
 * <h3>Version History</h3>
 * <ul>
 * <li> 05/10 - Daniel Heim - Initial Version </li>
 * <li> 02/17 - Benjamin Troester - adding documentation and revise code </li>
 * </ul>
 *
 * @author Daniel Heim
 *
 * @version 02/17
 */

public class GameList {
	private ArrayList<ComputedGame> computedGames;
	private int found=0;

	/**
	 * Constructor with empty game list
 	 */
	public GameList(){
		this.computedGames = new ArrayList<>();
	}

	/**
	 * Constructor with given games
	 * @param gameList
	 */
	public GameList(ArrayList<ComputedGame> gameList){
		this.computedGames = new ArrayList<>();
		this.computedGames.addAll(gameList);
	}

    /**
     * Method checks if a searched game, with given depth, is already enlisted in computedGames
     * @param   game    chessboard to be compared
     * @param   depth   given depth in the tree
     * @return  boolean
     */
	public boolean isInList(IChessGame game, int depth) {
	    for(ComputedGame cg : computedGames) {
	        if(cg.equals(game, depth)) {
	            found = computedGames.indexOf(cg);
	            return true;
            }
        }
        return false;
	}

    /**
     * Returns quality of a given chessboard and depth,
     * only allowed to be called after isInList!
     * @param   game    chessboard to return quality
     * @param   depth   depth in tree with this chessboard
     * @return  double
     */
	public double getGameValue(IChessGame game, int depth){
		return computedGames.get(found).getQuality();
	}

    /**
     * Setter for adding calculated boards to the game list.
     * Creates new ComputedGame in the ArrayList
     * @param   game    the to insert game
     * @param   depth   depth in the tree
     * @param   Quality quality of the given board, depth
     */
	public void setGame(IChessGame game, int depth, double Quality){
		computedGames.add(new ComputedGame(game, depth, Quality));
	}
}
