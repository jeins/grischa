package de.htw.grischa.chess.database.server;

import org.junit.Test;

import static org.junit.Assert.*;

public class RAMFileLockTest {

    @Test
    public void testConstructor() throws Exception {
        RAMFileLock lock = new RAMFileLock(0, 10);
        assertNotNull(lock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConstructor() throws Exception {
        RAMFileLock lock = new RAMFileLock(-1, 10);
        assertNull(lock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConstructor2() throws Exception {
        RAMFileLock lock = new RAMFileLock(5, 4);
        assertNull(lock);
    }

    @Test
    public void testFree() throws Exception {
        RAMFileLock lock = new RAMFileLock(0, 10);
        assertTrue(lock.free(11));
    }

    @Test
    public void testFree1() throws Exception {
        RAMFileLock lock = new RAMFileLock(0, 10);
        assertFalse(lock.free(5));
    }

    @Test
    public void testFree2() throws Exception {
        RAMFileLock lock = new RAMFileLock(0, 10);
        assertFalse(lock.free(3, 7));
    }

    @Test
    public void testFree3() throws Exception {
        RAMFileLock lock = new RAMFileLock(0, 10);
        assertFalse(lock.free(7, 12));
    }

    @Test
    public void testFree4() throws Exception {
        RAMFileLock lock = new RAMFileLock(0, 10);
        assertTrue(lock.free(11, 20));
    }

    @Test
    public void testGetStart() throws Exception {
        RAMFileLock lock = new RAMFileLock(0, 10);
        assertEquals(0, lock.getStart());
    }

    @Test
    public void testGetStop() throws Exception {
        RAMFileLock lock = new RAMFileLock(0, 10);
        assertEquals(10, lock.getStop());
    }

    @Test
    public void testToString() throws Exception {
        RAMFileLock lock = new RAMFileLock(0, 10);
        assertEquals("RAMFileLock{start=0,stop=10}", lock.toString());
    }
}