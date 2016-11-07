package de.htw.grischa.chess.database;

import org.apache.commons.cli.MissingArgumentException;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DatabaseModeTest {
    private String fileName = "ram.db";

    @Test
    public void testDatabaseMode() throws Exception {
        String[] args = {"5511", "1024", fileName};
        DatabaseMode databaseMode = new DatabaseMode(args);
        assertNotNull(databaseMode);
    }

    @Test(expected = MissingArgumentException.class)
    public void testDatabaseModeFromNull() throws Exception {
        DatabaseMode databaseMode = new DatabaseMode(null);
        assertNotNull(databaseMode);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDatabaseModeFromIllegal1() throws Exception {
        String[] args = {"5511", "1024", fileName, "127.0.0.1"};
        DatabaseMode databaseMode = new DatabaseMode(args);
        assertNull(databaseMode);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDatabaseModeFromIllegal2() throws Exception {
        String[] args = {"5511", "1s024", fileName};
        DatabaseMode databaseMode = new DatabaseMode(args);
        assertNull(databaseMode);
    }

    @After
    public void tearDown() throws Exception {
        File file = new File(fileName);
        file.delete();
    }

    @Test
    public void testGDBRunner() throws Exception {
        String[] args = {"5511", "1024", fileName, "127.0.0.1"};
        GDBRunner.main(args);
    }
}