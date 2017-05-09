package de.htw.grischa.chess;

/**
 * GridGameManager takes care of games to distribute it to nodes by
 * creating a DistributedSearch object. This class could then get
 * the current game, initialize a new game or set a game from a given
 * string.
 */

public class GridGameManager {
    private IChessGame board;
    private DistributedSearch abds;

    /**
     * Default constructor for GridGameManager
     * creates new instance of alpha beta distributed search
     * @see de.htw.grischa.chess.DistributedSearch
     */
    public GridGameManager() {
        abds = new DistributedSearch();
    }

    /**
     * Getter method for the current chessboard as IChessGame object.
     * @return the current chessboard
     */
    public IChessGame getCurrentGame() {
        return this.board;
    }

    /**
     * Getter method that return the turn for given time in String representation
     * @param time          holds the time that is used for computing moves
     * @return              the String representation of the move
     * @throws Exception    for handling errors and logging mechanismen, like timeout (exceed of time)
     */
    public String getTurn(long time) throws Exception {
        abds.getAlphaBetaTurn(board, time);
        board = abds.getNextGame();
        return board.getTurnNotation();
    }

    /**
     * Init method that generates a standard chessboard.
     * @see de.htw.grischa.chess.ChessBoard
     */
    public void init() {
        this.board = ChessBoard.getStandardChessBoard();
    }

    /**
     * Init method for non standard chessboards. This method take the String representation of the
     * a board, and sets the boolean flags of a game that mark if and which type of castling is available.
     * @param board         String representation of the chessboard
     * @param k_Castling    white short castling - over kingside
     * @param q_Castling    white long castling - over queenside
     * @param K_Castling    black short castling - over kingside
     * @param Q_Castling    black long castling - over queenside
     */
    public void init(String board, boolean k_Castling, boolean q_Castling, boolean K_Castling,
            boolean Q_Castling) {
        this.board = new ChessBoard();
        this.board.loadFromString(board);

        // is castling still achievable
        this.board.setRochade(k_Castling, q_Castling, K_Castling, Q_Castling);
    }

    /**
     * Sets the engine to make a turn from a given string by calling
     * the boards makeTurn method!
     * @param turn
     * @return turn in string representation
     * @throws Exception if anything goes wrong
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
