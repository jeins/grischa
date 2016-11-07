package de.htw.grischa;

import de.htw.grischa.chess.ChessBoard;
import de.htw.grischa.chess.IChessGame;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * Author: Karsten Kochan
 * Date: 31.07.14
 * Time: 11:15
 */
public class BoardCalculator {

    public static void main(String[] args) {
        int depth = 4;
        for (int i = 1; i < depth + 1; i++) {
            System.out.println(i + ": " + calc(i));
        }
    }

    public static int calc(int depth) {
        ArrayList<IChessGame> source = new ArrayList<IChessGame>();
        source.add(ChessBoard.getStandardChessBoard());
        int count = 0;
        for (int i = 0; i < depth; i++) {
            count = 0;
            ArrayList<IChessGame> next = new ArrayList<IChessGame>();
            for (IChessGame aSource : source) {
                ArrayList<IChessGame> temp = aSource.getNextTurns();
                count += temp.size();
                next.addAll(temp);
            }
            source = new ArrayList<IChessGame>(next);
        }
        return count;
    }
}
