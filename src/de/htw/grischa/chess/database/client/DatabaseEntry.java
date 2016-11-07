package de.htw.grischa.chess.database.client;

import java.util.regex.Pattern;

/**
 * Entry type to cover all information of one single database entry
 * <h3>Version History</h3>
 * <ul>
 * <li> 1.0 - 04/14 - Karsten Kochan - Initial Version</li>
 * <li> 1.1 - 06/14 - Karsten Kochan - Documentation, typo, added setDepth</li>
 * </ul>
 *
 * @author Karsten Kochan
 * @version 1.1
 */
public class DatabaseEntry {

    /**
     * Delimiter between segments in database
     */
    public static final char SEGMENTS_DELIMITER = '#';

    /**
     * Length of an database entry, not including newline
     */
    public static final int TARGET_LENGTH = 42;

    /**
     * Length of hash as string
     */
    public static final int TARGET_HASH_LENGTH = 32;

    /**
     * Number of segments string representation of object need to persist of
     */
    private static final int TARGET_SEGMENTS = 3;

    /**
     * Length of value as string with leading null
     */
    private static final int TARGET_VALUE_LENGTH = 4;

    /**
     * Length of depth as string with leading null
     */
    private static final int TARGET_DEPTH_LENGTH = 4;

    /**
     * Minimum for value
     */
    private static final int VALUE_MIN = -150;

    /**
     * Maximum for value
     */
    private static final int VALUE_MAX = 150;

    /**
     * Minimum for depth
     */
    private static final int DEPTH_MIN = 0;

    /**
     * Maximum for depth
     */
    private static final int DEPTH_MAX = 150;

    /**
     * md5-hash of the board
     *
     * @see de.htw.grischa.chess.ChessBoard#toDatabase(de.htw.grischa.chess.Player, int)
     */
    private String hash;

    /**
     * Calculated depth of Alpha-Beta-Search generated the value
     */
    private int depth;

    /**
     * Calculated value for current player
     */
    private int value;

    /**
     * If this entry was created as part of an existing file, the index contains the byte position as int to find the
     * beginning of the entry. Index byte is including
     */
    private int index;

    /**
     * If this entry was created as part of an existing file, the line contains the line number (single line end), to
     * find the entry
     */
    private int line;

    /**
     * Constructor from single values
     *
     * @param hash  String with hash
     * @param value int with value
     * @param depth int with depth
     * @throws java.lang.IllegalArgumentException if input string is not valid
     */
    public DatabaseEntry(String hash, int depth, int value) throws IllegalArgumentException {
        parameterCheck(hash, depth, value);
        this.hash = hash;
        this.value = value;
        this.depth = depth;
    }

    /**
     * Constructor from single string
     * <p>
     * Target: 0ee88f1786fb5a990863a1f6a5ab60c3#0001#0004
     * </p>
     *
     * @param entry String to build object from
     * @throws java.lang.IllegalArgumentException if input string is not valid
     */
    public DatabaseEntry(String entry) throws IllegalArgumentException {
        if (entry == null || entry.length() != TARGET_LENGTH) {
            throw new IllegalArgumentException("String length does not match or null");
        }
        String[] segments = entry.split(Pattern.quote(String.valueOf(SEGMENTS_DELIMITER)));
        if (segments.length != TARGET_SEGMENTS ||
            segments[0].length() != TARGET_HASH_LENGTH ||
            segments[1].length() != TARGET_DEPTH_LENGTH ||
            segments[2].length() != TARGET_VALUE_LENGTH) {
            throw new IllegalArgumentException("Segments lengths does not match or " +
                "number of segments is to high or low");
        }
        String temp_hash = segments[0];
        int temp_depth = Integer.valueOf(segments[1]);
        int temp_value = Integer.valueOf(segments[2]);
        parameterCheck(temp_hash, temp_depth, temp_value);
        this.hash = temp_hash;
        this.depth = temp_depth;
        this.value = temp_value;
    }

    /**
     * Calculates the line number of a given index
     *
     * @param index int byte position to get line of
     * @return int line number
     */
    public static int getLine(int index) {
        return index / (DatabaseEntry.TARGET_LENGTH + 1);
    }

    /**
     * Returns the byte index starting the next line
     * <p>
     * Make sure to enter the first byte of the current line as param
     * </p>
     *
     * @param index int current first line byte index
     * @return int next line byte index
     */
    public static int getNextLineIndex(int index) {
        return index + TARGET_LENGTH + 1;
    }

    /**
     * Helping function to get leading null for integer values
     * <p>
     * convert( 4,5) returns "00004"
     * convert(-4,5) returns "-0004"
     * </p>
     *
     * @param number target value to be converted
     * @param digit  number of digits needed
     * @return Converted String with leading null
     */
    public static String convert(int number, int digit) {
        String buffer = String.valueOf(number);
        while (buffer.length() != digit)
            if (number < 0) {
                buffer = "-0" + buffer.substring(1);
            } else {
                buffer = "0" + buffer;
            }
        return buffer;
    }

    /**
     * Checks incoming parameters if valid due to static constants
     *
     * @param temp_hash  String for hash
     * @param temp_depth int for depth
     * @param temp_value int for value
     * @throws java.lang.IllegalArgumentException if input parameters are invalid
     */
    private void parameterCheck(String temp_hash, int temp_depth, int temp_value) throws IllegalArgumentException {
        if (!temp_hash.matches("^[a-z0-9]{" + TARGET_HASH_LENGTH + "}$")) {
            throw new IllegalArgumentException("Hash needs to be alphanumeric md5");
        }
        if (temp_depth < DEPTH_MIN || temp_depth > DEPTH_MAX) {
            throw new IllegalArgumentException("Depth to high or low");
        }
        if (temp_value < VALUE_MIN || temp_value > VALUE_MAX) {
            throw new IllegalArgumentException("Value to high or low");
        }
    }

    /**
     * Converts object to String representation
     *
     * @return Concatenated string with "#" as delimiter
     */
    @Override
    public String toString() {
        return this.hash + SEGMENTS_DELIMITER + convert(depth, 4) + SEGMENTS_DELIMITER + convert(value, 4);
    }

    /**
     * Getter hash
     *
     * @return String hash
     */
    public String getHash() {
        return hash;
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
     * Getter depth
     *
     * @return int depth
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Setter for depth
     *
     * @param depth int depth to set
     * @throws IllegalArgumentException if depth is negative
     */
    public void setDepth(int depth) throws IllegalArgumentException {
        if (depth < 0) {
            throw new IllegalArgumentException("Depth must not be negative");
        }
        this.depth = depth;
    }

    /**
     * Getter index
     *
     * @return int index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Setter index
     *
     * @param index int index
     * @throws java.lang.IllegalArgumentException if index is negative
     */
    public void setIndex(int index) throws IllegalArgumentException {
        if (index < 0) {
            throw new IllegalArgumentException("Index must not be negative");
        }
        this.index = index;
        this.line = index / (DatabaseEntry.TARGET_LENGTH + 1);
    }

    /**
     * Getter line
     *
     * @return int line
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the line index from the last returned index of the entry
     *
     * @return int line index
     * @see de.htw.grischa.chess.database.client.DatabaseEntry#getNextLineIndex(int)
     */
    public int getNextLineIndex() {
        return getNextLineIndex(this.index);
    }
}
