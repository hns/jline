/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jline;

/**
 *
 */
class CharBuffer {

    private char[] buffer;
    private int pos;

    CharBuffer() {
        buffer = new char[10];
        pos = 0;
    }

    void append(char k) {
        if (pos == buffer.length) {
            resize(buffer.length + 1);
        }
        buffer[pos++] = k;
    }

    public String toString() {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < pos; i++) {
            result.append(buffer[i]);
        }

        return result.toString();
    }

    void resize(int newLength) {
        char[] newBuffer = new char[newLength];

        for (int i = 0; i < pos; i++) {
            newBuffer[i] = buffer[i];
        }

        buffer = newBuffer;
    }

    void clear() {
        pos = 0;
    }

    int length() {
        return pos;
    }

    char charAt(int index) {
        return buffer[index];
    }

    char[] getCharArray() {
        return buffer;
    }

    static final int NOT_EQUAL = 0;
    static final int PREFIX = 1;
    static final int EQUAL = 2;

    /**
     * Compares the CharBuffer to a string.
     *
     * @param seq
     * @return NOT_EQUAL, if the strings are different, EQUAL, if they are equal,
     *    and PREFIX if the char buffer is a prefix of the string.
     */
    int compare(String seq) {
        if (pos > seq.length()) {
            return NOT_EQUAL;
        } else if (pos == seq.length()) {
            for (int i = 0; i < pos; i++) {
                if (buffer[i] != seq.charAt(i)) {
                    return NOT_EQUAL;
                }
            }
            return EQUAL;
        } else {
            for (int i = 0; i < pos; i++) {
                if (buffer[i] != seq.charAt(i)) {
                    return NOT_EQUAL;
                }
            }
            return PREFIX;
        }
    }
}
