package de.htw.grischa.chess.database.server;

import org.junit.Test;

import static org.junit.Assert.assertNull;

public class WriteManagerTest {

    @Test(expected = IllegalAccessError.class)
    public void testConstructor0() throws Exception {
        int port = 11223;
        int size = 1024;
        WriteManager writeManager = new WriteManager(port, size, null);
        assertNull(writeManager);
    }

    @Test(expected = IllegalAccessError.class)
    public void testConstructor1() throws Exception {
        String fileName = "";
        int port = 11223;
        int size = 1024;
        WriteManager writeManager = new WriteManager(port, size, fileName);
        assertNull(writeManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor2() throws Exception {
        String fileName = "test.db";
        int port = 24;
        int size = 1024;
        WriteManager writeManager = new WriteManager(port, size, fileName);
        assertNull(writeManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor3() throws Exception {
        String fileName = "test.db";
        int port = 9879878;
        int size = 1024;
        WriteManager writeManager = new WriteManager(port, size, fileName);
        assertNull(writeManager);
    }
}