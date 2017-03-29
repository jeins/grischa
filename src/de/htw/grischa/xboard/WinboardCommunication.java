package de.htw.grischa.xboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;
import de.htw.grischa.GriScha;
import de.htw.grischa.chess.GameState;
import de.htw.grischa.chess.GridGameManager;
import de.htw.grischa.chess.Player;

/**
 * This class provides the communication interface to Xboard
 *
 * * <h3>Version History</h3>
 * <ul>
 * <li> 0.1
 * </ul>
 */

public class WinboardCommunication {
    //Loger for everything concerning communication with WinBoard
    private final static Logger LOG = Logger.getLogger(WinboardCommunication.class);
    // time how long GriScha has time to calculate it's move
    private static int time = 15000;

    public void run() {
        GridGameManager game;
        try {
            game = new GridGameManager();//get instance of GridGameManager
            String cmd = "";//command string
            String out = "";// output string
            BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
            String protocol = "";
            boolean isGo = false;//whose turn is it - default white-> GriScha is the black opponent, so false
            //because white has the initial move

            //game loop
            while (true) {
                try {
                    // check if match is drawn - no winner
                    if (game.getCurrentGame() != null && isGo
                            && game.getCurrentGame().getGameState() == GameState.DRAW) {
                        // out = "offer draw";
                        out = "result 1/2-1/2 {Stalemate}";
                        // okDialog("Patt! " + out);
                    }
                    // check if mate is given - who check mates
                    else if (game.getCurrentGame() != null && isGo &&
                            game.getCurrentGame().getGameState() == GameState.MATT) {
                        if (game.getCurrentGame().getPlayerToMakeTurn() == Player.WHITE)
                            out = "result 0-1 {Black mates}";
                        else
                            out = "result 1-0 {White mates}";
                        isGo = false;
                        System.out.println(out);
                        LOG.info("Check Mate" + out);
                    }
                    // read input from cmd
                    cmd = bin.readLine();
                    LOG.debug("Input: " + cmd);
                } catch (IOException e) {
                    String message = "Error while reading from STDIN";
                    LOG.error(message);
                }
                /*
                 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                 * Command line output
                 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                 */

                // close/exit program
                if (cmd.equalsIgnoreCase("q") || cmd.equalsIgnoreCase("exit")) {
                    System.out.println("exiting GriScha-Chess");
                    System.exit(0);
                }
                // Xboard is the used protocol
                else if (cmd.equalsIgnoreCase("xboard")) {
                    protocol = "xboard";
                }
                // UCI is the used protocol
                else if (cmd.equalsIgnoreCase("uci")) {
                    protocol = "uci";
                    System.out.printf("id name GriScha%n");
                    System.out.printf("id author HTW Berlin%n");
                    System.out.printf("uciok%n");
                }
                // process XBoard instructions
                else if (protocol.equalsIgnoreCase("xboard")) {
                    //new game - initial std board
                    if (cmd.equalsIgnoreCase("new")) {
                        game.init();
                        isGo = false;
                        LOG.info("generate new chessboard");
                        LOG.debug("generate new chessboard");
                    } else if (cmd.equalsIgnoreCase("quit")) {
                        LOG.info("game finished");
                        LOG.debug("game finished");
                        System.exit(0);
                    }

                    // result 1-0 {Xboard adjudication: Checkmate}
                    // result 1/2-1/2 {xboard exit but bare king}
                    // result 0-1 {White resigns}
                    // TODO: output with game of chess
                    else if (cmd.startsWith("result")) {
                        String[] fen = new String[7];
                        fen = cmd.split("[ -]");
                        String message = "";
                        int k;
                        if (fen[1].equals("*"))
                            k = 2;
                        else
                            k = 3;

                        // read reason for end chess game
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
                        //State -> draw, check mate
                        if (fen[1].equals("*"))
                            LOG.debug("Xboard exited " + message);//exited
                        else if (!fen[1].equals("0") && !fen[2].equals("0")) // Draw
                            LOG.info("Draw");
                        else if (!fen[1].equals("0") && fen[2].equals("0")) // White wins
                            LOG.info("white wins");
                        else if (fen[1].equals("0") && !fen[2].equals("0"))// Black wins
                            LOG.info("black wins");
                    }

                    else if (cmd.equalsIgnoreCase("force"))
                        isGo = false;

                    // Setup of non standard board
                    else if (cmd.startsWith("setboard")) {
                        // TODO: getting 0 1 from FEN notation
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
                        // mirroring along the x axis
                        for (int j = new_pos.length(); j > 0; j--) {
                            invert_x_pos += new_pos.charAt(j - 1);
                        }

                        // mirroring along the y axis
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

                        /*
                         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                         * Checking for castling
                         * KQkq = castling still possible at king(K)/ queen(Q) flank
                         * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                         */
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
                            LOG.info("The chessboard " + invert_y_pos + " was generated!");
                        }
                    /*
                    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                    * Whose turn is it?
                    * Computer vs. Human
                    * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                    */
                    } else if (cmd.equalsIgnoreCase("computer")) // opponent machine should start
                        isGo = false;
                    else if (cmd.equalsIgnoreCase("easy")) // game vs human: easy =
                        isGo = true;
                    else if (cmd.equalsIgnoreCase("hard")) // game vs human: hard =
                        isGo = true;
                    else if (cmd.equalsIgnoreCase("stop"))
                        isGo = false;
                    else if (cmd.equalsIgnoreCase("draw"))
                        out = "offer draw";
                    else if (cmd.equalsIgnoreCase("go")) {
                        try {
                            // check if game is draw
                            if (game.getCurrentGame().getGameState() == GameState.DRAW)
                                out = "result 1/2-1/2 {Stalemate}";
                            // if is not mate
                            else if (game.getCurrentGame().getGameState() != GameState.MATT) {
                                out = "move " + game.getTurn(time);
                                isGo = true;
                            }
                            /*
                             * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
                             * 0-1 {Black mates}
                             * 1-0 {White mates}
                             * 1/2-1/2 {Draw by repetition} (4 repetition)
                             * 1/2-1/2 {Stalemate} (Stalemate is a situation in chess where the
                             * player whose turn it is to move is not in check but has no legal
                             * moves. A stalemate ends the game in a draw)
                             * state is mate
                             */
                            else if (game.getCurrentGame().getGameState() == GameState.MATT) {
                                if (game.getCurrentGame().getPlayerToMakeTurn() == Player.WHITE)
                                    out = "result 0-1 {Black mates}";
                                else
                                    out = "result 1-0 {White mates}";
                                isGo = false;
                                LOG.info("Check Mate\n" + out);
                            }
                        } catch (Exception e) {
                            String message = "Error during invoke of getTurn() method!";
                            LOG.error(message);
                        }
                    } else if (cmd.matches("([a-hA-H])+([1-8])+([a-hA-H])+([1-8])") ||
                            cmd.matches("([a-hA-H])+([1-8])+([a-hA-H])+([1-8])+(.)")) {
                        // perform opponent move on chessboard
                        try {
                            if (game.opponentTurn(cmd)) {
                                // check if state is draw
                                if (game.getCurrentGame().getGameState() == GameState.DRAW)
                                    out = "result 1/2-1/2 {Stalemate}";
                                // if not mate
                                else if (game.getCurrentGame().getGameState() != GameState.MATT) {
                                    // AI calculates next move und prints out
                                    if (isGo)
                                        out = "move " + game.getTurn(time);
                                }
                                // if mate
                                else if (game.getCurrentGame().getGameState() == GameState.MATT) {
                                    if (game.getCurrentGame().getPlayerToMakeTurn() == Player.WHITE)
                                        out = "result 0-1 {Black mates}";
                                    else
                                        out = "result 1-0 {White mates}";
                                    isGo = false;
                                    LOG.debug("Check Mate!\n" + out);
                                }
                            } else
                                out = "Illegal move " + cmd;
                        } catch (Exception e) {
                            String message = "Error during invoke of opponentTurn(" + cmd + ") method";
                            LOG.error(message);
                        }
                    }
                    else if (cmd.equalsIgnoreCase("protover 2"))
                        out = "feature usermove=0 " + "setboard=1 " + "time=0 " + "sigint=0 "
                                + "sigterm=0 " + "draw=1 " + "reuse=1 " + "analyze=0 "
                                + "myname=\"GriScha" + GriScha.VERSION + "\" "
                                + "variants=\"normal\" " + "colors=0 "
                                + "feature option=\"Suchtiefe -string\" "
                                + "feature option=\"Suchtiefe -string blabla22\" "
                                + "feature option=\"Dummy String Example -file happy birthday!\" "
                                + "done=1 ";

                    // output of calculated move or other
                    if (!out.equals("")) {
                        LOG.info("Output: " + out);
                        System.out.println(out);
                    }
                    out = "";
                }
                // proceed UCI commands
                else if (protocol.equalsIgnoreCase("uci")
                        && (cmd.equalsIgnoreCase("ucinewgame") || cmd.equalsIgnoreCase("quit")
                        // || cmd.equalsIgnoreCase("result")
                                || cmd.equalsIgnoreCase("isready") || cmd.equalsIgnoreCase("go")
                                // || cmd.equalsIgnoreCase("computer")
                                || cmd.matches("([a-hA-H])+([1-8])+([a-hA-H])+([1-8])") || cmd
                                    .matches("([a-hA-H])+([1-8])+([a-hA-H])+([1-8])+(.)"))) {
                    // TODO: UCI commands process
                    if (!out.equals(""))
                        System.out.println(out);
                    out = "";
                }
            }
        } catch (Exception e1) {
            String message = "unknown ERROR occurred!";
            LOG.error(message);
        }
    }
}
