package de.htw.grischa.chess;

/**
 * GridGameManager takes care of games to distribute it to nodes
 */

public class GridGameManager {
    private IChessGame board;
    private DistributedSearch abds;

    /**
     *
     */
    public GridGameManager() {
        abds = new DistributedSearch();
    }

    /**
     *
     * @return
     */
    public IChessGame getCurrentGame() {
        return this.board;
    }

    /**
     *
     * @param time
     * @return
     * @throws Exception
     */
    public String getTurn(long time) throws Exception {
        abds.getAlphaBetaTurn(board, time);
        board = abds.getNextGame();
        return board.getTurnNotation();
    }

    /**
     *
     */
    public void init() {
        this.board = ChessBoard.getStandardChessBoard();
    }

    /**
     *
     * @param board
     * @param k_Castling
     * @param q_Castling
     * @param K_Castling
     * @param Q_Castling
     */
    public void init(String board, boolean k_Castling, boolean q_Castling, boolean K_Castling,
            boolean Q_Castling) {
        this.board = new ChessBoard();
        this.board.loadFromString(board);

        // is castling still achievable
        this.board.setRochade(k_Castling, q_Castling, K_Castling, Q_Castling);
    }

    /**
     *
     * @param turn
     * @return
     * @throws Exception
     */
    public boolean opponentTurn(String turn) throws Exception {
        try {
            board = board.makeTurn(turn);
        } catch (Exception x) {
            return false;
        }
        return true;
    }
}
