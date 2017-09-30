package de.htw.grischa.chess;

import java.awt.*;
import java.util.ArrayList;

/**
 * The Quality class holds the information about heuristical values of a board.
 * So every board could be assigned to value. The value in this class is double value.
 * For communication reasons this double is taken by 10k to get the decimals into a integer,
 * this is done due easier transferring of integers to string.
 *
 * <h3>Version History</h3>
 * <ul>
 * <li> 05/10 - Daniel Heim - Initial Version </li>
 * <li> 03/17 - Benjamin Troester - updating, adding documentation</li>
 * </ul>
 *
 * @author Daniel Heim
 *
 * @version 03/17
 */

public class Quality {
    private static final short[] POS_QUALITIES = {0, 0, -10, -30, -32, -55, -98, -1000, 0, 0, 0,
            0, 10, 30, 32, 55, 98, 1000};
    private static final int[] ROOK_DIRECTIONS = {-10, -1, 1, 10};
    // private static final int[] KNIGHT_DIRECTIONS = { -21, -19, -8, 12, 21, 19, 8, -12 };
    private static final int[] BISHOP_DIRECTIONS = {-11, -9, 9, 11};
    private static final int[] QUEEN_DIRECTIONS = {-11, -10, -9, -1, 1, 9, 10, 11};
    // defining constants
    private static byte EMPTYFIELD;
    private static byte ILLEGALFIELD;
    private static byte BLACK_PAWN;
    private static byte BLACK_ROOK;
    private static byte BLACK_KNIGHT;
    private static byte BLACK_BISHOP;
    private static byte BLACK_QUEEN;
    private static byte BLACK_KING;
    private static byte WHITE_PAWN;
    private static byte WHITE_ROOK;
    private static byte WHITE_KNIGHT;
    private static byte WHITE_BISHOP;
    private static byte WHITE_QUEEN;
    private static byte WHITE_KING;
    private static int points_2_bishops = 5;
    private static int points_knight_on_edge = -4;
    private static int points_pawn_in_a_row = -3;
    private static int points_pawn_isolated = -3;
    private static int points_pawn_backward = -3;
    private static int points_pawn_rows_moved = 2;
    // private static int points_pawn_in_middle = 2;
    private static int points_castle = 5;
    private static double KING_THREATENED_FACTOR = 0.992;
    private static double THREATENED_FACTOR = 0.87;//0.87 -> Simon 0.91 -> Redis
    public byte[] fields;
    private Player player = null;
    private ChessBoard board = null;
    private boolean BlackCanLongRochade;
    private boolean BlackCanShortRochade;
    private boolean WhiteCanLongRochade;
    private boolean WhiteCanShortRochade;

    /**
     *
     * @param board
     */
    public Quality(ChessBoard board) {
        this.board = board;
        this.fields = board.fields;

        BlackCanLongRochade = board.BlackCanLongRochade;
        BlackCanShortRochade = board.BlackCanShortRochade;
        WhiteCanLongRochade = board.WhiteCanLongRochade;
        WhiteCanShortRochade = board.WhiteCanShortRochade;

        EMPTYFIELD = ChessBoard.EMPTY_FIELD;
        ILLEGALFIELD = ChessBoard.ILLEGAL_FIELD;

        BLACK_PAWN = ChessBoard.BLACK_PAWN;
        BLACK_ROOK = ChessBoard.BLACK_ROOK;
        BLACK_KNIGHT = ChessBoard.BLACK_KNIGHT;
        BLACK_BISHOP = ChessBoard.BLACK_BISHOP;
        BLACK_QUEEN = ChessBoard.BLACK_QUEEN;
        BLACK_KING = ChessBoard.BLACK_KING;

        WHITE_PAWN = ChessBoard.WHITE_PAWN;
        WHITE_ROOK = ChessBoard.WHITE_ROOK;
        WHITE_KNIGHT = ChessBoard.WHITE_KNIGHT;
        WHITE_BISHOP = ChessBoard.WHITE_BISHOP;
        WHITE_QUEEN = ChessBoard.WHITE_QUEEN;
        WHITE_KING = ChessBoard.WHITE_KING;
    }

