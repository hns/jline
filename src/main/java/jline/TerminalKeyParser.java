/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jline;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class parses terminal key sequences like you get them on UNIX
 * terminals. For example, cursor left is "^[[A", and so on... .
 */
public class TerminalKeyParser
        implements ConsoleOperations {

    private final Map keyDefinitions;
    private final Map keySequences;
    private CharBuffer buffer;
    private boolean replaying;
    private int replayIndex;

    public TerminalKeyParser() {
        keyDefinitions = new TreeMap();
        keySequences = new TreeMap();
        buffer = new CharBuffer();
        replaying = false;
        replayIndex = 0;
    }

    public void defineKey(String sequence, int code) {
        keyDefinitions.put(sequence, new Integer(code));
        keySequences.put(new Integer(code), sequence);
    }

    /**
     * Parses a key (sequences). Basically, you either get the (virtual) key code as defined
     * in ConsoleOperations, or the constant TerminalKeyParser.MORE_KEYS_AVAILABLE which
     * indicates that the sequence read so far is not yet complete.
     *
     * A special case is the escape key, which can either stand by itself, or
     * be the beginning of a more elaborate escape sequence. The question is
     * whether it is followed by an '[' or not.
     *
     * @param key
     * @return Either the (virtual keycode), or -1 if more keys are expected.
     */
    public int readKey(InputStream in) throws IOException {
        if (replaying) {
            int key = buffer.charAt(replayIndex++);
            if (replayIndex >= buffer.length()) {
                resetParser();
            }
            return key;
        } else {
            int key = readUTF8Key(in);
            if (key == -1) {
                return -1;
            }

            boolean foundPrefix;

            /*
             * Iterate until no prefix has been found.
             * While you still have a prefix, read more characters,
             */
            do {
                buffer.append((char) key);

                //System.out.println("Buffer: " + buffer.toString());

                foundPrefix = false;

                for (Iterator i = keyDefinitions.keySet().iterator(); i.hasNext();) {
                    String seq = (String) i.next();
                    int cmp = buffer.compare(seq);
                    if (cmp == CharBuffer.EQUAL) {
                        int code = ((Integer) keyDefinitions.get(buffer.toString())).intValue();
                        resetParser();
                        return code;
                    } else if (cmp == CharBuffer.PREFIX) {
                        key = readUTF8Key(in);
                        if (key == -1) {
                            break;
                        } else {
                            //System.out.println("Found prefix for " + seq);
                            foundPrefix = true;
                            break; // out of for loop
                        }
                    }
                }
            } while (foundPrefix);

            if (buffer.length() == 1) {
                resetParser();
                return key;
            }

            // Apparently, we found no match, so we have to start replaying... .
            replayIndex = 0;
            replaying = true;
            return readKey(in);
        }
    }

    /**
     * Let's the parser forget whatever it was about to parse.
     */
    public void resetParser() {
        replaying = false;
        buffer.clear();
    }

    public int readUTF8Key(InputStream in) throws IOException {
        int key = in.read();
        if (key == -1) {
            return key;
        } else if (key < 128) {
            return key;
        } else if ((key & 0xe0) == 0xc0) {
            int key2 = in.read();
            return ((key & 0x1f) << 6) | (key2 & 0x3f);
        } else if ((key & 0xf0) == 0xe0) {
            int key2 = in.read();
            int key3 = in.read();
            return ((key & 0x0f) << 12) | ((key2 & 0x3f) << 6) | (key3 & 0x3f);
        } else if ((key & 0xf8) == 0xf0) {
            int key2 = in.read();
            int key3 = in.read();
            int key4 = in.read();
            return ((key & 0x07) << 18) | ((key2 & 0x3f) << 12) | ((key3 & 0x3f) << 6) | (key4 & 0x3f);
        } else {
            return key;
        }
    }

    public String getBinding(int virtualKey) {
        if (virtualKey == -1)
            return null;
        else {
            Integer i = new Integer(virtualKey);

            String keys = (String) keySequences.get(i);

            if (keys != null)
                return keys;
            else
                return String.valueOf((char) virtualKey);
        }
    }
}
