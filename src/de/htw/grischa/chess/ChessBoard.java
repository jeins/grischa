package de.htw.grischa.chess;

import org.apache.log4j.Logger;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Chessboard implementation, implements IChessGame
 * If you have no idea what ent passent, castling and stuff like that is, here
 * you go: <a href="http://www.schach-tipps.de/schachregeln-schach-lernen">Schachregeln</a>
 *
 * <h3>Version History</h3>
 * <ul>
 * <li> 0.0.1 - 12/09 - Daniel Heim - Initial Version</li>
 * <li> 0.0.? - 05/10 - Daniel Heim - ???</li>
 * <li> 0.0.3 - 06/14 - Karsten Kochan - Added toDatabase method, added parent</li>
 * <li> 0.0.3 - 02/17 - Benjamin Troester - Removing toDatabase method and parent,
 * because shared memory via database isn`t needed nor really working</li>
 * * <li> 0.0.4 - 03/17 - Benjamin Troester - Research and changes in the chess engine</li>
 * </ul>
 *
 * @author Heim
 * @version 1.3
 * @see de.htw.grischa.chess.IChessGame
 */

public class ChessBoard implements IChessGame, Serializable {
    // values for chess pieces in byte style
    public static final byte EMPTY_FIELD = 0;
    public static final byte ILLEGAL_FIELD = -1;//means out of chessboard range
    public static final byte BLACK_PAWN = 2;
    public static final byte BLACK_ROOK = 5;
    public static final byte BLACK_KNIGHT = 3;
    public static final byte BLACK_BISHOP = 4;
    public static final byte BLACK_QUEEN = 6;
    public static final byte BLACK_KING = 7;
    public static final byte WHITE_PAWN = 12;
    public static final byte WHITE_ROOK = 15;
    public static final byte WHITE_KNIGHT = 13;
    public static final byte WHITE_BISHOP = 14;
    public static final byte WHITE_QUEEN = 16;
    public static final byte WHITE_KING = 17;
    private final static Logger log = Logger.getLogger(ChessBoard.class);//logger
    private static final String[] NAMES = {"x", "", "B", "S", "L", "T", "D", "K", "", "", "", "",
            "b", "s", "l", "t", "d", "k"};
    private static final short[] QUALITIES = {0, 0, -1, -3, -3, -5, -9, -100,
            0, 0, 0, 0, 1, 3, 3, 5, 9, 100};
    private static final int[] ROOK_DIRECTIONS = {-10, -1, 1, 10};
    private static final int[] KNIGHT_DIRECTIONS = {-21, -19, -8, 12, 21, 19, 8, -12};
    private static final int[] BISHOP_DIRECTIONS = {-11, -9, 9, 11};
    private static final int[] QUEEN_DIRECTIONS = {-11, -10, -9, -1, 1, 9, 10, 11};
    public byte[] fields;
    public boolean BlackCanLongRochade;
    public boolean BlackCanShortRochade;
    public boolean WhiteCanLongRochade;
    public boolean WhiteCanShortRochade;
    private Player playerToMakeTurn;
    private ArrayList<IChessGame> entPassent;
    //private ArrayList<IChessGame> nextTurns;
    private int heuristicValue;
    private String TurnNotation;
    private int round_counter;
    private boolean WhiteLost = false;
    private boolean BlackLost = false;
    private IChessGame parent;

    /**
     * Constructor for a chess board, default chessboard!
     * This means that this constructor gets the initial position
     * of a chess game, where white makes the first move. The
     * chessboards is byte array with 120 fields, left and right
     * of the chessboard are each one row and to columns at the
     * top and bottom of the board. This is done so, because the
     * check easily for illegal moves of the knight.
     */
    public ChessBoard() {
        fields = new byte[120];
        this.initializeFields();

        this.round_counter = 0;
        playerToMakeTurn = Player.WHITE;

        WhiteCanLongRochade = true;
        WhiteCanShortRochade = true;
        BlackCanLongRochade = true;
        BlackCanShortRochade = true;

        //ent passent move are possible
        entPassent = new ArrayList<>();
    }

    /**
     * Constructor for already existing boards.
     * Takes a given board and sets it to current board
     * used.
     * @param   oldBoard    Chessboard to clone
     */
    private ChessBoard(ChessBoard oldBoard) {
        //actual cloning operation
        this.fields = oldBoard.fields.clone();

        entPassent = new ArrayList<>();

        //copies castling options of the given board
        this.BlackCanLongRochade = oldBoard.BlackCanLongRochade;
        this.BlackCanShortRochade = oldBoard.BlackCanShortRochade;
        this.WhiteCanLongRochade = oldBoard.WhiteCanLongRochade;
        this.WhiteCanShortRochade = oldBoard.WhiteCanShortRochade;

        //counts the moves of both players in this match
        this.round_counter = oldBoard.round_counter + 1;

        //sets bool if game state is lost
        this.BlackLost = oldBoard.BlackLost;
        this.WhiteLost = oldBoard.WhiteLost;

        //decision who is on the move
        if (oldBoard.playerToMakeTurn == Player.BLACK)
            playerToMakeTurn = Player.WHITE;
        else
            playerToMakeTurn = Player.BLACK;
    }

    /**
     * Provides the standard starting position of a chessboard
     * @return  ChessBoard  with starting position
     */
    public static ChessBoard getStandardChessBoard() {
        ChessBoard board = new ChessBoard();
        for (int i = 31; i < 39; i++) {
            board.fields[i] = WHITE_PAWN;
            board.fields[i + 50] = BLACK_PAWN;
        }
        board.fields[21] = WHITE_ROOK;
        board.fields[28] = WHITE_ROOK;
        board.fields[22] = WHITE_KNIGHT;
        board.fields[27] = WHITE_KNIGHT;
        board.fields[23] = WHITE_BISHOP;
        board.fields[26] = WHITE_BISHOP;
        board.fields[24] = WHITE_QUEEN;
        board.fields[25] = WHITE_KING;

        board.fields[91] = BLACK_ROOK;
        board.fields[98] = BLACK_ROOK;
        board.fields[92] = BLACK_KNIGHT;
        board.fields[97] = BLACK_KNIGHT;
        board.fields[93] = BLACK_BISHOP;
        board.fields[96] = BLACK_BISHOP;
        board.fields[95] = BLACK_KING;
        board.fields[94] = BLACK_QUEEN;
        board.round_counter = 0;

        return board;
    }

    /**
     * Convert String representation of field into int value
     * @param   in      String to convert
     * @return  int     representation
     * @throws  java.lang.IllegalArgumentException if input String is invalid
     */
    public static int fieldNameToIndex(String in) throws IllegalArgumentException {
        int fieldColumn;
        int fieldRow;
        fieldColumn = (int) in.toCharArray()[0] - 'a' + 1;
        fieldRow = Integer.parseInt(Character.toString(in.toCharArray()[1])) + 1;

        if (fieldColumn < 0 || fieldColumn >= 8) {
            log.warn("Could not convert " + in.toCharArray()[0]);
            throw new IllegalArgumentException();
        }
        if (fieldRow < 0 || fieldRow >= 8) {
            log.warn("Could not convert " + in.toCharArray()[1]);
            throw new IllegalArgumentException();
        }

        return fieldColumn + fieldRow * 10;
    }

    /**
     * Convert field by index to String representation
     *
     * @param in field index
     * @return String representation
     */
    public static String indexToFieldName(int in) {
        int fieldColumn;
        int fieldRow;
        String columnName = "";
        String rowName = "";

        fieldRow = (in / 10) - 1;
        fieldColumn = (in % 10) - 1;

        char c = (char) ('a' + fieldColumn);
        columnName += c;
        rowName = rowName + fieldRow;
        return columnName + rowName;
    }

    public Player getPlayerToMakeTurn() {
        return playerToMakeTurn;
    }

    /**
     * Setter playerToMakeTurn
     *
     * @param playerToMakeTurn player to set to
     */
    public void setPlayerToMakeTurn(Player playerToMakeTurn) {
        this.playerToMakeTurn = playerToMakeTurn;
    }

    /**
     * Stellt eine Figur auf das Brett
     *
     * @param position Das Feld der Figur zwischen 0-59
     * @param piece    Die Figur, die auf das Brett gestellt wird -
     *                 am besten Konstanten dieser Klasse nutzen
     */
    public void PutPiece(int position, byte piece) throws Exception {
        //*** Ungueltiges Feld beruecksichtigen ******************************************
        if (position < 0 || position >= 64) throw new Exception(position + " nicht im Feld");

        //TODO @Daniel ungueltige Figur abfangen 

        //*** linken und rechten rand beruecksichtigen ***********************************
        int realPosition = position / 8 * 10;
        realPosition += position % 8 + 1;

        //*** unteren Rand ueberspringen *************************************************
        realPosition += 20;

        //*** Figur setzen ***************************************************************
        fields[realPosition] = piece;
    }

    /**
     *
     * @param position
     * @return
     */
    public int parseField(int position) {
        //*** linken und rechten rand beruecksichtigen ***********************************
        int realPosition = position / 8 * 10;
        realPosition += position % 8 + 1;

        //*** unteren Rand ueberspringen *************************************************
        realPosition += 20;

        return realPosition;
    }


    /**********************************************************************************/
    /************************** Method: getNextTurns **********************************/
    /**********************************************************************************/


    /**
     * Getter method, that provides a readable version of the current board.
     * White pieces have lower case letters and black pieces have capital letters.
     * Every chessboard starts with an empyth string and at the coordinates A1
     * on the chessboard. In our byte array it means at field[10].
     * This getter is very slow, due the double for loop (quadratic runtime), it has to
     * iterate through the field array and check for the possible piece on each field.
     * @return String Converted Version of the chessboard that reads easier, especially
     * in debugging und logging.
     */
    public String getReadableString() {
        //declare variables
        String s = "";
        int i = 0;
        //start outer loop
        for (int dy = 10; dy > 1; dy--) {
            if (dy == 10)
                s += "  ";
            else
                s += dy - 1 + " ";
            //start inner loop
            for (int dx = 1; dx < 9; dx++) {
                if (dy == 10) s += (char) ('A' + dx - 1) + " ";
                i = dy * 10 + dx;
                //"pattern matching" for the pieces to put in String
                switch (fields[i]) {
                    case (EMPTY_FIELD):
                        s += "  ";
                        break;
                    case (BLACK_PAWN):
                        s += "B ";
                        break;
                    case (BLACK_KNIGHT):
                        s += "S ";
                        break;
                    case (BLACK_BISHOP):
                        s += "L ";
                        break;
                    case (BLACK_ROOK):
                        s += "T ";
                        break;
                    case (BLACK_QUEEN):
                        s += "D ";
                        break;
                    case (BLACK_KING):
                        s += "K ";
                        break;
                    case (WHITE_PAWN):
                        s += "b ";
                        break;
                    case (WHITE_KNIGHT):
                        s += "s ";
                        break;
                    case (WHITE_BISHOP):
                        s += "l ";
                        break;
                    case (WHITE_ROOK):
                        s += "t ";
                        break;
                    case (WHITE_QUEEN):
                        s += "d ";
                        break;
                    case (WHITE_KING):
                        s += "k ";
                        break;
                }
            }
            s += "\n";
        }
        return s;
    }

    /**
     * Method to initialize the Fields
     */
    private void initializeFields() {
        // below bottom line - illegal fields
        for (int i = 0; i < 20; i++) {
            fields[i] = ChessBoard.ILLEGAL_FIELD;
        }
        // middle field is empty - starting at bottom line a1 - h8
        for (int i = 20; i < 100; i++) {
            if (i % 10 == 0 || i % 10 == 9)
                fields[i] = ChessBoard.ILLEGAL_FIELD;
            else
                fields[i] = ChessBoard.EMPTY_FIELD;
        }
        // over top line - illegal fields
        for (int i = 100; i < 120; i++) {
            fields[i] = ChessBoard.ILLEGAL_FIELD;
        }
    }


    /**
     * Getter method for the next turns, splits the moves between white and black.
     * Every layer is dedicated to one colour in the game tree, so they have to be split
     * up.
     * @return ArrayList that contains the next possible turn.
     */
    public ArrayList<IChessGame> getNextTurns() {
        if (playerToMakeTurn == Player.WHITE)
            return this.makeWhiteMoves();
        else
            return this.makeBlackMoves();
    }

    /**
     * Method that creates whites next possible moves in an ArrayList.
     * @return
     */
    private ArrayList<IChessGame> makeWhiteMoves() {
        // declare vars
        int field;
        byte piece;
        ArrayList<IChessGame> moves = new ArrayList<>();//init moves

        // all legal fields
        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                field = y * 10 + x;// determine field index
                piece = fields[field];// determine kind of piece
                // condition: field empty, illegal or occupied by black
                if (piece < WHITE_PAWN) continue;
                // condition: pawns move
                if (piece == WHITE_PAWN) {
                    moves.addAll(this.makeWhitePawnMove(field));
                    continue;
                }
                // condition: rooks move
                if (piece == WHITE_ROOK) {
                    moves.addAll(this.genericWhiteMoves(field, ROOK_DIRECTIONS, piece));
                    continue;
                }
                // condition: bishops move
                if (piece == WHITE_BISHOP) {
                    moves.addAll(this.genericWhiteMoves(field, BISHOP_DIRECTIONS, piece));
                    continue;
                }
                // condition: knights move
                if (piece == WHITE_KNIGHT) {
                    moves.addAll(this.whiteSingleMove(field, KNIGHT_DIRECTIONS, piece));
                    continue;
                }
                // condition: kings move
                if (piece == WHITE_KING) {
                    moves.addAll(this.whiteSingleMove(field, QUEEN_DIRECTIONS, piece));
                    continue;
                }
                // condition: queens move
                if (piece == WHITE_QUEEN) {
                    moves.addAll(this.genericWhiteMoves(field, QUEEN_DIRECTIONS, piece));
                }
            }
        moves.addAll(entPassent);
        moves.addAll(this.getWhiteRochade());
        return moves;
    }

    /**
     * Method
     * @return  ArrayList<IChessGame>
     */
    private ArrayList<IChessGame> makeBlackMoves() {
        // declare vars
        int field;
        byte piece;
        ArrayList<IChessGame> moves = new ArrayList<>();//init moves
        // all legal fields
        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                field = y * 10 + x;// determine field index
                piece = fields[field];// determine kind of piece
                // condition: field empty, illegal or occupied by white
                if (piece < BLACK_PAWN && piece >= BLACK_KING) continue;
                // condition: pawns move*
                if (piece == BLACK_PAWN) {
                    moves.addAll(this.makeBlackPawnMove(field));
                    continue;
                }
                // condition: rooks move
                if (piece == BLACK_ROOK) {
                    moves.addAll(this.genericBlackMoves(field, ROOK_DIRECTIONS, piece));
                    continue;
                }
                // condition: bishops move
                if (piece == BLACK_BISHOP) {
                    moves.addAll(this.genericBlackMoves(field, BISHOP_DIRECTIONS, piece));
                    continue;
                }
                // condition: knights move
                if (piece == BLACK_KNIGHT) {
                    moves.addAll(this.blackSingleMove(field, KNIGHT_DIRECTIONS, piece));
                    continue;
                }
                // condition: kings move
                if (piece == BLACK_KING) {
                    moves.addAll(this.blackSingleMove(field, QUEEN_DIRECTIONS, piece));
                    continue;
                }
                // condition: queens move
                if (piece == BLACK_QUEEN) {
                    moves.addAll(this.genericBlackMoves(field, QUEEN_DIRECTIONS, piece));
                }
            }

        moves.addAll(entPassent);
        moves.addAll(this.getBlackRochade());
        return moves;
    }

    /**
     * Method that handles the black pawn moves.
     * @param field
     * @return
     */
    private ArrayList<ChessBoard> makeBlackPawnMove(int field) {
        //declare vars
        ChessBoard board;
        ArrayList<ChessBoard> followMoves = new ArrayList<>();
        // pawns starts in the forelast line -> starts at 40
        if (field < 40) {
            // condition: pawn could attack
            if (fields[field - 9] > ChessBoard.BLACK_KING) {
                board = this.executeMove(field, field - 9, ChessBoard.BLACK_PAWN);
                // Promotion (transformation from pawn to ...)
                ChessBoard b;
                b = board.Clone();
                b.fields[field - 9] = BLACK_BISHOP;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field - 9) + "b";
                followMoves.add(b);

                b = board.Clone();
                b.fields[field - 9] = BLACK_ROOK;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field - 9) + "r";
                followMoves.add(b);

                b = board.Clone();
                b.fields[field - 9] = BLACK_KNIGHT;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field - 9) + "n";
                followMoves.add(b);

                b = board.Clone();
                b.fields[field - 9] = BLACK_QUEEN;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field - 9) + "q";
                followMoves.add(b);
            }
            // condition: attacking
            if (fields[field - 11] > ChessBoard.BLACK_KING) {
                board = this.executeMove(field, field - 11, ChessBoard.BLACK_PAWN);
                // promotion
                ChessBoard b;

                b = board.Clone();
                b.fields[field - 11] = BLACK_BISHOP;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field - 11) + "b";
                followMoves.add(b);

                b = board.Clone();
                b.fields[field - 11] = BLACK_ROOK;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field - 11) + "r";
                followMoves.add(b);

                b = board.Clone();
                b.fields[field - 11] = BLACK_KNIGHT;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field - 11) + "n";
                followMoves.add(b);

                b = board.Clone();
                b.fields[field - 11] = BLACK_QUEEN;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field - 11) + "q";
                followMoves.add(b);
            }
            // straight forward move
            if (fields[field - 10] == ChessBoard.EMPTY_FIELD) {
                board = this.executeMove(field, field - 10, ChessBoard.BLACK_PAWN);
                //promotion
                ChessBoard b;
                b = board.Clone();
                b.fields[field - 10] = BLACK_BISHOP;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field - 10) + "b";
                followMoves.add(b);
                b = board.Clone();
                b.fields[field - 10] = BLACK_ROOK;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field - 10) + "r";
                followMoves.add(b);
                b = board.Clone();
                b.fields[field - 10] = BLACK_KNIGHT;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field - 10) + "n";
                followMoves.add(b);
                b = board.Clone();
                b.fields[field - 10] = BLACK_QUEEN;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field - 10) + "q";
                followMoves.add(b);
            }
            else
                return followMoves;
        }
        else {
            // condition: check for attack - take
            if (fields[field - 9] > ChessBoard.BLACK_KING) {
                board = this.executeMove(field, field - 9, ChessBoard.BLACK_PAWN);
                followMoves.add(board);
            }
            //check for other take -> direction moves
            if (fields[field - 11] > ChessBoard.BLACK_KING) {
                board = this.executeMove(field, field - 11, ChessBoard.BLACK_PAWN);
                followMoves.add(board);
            }
            // check straight forward move
            if (fields[field - 10] == ChessBoard.EMPTY_FIELD) {
                board = this.executeMove(field, field - 10, ChessBoard.BLACK_PAWN);
                followMoves.add(board);
            } else return followMoves;
        }

        // condition: straight forward move possible - is double forward move available
        if (field / 10 == 8 && fields[field - 20] == ChessBoard.EMPTY_FIELD) {
            board = this.executeMove(field, field - 20, ChessBoard.BLACK_PAWN);
            // condition: en passant taking
            if (fields[field - 19] == ChessBoard.WHITE_PAWN) {
                ChessBoard newBoard = new ChessBoard(board);
                newBoard.fields[field - 20] = ChessBoard.EMPTY_FIELD;
                newBoard.fields[field - 19] = ChessBoard.EMPTY_FIELD;
                newBoard.fields[field - 10] = ChessBoard.WHITE_PAWN;
                newBoard.TurnNotation = ChessBoard.indexToFieldName(field - 19) +
                        ChessBoard.indexToFieldName(field - 10);
                board.entPassent.add(newBoard);
            }
            if (fields[field - 21] == ChessBoard.WHITE_PAWN) {
                ChessBoard newBoard = new ChessBoard(board);
                newBoard.fields[field - 20] = ChessBoard.EMPTY_FIELD;
                newBoard.fields[field - 21] = ChessBoard.EMPTY_FIELD;
                newBoard.fields[field - 10] = ChessBoard.WHITE_PAWN;
                newBoard.TurnNotation = ChessBoard.indexToFieldName(field - 21) +
                        ChessBoard.indexToFieldName(field - 10);
                board.entPassent.add(newBoard);
            }

            followMoves.add(board);

        }
        return followMoves;// list of follow movs
    }

    /**
     *
     * @param field
     * @return
     */
    private ArrayList<ChessBoard> makeWhitePawnMove(int field) {
        // declare vars
        ChessBoard board;
        ArrayList<ChessBoard> followMoves = new ArrayList<>();
        // pawns row start in the forelast top row
        if (field > 79) {
            // condition: check for taking
            if (fields[field + 9] > ChessBoard.EMPTY_FIELD && fields[field + 9] < ChessBoard.WHITE_PAWN) {
                board = this.executeMove(field, field + 9, ChessBoard.WHITE_PAWN);
                // promotion (transformation from pawn to ...)
                ChessBoard b;
                b = board.Clone();
                b.fields[field + 9] = WHITE_BISHOP;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field + 9) + "b";
                followMoves.add(b);
                b = board.Clone();
                b.fields[field + 9] = WHITE_ROOK;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field + 9) + "r";
                followMoves.add(b);
                b = board.Clone();
                b.fields[field + 9] = WHITE_KNIGHT;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field + 9) + "n";
                followMoves.add(b);
                b = board.Clone();
                b.fields[field + 9] = WHITE_QUEEN;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field + 9) + "q";
                followMoves.add(b);
            }
            // condition: check taking in the other direction
            if (fields[field + 11] > ChessBoard.EMPTY_FIELD && fields[field + 11] < ChessBoard.WHITE_PAWN) {
                board = this.executeMove(field, field + 11, ChessBoard.WHITE_PAWN);
                // promotion
                ChessBoard b;
                b = board.Clone();
                b.fields[field + 11] = WHITE_BISHOP;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field + 11) + "b";
                followMoves.add(b);
                b = board.Clone();
                b.fields[field + 11] = WHITE_ROOK;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field + 11) + "r";
                followMoves.add(b);
                b = board.Clone();
                b.fields[field + 11] = WHITE_KNIGHT;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field + 11) + "n";
                followMoves.add(b);
                b = board.Clone();
                b.fields[field + 11] = WHITE_QUEEN;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field + 11) + "q";
                followMoves.add(b);
            }

            // condition: check straight forward move
            if (fields[field + 10] == ChessBoard.EMPTY_FIELD) {
                board = this.executeMove(field, field + 10, ChessBoard.WHITE_PAWN);
                //promotion
                ChessBoard b;
                b = board.Clone();
                b.fields[field + 10] = WHITE_BISHOP;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field + 10) + "b";
                followMoves.add(b);
                b = board.Clone();
                b.fields[field + 10] = WHITE_ROOK;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field + 10) + "r";
                followMoves.add(b);
                b = board.Clone();
                b.fields[field + 10] = WHITE_KNIGHT;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field + 10) + "n";
                followMoves.add(b);
                b = board.Clone();
                b.fields[field + 10] = WHITE_QUEEN;
                b.TurnNotation = ChessBoard.indexToFieldName(field) +
                        ChessBoard.indexToFieldName(field + 10) + "q";
                followMoves.add(b);
            }
            else
                return followMoves;
        }
        else {
            // condition: check taking
            if (fields[field + 9] > EMPTY_FIELD && fields[field + 9] < ChessBoard.WHITE_PAWN) {
                board = this.executeMove(field, field + 9, ChessBoard.WHITE_PAWN);
                followMoves.add(board);
            }
            // condition: check other direction for taking
            if (fields[field + 11] > EMPTY_FIELD && fields[field + 11] < ChessBoard.WHITE_PAWN) {
                board = this.executeMove(field, field + 11, ChessBoard.WHITE_PAWN);
                followMoves.add(board);
            }
            // condition: check straight forward taking
            if (fields[field + 10] == ChessBoard.EMPTY_FIELD) {
                board = this.executeMove(field, field + 10, ChessBoard.WHITE_PAWN);
                followMoves.add(board);
            }
            else
                return followMoves;
        }
        // condition: straight forward move possible - is double forward move available
        if (field / 10 == 3 && fields[field + 20] == ChessBoard.EMPTY_FIELD) {
            board = this.executeMove(field, field + 20, ChessBoard.WHITE_PAWN);
            // condition: en passant taking
            if (fields[field + 19] == ChessBoard.BLACK_PAWN) {
                ChessBoard newBoard = new ChessBoard(board);
                newBoard.fields[field + 20] = ChessBoard.EMPTY_FIELD;
                newBoard.fields[field + 19] = ChessBoard.EMPTY_FIELD;
                newBoard.fields[field + 10] = ChessBoard.BLACK_PAWN;
                newBoard.TurnNotation = ChessBoard.indexToFieldName(field + 19) +
                        ChessBoard.indexToFieldName(field + 10);
                board.entPassent.add(newBoard);
            }
            if (fields[field + 21] == ChessBoard.BLACK_PAWN) {
                ChessBoard newBoard = new ChessBoard(board);
                newBoard.fields[field + 20] = ChessBoard.EMPTY_FIELD;
                newBoard.fields[field + 21] = ChessBoard.EMPTY_FIELD;
                newBoard.fields[field + 10] = ChessBoard.BLACK_PAWN;
                newBoard.TurnNotation = ChessBoard.indexToFieldName(field + 21) +
                        ChessBoard.indexToFieldName(field + 10);
                board.entPassent.add(newBoard);
            }
            followMoves.add(board);
        }
        return followMoves;
    }

    /**
     * Generic move generator for white:
     * @param field
     * @param directions
     * @param piece
     * @return
     */
    private ArrayList<ChessBoard> genericWhiteMoves(int field, int[] directions, byte piece) {
        // declare vars
        ChessBoard board;
        int i = 0;
        int newField;
        ArrayList<ChessBoard> followMoves = new ArrayList<ChessBoard>();

        //double while loop - outer direction, inner empty field
        // for all directions - if direction index
        while (i < directions.length) {
            newField = field + directions[i];// determine field index
            // as long as field is empty
            while (fields[newField] == EMPTY_FIELD) {
                // execute next move - so it can be added to the move list
                board = this.executeMove(field, newField, piece);
                followMoves.add(board);
                newField = newField + directions[i];// determine next fields
            }
            // condition: new field isn`t empty field anymore nor illegal nor white
            if (fields[newField] != ILLEGAL_FIELD && fields[newField] < WHITE_PAWN) {
                board = this.executeMove(field, newField, piece);
                followMoves.add(board);
            }
            i++;// increment into the direction
        }
        return followMoves;
    }

    /**
     * Generic move generator for black!
     * @param field
     * @param directions
     * @param piece
     * @return
     */
    private ArrayList<ChessBoard> genericBlackMoves(int field, int[] directions, byte piece) {
        // declare vars
        ChessBoard board;
        int i = 0;
        int newField;
        ArrayList<ChessBoard> followMoves = new ArrayList<>();

        //double while loop - outer direction, inner empty field
        // for all directions - if direction index
        while (i < directions.length) {
            newField = field + directions[i];// determine field index
            // as long as field is empty
            while (fields[newField] == EMPTY_FIELD) {
                // execute next move - so it can be added to the move list
                board = this.executeMove(field, newField, piece);
                followMoves.add(board);
                newField = newField + directions[i];// determine next fields
            }
            // condition: new field isn`t empty field anymore nor illegal nor black
            if (fields[newField] > BLACK_KING) {
                board = this.executeMove(field, newField, piece);
                followMoves.add(board);
            }
            i++;// increment into the direction
        }
        return followMoves;
    }

    /**
     *
     * @param field
     * @param directions
     * @param piece
     * @return
     */
    private ArrayList<ChessBoard> whiteSingleMove(int field, int[] directions, byte piece) {
        // declare vars
        ChessBoard board;
        int i = 0;
        int newField;
        ArrayList<ChessBoard> followMoves = new ArrayList<>();

        //double while loop - outer direction, inner empty field
        // for all directions - if direction index
        while (i < directions.length) {
            newField = field + directions[i];// determine field index
            // condition: new field isn`t empty field anymore nor illegal nor white
            if (fields[newField] != ILLEGAL_FIELD && fields[newField] < WHITE_PAWN) {
                board = this.executeMove(field, newField, piece);
                followMoves.add(board);
            }
            i++;// increment into the direction
        }
        return followMoves;
    }

    /**
     *
     * @param field
     * @param directions
     * @param piece
     * @return
     */
    private ArrayList<ChessBoard> blackSingleMove(int field, int[] directions, byte piece) {
        // declare vars
        ChessBoard board;
        int i = 0;
        int newField;
        ArrayList<ChessBoard> followMoves = new ArrayList<>();

        //double while loop - outer direction, inner empty field
        // for all directions - if direction index
        while (i < directions.length) {
            newField = field + directions[i];// determine field index
            // condition: new field isn`t empty field anymore nor illegal nor black
            if (fields[newField] > BLACK_KING || fields[newField] == EMPTY_FIELD) {
                board = this.executeMove(field, newField, piece);
                followMoves.add(board);
            }
            i++;// increment into the direction
        }
        return followMoves;
    }

    /**
     *
     * @param oldField
     * @param newField
     * @param piece
     * @return
     */
    private ChessBoard executeMove(int oldField, int newField, byte piece) {
        // declare vars
        ChessBoard board;

        // execute move
        board = new ChessBoard(this);
        board.fields[oldField] = ChessBoard.EMPTY_FIELD;
        // condition: check for check mate
        if (board.fields[newField] == BLACK_KING)
            board.BlackLost = true;
        if (board.fields[newField] == WHITE_KING)
            board.WhiteLost = true;
        board.fields[newField] = piece;
        board.TurnNotation = ChessBoard.indexToFieldName(oldField) +
                ChessBoard.indexToFieldName(newField);
        // condition: check for castling & castling options
        if (piece == WHITE_ROOK) {
            if (oldField == 21)
                board.WhiteCanLongRochade = false;
            if (oldField == 28)
                board.WhiteCanShortRochade = false;
        }
        if (piece == WHITE_KING) {
            board.WhiteCanLongRochade = false;
            board.WhiteCanShortRochade = false;
        }

        if (piece == BLACK_ROOK) {
            if (oldField == 91)
                board.BlackCanLongRochade = false;
            if (oldField == 98)
                board.BlackCanShortRochade = false;
        }
        if (piece == BLACK_KING) {
            board.BlackCanLongRochade = false;
            board.BlackCanShortRochade = false;
        }
        return board;
    }

    /**
     *
     * @param   player      to calculated the quality for
     * @return
     */
    public int getQuality(Player player) {
        // declare vars
        int field;
        int quality = 0;

        // for all legal fields
        // iterate over the board and sum up qualities
        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                field = y * 10 + x;// determine field index
                // if field is empty or illegal -> skip
                if (fields[field] <= EMPTY_FIELD)
                    continue;
                // adjust the piece quality, from the array index, to the board quality
                quality += QUALITIES[fields[field]];
            }
        // condition for black player - negate the quality
        if (player == Player.BLACK)
            quality *= -1;
        return quality;
    }

    /**
     * Method that iterates over the byte board (byte array) and converts
     * each piece of the index to it`s string representative. String must
     * have a length of 64 characters.
     * <li>w - white</li>
     * <li>S - black</li>
     * <li>x - empty legal field</li>
     * <li>B - black pawn</li>
     * <li>b - white pawn</li>
     * <li>l - white bishop</li>
     * <li>L - black bishop</li>
     * <li>s - white knight</li>
     * <li>S - black knight</li>
     * <li>t - white rook</li>
     * <li>T - black Tower</li>
     * <li>d - white queen</li>
     * <li>D - black queen</li>
     * <li>k - white king</li>
     * <li>K - black king</li>
     * @return String holding the whole chessboard as a string.
     */
    public String getStringRepresentation() {
        // declare vars
        String game = "";
        int field;
        //iterate over the chessboard
        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                field = y * 10 + x;// determine the field index
                game += NAMES[fields[field]];// concat board with pieces of the board
            }
        // attach players colour
        if (playerToMakeTurn == Player.WHITE)
            game += "w";
        else
            game += "S";
        return game;
    }

    /**
     *
     * @param s
     */
    public void loadFromString(String s) {
        char[] c = new char[65];
        c = s.toCharArray();
        int j = 0;
        // condition: if player is white
        if (c[64] == 'w') {
            playerToMakeTurn = Player.WHITE;
            c[64] = ' ';
        }
        else {
            playerToMakeTurn = Player.BLACK;
            c[64] = ' ';
        }
        // iterate over the field and set illegal fields
        for (int i = 0; i < 120; i++) {
            if (i > 98 || i < 21 || i % 10 == 0 || i % 10 == 9) {
                fields[i] = ILLEGAL_FIELD;
            }
            else
                switch (c[j]) {
                    case ('x'):
                        fields[i] = EMPTY_FIELD;
                        j++;
                        break;
                    case ('b'):
                        fields[i] = WHITE_PAWN;
                        j++;
                        break;
                    case ('B'):
                        fields[i] = BLACK_PAWN;
                        j++;
                        break;
                    case ('t'):
                        fields[i] = WHITE_ROOK;
                        j++;
                        break;
                    case ('T'):
                        fields[i] = BLACK_ROOK;
                        j++;
                        break;
                    case ('s'):
                        fields[i] = WHITE_KNIGHT;
                        j++;
                        break;
                    case ('S'):
                        fields[i] = BLACK_KNIGHT;
                        j++;
                        break;
                    case ('l'):
                        fields[i] = WHITE_BISHOP;
                        j++;
                        break;
                    case ('L'):
                        fields[i] = BLACK_BISHOP;
                        j++;
                        break;
                    case ('d'):
                        fields[i] = WHITE_QUEEN;
                        j++;
                        break;
                    case ('D'):
                        fields[i] = BLACK_QUEEN;
                        j++;
                        break;
                    case ('k'):
                        fields[i] = WHITE_KING;
                        j++;
                        break;
                    case ('K'):
                        fields[i] = BLACK_KING;
                        j++;
                        break;
                }
        }
    }

    /**
     * Method that takes the board and generates a hash value out of it
     * @return
     */
    public String getHash() {
        // declare vars
        String hash = "";
        int empty = 0;
        int field;
        // for all legal fields
        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                field = y * 10 + x;// determine field index
                // condition: empty fields
                if (fields[field] <= EMPTY_FIELD)
                    empty++;
                else {
                    if (empty >= 2)
                        hash += empty + "m";
                    else if (empty == 1)
                        hash += "l";
                    empty = 0;
                    hash += fields[field];
                }
            }
        return hash;
    }

    /**
     *
     * @param   turn        String as turn to do
     * @return
     * @throws Exception
     */
    public IChessGame makeTurn(String turn) throws Exception {
        ArrayList<IChessGame> nextTurns = this.getNextTurns();
        for (int i = 0; i < nextTurns.size(); i++) {
            if (turn.equals(nextTurns.get(i).getTurnNotation()))
                return nextTurns.get(i);
        }
        throw new Exception(turn + " :Move not found in the list of legal moves!;");
    }

    /**
     * Method that decides if a move is a legal move, by iterating over
     * the list of available boards, if a move is enlisted in the ArrayList, then
     * it is a legal move, otherwise not.
     * @param   board current board
     * @return  boolean that holds the value if game is enlisted or not
     */
    public boolean isLegalMove(ChessBoard board) {
        // declare vars
        ArrayList<IChessGame> nextBoards;
        nextBoards = this.getNextTurns();// compute following moves
        // checking if board is contained in the follow moves list
        for (int i = 0; i < nextBoards.size(); i++) {
            if (((ChessBoard) nextBoards.get(i)).equals(board))
                return true;
        }
        return false;
    }

    /**
     * Method that compares a given chessboard
     * @param board
     * @return
     */
    public boolean equals(ChessBoard board) {
        // iterate over the board and compare each field
        for (int i = 0; i < this.fields.length; i++) {
            if (board.fields[i] != this.fields[i])
                return false;
        }
        // condition: check if the player has the right colour
        if (this.playerToMakeTurn != board.playerToMakeTurn)
            return false;
        return true;
    }

    /**
     * Method for cloning a board - will create a new
     * ChessBoard object and clone it`s settings to the
     * new one. The returned ChessBoard is equal but note the same
     * as the original one.
     * @return      CheesBoard  same as the current board, but put into
     * a new object.
     */
    private ChessBoard Clone() {
        ChessBoard board = new ChessBoard();
        board.fields = this.fields.clone();
        board.playerToMakeTurn = this.playerToMakeTurn;
        board.BlackCanLongRochade = this.BlackCanLongRochade;
        board.BlackCanShortRochade = this.BlackCanShortRochade;
        board.WhiteCanLongRochade = this.WhiteCanLongRochade;
        board.WhiteCanShortRochade = this.WhiteCanShortRochade;
        board.entPassent = new ArrayList<>();
        board.entPassent.addAll(this.entPassent);
        return board;
    }

    /**
     * Method that checks if a castling is available and if so it
     * executes the castling in one of both variants.
     * @return ArrayList containing games
     */
    ArrayList<IChessGame> getWhiteRochade() {
        // declare or init vars
        int field;
        ArrayList<IChessGame> rochadeList = new ArrayList<IChessGame>();

        // condition: check for short/ king side castling possible
        if (WhiteCanShortRochade) {
            // condition: if fields between king and rook are empty
            if (fields[26] == EMPTY_FIELD && fields[27] == EMPTY_FIELD
                    && fields[28] == WHITE_ROOK) {
                field = 25;
                // condition: check if is field attacked by opponent
                while (field < 28 && !IsFieldAttackedByBlack(field)) {
                    field++;
                }
                // condition: if no field in between king and rook is attacked by the opponent
                if (field == 28) {
                    // execute castling
                    ChessBoard b = this.Clone();
                    b.fields[26] = ChessBoard.WHITE_ROOK;
                    b.fields[27] = ChessBoard.WHITE_KING;
                    b.fields[25] = ChessBoard.EMPTY_FIELD;
                    b.fields[28] = ChessBoard.EMPTY_FIELD;
                    b.TurnNotation = "e1g1";
                    b.WhiteCanLongRochade = false;
                    b.WhiteCanShortRochade = false;
                    b.playerToMakeTurn = Player.BLACK;
                    rochadeList.add(b);
                }
            }
        }
        if (WhiteCanLongRochade) {
            // condition: check for long/ queen side castling possible
            if (fields[24] == EMPTY_FIELD && fields[23] == EMPTY_FIELD && fields[22] == EMPTY_FIELD && fields[21] == WHITE_ROOK) {
                field = 25;
                // condition: check if is field attacked by opponent
                while (field > 22 && !IsFieldAttackedByBlack(field)) {
                    field--;
                }
                // condition: if no field in between king and rook is attacked by the opponent
                if (field == 22) {
                    // execute castling
                    ChessBoard b = this.Clone();
                    b.fields[24] = ChessBoard.WHITE_ROOK;
                    b.fields[23] = ChessBoard.WHITE_KING;
                    b.fields[25] = ChessBoard.EMPTY_FIELD;
                    b.fields[21] = ChessBoard.EMPTY_FIELD;
                    b.TurnNotation = "e1c1";
                    b.WhiteCanLongRochade = false;
                    b.WhiteCanShortRochade = false;
                    b.playerToMakeTurn = Player.BLACK;
                    rochadeList.add(b);
                }
            }
        }
        return rochadeList;
    }


    /**
     * Getter
     * @return
     */
    ArrayList<IChessGame> getBlackRochade() {
        // declare or init vars
        ChessBoard b;
        int field;
        ArrayList<IChessGame> rochadeList = new ArrayList<>();

        // condition: check for short/ king side castling possible
        if (BlackCanShortRochade) {
            // condition: if fields between king and rook are empty
            if (fields[96] == EMPTY_FIELD && fields[97] == EMPTY_FIELD && fields[98] == BLACK_ROOK) {
                field = 95;
                // condition: check if is field attacked by opponent
                while (field < 98 && !IsFieldAttackedByWhite(field)) {
                    field++;
                }
                // condition: if no field in between king and rook is attacked by the opponent
                if (field == 98) {
                    // execute castling
                    b = this.Clone();
                    b.fields[96] = ChessBoard.BLACK_ROOK;
                    b.fields[97] = ChessBoard.BLACK_KING;
                    b.fields[95] = ChessBoard.EMPTY_FIELD;
                    b.fields[98] = ChessBoard.EMPTY_FIELD;
                    b.TurnNotation = "e8g8";
                    b.BlackCanLongRochade = false;
                    b.BlackCanShortRochade = false;
                    b.playerToMakeTurn = Player.WHITE;
                    rochadeList.add(b);
                }
            }
        }
        if (BlackCanLongRochade) {
            // condition: check for long/ queen side castling possible
            if (fields[94] == EMPTY_FIELD && fields[93] == EMPTY_FIELD && fields[92] == EMPTY_FIELD && fields[91] == BLACK_ROOK) {
                field = 95;
                // condition: check if is field attacked by opponent
                while (field > 92 && !IsFieldAttackedByWhite(field)) {
                    field--;
                }
                // condition: if no field in between king and rook is attacked by the opponent
                if (field == 92) {
                    // execute castling
                    b = this.Clone();
                    b.fields[94] = ChessBoard.BLACK_ROOK;
                    b.fields[93] = ChessBoard.BLACK_KING;
                    b.fields[95] = ChessBoard.EMPTY_FIELD;
                    b.fields[91] = ChessBoard.EMPTY_FIELD;
                    b.TurnNotation = "e8c8";
                    b.BlackCanLongRochade = false;
                    b.BlackCanShortRochade = false;
                    b.playerToMakeTurn = Player.WHITE;
                    rochadeList.add(b);
                }
            }
        }
        return rochadeList;
    }

    /**
     * Method that checks if a certain field is attacked by the black opponent.
     * @param field
     * @return
     */
    public boolean IsFieldAttackedByBlack(int field) {
        // declare vars
        byte piece;
        int u;
        // condition: is a pawn able to attack/ take another piece
        if (fields[field + 9] == BLACK_PAWN || fields[field + 11] == BLACK_PAWN)
            return true;

        // condition: is a knight able to attack/ take another piece
        for (int i = 0; i < KNIGHT_DIRECTIONS.length; i++) {
            if (fields[field + KNIGHT_DIRECTIONS[i]] == BLACK_KNIGHT)
                return true;
        }
        // condition: check for directions that a piece could be attacked by rook
        for (int i = 0; i < ROOK_DIRECTIONS.length; i++) {
            u = 1;
            // as long
            while ((piece = fields[field + ROOK_DIRECTIONS[i] * u]) == EMPTY_FIELD)
                u++;
            // condition: piece is in attack direction of rook or queen
            if (piece == BLACK_QUEEN || piece == BLACK_ROOK)
                return true;
            // condition: if king is only one field away from piece
            if (piece == BLACK_KING && u == 1)
                return true;
        }
        // condition: check for direction of the bishop
        for (int i = 0; i < ChessBoard.BISHOP_DIRECTIONS.length; i++) {
            u = 1;
            while ((piece = fields[field + BISHOP_DIRECTIONS[i] * u]) == EMPTY_FIELD) u++;
            // condition: is piece in direction of queen or bishop
            if (piece == BLACK_QUEEN || piece == BLACK_BISHOP)
                return true;
            // condition: if king is only one field away from piece
            if (piece == BLACK_KING && u == 1)
                return true;
        }
        // if none of the above cases matches, pieces is not attacked
        return false;
    }


    /**
     * Method that checks if a certain field is attacked by the white opponent.
     * @param field
     * @return
     */
    public boolean IsFieldAttackedByWhite(int field) {
        // declare variables
        byte piece;
        int u;
        // condition: is a pawn able to attack/ take another piece
        if (fields[field - 9] == WHITE_PAWN || fields[field - 11] == WHITE_PAWN)
            return true;
        // condition: is a knight able to attack/ take another piece
        for (int i = 0; i < KNIGHT_DIRECTIONS.length; i++) {
            if (fields[field + KNIGHT_DIRECTIONS[i]] == WHITE_KNIGHT)
                return true;
        }
        // condition: check for directions that a piece could be attacked by rook
        for (int i = 0; i < ROOK_DIRECTIONS.length; i++) {
            u = 1;
            while ((piece = fields[field + ROOK_DIRECTIONS[i] * u]) == EMPTY_FIELD) u++;
            // condition: piece is in attack direction of rook or queen
            if (piece == WHITE_QUEEN || piece == WHITE_ROOK)
                return true;
            // condition: if king is only one field away from piece
            if (piece == WHITE_KING && u == 1)
                return true;
        }
        // condition: check for direction of the bishop
        for (int i = 0; i < ChessBoard.BISHOP_DIRECTIONS.length; i++) {
            u = 1;
            while ((piece = fields[field + BISHOP_DIRECTIONS[i] * u]) == EMPTY_FIELD) u++;
            // condition: is piece in direction of queen or bishop
            if (piece == WHITE_QUEEN || piece == WHITE_BISHOP)
                return true;
            // condition: if king is only one field away from piece
            if (piece == WHITE_KING && u == 1)
                return true;
        }
        // if none of the above cases matches, pieces is not attacked
        return false;
    }

    /**
     *
     * @return
     */
    public String toDebug() {
        String s = "";

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                s += fields[y * 10 + x] + " ";
            }
            s += "\n";
        }

        return s;
    }

    /**
     *
     * @param o
     * @return
     */
    public int compareTo(IChessGame o) {
        return this.heuristicValue - o.getHeuristicValue();
    }

    /**
     *
     * @return
     */
    public int getHeuristicValue() {
        return heuristicValue;
    }


    public String getTurnNotation() {
        return this.TurnNotation;
    }

    /**
     *
     */
    public void addRound() {
        this.round_counter++;
    }

    /**
     * Getter method to calculate the current round in the match.
     * @return integer with the round number.
     */
    public int getRound() {
        return round_counter;
    }

    /**
     * Method that get you the position/ index of the king. If the
     * king is not found, it return -1.
     * @return integer value that contains the position of the king.
     * @param player
     * @return
     */
    private int getKing(Player player) {
        for (int i = 19; i < 100; i++) {
            if (fields[i] == WHITE_KING && player == Player.WHITE)
                return i;
            if (fields[i] == BLACK_KING && player == Player.BLACK)
                return i;
        }
        return -1;
    }

    /**
     *
     * @return
     */
    public GameState getGameState() {
        // declare vars
        int kingsField;
        ChessBoard nextTurn;
        ArrayList<IChessGame> nextGames;
        boolean isKingAttacked = false;//init as false
        boolean hasEscape = false;//

        // condition: check if board is in legal state
        if (!this.isLegalBoard())
            return GameState.ILLEGAL;

        // take care, that following moves will be known
        nextGames = this.getNextTurns();

        // condition: if there are no boards, game is draw
        if (nextGames.size() == 0)
            return GameState.DRAW;

        // Check for other check mate or draws!
        // condition: check if king is attacked
        if (playerToMakeTurn == Player.WHITE) {
            if (IsFieldAttackedByBlack(this.getKing(Player.WHITE)))
                isKingAttacked = true;
        }
        else {
            if (IsFieldAttackedByWhite(this.getKing(Player.BLACK)))
                isKingAttacked = true;
        }

        // condition: check all fields to determine current state for the king
        for (int i = 0; i < nextGames.size(); i++) {
            nextTurn = (ChessBoard) (nextGames.get(i));// determine successors
            // get the field index of the king
            kingsField = nextTurn.getKing(this.playerToMakeTurn);
            // if king is not available
            if (kingsField == -1)
                continue;
            // condition: check is there an escape if king is attacked
            if (this.playerToMakeTurn == Player.WHITE) {
                if (!nextTurn.IsFieldAttackedByBlack(kingsField))
                    hasEscape = true;
            }
            else {
                if (!nextTurn.IsFieldAttackedByWhite(kingsField))
                    hasEscape = true;
            }
        }

        //CHECK MATE EVALUATION!
        // condition: king is attacked and no escpae -> check mate
        if (isKingAttacked && !hasEscape)
            return GameState.MATT;
        // condition: king is not attacked, but has no escpae -> patt
        if (!hasEscape)
            return GameState.DRAW;
        // if nothing from the above conditions matches, the game goes on
        return GameState.LEGAL;
    }

    /**
     * isLegalBoard checks if a given board, this board, has a legal
     * status. So if the board is illegal, the game will end, because some rules
     * of a chess match are violated
     * @return boolean that will say whether the board is okay or not.
     */
    public boolean isLegalBoard() {
        int kingsField;
        // condition: it`s whites turn
        if (this.playerToMakeTurn == Player.WHITE) {
            kingsField = this.getKing(Player.BLACK);// get kings position
            // condition: no king no legal game
            if (kingsField == -1)
                return false;
            // condition: king is attacked - its not a legal board, for next games
            if (this.IsFieldAttackedByWhite(kingsField))
                return false;
        }
        else {
            kingsField = this.getKing(Player.WHITE);
            // condition: no king no legal game
            if (kingsField == -1)
                return false;
            // condition: king is attacked - its not a legal board, for next games
            if (this.IsFieldAttackedByBlack(kingsField))
                return false;
        }
        // if nothing from above matches, the game board is a legal board
        return true;
    }

    /**
     * Setter method that just set bool flags for casling options.
     * @param   k_Castling  boolean if white king side castling (short castling) can be done
     * @param   q_Castling  boolean if white queen side castling (long castling) can be done
     * @param   K_Castling  boolean if black king side castling (short castling) can be done
     * @param   Q_Castling  boolean if black queen side castling (long castling) can be done
     */
    public void setRochade(boolean k_Castling, boolean q_Castling, boolean K_Castling, boolean Q_Castling) {
        BlackCanLongRochade = Q_Castling;
        BlackCanShortRochade = K_Castling;
        WhiteCanLongRochade = q_Castling;
        WhiteCanShortRochade = k_Castling;
    }

    /**
     *
     * @return
     */
    public int getQ() {
        int q = 0;
        int field;

        // condtion: for all columns
        for (int x = 1; x < 9; x++) {
            // condition: for all rows
            for (int y = 2; y < 10; y++) {
                field = y * 10 + x;
                q += QUALITIES[this.fields[field]];//sums up the quality of each field
            }
        }
        return q;
    }

    /**
     * Getter the will deliver the integer value that holds the current value of the round.
     * @return integer
     */
    public int getTurnsMade() {
        return round_counter;
    }


    /**
     * Getter for hasBlackLost
     * @return boolean hasBlackLost
     */
    public boolean hasBlackLost() {
        return BlackLost;
    }

    /**
     * Getter for hasWhiteLost
     * @return boolean hasWhiteLost
     */
    public boolean hasWhiteLost() {
        return WhiteLost;
    }

    /**
     * Return the parent off the game
     * <p>
     * This method returns null if the parent is not set or does not exists. Beware that not every IChessGame needs
     * to have an parent
     * </p>
     *
     * @return IChessGame parent of the board
     */
    @Override
    public IChessGame getParent() {
        if (this.parent == null) {
            return null;
        } else {
            return this.parent;
        }
    }

    /**
     * Set the parent board to the IChessGame
     * <p>
     * You are able to set a chain of children of a board by setting a parent which having a parent itself
     * </p>
     *
     * @param parent IChessGame parent
     */
    @Override
    public void setParent(IChessGame parent) {
        this.parent = parent;
    }
}