    /**
     * Method to compute the quality of a chessboard. This is done by evaluating
     * the position and resulting possibilities of threats or being threatened by the
     * opponent. Checks several indicators for position quality by iterating through the
     * chess board - the to for loops.
     * Regards if chess piece is:
     * <p><ul>
     * <li>threatened by another piece</li>
     * <li>knight on side (edge of the field)</li>
     * <li>is there a bishop pair</li>
     * <li>doubled pawn</li>
     * <li>isolated pieces</li>
     * <li>undeveloped pieces</li>
     * <li>how does the center looks like</li>
     * <li>center is occupied by rook/Castle, bishop</li>
     * <li>queen is able to move n fields</li>
     * <li>castling is still possible</li>
     *</ul><p>
     * @param   player          the player who moves
     * @param   round_counter   the n-th turn
     * @return  double          value resulting from the calculation
     */

    public double getPositionQuality(Player player, int round_counter) {
        //define vars
        int field;
        double quality = 0;
        double temp_quality = 0;
        this.player = player;
        int black_Bishops = 0;
        int white_Bishops = 0;
        ArrayList<Point> blackPawns = new ArrayList<Point>();
        ArrayList<Point> whitePawns = new ArrayList<Point>();

        //For all legal fields
        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                field = y * 10 + x;//ascertain field index
                temp_quality = 0;//temporary var to sum up quality
                //condition: ignores empty/invalid fields, goes to next field
                if (fields[field] <= EMPTYFIELD)
                    continue;
                // hone quality due to type of piece and position
                // if piece is threatened: value * 0,91
                temp_quality = threatenedQuality(field);
                //white moves
                //CENTER
                if (fields[field] >= BLACK_PAWN && fields[field] <= BLACK_KING) {
                    temp_quality += -PointsCenter(field);
                } else
                    temp_quality += PointsCenter(field);

                /*
                BISHOPS
                 */

                // BISHOP BLACK
                if (fields[field] == BLACK_BISHOP) {
                    if (black_Bishops == 1)
                        temp_quality += -points_2_bishops;
                    black_Bishops++;
                    if (round_counter >= 10)
                        temp_quality += -long_move_quality(field, BISHOP_DIRECTIONS);
                    quality += temp_quality;
                    continue;
                }
                // BISHOP WHITE
                if (fields[field] == WHITE_BISHOP) {
                    if (white_Bishops == 1)
                        temp_quality += points_2_bishops;
                    white_Bishops++;
                    if (round_counter >= 10)
                        temp_quality += long_move_quality(field, BISHOP_DIRECTIONS);
                    quality += temp_quality;
                    continue;
                }

                /*
                KNIGHTS
                 */

                // KNIGHT BLACK
                if (fields[field] == BLACK_KNIGHT) {
                    if (y == 2 || y == 9 || x == 1 || x == 8)
                        temp_quality += -points_knight_on_edge;

                    quality += temp_quality;
                    continue;
                }

                // KNIGHT WHITE
                if (fields[field] == WHITE_KNIGHT) {
                    if (y == 2 || y == 9 || x == 1 || x == 8) {
                        temp_quality += points_knight_on_edge;
                    }
                    quality += temp_quality;
                    continue;
                }

                // ********PAWNS***********

                // PAWN
                //Several attacks
                if (fields[field] == BLACK_PAWN || fields[field] == WHITE_PAWN) {
                    if (fields[field] == BLACK_PAWN)
                        blackPawns.add(new Point(x, y));
                    else
                        whitePawns.add(new Point(x, y));
                    quality += temp_quality;
                    continue;
                }

                /*
                ROOKS
                 */

                if (fields[field] == BLACK_ROOK) {
                    if (round_counter >= 10)
                        temp_quality += -long_move_quality(field, ROOK_DIRECTIONS);
                    quality += temp_quality;
                    continue;
                }

                //ROOK WHITE
                if (fields[field] == WHITE_ROOK) {
                    if (round_counter >= 10)
                        temp_quality += long_move_quality(field, ROOK_DIRECTIONS);
                    quality += temp_quality;
                    continue;
                }

                /*
                QUEENS
                 */

                //QUEEN BLACK
                if (fields[field] == BLACK_QUEEN) {
                    if (round_counter >= 10)
                        temp_quality += -long_move_quality(field, QUEEN_DIRECTIONS);
                    else if (field != 94)
                        temp_quality += 8;
                    quality += temp_quality;
                    continue;
                }

                //QUEEN WHITE
                if (fields[field] == WHITE_QUEEN) {
                    if (round_counter >= 10)
                        temp_quality += long_move_quality(field, QUEEN_DIRECTIONS);
                    else if (field != 24)
                        temp_quality += -8;
                    quality += temp_quality;
                    continue;
                }

                /*
                KINGS
                 */

                //KING BLACK
                if (fields[field] == BLACK_KING) {
                    temp_quality += blackKingSafe(field);
                    quality += temp_quality;
                    continue;
                }
                //KING WHTEI
                if (fields[field] == WHITE_KING) {
                    temp_quality += whiteKingSafe(field);
                    quality += temp_quality;
                    continue;
                }
                //sum up quality arising from position on fields
                quality += temp_quality;
            }

