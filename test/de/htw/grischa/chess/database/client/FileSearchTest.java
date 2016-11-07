package de.htw.grischa.chess.database.client;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class FileSearchTest {
    private static final String testDBPath = "JUnit-DB.db";
    private static final String getTestDBHash = "ea79bb51ece6835865f410a469d298db";
    private RandomAccessFile memoryMappedFile;

    public FileSearchTest() {
        try {
            this.memoryMappedFile = new RandomAccessFile(testDBPath, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConstructor() throws Exception {
        FileSearch test = new FileSearch(memoryMappedFile, false);
        assertNotNull(test);
    }

    @Test
    public void testSearchExistingEntry() throws Exception {
        FileSearch test = new FileSearch(memoryMappedFile, true);
        String target = "44761b9599fb46c9e6f667f022a7725c";
        DatabaseEntry result = test.search(target);
        assertNotNull(result);
        assertEquals(target, result.getHash());
        assertEquals(30, result.getDepth());
        assertEquals(2, result.getValue());
        assertEquals(26, result.getLine());
    }

    @Test
    public void testSearchNonExistingEntry() throws Exception {
        FileSearch test = new FileSearch(memoryMappedFile, true);
        String target = "54761b9599fb46c9e6f667f022a7725c";
        DatabaseEntry result = test.search(target);
        assertNull(result);
    }

    @Test
    public void testReadLineExisting() throws Exception {
        FileSearch test = new FileSearch(memoryMappedFile, false);
        Method readLine = test.getClass().getDeclaredMethod("readLine", new Class[]{int.class});
        readLine.setAccessible(true);
        assertEquals("65a8627a7f180baf1ef20dab142da892#0000#0002", readLine.invoke(test, 86));
        assertEquals("180baf1ef20dab142da892#0000#0002", readLine.invoke(test, 96));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadLineIllegalIndex1() throws Exception {
        FileSearch test = new FileSearch(memoryMappedFile, false);
        Method readLine = test.getClass().getDeclaredMethod("readLine", new Class[]{int.class});
        readLine.setAccessible(true);
        try {
            readLine.invoke(test, -1);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e.getCause();
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadLineIllegalIndex2() throws Exception {
        FileSearch test = new FileSearch(memoryMappedFile, false);
        Method readLine = test.getClass().getDeclaredMethod("readLine", new Class[]{int.class});
        readLine.setAccessible(true);
        try {
            readLine.invoke(test, 976654);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e.getCause();
            }
        }
    }

    @Test
    public void testPositiveHasNext() throws Exception {
        FileSearch test = new FileSearch(memoryMappedFile, false);
        Method hasNext = test.getClass().getDeclaredMethod("hasNext", new Class[]{int.class});
        hasNext.setAccessible(true);
        assertEquals(true, hasNext.invoke(test, 43));
    }

    @Test
    public void testNegativeHasNext() throws Exception {
        FileSearch test = new FileSearch(memoryMappedFile, false);
        Method hasNext = test.getClass().getDeclaredMethod("hasNext", new Class[]{int.class});
        hasNext.setAccessible(true);
        int pos = (int) memoryMappedFile.length() - 41;
        assertEquals(false, hasNext.invoke(test, pos));
    }

    @Test
    public void testSearchExistingList() throws Exception {
        FileSearch test = new FileSearch(memoryMappedFile, false);
        ArrayList<String> hashes = new ArrayList<String>();
        hashes.add("ebd1431ef99a8da38cacc3d1745ee659");
        hashes.add("14ef9733a65a5cf0fd96f6dbcd7c9bf4");
        hashes.add("af617b7f827b9fcda0fd1aff531bb8a0");
        ArrayList<DatabaseEntry> results = test.search(hashes);
        assertNotNull(results);
        //The test file contains this hashes more than one time
        assertEquals(8, results.size());
        assertEquals("ebd1431ef99a8da38cacc3d1745ee659", results.get(0).getHash());
        assertEquals("14ef9733a65a5cf0fd96f6dbcd7c9bf4", results.get(1).getHash());
        assertEquals(2, results.get(2).getValue());
    }

//    @Test(expected=IOException.class)
//    public void testIOException1() throws Exception {
//        RandomAccessFile temp = new RandomAccessFile("temp", "rw");
//        FileSearch test = new FileSearch(temp, false);
//        File file = new File("temp");
//        file.delete();
//        Method getBuffer = test.getClass().getDeclaredMethod("getBuffer");
//        getBuffer.setAccessible(true);
//        MappedByteBuffer buffer = (MappedByteBuffer)getBuffer.invoke(test);
//        assertNull(buffer);
//    }
}