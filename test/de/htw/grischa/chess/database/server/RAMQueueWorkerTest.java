package de.htw.grischa.chess.database.server;

import de.htw.grischa.chess.database.client.DatabaseEntry;
import de.htw.grischa.chess.database.client.FileSearch;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import static org.junit.Assert.*;

public class RAMQueueWorkerTest {
    RAMFile file;
    private RAMQueue queue;

    public RAMQueueWorkerTest() {
        try {
            this.file = new RAMFile(RAMFileTest.path, 1024);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.queue = new RAMQueue();
    }

    @Test
    public void testRun() throws Exception {
        RAMQueueWorker worker = new RAMQueueWorker(this.queue, this.file);
        assertNotNull(worker);
        worker.start();
        assertTrue(this.queue.isEmpty());
        this.queue.addElement(new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab60c3#0001#0000"));
        assertFalse(this.queue.isEmpty());
        Thread.sleep(10);
        assertTrue(this.queue.isEmpty());
        this.queue.addElement(new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab60c3#0003#0000"));
    }

    @Test
    public void testCircularBuffer() throws Exception {
        this.file = new RAMFile(RAMFileTest.path, 171);//4 lines
        this.queue = new RAMQueue();
        RAMQueueWorker worker = new RAMQueueWorker(this.queue, this.file);
        worker.start();
        this.queue.addElement(new DatabaseEntry("0aa88f1786fb5a990863a1f6a4ab60c3#0001#0005"));
        this.queue.addElement(new DatabaseEntry("0bb88f1786fb5a990863a1f6a4ab60c3#0002#0004"));
        this.queue.addElement(new DatabaseEntry("0cc88f1786fb5a990863a1f6a4ab60c3#0003#0003"));
        this.queue.addElement(new DatabaseEntry("0dd88f1786fb5a990863a1f6a4ab60c3#0004#0002"));
        this.queue.addElement(new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab60c3#0005#0001"));
        RandomAccessFile readFile = new RandomAccessFile(RAMFileTest.path, "r");
        FileSearch test = new FileSearch(readFile, false);
        String target = "0ee88f1786fb5a990863a1f6a4ab60c3";
        Thread.sleep(25);
        DatabaseEntry result = test.search(target);
        assertNotNull(result);
        assertEquals(1, result.getValue());
        this.queue.addElement(new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab60c3#0004#0011"));
        this.queue.addElement(new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab60c3#0008#-007"));
        Thread.sleep(25);
        result = test.search(target);
        assertNotNull(result);
        assertEquals(-7, result.getValue());
    }

    @After
    public void tearDown() throws Exception {
        File file = new File(RAMFileTest.path);
        file.delete();
    }
}