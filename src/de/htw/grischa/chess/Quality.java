package de.htw.grischa.chess;

import java.awt.*;
import java.util.ArrayList;

/**
 * Player Quality
 */

public class Quality {

    private static final short[] POS_QUALITIES = {0, 0, -10, -30, -32, -55, -98, -1000, 0, 0, 0,
            0, 10, 30, 32, 55, 98, 1000};
    private static final int[] ROOK_DIRECTIONS = {-10, -1, 1, 10};
    // private static final int[] KNIGHT_DIRECTIONS = { -21, -19, -8, 12, 21, 19, 8, -12 };
    private static final int[] BISHOP_DIRECTIONS = {-11, -9, 9, 11};
    private static final int[] QUEEN_DIRECTIONS = {-11, -10, -9, -1, 1, 9, 10, 11};
    // *** Konstanten-Definition **********************************************************
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

    /************************************************************************************/
    /*************************** Funktion: getPositionQuality ***************************/
    /**
     * ********************************************************************************
     */
    /*
     * Funktionsumfang:
     * 
     * Figur wird bedroht Springer am Rand L??uferpaar vorhanden Bauer doppelt, isoliert,
     * r??ckst??ndig, in der Mitte des Schachbretts Turm, L??ufer, Dame kann n Felder gehen Rochade
     * m??glich
     */
    public double getPositionQuality(Player player, int round_counter) {
        // *** Variablen-Deklaration ******************************************************
        int field;
        double quality = 0;
        double temp_quality = 0;
        this.player = player;
        int black_Bishops = 0;
        int white_Bishops = 0;
        ArrayList<Point> blackPawns = new ArrayList<Point>();
        ArrayList<Point> whitePawns = new ArrayList<Point>();

        // *** For all legal fields ****************************************************
        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                // *** ascertain field index ****************************************************
                field = y * 10 + x;
                temp_quality = 0;

                // *** Bei leerem oder ungueltigem Feld naechstes Feld ************************
                if (fields[field] <= EMPTYFIELD)
                    continue;

                // *** Qualitaet anhand Figur und Position verfeinern *************************

                // Figur wird bedroht * 0,91
                // Weiss an der Reihe
                temp_quality = threatenedQuality(field);

                // *** zentrum
                if (fields[field] >= BLACK_PAWN && fields[field] <= BLACK_KING) {
                    temp_quality += -PointsCenter(field);
                } else {
                    temp_quality += PointsCenter(field);
                }

                // Laeufer
                if (fields[field] == BLACK_BISHOP) {
                    if (black_Bishops == 1)
                        temp_quality += -points_2_bishops;
                    black_Bishops++;
                    if (round_counter >= 10)
                        temp_quality += -long_move_quality(field, BISHOP_DIRECTIONS);
                    quality += temp_quality;
                    continue;
                }

                if (fields[field] == WHITE_BISHOP) {
                    if (white_Bishops == 1)
                        temp_quality += points_2_bishops;
                    white_Bishops++;
                    if (round_counter >= 10)
                        temp_quality += long_move_quality(field, BISHOP_DIRECTIONS);
                    quality += temp_quality;
                    continue;
                }

                // Springer
                if (fields[field] == BLACK_KNIGHT) {
                    if (y == 2 || y == 9 || x == 1 || x == 8) {
                        temp_quality += -points_knight_on_edge;
                    }
                    quality += temp_quality;
                    continue;
                }

                // Springer
                if (fields[field] == WHITE_KNIGHT) {
                    if (y == 2 || y == 9 || x == 1 || x == 8) {
                        temp_quality += points_knight_on_edge;
                    }
                    quality += temp_quality;
                    continue;
                }

                // Bauern
                if (fields[field] == BLACK_PAWN || fields[field] == WHITE_PAWN) {
                    // temp_quality += pawn_quality(field, y);
                    // quality += temp_quality;
                    if (fields[field] == BLACK_PAWN) {
                        blackPawns.add(new Point(x, y));
                    } else {
                        whitePawns.add(new Point(x, y));
                    }
                    quality += temp_quality;
                    continue;
                }

                if (fields[field] == BLACK_ROOK) {
                    if (round_counter >= 10)
                        temp_quality += -long_move_quality(field, ROOK_DIRECTIONS);
                    quality += temp_quality;
                    continue;
                }

                if (fields[field] == WHITE_ROOK) {
                    if (round_counter >= 10)
                        temp_quality += long_move_quality(field, ROOK_DIRECTIONS);
                    quality += temp_quality;
                    continue;
                }

                if (fields[field] == BLACK_QUEEN) {
                    if (round_counter >= 10)
                        temp_quality += -long_move_quality(field, QUEEN_DIRECTIONS);
                    else if (field != 94)
                        temp_quality += 8;
                    quality += temp_quality;
                    continue;
                }

                if (fields[field] == WHITE_QUEEN) {
                    if (round_counter >= 10)
                        temp_quality += long_move_quality(field, QUEEN_DIRECTIONS);
                    else if (field != 24)
                        temp_quality += -8;
                    quality += temp_quality;
                    continue;
                }

                if (fields[field] == BLACK_KING) {
                    temp_quality += blackKingSafe(field);
                    quality += temp_quality;
                    continue;
                }

                if (fields[field] == WHITE_KING) {
                    temp_quality += whiteKingSafe(field);
                    quality += temp_quality;
                    continue;
                }

                // *** Qualitaet entsprechend der ermittelten positionellen Qialitaet anpassen *****
                quality += temp_quality;
            }