        //These qualities could be calculated once!, so the have to be out of the for loop
        quality += castle_quality();
        quality += PawnsQualityBlack(blackPawns);
        quality += PawnsQualityWhite(whitePawns);

        // condition: for black player - negative sign
        if (this.player == Player.BLACK)
            quality *= -1;
        // originally: qualtity * 1000 -> some check mates are not detected!?
        // Due the cast to int there could be a loss of precision -> quality * 10k
        return quality * 10000;
    }

    /**
     * Method that computes the threatening quality of a given field.
     * This concerns pawns, bishops, rooks, queens
     * @param   field   The field that has to calculated
     * @return  double value that holds the quality
     */
    private double threatenedQuality(int field) {
        double temp_quality = 0;//init with zero
        // black pawn, bishop, rooks, queen
        if (fields[field] >= BLACK_PAWN && fields[field] <= BLACK_QUEEN) {
            // condition: piece is threatened by white
            if (board.IsFieldAttackedByWhite(field)) {
                // reduce blacks piece quality
                temp_quality = (POS_QUALITIES[fields[field]] * THREATENED_FACTOR);
                return temp_quality;
            }
            // not threatened by white - returns full quality
            else {
                temp_quality = POS_QUALITIES[fields[field]];
                return temp_quality;
            }
        }
        // white pawn, bishop, rooks, queen
        if (fields[field] >= WHITE_PAWN && fields[field] <= WHITE_QUEEN) {
            // condition: piece is threatened by black
            if (board.IsFieldAttackedByBlack(field)) {
                // reduce white piece quality
                temp_quality = (POS_QUALITIES[fields[field]] * THREATENED_FACTOR);
                return temp_quality;
            }
            // not threatened by black - returns full quality
            else {
                temp_quality = POS_QUALITIES[fields[field]];
                return temp_quality;
            }
        }
        // BLACK KING:
        if (fields[field] == BLACK_KING) {
            //condition: black king is attacked
            if (board.IsFieldAttackedByWhite(field)) {
                temp_quality = POS_QUALITIES[BLACK_KING] * KING_THREATENED_FACTOR;
                return temp_quality;
            }
            // not threatened by white - returns full quality
            else {
                temp_quality = POS_QUALITIES[BLACK_KING];
                return temp_quality;
            }
        }
        // WHITE KING:
        if (fields[field] == WHITE_KING) {
            //condition: white king is attacked
            if (board.IsFieldAttackedByBlack(field)) {
                temp_quality = POS_QUALITIES[WHITE_KING] * KING_THREATENED_FACTOR;
                return temp_quality;
            }
            // not threatened by black - returns full quality
            else {
                temp_quality = POS_QUALITIES[WHITE_KING];
                return temp_quality;
            }
        }
        //if none of the above conditions are fulfilled - nothing to return - no neutral sum
        return 0;
    }

    /**
     * Castling is nearly always a good idea! So this method computes castling
     * options and takes this into effort to the quality of the match.
     *
     * @return double value
     */
    private double castle_quality() {
        double temp_quality = 0;
        int field;
        boolean enemy_queen = false;
        // iterate over the chess board and...
        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                // ... determine the field index of the queen(s)
                field = y * 10 + x;
                if ((player == Player.WHITE && fields[field] == BLACK_QUEEN)
                        || (player == Player.BLACK && fields[field] == WHITE_QUEEN))
                    enemy_queen = true;
            }
        // Conditions: which castling are possible - long/ queenside, short/ kingside
        if (enemy_queen && WhiteCanLongRochade)
            temp_quality += points_castle;
        if (enemy_queen && WhiteCanShortRochade)
            temp_quality += points_castle;
        if (enemy_queen && BlackCanLongRochade)
            temp_quality += -points_castle;
        if (enemy_queen && BlackCanShortRochade)
            temp_quality += -points_castle;
        return temp_quality;
    }

    /**
     * The moves concerns about long moves. It takes a field with a
     * given piece and direction. It counts the amount of fields, that
     * the piece could move. Long moves make it possible to attack or defend
     * fast affects the quality.
     * @param   field       the given field
     * @param   directions  direction to move towards
     * @return  integer that holds the amount of fields that can be occupied
     */
    private int long_move_quality(int field, int[] directions) {
        int i = 0;
        int counter = 0;
        int newField;
        while (i < directions.length) {//in which direction - holds byte values of the index
            newField = field + directions[i];
            //as long as there is no opponent nor own piece in the way
            while (fields[newField] == EMPTYFIELD) {
                counter++;
                newField = newField + directions[i];
            }
            i++;
        }
        return counter;
    }

    /**
     * PawnsQualityWhite takes care of white pawns.
     * @param positions
     * @return
     */
    private double PawnsQualityWhite(ArrayList<Point> positions) {
        short[] rows = new short[12];
        double temp_quality = 0;//init
        int y = 0;
        short[] lanes = new short[10];

        for (int i = 0; i < positions.size(); i++) {
            y = positions.get(i).y;
            rows[positions.get(i).y]++;
            lanes[positions.get(i).x]++;
            // is already moved forward?
            temp_quality += (points_pawn_rows_moved * (y - 3));
        }

        int u = 0;
        // undeveloped pawns
        for (u = 0; u < 11; u++) {
            if (rows[u] != 0)
                break;
        }
        if (rows[u] == 1 && rows[u + 1] == 0 && rows[u + 2] == 0)
            temp_quality += points_pawn_backward;

        // doubled and isolated pawns - bad protection -> worse transformation possibilities
        for (int i = 1; i < 9; i++) {
            if (lanes[i] != 0 && lanes[i - 1] == 0 && lanes[i + 1] == 0)
                temp_quality += points_pawn_isolated;
            if (lanes[i] > 1)
                temp_quality += (lanes[i] - 1) * points_pawn_in_a_row;
        }
        return temp_quality;
    }

    /**
     * PawnsQualityBlack takes care of black pawns.
     * @param positions
     * @return
     */
    private double PawnsQualityBlack(ArrayList<Point> positions) {
        short[] rows = new short[12];
        double temp_quality = 0;
        int y = 0;
        short[] lanes = new short[10];

        for (int i = 0; i < positions.size(); i++) {
            y = positions.get(i).y;
            rows[positions.get(i).y]++;
            lanes[positions.get(i).x]++;
            // is already moved forward?
            temp_quality += (points_pawn_rows_moved * (8 - y));
        }
        // undeveloped pawns
        int u = 0;
        for (u = 9; u > 2; u--) {
            if (rows[u] != 0)
                break;
        }
        if (rows[u] == 1 && rows[u - 1] == 0 && rows[u - 2] == 0)
            temp_quality += points_pawn_backward;

        // doubled and isolated pawns - bad protection -> worse transformation possibilities
        for (int i = 1; i < 9; i++) {
            if (lanes[i] != 0 && lanes[i - 1] == 0 && lanes[i + 1] == 0)
                temp_quality += points_pawn_isolated;
            if (lanes[i] > 1)
                temp_quality += (lanes[i] - 1) * points_pawn_in_a_row;
        }
        return -temp_quality;
    }

    /**
     *
     * @param field
     * @return
     */
    private double PointsCenter(int field) {
        double temp_quality = 0;

        if (field == 54 || field == 55 || field == 64 || field == 65)
            temp_quality += 5;
        else if ((field > 42 && field < 47) || (field > 72 && field < 77) || field == 53
                || field == 63 || field == 56 || field == 66)
            temp_quality += 2.5;

        return temp_quality;
    }

    /**
     *
     * @param field
     * @return
     */
    private double blackKingSafe(int field) {
        int testfield = 0;
        int temp_quality = 0;
        for (int i = 0; i < QUEEN_DIRECTIONS.length; i++) {
            testfield = field + QUEEN_DIRECTIONS[i];
            if (this.board.fields[testfield] != ILLEGALFIELD) {
                if (board.IsFieldAttackedByWhite(testfield))
                    temp_quality += -3;
            }
        }
        return -temp_quality;
    }

    /**
     *
     * @param field
     * @return
     */
    private double whiteKingSafe(int field) {
        int testfield = 0;
        int temp_quality = 0;
        for (int i = 0; i < QUEEN_DIRECTIONS.length; i++) {
            testfield = field + QUEEN_DIRECTIONS[i];
            if (this.board.fields[testfield] != ILLEGALFIELD) {
                if (board.IsFieldAttackedByBlack(testfield))
                    temp_quality += -3;
            }
        }
        return temp_quality;
    }
}
