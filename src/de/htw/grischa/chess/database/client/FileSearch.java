package de.htw.grischa.chess.database.client;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Class for searching for games hashes in the database
 * <h3>Version History</h3>
 * <ul>
 * <li> 1.0 - 04/14 - Karsten Kochan - Initial Version</li>
 * </ul>
 *
 * @author Karsten Kochan
 * @version 1.0
 */
public class FileSearch {

    /**
     * Logger
     */
    private final static Logger log = Logger.getLogger(FileSearch.class);

    /**
     * File mapped into RAM
     */
    private final RandomAccessFile file;

    /**
     * If timing the search process is needed
     */
    private boolean timed = false;

    /**
     * Constructor
     *
     * @param file  RandomAccessFile to search in
     * @param timed if timing the search is needed
     */
    public FileSearch(RandomAccessFile file, boolean timed) {
        this.file = file;
        this.timed = timed;
    }

    /**
     * Searches the file for a given single hash
     * <p>
     * Returns null if not found
     * </p>
     * <p>
     * This method will only return the first occurrence of the target string even if it is more than one time inside
     * the file. If you need to find all occurrences of a single target use
     * {@link de.htw.grischa.chess.database.client.FileSearch#search(java.util.ArrayList)} with an ArrayList size one.
     * </p>
     *
     * @param target String to search for
     * @return DatabaseEntry if found, else null
     */
    public DatabaseEntry search(String target) {
        ArrayList<String> temp = new ArrayList<String>();
        temp.add(target);
        ArrayList<DatabaseEntry> result = search(temp);
        if (result == null || result.size() == 0) {
            return null;
        } else {
            return result.get(0);
        }
    }

    /**
     * Searches the file for a given list of hashes
     * <p>
     * Returns null if not found anything
     * </p>
     * <p>
     * This method will return every occurrence of every target inside the file.
     * </p>
     *
     * @param targets ArrayList<String> to search for
     * @return ArrayList<DatabaseEntry> if found, else null
     */
    public ArrayList<DatabaseEntry> search(ArrayList<String> targets) {
        ArrayList<DatabaseEntry> result = new ArrayList<DatabaseEntry>();
        int index = 0;
        long start_time = 0;
        long end_time;
        if (timed) {
            start_time = System.nanoTime();
        }
        while (hasNext(index)) {
            String line = readLine(index);
            DatabaseEntry temp = new DatabaseEntry(line);
            for (String target : targets) {
                if (temp.getHash().equals(target)) {
                    log.info("Matched the game state in database: " + line);
                    log.trace(line + " at line " + index / (DatabaseEntry.TARGET_LENGTH + 1));
                    temp.setIndex(index);
                    result.add(temp);
                }
            }
            index = DatabaseEntry.getNextLineIndex(index);
        }
        if (timed) {
            end_time = System.nanoTime();
            double difference = (end_time - start_time) / 1e6;
            log.debug(
                "It took " + difference + "ms to search " + index / (DatabaseEntry.TARGET_LENGTH + 1) + " entries");
        }
        if (result.size() == 0) {
            return null;
        } else {
            return result;
        }
    }

    /**
     * Returns a MappedByteBuffer with the size of the file to read from
     * <p>
     * ByteBuffer is always read only
     * </p>
     *
     * @return MappedByteBuffer to read from
     */
    private MappedByteBuffer getBuffer() {
        try {
            return this.file.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, this.file.length());
        } catch (IOException e) {
            log.error("Could not generate Buffer from RandomAccessFile");
            return null;
        }
    }

    /**
     * Checks if the MappedByteBuffer contains a next entry line
     *
     * @param index position to start at
     * @return true if available
     */
    private boolean hasNext(int index) {
        MappedByteBuffer buffer = this.getBuffer();
        char c;
        try {
            c = (char) buffer.get(index + DatabaseEntry.TARGET_HASH_LENGTH);
        } catch (IndexOutOfBoundsException e) {
            //log.trace("Reached end of file inside hasNext");
            return false;
        }
        return c == DatabaseEntry.SEGMENTS_DELIMITER;
    }

    /**
     * Read one line terminated by '\n' from MappedByteBuffer
     *
     * @param index byte position to start
     * @return String line
     * @throws java.lang.IllegalArgumentException if index not in range of file
     */
    private String readLine(int index) throws IllegalArgumentException {
        MappedByteBuffer buffer = this.getBuffer();
        int i = index;
        StringBuilder res = new StringBuilder();
        try {
            if (index < 0 || index > this.file.length()) {
                throw new IllegalArgumentException("Index not in range of file");
            }
        } catch (IOException e) {
            log.error("Could not read from file");
        }
        boolean limit = false;
        while (!limit) {
            char cur;
            try {
                cur = (char) buffer.get(i);
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Index not in range of file");
            }
            if (cur != '\n') {
                res.append(cur);
                i++;
            } else {
                limit = true;
            }
        }
        return res.toString();
    }
}
