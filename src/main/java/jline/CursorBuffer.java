/*
 * Copyright (c) 2002-2007, Marc Prud'hommeaux. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package jline;

import java.io.IOException;

/**
 * A CursorBuffer is a holder for a {@link StringBuffer} that also contains the
 * current cursor position.
 *
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 */
public class CursorBuffer {

    private int cursor = 0;
    private StringBuffer buffer = new StringBuffer();
    private boolean overtyping = false;

    /**
     * Get length of String currently in buffer.
     */
    public int length() {
        return buffer.length();
    }

    /**
     * Return character at cursor position.
     */
    public char current() {
        if (cursor <= 0) {
            return 0;
        }

        return buffer.charAt(cursor - 1);
    }

    /**
     * Empties the buffer and sets the cursor to position 0.
     *
     * @return true if buffer was already empty.
     */
    public boolean clearBuffer() {
        if (buffer.length() == 0) {
            return false;
        }

        buffer.delete(0, buffer.length());
        cursor = 0;
        return true;
    }

    /**
     * Write the specific character into the buffer, setting the cursor position
     * ahead one. The text may overwrite or insert based on the current setting
     * of isOvertyping().
     *
     * @param c  the character to insert
     */
    public void write(final char c) {
        buffer.insert(cursor++, c);
        if (isOvertyping() && cursor < buffer.length()) {
            buffer.deleteCharAt(cursor);
        }
    }

    /**
     * Insert the specified {@link String} into the buffer, setting the cursor
     * to the end of the insertion point.
     *
     * @param str  the String to insert. Must not be null.
     */
    public void write(final String str) {
        if (buffer.length() == 0) {
            buffer.append(str);
        } else {
            buffer.insert(cursor, str);
        }

        cursor += str.length();

        if (isOvertyping() && cursor < buffer.length()) {
            buffer.delete(cursor, (cursor + str.length()));
        }
    }

    /**
     * Get the contents of the buffer as a string.
     */
    public String toString() {
        return buffer.toString();
    }

    /**
     * Whether or not you are in overtyping mode.
     */
    public boolean isOvertyping() {
        return overtyping;
    }

    /**
     * Set the overtyping mode.
     */
    public void setOvertyping(boolean b) {
        overtyping = b;
    }

    /**
     * Get the StringBuffer object which holds the actual line.
     */
    public StringBuffer getBuffer() {
        return buffer;
    }

    /**
     * Set the buffer to the given StringBuffer. Copies the content.
     */
    public void setBuffer(StringBuffer buffer) {
        buffer.setLength(0);
        buffer.append(this.buffer.toString());

        this.buffer = buffer;
    }

    /**
     * Get current cursor position.
     */
    public int getCursor() {
        return cursor;
    }

    /**
     * Is the buffer empty?
     */
    public boolean isEmpty() {
        return buffer.length() == 0;
    }

    /**
     * Sets the cursor position. Does not redraw the buffer.
     *
     * @param position has to be >= 0 and <= length()
     */
    public void setCursor(int position) {
        if (position < 0) {
            position = 0;
        } else if (position > buffer.length()) {
            position = buffer.length() - 1;
        }
        cursor = position;
    }

    void deleteCharAt(int i) {
        buffer.deleteCharAt(i);
    }

    char charAt(int i) {
        return buffer.charAt(i);
    }

    String substring(int i) {
        return buffer.substring(i);
    }

    String substring(int i, int j) {
        return buffer.substring(i, j);
    }

    void delete(int i, int j) {
        buffer.delete(i, j);
    }

    void replace(int i, int j, String replacement) {
        replace(i, j, replacement);
    }

    /** Delete character at current cursor position. */
    void deleteChar() {
        buffer.delete(getCursor(), getCursor() + 1);
    }

    boolean isAtStart() {
        return getCursor() == 0;
    }

    boolean isAtEnd() {
        return getCursor() == length();
    }

    void setLength(int newLength) {
        buffer.setLength(newLength);
    }

    int getChar() {
        return buffer.charAt(getCursor());
    }
}
