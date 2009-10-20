/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jline;

import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;

/**
 *
 * @author mikio
 */
public class TerminalKeyParserTest extends TestCase {

    public TerminalKeyParserTest(String testName) {
        super(testName);
    }

    public void testPlain() throws IOException {
        TerminalKeyParser tkp = new TerminalKeyParser();
        InputStream in = new ShortArrayInputStream(new short[]{1, 2, 3});

        assertEquals(1, tkp.readKey(in));
        assertEquals(2, tkp.readKey(in));
        assertEquals(3, tkp.readKey(in));
        assertEquals(-1, tkp.readKey(in));

        tkp.readKey(in);
    }

    public void testKeyParserMatch() throws IOException {
        TerminalKeyParser tkp = new TerminalKeyParser();
        InputStream in = new ShortArrayInputStream(new short[]{'a', 'b'});

        int code;

        tkp.defineKey("a", 1);

        code = tkp.readKey(in);
        assertEquals(1, code);

        code = tkp.readKey(in);
        assertEquals('b', code);

        code = tkp.readKey(in);
    }

    public void testKeyParserMoreKeys() throws IOException {
        TerminalKeyParser tkp = new TerminalKeyParser();
        InputStream in = new ShortArrayInputStream(new short[]{'a', 'b', '[', 'b', 'b', '[', 'c'});

        tkp.defineKey("a", 1);
        tkp.defineKey("[b", 2);

        // First, check whether we recognize the ('a')
        assertEquals(1, tkp.readKey(in));
        // Then, check whether an unknown keycode is passed through ('b')
        assertEquals('b', tkp.readKey(in));
        // Now, we want to parse '[b' correctly.
        assertEquals(2, tkp.readKey(in));
        // See whether state was reset properly
        assertEquals('b', tkp.readKey(in));
        // Parsing '[c' which seems to be defined, but isn't
        assertEquals('[', tkp.readKey(in));
        assertEquals('c', tkp.readKey(in));
        // And EOF.
        assertEquals(-1, tkp.readKey(in));
    }

    public void testKeyParserCursorKeys() throws IOException {
        TerminalKeyParser tkp = new TerminalKeyParser();
        InputStream in = new ShortArrayInputStream(new short[]{'h', 'e', 'l', 'l', 'o', 27, '[', 'A'});

        tkp.defineKey("\u001b[A", ConsoleOperations.VK_UP);

        assertEquals('h', tkp.readKey(in));
        assertEquals('e', tkp.readKey(in));
        assertEquals('l', tkp.readKey(in));
        assertEquals('l', tkp.readKey(in));
        assertEquals('o', tkp.readKey(in));
        assertEquals(ConsoleOperations.VK_UP, tkp.readKey(in));

    }

    public void testReadUTF8Key() throws IOException {
        TerminalKeyParser tkp = new TerminalKeyParser();
        InputStream in = new ShortArrayInputStream(new short[]{
                    '$',
                    0xc3, 0xb6, // ö
                    0xe2, 0x82, 0xac, // €
                    0xF0, 0xA4, 0xAD, 0xA2 // ???
                });

        assertEquals('$', tkp.readUTF8Key(in));
        assertEquals('ö', tkp.readUTF8Key(in));
        assertEquals('€', tkp.readUTF8Key(in));
        assertEquals(0x24b62, tkp.readUTF8Key(in));
        assertEquals(-1, tkp.readUTF8Key(in));
    }

    public void testUTF8KeySequences() throws IOException {
        TerminalKeyParser tkp = new TerminalKeyParser();
        InputStream in = new ShortArrayInputStream(new short[]{'a', 0xc3, 0xb6, '<', 'a'});

        tkp.defineKey("ö<", 1);

        assertEquals('a', tkp.readKey(in));
        assertEquals(1, tkp.readKey(in));
        assertEquals('a', tkp.readKey(in));
        assertEquals(-1, tkp.readKey(in));
    }

    public void testGetBinding() {
        TerminalKeyParser tkp = new TerminalKeyParser();

        tkp.defineKey("frank", 1);

        assertEquals("frank", tkp.getBinding(1));
    }
}
