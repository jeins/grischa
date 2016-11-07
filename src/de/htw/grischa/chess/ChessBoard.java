package de.htw.grischa.chess;

import de.htw.grischa.chess.database.client.DatabaseEntry;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Chessboard implementation, implements IChessGame
 * <h3>Version History</h3>
 * <ul>
 * <li> 1.0 - 12/09 - Heim - Initial Version</li>
 * <li> 1.? - 05/10 - Heim - ???</li>
 * <li> 1.3 - 06/14 - Karsten Kochan - Added toDatabase method, added parent</li>
 * </ul>
 *
 * @author Heim
 * @version 1.3
 * @see de.htw.grischa.chess.IChessGame
 */
public class ChessBoard implements IChessGame, Serializable {
    public static final byte EMPTY_FIELD = 0;
    public static final byte ILLEGAL_FIELD = -1;
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
    /**
     * Logger
     */
    private final static Logger log = Logger.getLogger(ChessBoard.class);
    private static final String[] NAMES = {"x", "", "B", "S", "L", "T", "D", "K", "", "", "", "",
            "b", "s", "l", "t", "d", "k"};
    private static final short[] QUALITIES = {0, 0, -1, -3, -3, -5, -9, -100, 0, 0, 0, 0, 1, 3, 3, 5, 9, 100};
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
     * Constructor
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

