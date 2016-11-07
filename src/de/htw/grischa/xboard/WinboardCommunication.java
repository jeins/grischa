package de.htw.grischa.xboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import de.htw.grischa.GriScha;
import de.htw.grischa.chess.GameState;
import de.htw.grischa.chess.GridGameManager;
import de.htw.grischa.chess.Player;

public class WinboardCommunication {
    private final static Logger LOG = Logger.getLogger(WinboardCommunication.class);
    private static int time = 15000;

    public void run() {

        GridGameManager game;
        try {
            game = new GridGameManager();

            String cmd = "";
            String out = "";
            BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
            String protocol = "";
            boolean isGo = false;

            while (true) {
                try {
                    // Pr??fen ob Draw ist
                    if (game.getCurrentGame() != null && isGo
                            && game.getCurrentGame().getGameState() == GameState.DRAW) {
                        // out = "offer draw";
                        out = "result 1/2-1/2 {Stalemate}";
                        // okDialog("Patt!\n"+out);
                    }
                    // Wenn Matt ist okDialog ausgeben
                    else if (game.getCurrentGame() != null && isGo
                            && game.getCurrentGame().getGameState() == GameState.MATT) {
                        if (game.getCurrentGame().getPlayerToMakeTurn() == Player.WHITE)
                            out = "result 0-1 {Black mates}";
                        else
                            out = "result 1-0 {White mates}";
                        isGo = false;
                        System.out.println(out);
                        LOG.debug("Schach Matt!" + out);
                        // okDialog("Schach Matt!\n"+out);
                    }
                    // Eingabe auf Console lesen
                    cmd = bin.readLine();
                    LOG.debug("Input: " + cmd);
                } catch (IOException e) {
                    String message = "Fehler beim Lesen auf der stdin";
                    LOG.error(message);
                }
                // Programm beenden
                if (cmd.equalsIgnoreCase("q") || cmd.equalsIgnoreCase("exit")) {
                    System.err.println("Schach beendet");
                    System.exit(0);
                }
                // Xboard ist das verwendete Protokoll
                else if (cmd.equalsIgnoreCase("xboard")) {
                    protocol = "xboard";
                }
                // UCI ist das verwendete Protokoll
                else if (cmd.equalsIgnoreCase("uci")) {
                    protocol = "uci";
                    System.out.printf("id name GriScha%n");
                    System.out.printf("id author HTW Berlin%n");
                    System.out.printf("uciok%n");
                }
                // XBoard-Befehle abarbeiten
                else if (protocol.equalsIgnoreCase("xboard")) {
                    //
                    if (cmd.equalsIgnoreCase("new")) {
                        game.init();
                        isGo = false;
                        LOG.info("Neues Spielfeld generiert");
                        LOG.debug("Neues Spielfeld generiert");
                    } else if (cmd.equalsIgnoreCase("quit")) {
                        LOG.info("Spiel beendet");
                        LOG.debug("Spiel beendet");
                        System.exit(0);
                    }

                    // result 1-0 {Xboard adjudication: Checkmate}
                    // result 1/2-1/2 {xboard exit but bare king}
                    // result 0-1 {White resigns}
                    // TODO: Ausgabe bei Partie
                    else if (cmd.startsWith("result")) {
                        String[] fen = new String[7];
                        fen = cmd.split("[ -]");
                        String message = "";
                        int k;
                        if (fen[1].equals("*"))
                            k = 2;
                        else
                            k = 3;

                        // Auslesen der Begruendung fuer Partieende
                        if (fen[k].contains("{")) {
                            message += " " + fen[k].substring(1, fen[k].length());
                            k++;
                            if (k < fen.length) {
                                while (!fen[k].contains("}")) {
                                    message += " " + fen[k];
                                    k++;
                                }
                                message += " " + fen[k].substring(0, fen[k].length() - 1);
                            } else {
                                message = message.substring(0, message.length() - 1);
                            }
                        }
                        if (fen[1].equals("*")) {
                            LOG.debug("Xboard beendet " + message);
                        } else if (!fen[1].equals("0") && !fen[2].equals("0")) // Draw
                        {
                            // okDialog("Draw\n"+message);
                        } else if (!fen[1].equals("0") && fen[2].equals("0")) // White wins
                        {
                            // okDialog("White wins\n"+message);
                        } else if (fen[1].equals("0") && !fen[2].equals("0")) // Black wins
                        {
                            // okDialog("Black wins\n"+message);
                        }
                    }

                    else if (cmd.equalsIgnoreCase("force"))
                        isGo = false;

                    // setboard rn1qkbnr/pp1p1ppp/1bp1p3/8/4P3/3B4/PPPP1PPP/RNBQK1NR w KQkq - 0 1
                    // setboard rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 1
                    // setboard rnb1kbnr/pp1ppppp/3q4/2p5/2P5/5N2/PP1PPPPP/RNBQKB1R b KQkq - 0 1
                    // setboard r3k3/8/8/8/8/8/8/1R2K3 w q - 0 1
                    // Forsythe-Edwards Notation, as defined in the PGN standard
                    // FEN := Position " " Spieler " " Rochade " " en-passant " " Halbz??ge " "
                    // Zugnummer
                    // p=pawn, r=rook, n=knight, b=bishop, q=queen, k=king, /=next row, int=count of
                    // empty fields, w=white next, KQkq=something with castling
                    // bauer turm springer l??ufer dame k??nig
                    else if (cmd.startsWith("setboard")) {
                        // TODO: einbauen 0 1
                        String[] fen = new String[7];
                        fen = cmd.split(" ");
                        String new_pos = "";
                        String invert_x_pos = "";
                        String invert_y_pos = "";
                        boolean k_Castling = false, q_Castling = false, K_Castling = false, Q_Castling = false;
                        int rows = 0;

                        for (int i = 0; i < fen[1].length(); i++) {
                            switch (fen[1].charAt(i)) {
                            case ('/'):
                                rows++;
                                break;
                            case ('1'):
                                new_pos += "x";
                                break;
                            case ('2'):
                                new_pos += "xx";
                                break;
                            case ('3'):
                                new_pos += "xxx";
                                break;
                            case ('4'):
                                new_pos += "xxxx";
                                break;
                            case ('5'):
                                new_pos += "xxxxx";
                                break;
                            case ('6'):
                                new_pos += "xxxxxx";
                                break;
                            case ('7'):
                                new_pos += "xxxxxxx";
                                break;
                            case ('8'):
                                new_pos += "xxxxxxxx";
                                break;
                            case ('p'):
                                new_pos += "B";
                                break;
                            case ('P'):
                                new_pos += "b";
                                break;
                            case ('r'):
                                new_pos += "T";
                                break;
                            case ('R'):
                                new_pos += "t";
                                break;
                            case ('n'):
                                new_pos += "S";
                                break;
                            case ('N'):
                                new_pos += "s";
                                break;
                            case ('b'):
                                new_pos += "L";
                                break;
                            case ('B'):
                                new_pos += "l";
                                break;
                            case ('q'):
                                new_pos += "D";
                                break;
                            case ('Q'):
                                new_pos += "d";
                                break;
                            case ('k'):
                                new_pos += "K";
                                break;
                            case ('K'):
                                new_pos += "k";
                                break;
                            }
                        }
                        // An der x-Achse spiegeln
                        for (int j = new_pos.length(); j > 0; j--) {
                            invert_x_pos += new_pos.charAt(j - 1);
                        }

                        // An der y-Achse spiegeln
                        int l = 7;
                        for (int k = 0; k < invert_x_pos.length(); k++) {
                            if (k % 8 == 0) {
                                l = k + 7;
                            }
                            invert_y_pos += invert_x_pos.charAt(l);
                            l--;
                        }
                        if (fen[2].equals("w")) {
                            invert_y_pos += "w";
                        } else {
                            invert_y_pos += "B";
                        }

                        // KQkq = rochade noch m??glich auf der Seite des K Q sowie wei?? k q
                        if (fen.length > 3) {
                            if (fen[3].contains("K"))
                                K_Castling = true;
                            if (fen[3].contains("k"))
                                k_Castling = true;
                            if (fen[3].contains("Q"))
                                Q_Castling = true;
                            if (fen[3].contains("q"))
                                q_Castling = true;
                        }

                        if (rows == 7) {
                            game.init(invert_y_pos, k_Castling, q_Castling, K_Castling, Q_Castling);
                            isGo = false;
                            LOG.info("Das Spielfeld " + invert_y_pos + " wurde generiert");
                        }
                    } else if (cmd.equalsIgnoreCase("computer")) // der andere Computer soll als
                                                                 // erstes ziehen
                        isGo = false;
                    else if (cmd.equalsIgnoreCase("easy")) // Spiel gegen Mensch: easy =
                                                           // Startkommando
                        isGo = true;
                    else if (cmd.equalsIgnoreCase("hard")) // Spiel gegen Mensch: hard =
                                                           // Startkommando f??r xboard
                        isGo = true;
                    else if (cmd.equalsIgnoreCase("stop"))
                        isGo = false;
                    else if (cmd.equalsIgnoreCase("draw"))
                        out = "offer draw";
                    else if (cmd.equalsIgnoreCase("go")) {
                        try {
                            // Pr??fen ob Draw ist
                            if (game.getCurrentGame().getGameState() == GameState.DRAW) {
                                // out = "offer draw";
                                out = "result 1/2-1/2 {Stalemate}";
                                // okDialog("Patt!\nStalemate");
                            }
                            // Wenn nicht Matt ist
                            else if (game.getCurrentGame().getGameState() != GameState.MATT) {
                                out = "move " + game.getTurn(time);
                                isGo = true;
                            }
                            // 0-1 {Black mates}
                            // 1-0 {White mates}
                            // 1/2-1/2 {Draw by repetition} (4 repetition)
                            // 1/2-1/2 {Stalemate} (Stalemate is a situation in chess where the
                            // player whose turn it is to move is not in check but has no legal
                            // moves. A stalemate ends the game in a draw)
                            // Wenn Matt ist
                            else if (game.getCurrentGame().getGameState() == GameState.MATT) {
                                if (game.getCurrentGame().getPlayerToMakeTurn() == Player.WHITE)
                                    out = "result 0-1 {Black mates}";
                                else
                                    out = "result 1-0 {White mates}";
                                isGo = false;
                                LOG.debug("Schach Matt!\n" + out);
                                // okDialog("Schach Matt!\n"+out);
                            }
                        } catch (Exception e) {
                            String message = "Fehler beim Abrufen von getTurn() ";
                            LOG.error(message);
                        }
                    } else if (cmd.matches("([a-hA-H])+([1-8])+([a-hA-H])+([1-8])")
                            || cmd.matches("([a-hA-H])+([1-8])+([a-hA-H])+([1-8])+(.)")) {
                        // Gegnerzug auf Spielbrett ausfuehren
                        try {
                            if (game.opponentTurn(cmd)) {
                                // Pr??fen ob Draw ist
                                if (game.getCurrentGame().getGameState() == GameState.DRAW) {
                                    // out = "offer draw";
                                    out = "result 1/2-1/2 {Stalemate}";
                                    // okDialog("Patt!\nStalemate");
                                }
                                // Wenn nicht Matt ist
                                else if (game.getCurrentGame().getGameState() != GameState.MATT) {
                                    // KI berechnet naechsten bewegung und ausgabe in Output
                                    if (isGo)
                                        out = "move " + game.getTurn(time);
                                }
                                // Wenn Matt ist
                                else if (game.getCurrentGame().getGameState() == GameState.MATT) {
                                    if (game.getCurrentGame().getPlayerToMakeTurn() == Player.WHITE)
                                        out = "result 0-1 {Black mates}";
                                    else
                                        out = "result 1-0 {White mates}";
                                    isGo = false;
                                    LOG.debug("Schach Matt!\n" + out);
                                    // okDialog("Schach Matt!\n"+out);
                                }
                            } else
                                out = "Illegal move " + cmd;
                        } catch (Exception e) {
                            String message = "Fehler beim Abruf von opponentTurn(" + cmd + ")";
                            LOG.error(message);
                        }
                    } else if (cmd.equalsIgnoreCase("protover 2"))
                        out = "feature usermove=0 " + "setboard=1 " + "time=0 " + "sigint=0 "
                                + "sigterm=0 " + "draw=1 " + "reuse=1 " + "analyze=0 "
                                + "myname=\"GriScha" + GriScha.VERSION + "\" "
                                + "variants=\"normal\" " + "colors=0 "
                                + "feature option=\"Suchtiefe -string\" "
                                + "feature option=\"Suchtiefe -string blabla22\" "
                                + "feature option=\"Dummy String Example -file happy birthday!\" "
                                + "done=1 ";

                    // Ausgeben des berechneten Zuges oder Sonstiges
                    if (!out.equals("")) {
                        LOG.debug("Output: " + out);
                        System.out.println(out);
                    }
                    out = "";

                    // Ausgabe des Schachbrettes zum TESTEN auf der Console
                    // try
                    // {
                    // System.out.println(game.getCurrentGame().getReadableString());
                    // }
                    // catch (Exception e)
                    // {
                    //
                    // }
                }
                // UCI-Befehle abarbeiten
                else if (protocol.equalsIgnoreCase("uci")
                        && (cmd.equalsIgnoreCase("ucinewgame") || cmd.equalsIgnoreCase("quit")
                        // || cmd.equalsIgnoreCase("result")
                                || cmd.equalsIgnoreCase("isready") || cmd.equalsIgnoreCase("go")
                                // || cmd.equalsIgnoreCase("computer")
                                || cmd.matches("([a-hA-H])+([1-8])+([a-hA-H])+([1-8])") || cmd
                                    .matches("([a-hA-H])+([1-8])+([a-hA-H])+([1-8])+(.)"))) {
                    // TODO: UCI Kommandos verarbeiten
                    if (!out.equals(""))
                        System.out.println(out);
                    out = "";
                }
            }
        } catch (Exception e1) {
            String message = "unbekannter Fehler aufgetreten";
            LOG.error(message);
        }
    }
}
