package de.htw.grischa.chess;

import java.util.ArrayList;
import java.util.TreeMap;
import org.apache.log4j.Logger;

/**
 * This class takes care of the results of the game tree that the nodes deliver.
 *
 * @see de.htw.grischa.chess.AlphaBetaSearch
 */

public class AlphaBetaSearchGridResults extends AlphaBetaSearch {
	private final static Logger _log = Logger.getLogger(AlphaBetaSearch.class);
	private ArrayList<String> sendedGames;//List of all games
	private TreeMap<String, Integer> results;//red-black tree with key value

    /**
     * Constructor for generating objects that holds the computed results
     * from the grid nodes.
     * @param	sendedGames		ArrayList of String, that holds the games
     * @param   results         Ordered black-red tree, with the result set
     */
	public AlphaBetaSearchGridResults(ArrayList<String> sendedGames,
									  TreeMap<String, Integer> results) {
		this.sendedGames=sendedGames;
		this.results=results;
	}

    /**
     *
     * @param   game    current chessboard to calculate
     * @return
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
     *
     * @param   game    current chessboard with
     * @param   depth   the depth in the game tree
     * @return
     */
	@Override
	protected boolean isLeaf(IChessGame game, int depth) {
		if(sendedGames.contains(game.getStringRepresentation()))
			return true;
		return false;
	}

    /**
     *
     * @param   game    current chessboard to calculate
     * @return
     */
	@Override
	protected double getPosQuality(IChessGame game) {
		return getQuality(game);
	}

}
