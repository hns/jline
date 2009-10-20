/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jline;

import junit.framework.TestCase;

/**
 *
 * @author mikio
 */
public class CharBufferTest extends TestCase {
    
    public CharBufferTest(String testName) {
        super(testName);
    }

    public void testAppendAndToString() {
        CharBuffer buffer = new CharBuffer();

        assertEquals("", buffer.toString());
        buffer.append('h');
        assertEquals("h", buffer.toString());
        buffer.append('e');
        assertEquals("he", buffer.toString());
        buffer.append('l');
        assertEquals("hel", buffer.toString());
    }

    public void testClear() {
        CharBuffer buffer = new CharBuffer();

        buffer.append('h');
        buffer.append('e');
        assertEquals("he", buffer.toString());
        buffer.clear();
        assertEquals(0, buffer.length());
        assertEquals("", buffer.toString());
    }

    public void testLength() {
        CharBuffer buffer = new CharBuffer();

        assertEquals(0, buffer.length());
        buffer.append('h');
        assertEquals(1, buffer.length());
        buffer.append('e');
        assertEquals(2, buffer.length());
        buffer.append('l');
        assertEquals(3, buffer.length());
    }

    public void testCompare() {
        CharBuffer buffer = new CharBuffer();

        buffer.append('h');
        buffer.append('e');

        assertEquals(CharBuffer.NOT_EQUAL, buffer.compare("h"));
        assertEquals(CharBuffer.NOT_EQUAL, buffer.compare("hd"));
        assertEquals(CharBuffer.NOT_EQUAL, buffer.compare("hdf"));

        assertEquals(CharBuffer.EQUAL, buffer.compare("he"));

        assertEquals(CharBuffer.PREFIX, buffer.compare("hef"));

    }
}
