package de.htw.grischa.chess;

/**
 * Abstract alpha-beta-search implementing the most important methods for searching tree
 * Alpha-Beta pruning a game tree to optimize speed, memory, cpu, network usage
 * Fixed the problem of depth
 *
 */

public class AlphaBetaSearchFixedDepth extends AlphaBetaSearch {
    protected int getQuality(IChessGame game) {
        return game.getQuality(maximizingPlayer);
    }


    protected double getPosQuality(IChessGame game) {
        Quality q = new Quality((ChessBoard) game);

        return q.getPositionQuality(maximizingPlayer, game.getTurnsMade());
    }

    protected boolean isLeaf(IChessGame game, int depth) {
        return depth >= maxSearchDepth;
    }
}
