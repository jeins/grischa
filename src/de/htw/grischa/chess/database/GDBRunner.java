package de.htw.grischa.chess.database;

import org.apache.log4j.Logger;

/**
 * Entry point with main method for database mode
 * <p>
 * Always calling {@link de.htw.grischa.chess.database.DatabaseMode#DatabaseMode(String[])}
 * </p>
 * <h3>Version History</h3>
 * <ul>
 * <li> 1.0 - 06/14 - Karsten Kochan - Initial Version</li>
 * </ul>
 *
 * @author Karsten Kochan
 * @version 1.0
 */
public class GDBRunner {
    public static final String properties = "database.properties";
    /**
     * Logger
     */
    private final static Logger log = Logger.getLogger(GDBRunner.class);

    /**
     * Entry method for GriScha Database Nodes
     *
     * @param args Array of Strings, arguments to start with containing port, size, filename
     */
    public static void main(String[] args) {
        log.info("Starting GriScha Database Node");
        DatabaseMode databaseMode;
        try {
            databaseMode = new DatabaseMode(args);
        } catch (Exception e) {
            log.fatal("Illegal commandline usage");
            return;
        }
        databaseMode.start();
    }
}
