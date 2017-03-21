package de.htw.grischa.chess;

import de.htw.grischa.chess.database.GDBRunner;
import de.htw.grischa.chess.database.client.CommitThread;
import de.htw.grischa.chess.database.client.DatabaseEntry;
import de.htw.grischa.chess.database.client.FileSearch;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Iterative Alpha-Beta-search for moving down inside breadth-first search
 * <p>
 * Goes down the Breadth-first search and searches every second ply for best move
 * </p>
 * <h3>Version History</h3>
 * <ul>
 * <li> 0.1 - 04/10 - Heim - Initial Version </li>
 * <li> 0.2 - 06/10 - Neumann - ??? </li>
 * <li> 0.3 - 07/11 - Laurence Bortfeld - ??? </li>
 * <li> 0.4 - 04/14 - Karsten Kochan - Integration of database, typo/language, documentation </li>
 * <li> 0.5 - 07/14 - Karsten Kochan - Added properties based variant to use or not use the database</li>
 * <li> 0.6 - 09/14 - Karsten Kochan - Fallback for missing property or missing db file</li>
 * <li> 0.7 - 02/17 - Benjamin Troester - internship -> cleanup & research</li>
 * <li> 0.8 - 02/17 - Benjamin Troester - research & preparation for Monte Carlo implementation</li>
 * </ul>
 * @version 0.6
 */
public class IterativeAlphaBetaSearch implements Runnable {
    //looger
    private final static Logger log = Logger.getLogger(IterativeAlphaBetaSearch.class);
    // Current game (board) to search the best move for
    private final IChessGame game;
    // Player to be maximized
    private final Player maximizingPlayer;
    // Best turn calculated by Alpha-Beta-Search
    public IChessGame bestTurn;
    //Current value of the board calculated
    private int value;

    /**
     * Constructor extracting player to maximize from game
     *
     * @param game IChessGame board to calculate the best move
     */
    public IterativeAlphaBetaSearch(IChessGame game) {
        this.game = game;
        this.maximizingPlayer = game.getPlayerToMakeTurn();
    }

    /**
     * Constructor given explicit player to maximize
     *
     * @param game             IChessGame board to calculate the best move
     * @param maximizingPlayer Player to maximize
     */
    public IterativeAlphaBetaSearch(IChessGame game, Player maximizingPlayer) {
        this.game = game;
        this.maximizingPlayer = maximizingPlayer;
    }

    /**
     * Getter value
     *
     * @return int value
     */
    public int getValue() {
        return value;
    }

    /**
     * threading through runnable interface
     * run method containing breadth-first-search
     *
     * Contains database usage, alpha beta search
     */
    public void run() {
        Properties database = new Properties();
        String host = null;
        int port = 0;
        String databaseFilePath = null;
        boolean useDB;
        FileSearch fileSearch = null;
        BlockingQueue<DatabaseEntry> queue = null;
        CommitThread worker;
        int depth;
        AlphaBetaSearchFixedDepth abp;

        try {
            FileInputStream propertiesFile = new FileInputStream(GDBRunner.properties);
            database.load(propertiesFile);
            propertiesFile.close();
            host = database.getProperty("grischa.DBHost");
            port = Integer.valueOf(database.getProperty("grischa.DBPort"));
            useDB = database.getProperty("grischa.useDB").equals("true");
            databaseFilePath = database.getProperty("grischa.DBPath");
        } catch (Exception e) {
            log.error("Could not detect database settings from properties file");
            useDB = false;
        }
        if (this.maximizingPlayer == game.getPlayerToMakeTurn()) {
            depth = 0;
        } else {
            depth = 1;
        }
        if (useDB) {
            log.info("This is a database based search run");
            RandomAccessFile memoryMappedFile = null;
            try {
                memoryMappedFile = new RandomAccessFile(databaseFilePath, "r");
                fileSearch = new FileSearch(memoryMappedFile, false);
                queue = new LinkedBlockingQueue<DatabaseEntry>();
            } catch (Exception e) {
                log.error("Could not create memory mapped file");
                useDB = false;
            }
            if (useDB) {
                worker = new CommitThread(queue, port, host);
                worker.start();
            }
        } else {
            log.info("This is a non databased search run");
        }
        while (true) {
            abp = new AlphaBetaSearchFixedDepth();
            abp.setUseDB(useDB);
            if (depth > 3 && useDB) {
                abp.setQueue(queue);
                abp.setFileSearch(fileSearch);
            }
            long startTime = System.nanoTime();
            value = abp.getAlphaBetaTurn(depth, game, fileSearch);
            long endTime = System.nanoTime();
            //Mark best move
            bestTurn = abp.nextGame;
            long duration = ((endTime - startTime) / 1000000 );
            //Search two plies deeper
            //depth += 2;
            log.debug("Breadth-first depth: " + depth + " Value: " + value + " calculation duration: " + duration + " ms");
            log.info("Breadth-first depth: " + depth + " Value: " + value + " calculation duration: " + duration + " ms");
            // original value 40
            if (depth > 8) {
                log.debug("Breadth-first search terminated due to maximum depth of 8");
                break;
            }
            depth += 2;
        }
    }
}

