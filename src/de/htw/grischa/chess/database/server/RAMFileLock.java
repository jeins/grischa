package de.htw.grischa.chess.database.server;

/**
 * A RAMFileLock blocks a part of a {@link de.htw.grischa.chess.database.server.RAMFile} for writing
 * <p>
 * Gives the possibility to write to the MemoryMappedFile with many threads without dirty read or dirty write
 * </p>
 * <h3>Version History</h3>
 * <ul>
 * <li> 1.0 - 11/13 - Karsten Kochan - Initial Version</li>
 * <li> 1.1 - 04/14 - Karsten Kochan - Changed package structure, typo/documentation</li>
 * </ul>
 *
 * @author Karsten Kochan
 * @version 1.1
 */
class RAMFileLock {
    /**
     * Integer position of first locked byte
     * <p>
     * This byte is including
     * </p>
     */
    private final int start;

    /**
     * Integer position of last locked byte
     * <p>
     * This byte is including
     * </p>
     */
    private final int stop;

    /**
     * Constructor, generates a lock from given start and end
     *
     * @param first int position of first locked byte
     * @param last  int position of last locked byte
     * @throws java.lang.IllegalArgumentException if first or last are invalid
     */
    public RAMFileLock(int first, int last) throws IllegalArgumentException {
        if (first < 0) {
            throw new IllegalArgumentException("First byte must not be smaller null");
        }
        if (last < first) {
            throw new IllegalArgumentException("Last byte must not be smaller than first byte");
        }
        this.start = first;
        this.stop = last;
    }

    /**
     * Returns false if given integer byte position is covered by this lock
     *
     * @param sector Integer position of byte to test
     * @return false if locked, true if not
     */
    boolean free(int sector) {
        return !(sector >= start && sector <= stop);
    }

    /**
     * Returns true if every byte position between and including start and stop is not locked
     *
     * @param first Integer position of first byte of block to test
     * @param last  Integer position of last byte of block to test
     * @return true if every position in block is not locked
     */
    boolean free(int first, int last) {
        for (int i = 0; i < (last - first); i++) {
            if (!this.free(first + i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Getter for first byte position of lock
     *
     * @return int position of first locked byte
     */
    public int getStart() {
        return start;
    }

    /**
     * Getter for last byte position of lock
     *
     * @return int position of last locked byte
     */
    public int getStop() {
        return stop;
    }

    @Override
    public String toString() {
        return "RAMFileLock{" + "start=" + start + ",stop=" + stop + '}';
    }
}
