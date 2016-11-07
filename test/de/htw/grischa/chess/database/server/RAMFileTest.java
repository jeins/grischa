package de.htw.grischa.chess.database.server;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.RandomAccessFile;

import static org.junit.Assert.*;

public class RAMFileTest {
    public static final String path = "JUnit-RAMFile.db";

    @BeforeClass
    public static void setUp() throws Exception {
        try {
            File file = new File(path);
            if (file.delete()) {
                System.out.println("Left over JUnit test file deleted");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        try {
            File file = new File(path);
            if (!file.delete()) {
                System.err.println("Could not delete JUnit test file");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConstructor() throws Exception {
        RAMFile file = new RAMFile(path, 1024);
        assertNotNull(file);
    }

    @Test
    public void testGetSize() throws Exception {
        RAMFile file = new RAMFile(path, 1024);
        assertEquals(1024, file.getSize());
    }

    @Test
    public void testCreateLock() throws Exception {
        RAMFile file = new RAMFile(path, 1024);
        assertTrue(file.createLock(5, 10));
        assertFalse(file.createLock(8, 12));
    }

    @Test
    public void testRemoveLock() throws Exception {
        RAMFile file = new RAMFile(path, 1024);
        assertTrue(file.createLock(5, 10));
        assertFalse(file.createLock(8, 12));
        file.removeLock(5, 10);
        assertTrue(file.createLock(8, 12));
    }

    @Test
    public void testToString() throws Exception {
        RAMFile file = new RAMFile(path, 1024);
        assertEquals("de.htw.grischa.database.RAMFile{size=1024,locks=[]," +
                "memoryMappedFile=java.io.RandomAccessFile@", file.toString().substring(0, 93));
        file.createLock(0, 10);
        assertEquals("de.htw.grischa.database.RAMFile{size=1024,locks=[RAMFileLock{start=0,stop=10}]," +
                "memoryMappedFile=java.io.RandomAccessFile@", file.toString().substring(0, 121));
        file.createLock(11, 15);
        assertEquals("de.htw.grischa.database.RAMFile{size=1024,locks=[RAMFileLock{start=0,stop=10}, " +
                        "RAMFileLock{start=11,stop=15}]," + "memoryMappedFile=java.io.RandomAccessFile@",
                file.toString().substring(0, 152));
        file.removeLock(11, 15);
        file.removeLock(0, 10);
        assertEquals("de.htw.grischa.database.RAMFile{size=1024,locks=[]," +
                "memoryMappedFile=java.io.RandomAccessFile@", file.toString().substring(0, 93));
    }

    @Test
    public void testGetMemoryMappedFileFail() throws Exception {
        RAMFile file = new RAMFile("test.db", 1024);
        RandomAccessFile readonly1 = file.getMemoryMappedFile();
        assertNotNull(readonly1);
        File temp = new File("test.db");
        temp.delete();
        RandomAccessFile readonly2 = file.getMemoryMappedFile();
        assertNull(readonly2);
    }
}