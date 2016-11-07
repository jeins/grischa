package de.htw.grischa.chess.database.server;

import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

/**
 * RAMFile is a possibility to use MemoryMappedFiles with more than one thread
 * <p>
 * Gives the possibility to write to the MemoryMappedFile with many threads without dirty read or dirty write
 * </p>
 * <h3>Version History</h3>
 * <ul>
 * <li> 1.0 - 11/13 - Karsten Kochan - Initial Version</li>
 * <li> 1.1 - 04/14 - Karsten Kochan - Changed package structure, typo/documentation, added size()
 * </li>
 * </ul>
 *
 * @author Karsten Kochan
 * @version 1.1
 */
class RAMFile {
    /**
     * Logger
     */
    private final static Logger log = Logger.getLogger(RAMFile.class);

    /**
     * int value for file size
     */
    private final int size;
    /**
     * Vector of {@link de.htw.grischa.chess.database.server.RAMFileLock} for this file
     */
    private final Vector<RAMFileLock> locks = new Vector<RAMFileLock>();
    /**
     * Path to file in filesystem
     */
    private String path;
    /**
     * RandomAccessFile as InMemory-Representation of the file
     */
    private RandomAccessFile memoryMappedFile;

    /**
     * Constructor
     *
     * @param path     String path to create the file, needs to be able to access
     * @param fileSize Integer byte count for file size
     * @throws java.io.FileNotFoundException      If there is any error with create filesystem representation of RAMFile
     * @throws java.lang.IllegalArgumentException if fileSize is null or less
     */
    public RAMFile(String path, int fileSize) throws FileNotFoundException, IllegalArgumentException {
        if (fileSize <= 0) {
            throw new IllegalArgumentException("File size must not be null or less");
        }
        this.size = fileSize;
        this.path = path;
        this.memoryMappedFile = new RandomAccessFile(path, "rw");
    }

    /**
     * Provides a read-only version of memoryMappedFile
     *
     * @return read-only RandomAccessFile
     */
    public RandomAccessFile getMemoryMappedFile() {
        try {
            return new RandomAccessFile(this.path, "r");
        } catch (FileNotFoundException e) {
            log.fatal("Lost file while operation");
            return null;
        }
    }

    /**
     * Returns the size, of {@link de.htw.grischa.chess.database.server.RAMFile#size}
     *
     * @return Integer size
     * @see RAMFile#size
     */
    public int getSize() {
        return size;
    }

    /**
     * Creates a lock in {@link de.htw.grischa.chess.database.server.RAMFile#locks} before writing.
     * <p>
     * This Method is Thread-Critical and therefor synchronized
     * </p>
     *
     * @param start Integer byte position of lock start
     * @param stop  Integer byte position of lock end
     * @return true if lock was created
     */
    synchronized boolean createLock(int start, int stop) {
        if (!(locks.size() == 0)) {
            for (RAMFileLock lock : locks) {
                if (!lock.free(start, stop)) {
                    return false;
                }
            }
        }
        locks.add(new RAMFileLock(start, stop));
        return true;
    }

    /**
     * Removes a lock from {@link de.htw.grischa.chess.database.server.RAMFile#locks} after writing.
     * <p>
     * This Method is Thread-Critical and therefor synchronized
     * </p>
     *
     * @param start Integer byte position of lock start
     * @param stop  Integer byte position of lock end
     */
    synchronized void removeLock(int start, int stop) {
        if (this.locks.size() == 0) {
            return;
        }
        for (int i = 0; i < this.locks.size(); i++) {
            if (this.locks.elementAt(i).getStart() == start && this.locks.elementAt(i).getStop() == stop) {
                this.locks.remove(i);
                return;
            }
        }
    }

    /**
     * Requests to write message at specific byte area in
     * {@link de.htw.grischa.chess.database.server.RAMFile#memoryMappedFile}
     *
     * @param start   Integer byte position on file to start
     * @param length  Integer byte sequence length to write
     * @param message String ASCII bytes to write
     */
    public void write(int start, int length, String message) {
        if (start + length - 1 < size) {
            boolean locked = false;
            while (!locked) {
                locked = this.createLock(start, start + length - 1);
            }
            MappedByteBuffer out = null;
            try {
                out = this.memoryMappedFile.getChannel().map(FileChannel.MapMode.READ_WRITE, start, length);
            } catch (IOException e) {
                log.error("Channel could not been opened for writing");
            }
            for (int i = 0; i < length; i++) {
                assert out != null;
                out.put((byte) message.charAt(i));
            }
            this.removeLock(start, start + length - 1);
        }
    }

    /**
     * Generates String representation of the Object
     *
     * @return String containing size and locks
     */
    @Override
    public String toString() {
        return "de.htw.grischa.database.RAMFile{size=" + size + ",locks=" + locks +
                ",memoryMappedFile=" + memoryMappedFile + "}";
    }
}
