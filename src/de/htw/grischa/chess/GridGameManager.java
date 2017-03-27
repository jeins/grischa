package de.htw.grischa.chess;

/**
 * GridGameManager takes care of games to distribute it to nodes
 */
public class GridGameManager {
    private IChessGame board;
    private DistributedSearch abps;

    public GridGameManager() {
        abps = new DistributedSearch();
    }

    public IChessGame getCurrentGame() {
        return this.board;
    }

    public String getTurn(long time) throws Exception {
        abps.getAlphaBetaTurn(board, time);
        board = abps.getNextGame();
        return board.getTurnNotation();
    }

    public void init() {
        this.board = ChessBoard.getStandardChessBoard();
    }

    public void init(String board, boolean k_Castling, boolean q_Castling, boolean K_Castling,
            boolean Q_Castling) {
        this.board = new ChessBoard();
        this.board.loadFromString(board);

        // is castling still achievable
        this.board.setRochade(k_Castling, q_Castling, K_Castling, Q_Castling);
    }

    public boolean opponentTurn(String turn) throws Exception {
        try {
            board = board.makeTurn(turn);
        } catch (Exception x) {
            return false;
        }
        return true;
    }
}
