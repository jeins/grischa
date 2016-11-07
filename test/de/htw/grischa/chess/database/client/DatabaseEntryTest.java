package de.htw.grischa.chess.database.client;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class DatabaseEntryTest {
    @Test
    public void testValidConstructorFromString() throws Exception {
        DatabaseEntry test = new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab60c3#0001#0000");
        assertNotNull(test);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConstructorFromString() throws Exception {
        DatabaseEntry test = new DatabaseEntry("0ee88f1786fb5a990863a1f6aeb60c3#0001#0000");
        System.out.println(test.toString());
    }

    @Test
    public void testConstructorVariants() throws Exception {
        DatabaseEntry test1 = new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab60c3#0001#0000");
        String hash = "0ee88f1786fb5a990863a1f6a4ab60c3";
        int value = 0;
        int depth = 1;
        DatabaseEntry test2 = new DatabaseEntry(hash, depth, value);
        assertEquals(test1.toString(), test2.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDepthMinValue() throws Exception {
        String source = "0ee88f1786fb5a990863a1f6a4ab60c3#-001#0000";
        DatabaseEntry test = new DatabaseEntry(source);
        System.out.println(test.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDepthMaxValue() throws Exception {
        String source = "0ee88f1786fb5a990863a1f6a4ab60c3#0401#0000";
        DatabaseEntry test = new DatabaseEntry(source);
        System.out.println(test.toString());
    }

    @Test
    public void testToString() throws Exception {
        String source = "0ee88f1786fb5a990863a1f6a4ab60c3#0001#0000";
        DatabaseEntry test = new DatabaseEntry(source);
        assertEquals(1, test.getDepth());
        assertEquals(source, test.toString());
    }

    @Test
    public void testGetHash() throws Exception {
        String source = "0ee88f1786fb5a990863a1f6a4ab60c3#0001#0000";
        String hash = "0ee88f1786fb5a990863a1f6a4ab60c3";
        DatabaseEntry test = new DatabaseEntry(source);
        assertEquals(hash, test.getHash());
    }

    @Test
    public void testGetValue() throws Exception {
        String source = "0ee88f1786fb5a990863a1f6a4ab60c3#0001#0000";
        int value = 0;
        DatabaseEntry test = new DatabaseEntry(source);
        assertEquals(value, test.getValue());
    }

    @Test
    public void testGetDepth() throws Exception {
        String source = "0ee88f1786fb5a990863a1f6a4ab60c3#0001#0000";
        int depth = 1;
        DatabaseEntry test = new DatabaseEntry(source);
        assertEquals(depth, test.getDepth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonAlphaNumSource() throws Exception {
        String source = "0Ee88f1786fb5a990863a1f6a4ab60c3#0001#0000";
        DatabaseEntry test = new DatabaseEntry(source);
        System.out.println(test.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonAlphaNumSource2() throws Exception {
        String source = "0!e88f1786fb5a990863a1f6a4ab60c3#0001#0000";
        DatabaseEntry test = new DatabaseEntry(source);
        System.out.println(test.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongNumberOfSegments() throws Exception {
        String source = "0ee88f1786fb5a990863a1f6a4ab60c3#0003#0001#0000";
        DatabaseEntry test = new DatabaseEntry(source);
        System.out.println(test.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueMax() throws Exception {
        String source = "0ee88f1786fb5a990863a1f6a4ab60c3#0001#9000";
        DatabaseEntry test = new DatabaseEntry(source);
        System.out.println(test.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueMin() throws Exception {
        String source = "0ee88f1786fb5a990863a1f6a4ab60c3#0001#-900";
        DatabaseEntry test = new DatabaseEntry(source);
        System.out.println(test.toString());
    }

    @Test
    public void testConvert() throws Exception {
        DatabaseEntry test = new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab60c3#0001#0000");
        Method convert = test.getClass().getDeclaredMethod("convert", new Class[]{int.class, int.class});
        convert.setAccessible(true);
        assertEquals("00003", convert.invoke(test, 3, 5));
        assertEquals("-0003", convert.invoke(test, -3, 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFromEmpty() throws Exception {
        DatabaseEntry test = new DatabaseEntry("");
        assertNull(test);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorFromNull() throws Exception {
        DatabaseEntry test = new DatabaseEntry(null);
        assertNull(test);
    }

    @Test
    public void testGetLine1() throws Exception {
        assertEquals(0, DatabaseEntry.getLine(0));
        assertEquals(0, DatabaseEntry.getLine(1));
        assertEquals(0, DatabaseEntry.getLine(41));
        assertEquals(0, DatabaseEntry.getLine(42));
        assertEquals(1, DatabaseEntry.getLine(43));
        assertEquals(1, DatabaseEntry.getLine(84));
        assertEquals(1, DatabaseEntry.getLine(85));
        assertEquals(2, DatabaseEntry.getLine(86));
        assertEquals(2, DatabaseEntry.getLine(127));
        assertEquals(2, DatabaseEntry.getLine(128));
        assertEquals(3, DatabaseEntry.getLine(129));
        assertEquals(3, DatabaseEntry.getLine(170));
        assertEquals(3, DatabaseEntry.getLine(171));
        assertEquals(4, DatabaseEntry.getLine(172));
    }

    @Test
    public void testGetNextLineIndex() throws Exception {
        assertEquals(43, DatabaseEntry.getNextLineIndex(0));
        assertEquals(86, DatabaseEntry.getNextLineIndex(43));
        assertEquals(129, DatabaseEntry.getNextLineIndex(86));
        assertEquals(172, DatabaseEntry.getNextLineIndex(129));
    }

    @Test
    public void testGetLine2() throws Exception {
        DatabaseEntry test = new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab60c3#0001#0000");
        test.setIndex(0);
        assertEquals(0, test.getLine());
        assertEquals(43, test.getNextLineIndex());
        test.setIndex(41);
        assertEquals(0, test.getLine());
        test.setIndex(84);
        assertEquals(1, test.getLine());
        test.setIndex(128);
        assertEquals(2, test.getLine());
        test.setIndex(171);
        assertEquals(3, test.getLine());
        assertEquals(171, test.getIndex());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNegativeIndex() throws Exception {
        DatabaseEntry test = new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab60c3#0001#0000");
        test.setIndex(-5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongSegmentsLengths1() throws Exception {
        DatabaseEntry test = new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab60c3cc#0001#00");
        System.out.println(test.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongSegmentsLengths2() throws Exception {
        DatabaseEntry test = new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab6c30#00001#000");
        System.out.println(test.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongSegmentsLengths3() throws Exception {
        DatabaseEntry test = new DatabaseEntry("0ee88f1786fb5a990863a1f6a4b60c3#0001#00000");
        System.out.println(test.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrongSegmentsLengths4() throws Exception {
        DatabaseEntry test = new DatabaseEntry("0ee88f1786fb5a990863a1f6a4b60c3#0001#00#00");
        System.out.println(test.toString());
    }

    @Test
    public void testSetDepth() throws Exception {
        DatabaseEntry test = new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab60c3#0001#0005");
        assertEquals(1, test.getDepth());
        test.setDepth(4);
        assertEquals(4, test.getDepth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetIllegalDepth() throws Exception {
        DatabaseEntry test = new DatabaseEntry("0ee88f1786fb5a990863a1f6a4ab60c3#0001#0005");
        assertEquals(1, test.getDepth());
        test.setDepth(-1);
    }
}