        // Ausserhalb der for-Schleife Faelle betrachten die nur einmal gerechnet werden duerfen

        quality += castle_quality();
        quality += PawnsQualityBlack(blackPawns);
        quality += PawnsQualityWhite(whitePawns);

        // *** Aus Sicht von Schwarz Qualitaets Vorzeichen aendern *************************
        if (this.player == Player.BLACK)
            quality *= -1;

        // System.out.println(board.getReadableString()+" "+quality+"\n\n");
        // qualtity * 1000 -> Matt wird nicht erkannt!?!?
        // *** Wert zurueckgeben **********************************************************
        return quality * 10000;
    }

    private double threatenedQuality(int field) {
        double temp_quality = 0;
        // *** schwarze B, L, T, D
        if (fields[field] >= BLACK_PAWN && fields[field] <= BLACK_QUEEN) {
            // *** Figur wird von Weiss bedroht *******************************************
            if (board.IsFieldAttackedByWhite(field)) {
                // *** Qualit??t der scharzen figur verringeren ****************************
                temp_quality = (POS_QUALITIES[fields[field]] * THREATENED_FACTOR);
                return temp_quality;
            }
            // *** nicht von wei?? bedroht *************************************************
            else {
                // *** volle Qualit??t der schwarzen Figur zur??ck **************************
                temp_quality = POS_QUALITIES[fields[field]];
                return temp_quality;
            }
        }

        // *** wei??er B, L, T, D
        if (fields[field] >= WHITE_PAWN && fields[field] <= WHITE_QUEEN) {
            // *** Figur wird von schwarz bedroht *****************************************
            if (board.IsFieldAttackedByBlack(field)) {
                // *** Qualit??t der wei??en figur verringeren und addieren *****************
                temp_quality = (POS_QUALITIES[fields[field]] * THREATENED_FACTOR);
                return temp_quality;
            }
            // *** nicht von schwarz bedroht **********************************************
            else {
                // *** volle Qualit der wei??en Figur addieren *****************************
                temp_quality = POS_QUALITIES[fields[field]];
                return temp_quality;
            }
        }

        // schwarzer K
        if (fields[field] == BLACK_KING) {
            // *** Wenn der K??nig angegriffen wird ****************************************
            if (board.IsFieldAttackedByWhite(field)) {
                temp_quality = POS_QUALITIES[BLACK_KING] * KING_THREATENED_FACTOR;
                return temp_quality;
            } else {
                temp_quality = POS_QUALITIES[BLACK_KING];
                return temp_quality;
            }
        }

        // wei??er K
        if (fields[field] == WHITE_KING) {
            // *** Wenn der K??nig angegriffen wird ****************************************
            if (board.IsFieldAttackedByBlack(field)) {
                temp_quality = POS_QUALITIES[WHITE_KING] * KING_THREATENED_FACTOR;
                return temp_quality;
            } else {
                temp_quality = POS_QUALITIES[WHITE_KING];
                return temp_quality;
            }

        }

        return 0;
    }

    // private double pawn_quality(int field, int y)
    // {
    // double temp_quality=0;
    //
    // // Bauer: isoliert, doppelt, rueckstaendig, usw
    // if (fields[field]==BLACK_PAWN)
    // {
    // // Bauer n Reihen gelaufen +points_pawn_rows_moved*n
    // temp_quality += -(points_pawn_rows_moved*(8-y));
    //
    // // Bauer in der Mitte des Spielfeldes 2x2 = +2, Rand darum =+1 points_pawn_in_middle=2
    // if (field==54 || field==55 || field==64 || field==65)
    // temp_quality += -points_pawn_in_middle;
    // else if ((field>42 && field<47) || (field>72 && field<77) || field==53 || field==63 ||
    // field==56 || field==66)
    // temp_quality += -(points_pawn_in_middle / 2);
    //
    // // Bauer: doppelt
    // for (int i=1;i<7;i++)
    // {
    // // pro Bauer zuviel in einer Reihe points_pawn_in_a_row=-3
    // if (field-(i*10)>20 && (fields[field-(i*10)]==BLACK_PAWN))
    // {
    // temp_quality += -points_pawn_in_a_row;
    // }
    // }
    //
    // // Bauer isoliert (keine Bauern auf der Nachbarlinie)
    // boolean isolated_pawn = true;
    // for (int i=0;i<7;i++)
    // {
    // // linke und rechte Reihe auf bauer absuchen points_pawn_isolated=-3
    // if ((field+(i*10)+1<99 && (fields[field+(i*10+1)]==BLACK_PAWN ||
    // fields[field+(i*10-1)]==BLACK_PAWN)) || (field-(i*10)-1>20 &&
    // (fields[field-(i*10+1)]==BLACK_PAWN || fields[field-(i*10-1)]==BLACK_PAWN)))
    // isolated_pawn = false;
    //
    // }
    // if (isolated_pawn)
    // temp_quality += -points_pawn_isolated;
    //
    // // Bauer rueckstaendig(ein Bauer mind 2 felder hinter allen anderen) points_pawn_backward=-3
    // boolean backward_pawn = true;
    // for (int i=1;i<7;i++)
    // {
    // // links und rechts auf Nachbarbauer absuchen (2 felder vor ihm)
    // if ((fields[field-i]==BLACK_PAWN || fields[field+i]==BLACK_PAWN ||
    // fields[field-i+10]==BLACK_PAWN || fields[field+i+10]==BLACK_PAWN ||
    // fields[field-i+20]==BLACK_PAWN || fields[field+i+20]==BLACK_PAWN
    // || fields[field-i-10]==BLACK_PAWN || fields[field+i-10]==BLACK_PAWN ||
    // fields[field-i-20]==BLACK_PAWN || fields[field+i-20]==BLACK_PAWN))
    // backward_pawn = false;
    // }
    // if (backward_pawn)
    // temp_quality += -points_pawn_backward;
    // }
    // else
    // {
    // // Bauer n Reihen gelaufen +points_pawn_rows_moved*n
    // temp_quality += (points_pawn_rows_moved*(y-3));
    //
    // // Bauer in der Mitte des Spielfeldes 2x2 = +2, Rand darum =+1 points_pawn_in_middle=2
    // if (field==54 || field==55 || field==64 || field==65)
    // temp_quality += points_pawn_in_middle;
    // else if ((field>42 && field<47) || (field>72 && field<77) || field==53 || field==63 ||
    // field==56 || field==66)
    // temp_quality += points_pawn_in_middle / 2;
    //
    // // Bauer: doppelt
    // for (int i=1;i<7;i++)
    // {
    // // pro Bauer zuviel in einer Reihe points_pawn_in_a_row=-3
    // if (field+(i*10)<99 && (fields[field+(i*10)]==WHITE_PAWN))
    // {
    // temp_quality += points_pawn_in_a_row;
    // }
    // }
    //
    // // Bauer isoliert (keine Bauern auf der Nachbarlinie)
    // boolean isolated_pawn = true;
    // for (int i=0;i<7;i++)
    // {
    // // linke und rechte Reihe auf bauer absuchen points_pawn_isolated=-3
    // if ((field+(i*10)+1<99 && (fields[field+(i*10+1)]==WHITE_PAWN ||
    // fields[field+(i*10-1)]==WHITE_PAWN)) || (field-(i*10)-1>20 &&
    // (fields[field-(i*10+1)]==WHITE_PAWN || fields[field-(i*10-1)]==WHITE_PAWN)))
    // isolated_pawn = false;
    // }
    // if (isolated_pawn)
    // temp_quality += points_pawn_isolated;
    //
    // // Bauer rueckstaendig(ein Bauer mind 2 felder hinter allen anderen) points_pawn_backward=-3
    // boolean backward_pawn = true;
    // for (int i=1;i<7;i++)
    // {
    // // links und rechts auf Nachbarbauer absuchen (2 felder vor ihm)
    // if ((fields[field-i]==WHITE_PAWN || fields[field+i]==WHITE_PAWN ||
    // fields[field-i+10]==WHITE_PAWN || fields[field+i+10]==WHITE_PAWN ||
    // fields[field-i+20]==WHITE_PAWN || fields[field+i+20]==WHITE_PAWN
    // || fields[field-i-10]==WHITE_PAWN || fields[field+i-10]==WHITE_PAWN ||
    // fields[field-i-20]==WHITE_PAWN || fields[field+i-20]==WHITE_PAWN))
    // backward_pawn = false;
    // }
    // if (backward_pawn)
    // temp_quality += points_pawn_backward;
    // }
    // return temp_quality;
    // }

    private double castle_quality() {
        double temp_quality = 0;
        int field;

        // Rochade moeglich (nur wenn die GegnerDame existiert) points_castle=5
        boolean enemy_queen = false;
        for (int y = 2; y < 10; y++)
            for (int x = 1; x < 9; x++) {
                // *** Feldindex bestimmen ************************************************
                field = y * 10 + x;
                if ((player == Player.WHITE && fields[field] == BLACK_QUEEN)
                        || (player == Player.BLACK && fields[field] == WHITE_QUEEN))
                    enemy_queen = true;
            }
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

    private int long_move_quality(int field, int[] directions) {
        int i = 0;
        int counter = 0;
        int newField;
        while (i < directions.length) {
            newField = field + directions[i];
            while (fields[newField] == EMPTYFIELD) {
                counter++;
                newField = newField + directions[i];
            }
            i++;
        }
        return counter;
    }

    private double PawnsQualityWhite(ArrayList<Point> positions) {
        short[] rows = new short[12];
        double temp_quality = 0;
        int y = 0;
        short[] lanes = new short[10];

        for (int i = 0; i < positions.size(); i++) {
            y = positions.get(i).y;
            rows[positions.get(i).y]++;
            lanes[positions.get(i).x]++;

            // *** vorger??ckte
            temp_quality += (points_pawn_rows_moved * (y - 3));
        }

        int u = 0;
        // *** r??ckst??ndig
        for (u = 0; u < 11; u++) {
            if (rows[u] != 0)
                break;
        }
        if (rows[u] == 1 && rows[u + 1] == 0 && rows[u + 2] == 0)
            temp_quality += points_pawn_backward;

        // *** doppelte und isolierte
        for (int i = 1; i < 9; i++) {
            if (lanes[i] != 0 && lanes[i - 1] == 0 && lanes[i + 1] == 0)
                temp_quality += points_pawn_isolated;
            if (lanes[i] > 1)
                temp_quality += (lanes[i] - 1) * points_pawn_in_a_row;
        }

        return temp_quality;
    }

    private double PawnsQualityBlack(ArrayList<Point> positions) {
        short[] rows = new short[12];
        double temp_quality = 0;
        int y = 0;
        short[] lanes = new short[10];

        for (int i = 0; i < positions.size(); i++) {
            y = positions.get(i).y;
            rows[positions.get(i).y]++;
            lanes[positions.get(i).x]++;

            // *** vorger??ckte
            temp_quality += (points_pawn_rows_moved * (8 - y));
        }
        // *** r??ckst??ndig
        int u = 0;
        // *** r??ckst??ndig
        for (u = 9; u > 2; u--) {
            if (rows[u] != 0)
                break;
        }
        if (rows[u] == 1 && rows[u - 1] == 0 && rows[u - 2] == 0)
            temp_quality += points_pawn_backward;

        // *** doppelte und isolierte
        for (int i = 1; i < 9; i++) {
            if (lanes[i] != 0 && lanes[i - 1] == 0 && lanes[i + 1] == 0)
                temp_quality += points_pawn_isolated;
            if (lanes[i] > 1)
                temp_quality += (lanes[i] - 1) * points_pawn_in_a_row;
        }
        return -temp_quality;
    }

    private double PointsCenter(int field) {
        double temp_quality = 0;

        if (field == 54 || field == 55 || field == 64 || field == 65)
            temp_quality += 5;
        else if ((field > 42 && field < 47) || (field > 72 && field < 77) || field == 53
                || field == 63 || field == 56 || field == 66)
            temp_quality += 2.5;

        return temp_quality;
    }

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
