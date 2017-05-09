package de.htw.grischa.chess;

import java.util.ArrayList;
import java.util.TreeMap;
import org.apache.log4j.Logger;

/**
 * This class takes care of the results computed by the (grid) nodes.
 * AlphaBetaSearchGridResults inherited it's method form the AlphaBetaSearch
 * class, it overrides the getQuality, getPosQulaity and the isLeaf method.
 * The class attributes are a ArrayList of strings for the sent games from
 * the grid and a TreeMap that contains both, games as string and integers
 * for the quality values. Last but not least it contains a logging object
 * to make debugging easier.
 * @see de.htw.grischa.chess.AlphaBetaSearch
 */

public class AlphaBetaSearchGridResults extends AlphaBetaSearch {
	private final static Logger _log = Logger.getLogger(AlphaBetaSearch.class);
	private ArrayList<String> sentGames;//List of all games
	private TreeMap<String, Integer> results;//red-black tree with key value

    /**
     * Constructor method that holds the fields sentGames, which is an ArrayList
     * of strings that contains the games sent and a TreeMap with strings that represents
     * the games, here the keys and the quality of the corresponding games as integer, here
     * the values.
     * @param	sentGames		ArrayList of String, that holds the games
     * @param   results         Ordered black-red tree, with the result set
     */
	public AlphaBetaSearchGridResults(ArrayList<String> sentGames,
									  TreeMap<String, Integer> results) {
		this.sentGames =sentGames;
		this.results=results;
	}

    /**
     * Getter Method inherited from AlphaBetaSearch class to calculate
     * the value of a given game. The method will check in result set for the
     * right game. This is done in a red black tree that contains the games in
     * string representation for the keys (red black trees are well balanced trees
     * which perform pretty good). <p>
     * If a game isn't found in the results TreeMap than the game will computed locally,
     * by creating  a new AlphaBetaSearchFixedDepth object and getting the quality, as
     * an integer, from the getAlphaBetaTurn. There will also be a statement which player
     * it is to maximize, so the value could be returned or negated by multiply it with -1.
     * @param   game    current chessboard to calculate
     * @return  calculated quality of the chessboard as an integer value
     */
	@Override
	protected int getQuality(IChessGame game) {
		if(results.containsKey(game.getStringRepresentation())) {
			_log.debug("calculating game: "+game.getStringRepresentation()+" " +
					   "calculated quality: "+results.get(game.getStringRepresentation()));
			return results.get(game.getStringRepresentation());
		}
		else {
			_log.debug("Node result not existing! Calculating locally. Game: "+game.getStringRepresentation());
			AlphaBetaSearchFixedDepth abs=new AlphaBetaSearchFixedDepth();
			if(game.getPlayerToMakeTurn()==maximizingPlayer) {
				int v= abs.getAlphaBetaTurn(0, game);
				return v;
			}
			else {
				int v= (abs.getAlphaBetaTurn(1, game)* (-1));
				return v;
			}
		}
	}

    /**
     * The isLeaf method checks if a node of the game tree in game tree is a
	 * leaf or not. So it could be considered to check the game tree for certain
     * nodes of the tree.
     * @param   game    Chessboard with a given depth
     * @param   depth   the depth in the game tree
     * @return	boolean value - that contrains true if the game is a leaf or false if not
     */
	@Override
	protected boolean isLeaf(IChessGame game, int depth) {
		if(sentGames.contains(game.getStringRepresentation()))
			return true;
		return false;
	}

    /**
     * Getter method that returns the quality of a given game by
     * simply calling getQuality method of the very same class.
     * This method overrides the original method inherited by
     * AlphaBetaSearch class. <p>
     * This method will return a double, even though the
     * getQuality method will just return an integer! So it is
     * basically a integer casted to double.
     * @param   game    current chessboard to calculate
     * @return	quality of the game in double floating format
     */
	@Override
	protected double getPosQuality(IChessGame game) {
		return getQuality(game);
	}

}
