package de.htw.grischa.chess.database.server;

import de.htw.grischa.chess.database.client.DatabaseEntry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class RAMQueueTest {
    private RAMQueue queue;
    private String dbEntry1 = "0ee88f1786fb5a990863a1f6a4ab60c3#0001#0000";
    private String dbEntry2 = "0ee88f1786fb5a990863a1f6a4ab60c3#0002#0000";
    private String dbEntry3 = "0ee88f1786fb5a990863a1f6a4ab60c3#0003#0000";
    private String dbEntry4 = "0ee88f1786fb5a990863a1f6a4ab60c3#0004#0000";
    private DatabaseEntry entry1;
    private DatabaseEntry entry2;
    private DatabaseEntry entry3;
    private DatabaseEntry entry4;

    @Before
    public void setUp() throws Exception {
        this.entry1 = new DatabaseEntry(this.dbEntry1);
        this.entry2 = new DatabaseEntry(this.dbEntry2);
        this.entry3 = new DatabaseEntry(this.dbEntry3);
        this.entry4 = new DatabaseEntry(this.dbEntry4);
        this.queue = new RAMQueue();
    }

    @Test
    public void testGetElement() throws Exception {
        this.queue.addElement(this.entry1);
        this.queue.addElement(this.entry2);
        DatabaseEntry current = this.queue.getElement();
        assertEquals(1, current.getDepth());
    }

    @Test
    public void testAddElement() throws Exception {
        this.queue.addElement(this.entry1);
        this.queue.addElement(this.entry2);
        this.queue.addElement(this.entry3);
        this.queue.addElement(this.entry4);
        assertFalse(this.queue.isEmpty());
        assertEquals(4, this.queue.size());
        assertEquals(1, this.queue.getElement().getDepth());
        assertEquals(2, this.queue.getElement().getDepth());
        assertEquals(3, this.queue.getElement().getDepth());
        assertEquals(4, this.queue.getElement().getDepth());
        assertTrue(this.queue.isEmpty());
    }
}