/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jline;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class ShortArrayInputStream extends InputStream {

    private short[] array;
    private int pos;

    public ShortArrayInputStream(short[] data) {
        array = data;
        pos = 0;
    }

    public int read() throws IOException {
        if (pos >= array.length)
            return -1;
        else
            return array[pos++];
    }
}