        entPassent = new ArrayList<IChessGame>();
        //BlackCanRochade=true;
        //WhiteCanRochade=true;
    }

    /**
     * Constructor from existing board
     *
     * @param oldBoard Chessboard to clone
     */
    private ChessBoard(ChessBoard oldBoard) {
        this.fields = oldBoard.fields.clone();
        entPassent = new ArrayList<IChessGame>();

        this.BlackCanLongRochade = oldBoard.BlackCanLongRochade;
        this.BlackCanShortRochade = oldBoard.BlackCanShortRochade;
        this.WhiteCanLongRochade = oldBoard.WhiteCanLongRochade;
        this.WhiteCanShortRochade = oldBoard.WhiteCanShortRochade;

        this.round_counter = oldBoard.round_counter + 1;

        this.BlackLost = oldBoard.BlackLost;
        this.WhiteLost = oldBoard.WhiteLost;

        if (oldBoard.playerToMakeTurn == Player.BLACK) playerToMakeTurn = Player.WHITE;
        else playerToMakeTurn = Player.BLACK;
    }

    /**
     * Provides the standard starting position as Chessboard
     *
     * @return ChessBoard with starting position
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
     *
     * @param in String to convert
     * @return int representation
     * @throws java.lang.IllegalArgumentException if input String is invalid
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


    /************************************************************************************/
    /************************* Prozedur: initializeFields *******************************/

    /**
     * ********************************************************************************
     */

    public int parseField(int position) {
        //*** linken und rechten rand beruecksichtigen ***********************************
        int realPosition = position / 8 * 10;
        realPosition += position % 8 + 1;

        //*** unteren Rand ueberspringen *************************************************
        realPosition += 20;

        return realPosition;
    }


    /************************************************************************************/
    /************************** Funktion: getNextTurns **********************************/

    /**
     * Gibt ein halbwegs lesbares Brett zur�ck schwarze Figuren sind gro�, wei�e
     * klein geschrieben
     */
    public String getReadableString() {
        //*** Variablen-Deklaration ******************************************************
        String s = "";
        int i = 0;

        for (int dy = 10; dy > 1; dy--) {
            if (dy == 10) s += "  ";
            else s += dy - 1 + " ";
            for (int dx = 1; dx < 9; dx++) {
                if (dy == 10) s += (char) ('A' + dx - 1) + " ";

                i = dy * 10 + dx;
                //*** Figuren eintragen ******************************************************
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

        //*** String zurueckgeben ********************************************************
        return s;
    }


    /************************************************************************************/
    /************************** Funktion: makeBlackMoves ********************************/

    /**
     * ********************************************************************************
     */

    private void initializeFields() {
        //*** Einzelne Felder initializieren *********************************************

        //*** Unterer Rand ***************************************************************
        for (int i = 0; i < 20; i++) {
            fields[i] = ChessBoard.ILLEGAL_FIELD;
        }
        //*** Mittlerer Teil mit leeren Feldern ******************************************
        for (int i = 20; i < 100; i++) {
            if (i % 10 == 0 || i % 10 == 9) fields[i] = ChessBoard.ILLEGAL_FIELD;
            else fields[i] = ChessBoard.EMPTY_FIELD;
        }
        //*** Oberer Rand ****************************************************************
        for (int i = 100; i < 120; i++) {
            fields[i] = ChessBoard.ILLEGAL_FIELD;
        }
    }


    /************************************************************************************/
    /************************** Funktion: makeWhiteMoves ********************************/

    /**
     * ********************************************************************************
     */

    public ArrayList<IChessGame> getNextTurns() {
        //	if(nextTurns!=null) return nextTurns;
        if (playerToMakeTurn == Player.WHITE) {
            return this.makeWhiteMoves();
        } else {
            return this.makeBlackMoves();
        }
    }


    /************************************************************************************/
    /************************ Funktion: makeBlackPawnMove *******************************/

    /**
     * ********************************************************************************
     */

    private ArrayList<IChessGame> makeWhiteMoves() {
        //*** Variablen-Deklaration ******************************************************
        int field;
        byte piece;
        ArrayList<IChessGame> moves;

        //*** Liste initialisieren *******************************************************
        moves = new ArrayList<IChessGame>();

        //*** F�r alle legalen Felder ****************************************************
        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                //*** Feldindex bestimmen ****************************************************
                field = y * 10 + x;

                //*** Figur bestimmen ********************************************************
                piece = fields[field];

                //*** Auf Fels ist leer undgueltig und von schwarz besetzt ragieren **********
                if (piece < WHITE_PAWN) continue;

                //*** Bauern Zug machen ******************************************************
                if (piece == WHITE_PAWN) {
                    moves.addAll(this.makeWhitePawnMove(field));
                    continue;
                }

                //*** Turm Zug machen ********************************************************
                if (piece == WHITE_ROOK) {
                    moves.addAll(this.genericWhiteMoves(field, ROOK_DIRECTIONS, piece));
                    continue;
                }

                //*** Laeufer Zug machen *****************************************************
                if (piece == WHITE_BISHOP) {
                    moves.addAll(this.genericWhiteMoves(field, BISHOP_DIRECTIONS, piece));
                    continue;
                }

                //*** Springer ziehen ********************************************************
                if (piece == WHITE_KNIGHT) {
                    moves.addAll(this.whiteSingleMove(field, KNIGHT_DIRECTIONS, piece));
                    continue;
                }

                //*** Koenig ziehen **********************************************************
                if (piece == WHITE_KING) {
                    moves.addAll(this.whiteSingleMove(field, QUEEN_DIRECTIONS, piece));
                    continue;
                }

                //*** Dame ziehen ************************************************************
                if (piece == WHITE_QUEEN) {
                    moves.addAll(this.genericWhiteMoves(field, QUEEN_DIRECTIONS, piece));
                }
            }

        moves.addAll(entPassent);
        moves.addAll(this.getWhiteRochade());

        //** Züge merken *******************************************************
        //nextTurns=moves;
        //*** Zuege zurueckgeben *********************************************************
        return moves;
    }


    /************************************************************************************/
    /************************ Funktion: makeWhitePawnMove *******************************/

    /**
     * ********************************************************************************
     */

    private ArrayList<IChessGame> makeBlackMoves() {
        //*** Variablen-Deklaration ******************************************************
        int field;
        byte piece;
        ArrayList<IChessGame> moves;

        //*** Liste initialisieren *******************************************************
        moves = new ArrayList<IChessGame>();


        //*** F�r alle legalen Felder ****************************************************
        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                //*** Feldindex bestimmen ****************************************************
                field = y * 10 + x;

                //*** Figur bestimmen ********************************************************
                piece = fields[field];

                //*** Auf Fels ist leer undgueltig und von wei� besetzt ragieren *************
                if (piece < BLACK_PAWN && piece >= BLACK_KING) continue;

                //*** Bauern Zug machen ******************************************************
                if (piece == BLACK_PAWN) {
                    moves.addAll(this.makeBlackPawnMove(field));
                    continue;
                }

                //*** Turm ziehen ************************************************************
                if (piece == BLACK_ROOK) {
                    moves.addAll(this.genericBlackMoves(field, ROOK_DIRECTIONS, piece));
                    continue;
                }

                //*** Laeufer Zug machen *****************************************************
                if (piece == BLACK_BISHOP) {
                    moves.addAll(this.genericBlackMoves(field, BISHOP_DIRECTIONS, piece));
                    continue;
                }

                //*** Springer ziehen ********************************************************
                if (piece == BLACK_KNIGHT) {
                    moves.addAll(this.blackSingleMove(field, KNIGHT_DIRECTIONS, piece));
                    continue;
                }

                //*** Koenig ziehen **********************************************************
                if (piece == BLACK_KING) {
                    moves.addAll(this.blackSingleMove(field, QUEEN_DIRECTIONS, piece));
                    continue;
                }

                //*** Dame ziehen ************************************************************
                if (piece == BLACK_QUEEN) {
                    moves.addAll(this.genericBlackMoves(field, QUEEN_DIRECTIONS, piece));
                }
            }

        moves.addAll(entPassent);
        moves.addAll(this.getBlackRochade());

        //** Züge merken *******************************************************
        //	nextTurns=moves;

        //*** Zuege zurueckgeben *********************************************************
        return moves;
    }


    /************************************************************************************/
    /************************* Funktion: genericWhiteMoves ******************************/

    /**
     * ********************************************************************************
     */

    private ArrayList<ChessBoard> makeBlackPawnMove(int field) {
        //*** Variablen-Deklaration ******************************************************
        ArrayList<ChessBoard> followMoves;
        ChessBoard board;

        //*** Liste initialisieren *******************************************************
        followMoves = new ArrayList<ChessBoard>();

        //*** Bauer in vorletzter Reihe **************************************************
        if (field < 40) {
            //*** Wurf �berpr�fen ********************************************************
            if (fields[field - 9] > ChessBoard.BLACK_KING) {
                board = this.executeMove(field, field - 9, ChessBoard.BLACK_PAWN);
                //*** Figurentausch durchf�hren ******************************************
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

            //*** Wurf in andere Richtung �berpr�fen *************************************
            if (fields[field - 11] > ChessBoard.BLACK_KING) {
                board = this.executeMove(field, field - 11, ChessBoard.BLACK_PAWN);
                //*** Figurentausch durchf�hren ******************************************
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

            //*** Gerade aus Zug pr�fen **************************************************
            if (fields[field - 10] == ChessBoard.EMPTY_FIELD) {
                board = this.executeMove(field, field - 10, ChessBoard.BLACK_PAWN);
                //*** Figurentausch durchf�hren ******************************************
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
            } else return followMoves;
        } else {
            //*** Wurf �berpr�fen ********************************************************
            if (fields[field - 9] > ChessBoard.BLACK_KING) {
                board = this.executeMove(field, field - 9, ChessBoard.BLACK_PAWN);
                followMoves.add(board);
            }

            //*** Wurf in andere Richtung �berpr�fen *************************************
            if (fields[field - 11] > ChessBoard.BLACK_KING) {
                board = this.executeMove(field, field - 11, ChessBoard.BLACK_PAWN);
                followMoves.add(board);
            }

            //*** Gerade aus Zug pr�fen **************************************************
            if (fields[field - 10] == ChessBoard.EMPTY_FIELD) {
                board = this.executeMove(field, field - 10, ChessBoard.BLACK_PAWN);
                followMoves.add(board);
            } else return followMoves;
        }

        //*** Wenn gerade aus Zug moeglich war auch doppelt geradeaus pruefen ************
        if (field / 10 == 8 && fields[field - 20] == ChessBoard.EMPTY_FIELD) {
            board = this.executeMove(field, field - 20, ChessBoard.BLACK_PAWN);

            //*** En Passent schlagen bearbeiten *****************************************
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
        //*** Liste zur�ckgeben **********************************************************
        return followMoves;
    }


    /************************************************************************************/
    /************************* Funktion: genericBlackMoves ******************************/

    /**
     * ********************************************************************************
     */

    private ArrayList<ChessBoard> makeWhitePawnMove(int field) {
        //*** Variablen-Deklaration ******************************************************
        ArrayList<ChessBoard> followMoves;
        ChessBoard board;

        //*** Liste initialisieren *******************************************************
        followMoves = new ArrayList<ChessBoard>();

        //*** Bauer in vorletzter Reihe **************************************************
        if (field > 79) {
            //*** Wurf �berpr�fen ********************************************************
            if (fields[field + 9] > ChessBoard.EMPTY_FIELD && fields[field + 9] < ChessBoard.WHITE_PAWN) {
                board = this.executeMove(field, field + 9, ChessBoard.WHITE_PAWN);
                //*** Figurentausch durchf�hren ******************************************
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

            //*** Wurf in andere Richtung �berpr�fen *************************************
            if (fields[field + 11] > ChessBoard.EMPTY_FIELD && fields[field + 11] < ChessBoard.WHITE_PAWN) {
                board = this.executeMove(field, field + 11, ChessBoard.WHITE_PAWN);
                //*** Figurentausch durchf�hren ******************************************
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

            //*** Gerade aus Zug pr�fen **************************************************
            if (fields[field + 10] == ChessBoard.EMPTY_FIELD) {
                board = this.executeMove(field, field + 10, ChessBoard.WHITE_PAWN);
                //*** Figurentausch durchf�hren ******************************************
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
            } else return followMoves;
        } else {
            //*** Wurf �berpr�fen ********************************************************
            if (fields[field + 9] > EMPTY_FIELD && fields[field + 9] < ChessBoard.WHITE_PAWN) {
                board = this.executeMove(field, field + 9, ChessBoard.WHITE_PAWN);
                followMoves.add(board);
            }

            //*** Wurf in andere Richtung �berpr�fen *************************************
            if (fields[field + 11] > EMPTY_FIELD && fields[field + 11] < ChessBoard.WHITE_PAWN) {
                board = this.executeMove(field, field + 11, ChessBoard.WHITE_PAWN);
                followMoves.add(board);
            }

            //*** Gerade aus Zug pr�fen **************************************************
            if (fields[field + 10] == ChessBoard.EMPTY_FIELD) {
                board = this.executeMove(field, field + 10, ChessBoard.WHITE_PAWN);
                followMoves.add(board);
            } else return followMoves;
        }

        //*** Wenn gerade aus Zug moeglich war auch doppelt geradeaus pruefen ************
        if (field / 10 == 3 && fields[field + 20] == ChessBoard.EMPTY_FIELD) {
            board = this.executeMove(field, field + 20, ChessBoard.WHITE_PAWN);

            //*** En Passent schlagen bearbeiten *****************************************
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

        //*** Liste zur�ckgeben **********************************************************
        return followMoves;
    }


    /************************************************************************************/
    /************************** Funktion: whiteSingleMove *******************************/

    /**
     * ********************************************************************************
     */

    private ArrayList<ChessBoard> genericWhiteMoves(int field, int[] directions, byte piece) {
        //*** Variablen-Deklaration ******************************************************
        ArrayList<ChessBoard> followMoves;
        ChessBoard board;
        int i = 0;
        int newField;

        //*** Liste initialisieren *******************************************************
        followMoves = new ArrayList<ChessBoard>();

        //*** F�r alle Richtungen ********************************************************
        while (i < directions.length) {
            //*** n�chstes Feld bestimmen ************************************************
            newField = field + directions[i];

            //*** Solange n�chstes Feld frei ist *****************************************
            while (fields[newField] == EMPTY_FIELD) {
                //*** Zug ausf�hren ******************************************************
                board = this.executeMove(field, newField, piece);
                followMoves.add(board);

                //*** n�chstes Feld bestimmen ********************************************
                newField = newField + directions[i];
            }
            //*** Neues Feld ist kein leeres Feld mehr und weder illegal noch wei� *******
            if (fields[newField] != ILLEGAL_FIELD && fields[newField] < WHITE_PAWN) {
                board = this.executeMove(field, newField, piece);
                followMoves.add(board);
            }

            //*** Richtungsz�hler erh�hen ************************************************
            i++;
        }

        //*** Liste zur�ckgeben **********************************************************
        return followMoves;
    }


    /************************************************************************************/
    /************************* Funktion: blackSingleMove ********************************/

    /**
     * ********************************************************************************
     */

    private ArrayList<ChessBoard> genericBlackMoves(int field, int[] directions, byte piece) {
        //*** Variablen-Deklaration ******************************************************
        ArrayList<ChessBoard> followMoves;
        ChessBoard board;
        int i = 0;
        int newField;

        //*** Liste initialisieren *******************************************************
        followMoves = new ArrayList<ChessBoard>();

        //*** F�r alle Richtungen ********************************************************
        while (i < directions.length) {
            //*** n�chstes Feld bestimmen ************************************************
            newField = field + directions[i];

            //*** Solange n�chstes Feld frei ist *****************************************
            while (fields[newField] == EMPTY_FIELD) {
                //*** Zug ausf�hren ******************************************************
                board = this.executeMove(field, newField, piece);
                followMoves.add(board);

                //*** n�chstes Feld bestimmen ********************************************
                newField = newField + directions[i];
            }
            //*** Neues Feld ist kein leeres Feld mehr und weder illegal noch schwarz ****
            if (fields[newField] > BLACK_KING) {
                board = this.executeMove(field, newField, piece);
                followMoves.add(board);
            }

            //*** Richtungsz�hler erh�hen ************************************************
            i++;
        }

        //*** Liste zur�ckgeben **********************************************************
        return followMoves;
    }


    /************************************************************************************/
    /*************************** Funktion: executeMove **********************************/

    /**
     * ********************************************************************************
     */

    private ArrayList<ChessBoard> whiteSingleMove(int field, int[] directions, byte piece) {
        //*** Variablen-Deklaration ******************************************************
        ArrayList<ChessBoard> followMoves;
        ChessBoard board;
        int i = 0;
        int newField;

        //*** Liste initialisieren *******************************************************
        followMoves = new ArrayList<ChessBoard>();

        //*** F�r alle Richtungen ********************************************************
        while (i < directions.length) {
            //*** n�chstes Feld bestimmen ************************************************
            newField = field + directions[i];

            //*** Neues Feld ist kein leeres Feld und weder illegal noch wei� ************
            if (fields[newField] != ILLEGAL_FIELD && fields[newField] < WHITE_PAWN) {
                board = this.executeMove(field, newField, piece);
                followMoves.add(board);
            }

            //*** Richtungsz�hler erh�hen ************************************************
            i++;
        }

        //*** Liste zur�ckgeben **********************************************************
        return followMoves;
    }


    /************************************************************************************/
    /*************************** Funktion: getQuality ***********************************/

    /**
     * ********************************************************************************
     */

    private ArrayList<ChessBoard> blackSingleMove(int field, int[] directions, byte piece) {
        //*** Variablen-Deklaration ******************************************************
        ArrayList<ChessBoard> followMoves;
        ChessBoard board;
        int i = 0;
        int newField;

        //*** Liste initialisieren *******************************************************
        followMoves = new ArrayList<ChessBoard>();

        //*** F�r alle Richtungen ********************************************************
        while (i < directions.length) {
            //*** n�chstes Feld bestimmen ************************************************
            newField = field + directions[i];

            //*** Neues Feld ist kein leeres Feld und weder illegal noch schwarz *********
            if (fields[newField] > BLACK_KING || fields[newField] == EMPTY_FIELD) {
                board = this.executeMove(field, newField, piece);
                followMoves.add(board);
            }

            //*** Richtungsz�hler erh�hen ************************************************
            i++;
        }

        //*** Liste zur�ckgeben **********************************************************
        return followMoves;
    }


    /************************************************************************************/
    /********************** Funktion: getStringRepresentation ***************************/

    /**
     * ********************************************************************************
     */

    private ChessBoard executeMove(int oldField, int newField, byte piece) {
        //*** Variablen-Deklaration ******************************************************
        ChessBoard board;

        //*** Zug vornehmen **************************************************************
        board = new ChessBoard(this);
        board.fields[oldField] = ChessBoard.EMPTY_FIELD;
        if (board.fields[newField] == BLACK_KING) board.BlackLost = true;
        if (board.fields[newField] == WHITE_KING) board.WhiteLost = true;
        board.fields[newField] = piece;
        board.TurnNotation = ChessBoard.indexToFieldName(oldField) +
                ChessBoard.indexToFieldName(newField);
        //*** Rochade m�glickeite testen *************************************************
        if (piece == WHITE_ROOK) {
            if (oldField == 21) board.WhiteCanLongRochade = false;
            if (oldField == 28) board.WhiteCanShortRochade = false;
        }
        if (piece == WHITE_KING) {
            board.WhiteCanLongRochade = false;
            board.WhiteCanShortRochade = false;
        }

        if (piece == BLACK_ROOK) {
            if (oldField == 91) board.BlackCanLongRochade = false;
            if (oldField == 98) board.BlackCanShortRochade = false;
        }
        if (piece == BLACK_KING) {
            board.BlackCanLongRochade = false;
            board.BlackCanShortRochade = false;
        }

        //*** Brett zurueckgeben *********************************************************
        return board;
    }


    /************************************************************************************/
    /************************** Funktion: loadFromString ********************************/

    /**
     * ********************************************************************************
     */

    public int getQuality(Player player) {
        //*** Variablen-Deklaration ******************************************************
        int field;
        int quality = 0;

        //*** Fuer alle legalen Felder ****************************************************
        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                //*** Feldindex bestimmen ****************************************************
                field = y * 10 + x;

                //*** Bei leerem oder ungueltigem Feld naechstes Feld ************************
                if (fields[field] <= EMPTY_FIELD) continue;

                //*** Qualitaet entsprechend der Figuren Qualiteat (Array Index) anpassen *****
                quality += QUALITIES[fields[field]];
            }

        //*** Aus Sicht von Schwarz Qualitaets Vorzeichen aendern *************************
        if (player == Player.BLACK) quality *= -1;

        //*** Wert zurueckgeben **********************************************************
        return quality;
    }


    /************************************************************************************/
    /********************** Funktion: getHash *******************************************/

    /**
     * ********************************************************************************
     */

    public String getStringRepresentation() {
        String game = "";
        int field;

        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                //*** Feldindex bestimmen ****************************************************
                field = y * 10 + x;

                game += NAMES[fields[field]];
            }
        if (playerToMakeTurn == Player.WHITE) game += "w";
        else game += "S";

        return game;
    }


    /************************************************************************************/
    /****************************** Funktion: makeTurn **********************************/

    /**
     * ********************************************************************************
     */

    public void loadFromString(String s) {
        char[] c = new char[65];
        c = s.toCharArray();
        int j = 0;

        if (c[64] == 'w') {
            playerToMakeTurn = Player.WHITE;
            c[64] = ' ';
        } else {
            playerToMakeTurn = Player.BLACK;
            c[64] = ' ';
        }

        for (int i = 0; i < 120; i++) {
            if (i > 98 || i < 21 || i % 10 == 0 || i % 10 == 9) {
                fields[i] = ILLEGAL_FIELD;
            } else
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


    /************************************************************************************/
    /*************************** Funktion: isLegalMove **********************************/

    /**
     * ********************************************************************************
     */

    public String getHash() {
        //*** Variablen-Deklaration ******************************************************
        String hash = "";
        int empty = 0;
        int field;

        //*** F�r alle legalen Felder ****************************************************
        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                //*** Feldindex bestimmen ****************************************************
                field = y * 10 + x;

                //*** Bei leerem oder ungueltigem Feld leere Felder **************************
                if (fields[field] <= EMPTY_FIELD) empty++;
                else {
                    if (empty >= 2) hash += empty + "m";
                    else if (empty == 1) hash += "l";
                    empty = 0;
                    hash += fields[field];
                }
            }

        //*** Wert zurueckgeben **********************************************************
        return hash;
    }


    /************************************************************************************/
    /****************************** Funktion: equals ************************************/

    /**
     * ********************************************************************************
     */

    public IChessGame makeTurn(String turn) throws Exception {
        ArrayList<IChessGame> nextTurns = this.getNextTurns();
        for (int i = 0; i < nextTurns.size(); i++) {
            if (turn.equals(nextTurns.get(i).getTurnNotation())) {
                return nextTurns.get(i);
            }
        }
        throw new Exception(turn + " :Zug nicht in Liste legaler Z�ge gefunden;");
    }


    /************************************************************************************/
    /****************************** Funktion: fieldNameToIndex **************************/

    /**
     * ********************************************************************************
     */

    public boolean isLegalMove(ChessBoard board) {
        //*** Variablen Deklaration ******************************************************
        ArrayList<IChessGame> nextBoards;

        //*** Folge Zuege berechnen ******************************************************
        nextBoards = this.getNextTurns();

        //*** Pr�fen ob Brett unter den Folge z�gen **************************************
        for (int i = 0; i < nextBoards.size(); i++) {
            if (((ChessBoard) nextBoards.get(i)).equals(board)) return true;
        }

        return false;
    }


    /************************************************************************************/
    /****************************** Funktion: indexToFieldName ***************************/

    /**
     * ********************************************************************************
     */

    public boolean equals(ChessBoard board) {
        for (int i = 0; i < this.fields.length; i++) {
            if (board.fields[i] != this.fields[i]) return false;
        }
        if (this.playerToMakeTurn != board.playerToMakeTurn) return false;

        return true;
    }


    /************************************************************************************/
    /******************************* Funktion: Clone ************************************/

    /**
     * ********************************************************************************
     */

    private ChessBoard Clone() {
        ChessBoard board = new ChessBoard();
        board.fields = this.fields.clone();
        board.playerToMakeTurn = this.playerToMakeTurn;

        board.BlackCanLongRochade = this.BlackCanLongRochade;
        board.BlackCanShortRochade = this.BlackCanShortRochade;
        board.WhiteCanLongRochade = this.WhiteCanLongRochade;
        board.WhiteCanShortRochade = this.WhiteCanShortRochade;

        board.entPassent = new ArrayList<IChessGame>();
        board.entPassent.addAll(this.entPassent);
        return board;
    }


    /************************************************************************************/
    /************************** Funktion: getWhiteRochade *******************************/
    /**
     * ********************************************************************************
     */

    ArrayList<IChessGame> getWhiteRochade() {
        //*** Variablen-Deklaration ******************************************************
        ArrayList<IChessGame> rochadeList;
        int field;

        //*** Liste initialisieren *******************************************************
        rochadeList = new ArrayList<IChessGame>();

        //*** Testen ob kurze Rochade m�glich ********************************************
        if (WhiteCanShortRochade) {
            //*** Wenn Felder zwischen Turm und K�nig leer und Turm da ********************
            if (fields[26] == EMPTY_FIELD && fields[27] == EMPTY_FIELD && fields[28] == WHITE_ROOK) {
                field = 25;
                //*** Testen ob ein Feld angegriffen wird ********************************
                while (field < 28 && !IsFieldAttackedByBlack(field)) {
                    field++;
                }
                //*** Wenn kein Feld angegriffen wurde ***********************************
                if (field == 28) {
                    //*** Rochieren ******************************************************
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
            //*** Wenn Felder zwischen Turm und K�nig leer und Turm da ************************
            if (fields[24] == EMPTY_FIELD && fields[23] == EMPTY_FIELD && fields[22] == EMPTY_FIELD && fields[21] == WHITE_ROOK) {
                field = 25;
                //*** Testen ob ein Feld angegriffen wird ********************************
                while (field > 22 && !IsFieldAttackedByBlack(field)) {
                    field--;
                }
                //*** Wenn kein Feld angegriffen wurde ***********************************
                if (field == 22) {
                    //*** Rochieren ******************************************************
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

        //*** Liste zur�ckgeben **********************************************************
        return rochadeList;
    }


    /************************************************************************************/
    /*********************** Funktion: IsFieldAttackedByBlack ***************************/
    /**
     * ********************************************************************************
     */

    public boolean IsFieldAttackedByBlack(int field) {
        //*** Variablen-Deklaration ******************************************************
        byte piece;
        int u;

        //*** Testen ob Bauer werfen kann ************************************************
        if (fields[field + 9] == BLACK_PAWN || fields[field + 11] == BLACK_PAWN) return true;

        //*** Testen ob Springer werfen kann *********************************************
        for (int i = 0; i < KNIGHT_DIRECTIONS.length; i++) {
            if (fields[field + KNIGHT_DIRECTIONS[i]] == BLACK_KNIGHT) return true;
        }

        //*** Turm Richtungen testen *****************************************************
        for (int i = 0; i < ROOK_DIRECTIONS.length; i++) {
            u = 1;
            while ((piece = fields[field + ROOK_DIRECTIONS[i] * u]) == EMPTY_FIELD) u++;
            //*** Wenn in Sicht von Turm oder Dame - wird angegriffen ********************
            if (piece == BLACK_QUEEN || piece == BLACK_ROOK) return true;
            //*** Wenn K�nig nur eins weit weg ist - wird angegriffen ********************
            if (piece == BLACK_KING && u == 1) return true;
        }

        //*** Turm Richtungen testen *****************************************************
        for (int i = 0; i < ChessBoard.BISHOP_DIRECTIONS.length; i++) {
            u = 1;
            while ((piece = fields[field + BISHOP_DIRECTIONS[i] * u]) == EMPTY_FIELD) u++;
            //*** Wenn in Sicht von Turm oder Dame - wird angegriffen ********************
            if (piece == BLACK_QUEEN || piece == BLACK_BISHOP) return true;
            //*** Wenn K�nig nur eins weit weg ist - wird angegriffen ********************
            if (piece == BLACK_KING && u == 1) return true;
        }

        //*** Default: false *************************************************************
        return false;
    }


    /************************************************************************************/
    /************************** Funktion: getBlackRochade *******************************/
    /**
     * ********************************************************************************
     */

    ArrayList<IChessGame> getBlackRochade() {
        //*** Variablen-Deklaration ******************************************************
        ArrayList<IChessGame> rochadeList;
        ChessBoard b;
        int field;

        //*** Liste initialisieren *******************************************************
        rochadeList = new ArrayList<IChessGame>();

        //*** Testen ob kurze Rochade m�glich ********************************************
        if (BlackCanShortRochade) {
            //*** Wenn Felder zwischen Turm und K�nig leer und Turm da **********************
            if (fields[96] == EMPTY_FIELD && fields[97] == EMPTY_FIELD && fields[98] == BLACK_ROOK) {
                field = 95;
                //*** Testen ob ein Feld angegriffen wird ********************************
                while (field < 98 && !IsFieldAttackedByWhite(field)) {
                    field++;
                }
                //*** Wenn kein Feld angegriffen wurde ***********************************
                if (field == 98) {
                    //*** Rochieren ******************************************************
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
            //*** Wenn Felder zwischen Turm und K�nig leer und Turm da **********************
            if (fields[94] == EMPTY_FIELD && fields[93] == EMPTY_FIELD && fields[92] == EMPTY_FIELD && fields[91] == BLACK_ROOK) {
                field = 95;
                //*** Testen ob ein Feld angegriffen wird ********************************
                while (field > 92 && !IsFieldAttackedByWhite(field)) {
                    field--;
                }
                //*** Wenn kein Feld angegriffen wurde ***********************************
                if (field == 92) {
                    //*** Rochieren ******************************************************
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

        //*** Liste zur�ckgeben **********************************************************
        return rochadeList;
    }


    /************************************************************************************/
    /*********************** Funktion: IsFieldAttackedByWhite ***************************/
    /**
     * ********************************************************************************
     */

    public boolean IsFieldAttackedByWhite(int field) {
        //*** Variablen-Deklaration ******************************************************
        byte piece;
        int u;

        //*** Testen ob Bauer werfen kann ************************************************
        if (fields[field - 9] == WHITE_PAWN || fields[field - 11] == WHITE_PAWN) return true;

        //*** Testen ob Springer werfen kann *********************************************
        for (int i = 0; i < KNIGHT_DIRECTIONS.length; i++) {
            if (fields[field + KNIGHT_DIRECTIONS[i]] == WHITE_KNIGHT) return true;
        }

        //*** Turm Richtungen testen *****************************************************
        for (int i = 0; i < ROOK_DIRECTIONS.length; i++) {
            u = 1;
            while ((piece = fields[field + ROOK_DIRECTIONS[i] * u]) == EMPTY_FIELD) u++;
            //*** Wenn in Sicht von Turm oder Dame - wird angegriffen ********************
            if (piece == WHITE_QUEEN || piece == WHITE_ROOK) return true;
            //*** Wenn K�nig nur eins weit weg ist - wird angegriffen ********************
            if (piece == WHITE_KING && u == 1) return true;
        }

        //*** Turm Richtungen testen *****************************************************
        for (int i = 0; i < ChessBoard.BISHOP_DIRECTIONS.length; i++) {
            u = 1;
            while ((piece = fields[field + BISHOP_DIRECTIONS[i] * u]) == EMPTY_FIELD) u++;
            //*** Wenn in Sicht von Turm oder Dame - wird angegriffen ********************
            if (piece == WHITE_QUEEN || piece == WHITE_BISHOP) return true;
            //*** Wenn K�nig nur eins weit weg ist - wird angegriffen ********************
            if (piece == WHITE_KING && u == 1) return true;
        }

        //*** Default: false *************************************************************
        return false;
    }


//	private ArrayList<ChessBoard> makePawnMove(int field, Player owner)
//	{
//		//*** Variablen-Deklaration ******************************************************
//		ArrayList<ChessBoard> followMoves;
//		
//		//*** Liste initialisieren *******************************************************
//		followMoves=new ArrayList<ChessBoard>();
//		
//		//*** Liste zur�ckgeben **********************************************************
//		return followMoves;
//	}

    public String ToDebug() {
        String s = "";

        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 10; x++) {
                s += fields[y * 10 + x] + " ";
            }
            s += "\n";
        }

        return s;
    }


    public int compareTo(IChessGame o) {
        return this.heuristicValue - o.getHeuristicValue();
    }


    public int getHeuristicValue() {
        return heuristicValue;
    }


    public String getTurnNotation() {
        return this.TurnNotation;
    }


    public void addRound() {
        this.round_counter++;
    }


    public int getRound() {
        return round_counter;
    }

    //*** Liefert den Index des Feldes mit dem König der Übergebenen Farbe zurück
    //*** Wenn der König nciht gefunden wurde -1;
    private int getKing(Player player) {
        for (int i = 19; i < 100; i++) {
            if (fields[i] == WHITE_KING && player == Player.WHITE)
                return i;
            if (fields[i] == BLACK_KING && player == Player.BLACK)
                return i;
        }
        return -1;
    }


    public GameState getGameState() {
        //*** variablen initialisieren *****************************************
        int kingsField;
        ChessBoard nextTurn;
        ArrayList<IChessGame> nextGames = new ArrayList<IChessGame>();
        boolean isKingAttacked = false;
        boolean hasEscape = false;

        //*** Prüfen ob Stellung legal ist ***********************************************
        if (!this.isLegalBoard()) return GameState.ILLEGAL;

        //** Dafür sorgen dass nachfolge stellung bekannt sind *****************
        nextGames = this.getNextTurns();

        //*** Wenn keine Nachfolgebretter existieren ist draw ****************************
        if (nextGames.size() == 0) return GameState.DRAW;

        //*** Auf MAtt und Draw weiter prüfen ********************************************

        //*** Prüfen ob der König angegriffen wird ***************************************
        if (playerToMakeTurn == Player.WHITE) {
            if (IsFieldAttackedByBlack(this.getKing(Player.WHITE)))
                isKingAttacked = true;
        } else {
            if (IsFieldAttackedByWhite(this.getKing(Player.BLACK)))
                isKingAttacked = true;
        }

        //*** Prüfe alle Felder ************************************************
        for (int i = 0; i < nextGames.size(); i++) {
            //*** nachfolger bestimmen *****************************************
            nextTurn = (ChessBoard) (nextGames.get(i));

            //*** KönigsFeld des Nachfolgers besorgen **************************
            kingsField = nextTurn.getKing(this.playerToMakeTurn);

            //*** Wenn der König schon weg ist weiter ************************************
            if (kingsField == -1) continue;

            //*** Wenn das KönigsFeld nicht vom Gegner angegriffen gibts ein escape
            if (this.playerToMakeTurn == Player.WHITE) {
                if (!nextTurn.IsFieldAttackedByBlack(kingsField)) hasEscape = true;
            } else {
                if (!nextTurn.IsFieldAttackedByWhite(kingsField)) hasEscape = true;
            }
        }

        //*** Auswerten ******************************************************************
        if (isKingAttacked && !hasEscape) return GameState.MATT;
        if (!hasEscape) return GameState.DRAW;

        //*** DEfault legal **************************************************************
        return GameState.LEGAL;
    }


    public boolean isLegalBoard() {
        int kingsField;

        //*** Wenn weiß dran ist *********************************************************
        if (this.playerToMakeTurn == Player.WHITE) {
            kingsField = this.getKing(Player.BLACK);
            //*** Wenn kein König vorhanden ist ist das Brett auch nicht legal ***********
            if (kingsField == -1) return false;
            //*** Wenn der König angegriffen wird ****************************************
            if (this.IsFieldAttackedByWhite(kingsField)) return false;
        } else {
            kingsField = this.getKing(Player.WHITE);
            //*** Wenn kein König vorhanden ist ist das Brett auch nicht legal ***********
            if (kingsField == -1) return false;
            //*** Wenn der König angegriffen wird auch nicht *****************************
            if (this.IsFieldAttackedByBlack(kingsField)) return false;
        }

        return true;
    }


    public void setRochade(boolean k_Castling, boolean q_Castling, boolean K_Castling, boolean Q_Castling) {
        BlackCanLongRochade = Q_Castling;
        BlackCanShortRochade = K_Castling;
        WhiteCanLongRochade = q_Castling;
        WhiteCanShortRochade = k_Castling;
    }


    public int getQ() {
        int q = 0;
        int field;

        //*** Für alle Spalten
        for (int x = 1; x < 9; x++) {
            //*** Für alle Reihen
            for (int y = 2; y < 10; y++) {
                field = y * 10 + x;
                //*** fig quali geht ein
                q += QUALITIES[this.fields[field]];
            }
        }
        return q;
    }

    public int getTurnsMade() {
        return round_counter;
    }


    /**
     * Getter for hasBlackLost
     *
     * @return boolean hasBlackLost
     */
    public boolean hasBlackLost() {
        return BlackLost;
    }

    /**
     * Getter for hasWhiteLost
     *
     * @return boolean hasWhiteLost
     */
    public boolean hasWhiteLost() {
        return WhiteLost;
    }

    /**
     * Generates database-String for current board including hash, depth and value
     * <p>
     * Syntax: Hash#Depth#Value
     * </p>
     *
     * @return Hashed Chessboard String with depth and value, null if MD5 missing
     * http://www.avajava.com/tutorials/lessons/how-do-i-generate-an-md5-digest-for-a-string.html</a>
     */
    public String toDatabase(Player current, int depth) {
        String hash = this.getMD5Hash();
        if (hash == null || hash.length() == 0) {
            return null;
        }
        return hash + DatabaseEntry.SEGMENTS_DELIMITER + DatabaseEntry.convert(depth, 4) +
                DatabaseEntry.SEGMENTS_DELIMITER + DatabaseEntry.convert(this.getQuality(current), 4);
    }

    /**
     * Convert the chessboard to a database string containing figures, player and castling
     * <p>
     * MD5(Board+Active Player+WSR+WLR+BSR+BLR)
     * </p>
     *
     * @return String representation of MD5-Hash from Board
     * @see de.htw.grischa.chess.database.client.DatabaseEntry#DatabaseEntry(String)
     * @see <a href="http://www.avajava.com/tutorials/lessons/how-do-i-generate-an-md5-digest-for-a-string.html">
     */
    public String getMD5Hash() {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            log.fatal("MD5 not available");
            return null;
        }
        String original = this.getStringRepresentation() + String.valueOf(this.WhiteCanShortRochade) +
                String.valueOf(this.WhiteCanLongRochade) + String.valueOf(this.BlackCanShortRochade) +
                String.valueOf(this.BlackCanLongRochade);
        md.update(original.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            String temp = Integer.toHexString((b & 0xff));
            sb.append(temp.length() == 1 ? DatabaseEntry.convert(0, 2 - temp.length()) + temp : temp);
        }
        return sb.toString();
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
