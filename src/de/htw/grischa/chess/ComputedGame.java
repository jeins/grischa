package de.htw.grischa.chess;

/**
 * Class for the storing a given game
 * In the tree alpha-beta is calculating the certain check positions,
 * this class stores the chessboard representation, depth and quality given
 *
 * TODO: same game + depth with different qualities are not allowed to be contained
 */

public class ComputedGame {
	private IChessGame game;//chessboard representation
	private int depth;//depth in game tree
	private double quality;//quality of the chessboard/check position

	/**
	 * Constructor for ComputedGame
 	 * @param	game	current game/chessboard
	 * @param	depth	depth of game in game tree
	 * @param 	quality	calculated quality of given game and depth
	 */
	public ComputedGame(IChessGame game, int depth, Double quality){
		this.game = game;
		this.depth = depth;
		this.quality = quality;
	}

	/**
	 * Methods checks if given game with given depth is already contained
	 * @param 	game	current game/chessboard
	 * @param 	depth	depth of game in the game tree
	 * @return	boolean	is game with this check position and depth already enlisted
	 */
	public boolean equals(IChessGame game, int depth){
		
		if(this.game.equals(game) && this.depth==depth)
			return true;
		else
			return false;
	}

	/**
	 * Methods checks if given game with given depth and quality is already contained
	 * @param 	game	current game/chessboard
	 * @param 	depth	depth of game in the game tree
	 * @return	boolean	is game with this check position and depth already enlisted
	 */
	public boolean equals(IChessGame game, int depth, Double quality){
		
		if(this.game.equals(game) && this.depth==depth && this.quality ==quality)
			return true;
		else
			return false;
	}

	/**
	 * Getter for quality in a computed game
	 * @return	double	returns double value for the chessboard
	 */
	public double getQuality() {
		return quality;
	}
}
