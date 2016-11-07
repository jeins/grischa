package de.htw.grischa.chess.database;

import de.htw.grischa.chess.database.server.WriteManager;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Running mode for database
 * <h3>Version History</h3>
 * <ul>
 * <li> 1.0 - 06/14 - Karsten Kochan - Initial Version</li>
 * </ul>
 *
 * @author Karsten Kochan
 * @version 1.0
 */
public class DatabaseMode {
    /**
     * Logger
     */
    private final static Logger log = Logger.getLogger(DatabaseMode.class);

    /**
     * Filename, may contain path, to use for database file
     */
    private String fileName;

    /**
     * Port to open
     */
    private int port;

    /**
     * Maximum file size for database file
     */
    private int size;

    /**
     * WriteManager to start
     */
    private WriteManager writeManager;

    /**
     * Constructor, returning null on invalid arguments with throwing exception
     *
     * @param args Array of Strings, expecting Port, Size, FileName
     * @throws MissingArgumentException           If no arguments have been provided
     * @throws java.lang.IllegalArgumentException If illegal arguments have been provided
     */
    public DatabaseMode(String[] args) throws MissingArgumentException, IllegalArgumentException {
        if (args == null || args.length == 0) {
            log.fatal("No arguments provided");
            throw new MissingArgumentException("No arguments provided");
        } else if (checkInputArguments(args)) {
            try {
                this.writeManager = new WriteManager(this.port, this.size, this.fileName);
            } catch (IOException e) {
                log.error("Could not open or create database file");
                throw new IllegalArgumentException("Invalid database file");
            }
        } else {
            throw new IllegalArgumentException("Incorrect arguments provided");
        }
    }

    /**
     * Checking of the provided arguments match the programs assumption
     * <p>
     * Expected is array of 3 String containing port, size, filename
     * </p>
     * <p>
     * Manipulates the class members to set port, size and filename for valid arguments
     * </p>
     *
     * @param args Array of Strings to check
     * @return true if valid, false if note
     */
    private boolean checkInputArguments(String[] args) {
        if (args.length != 3) {
            return false;
        }
        try {
            this.port = Integer.parseInt(args[0]);
            this.size = Integer.parseInt(args[1]);
            this.fileName = args[2];
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Starting the WriteManager created by constructor
     */
    public void start() {
        this.writeManager.start();
    }
}
