package de.htw.grischa.chess;

/**
 * Alpha-beta-search implementing the most important methods for searching tree
 * Alpha-Beta pruning a game tree to optimize speed, memory, cpu, network usage
 * Fixed version for problems with depth.
 *
 */

public class AlphaBetaSearchFixedDepth extends AlphaBetaSearch {

    /**
     * Getter method for quality of given chessboard
     * @param   game    current chessboard to calculate
     * @return  int     a integer value casted from double value in
     * the Quality class
     * @see Quality
     */
    @Override
    protected int getQuality(IChessGame game) {
        return game.getQuality(maximizingPlayer);
    }

    /**
     * Getter for the calculated game
     * @param   game    current chessboard to calculate
     * @return  double value for maximizing party of a chess match of a certain chess
     * board
     */
    @Override
    protected double getPosQuality(IChessGame game) {
        Quality q = new Quality((ChessBoard) game);

        return q.getPositionQuality(maximizingPlayer, game.getTurnsMade());
    }

    /**
     * Checks if the given board with given depth is a leaf in game tree
     * @param   game    current chessboard with
     * @param   depth   the depth in the game tree
     * @return
     */
    @Override
    protected boolean isLeaf(IChessGame game, int depth) {
        return depth >= maxSearchDepth;
    }
}
