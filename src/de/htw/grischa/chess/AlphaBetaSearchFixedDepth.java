package de.htw.grischa.chess;

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
