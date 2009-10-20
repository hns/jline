/*
 * Copyright (c) 2002-2007, Marc Prud'hommeaux. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package jline;

import java.io.*;
import java.util.*;

/**
 *  <p>
 *  Terminal that is used for unix platforms. Terminal initialization
 *  is handled by issuing the <em>stty</em> command against the
 *  <em>/dev/tty</em> file to disable character echoing and enable
 *  character input. All known unix systems (including
 *  Linux and Macintosh OS X) support the <em>stty</em>), so this
 *  implementation should work for an reasonable POSIX system.
 *        </p>
 *
 *  @author  <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 *  @author  Updates <a href="mailto:dwkemp@gmail.com">Dale Kemp</a> 2005-12-03
 */
public class UnixTerminal extends ANSITerminal {

    public static final short ARROW_START = 27;  // Escape
    public static final short ARROW_PREFIX = 91; // '['
    public static final short ARROW_LEFT = 68;   // 'D'
    public static final short ARROW_RIGHT = 67;  // 'C'
    public static final short ARROW_UP = 65;     // 'A'
    public static final short ARROW_DOWN = 66;   // 'B'
    public static final short O_PREFIX = 79;     // 'O'
    public static final short HOME_CODE = 72;    // 'H'
    public static final short END_CODE = 70;     // 'F'
    public static final short DEL_THIRD = 51;    // '3'
    public static final short DEL_SECOND = 126;  // '~'
    private Map terminfo;
    private boolean echoEnabled;
    private String ttyConfig;
    private boolean backspaceDeleteSwitched = false;
    private static String sttyCommand =
            System.getProperty("jline.sttyCommand", "stty");
    String encoding = System.getProperty("input.encoding", "UTF-8");
    ReplayPrefixOneCharInputStream replayStream = new ReplayPrefixOneCharInputStream(encoding);
    InputStreamReader replayReader;
    private TerminalKeyParser keyParser;

    public UnixTerminal() {
        try {
            replayReader = new InputStreamReader(replayStream, encoding);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        keyParser = new TerminalKeyParser();
        initializeKeyParserBindings();
    }

    private void initializeKeyParserBindings() {
        keyParser.defineKey("\u001b[A", VK_UP);
        keyParser.defineKey("\u001b[1;2A", SHIFT + VK_UP);
        keyParser.defineKey("\u001b[1;3A", META + VK_UP);
        keyParser.defineKey("\u001b[1;4A", SHIFT + CTRL + VK_UP);
        keyParser.defineKey("\u001b[1;5A", CTRL + VK_UP);
        keyParser.defineKey("\u001b[B", VK_DOWN);
        keyParser.defineKey("\u001b[1;2B", SHIFT + VK_DOWN);
        keyParser.defineKey("\u001b[1;3B", META + VK_DOWN);
        keyParser.defineKey("\u001b[1;4B", SHIFT + CTRL + VK_DOWN);
        keyParser.defineKey("\u001b[1;5B", CTRL + VK_DOWN);
        keyParser.defineKey("\u001b[C", VK_RIGHT);
        keyParser.defineKey("\u001b[1;2C", SHIFT + VK_RIGHT);
        keyParser.defineKey("\u001b[1;3C", META + VK_RIGHT);
        keyParser.defineKey("\u001b[1;4C", SHIFT + CTRL + VK_RIGHT);
        keyParser.defineKey("\u001b[1;5C", CTRL + VK_RIGHT);
        keyParser.defineKey("\u001b[D", VK_LEFT);
        keyParser.defineKey("\u001b[1;2D", SHIFT + VK_LEFT);
        keyParser.defineKey("\u001b[1;3D", META + VK_LEFT);
        keyParser.defineKey("\u001b[1;4D", SHIFT + CTRL + VK_LEFT);
        keyParser.defineKey("\u001b[1;5D", CTRL + VK_LEFT);
        keyParser.defineKey("\u001b[5~", VK_PAGE_UP);
        keyParser.defineKey("\u001b[6~", VK_PAGE_DOWN);
        keyParser.defineKey("\u001b[3~", VK_DELETE);
        keyParser.defineKey("\u001b[2~", VK_INSERT);
        keyParser.defineKey("\u001bOH", VK_HOME);
        keyParser.defineKey("\u001bOF", VK_END);
        keyParser.defineKey("\u007f", VK_BACKSPACE);

        String letters = "abcdefghijklmnopqrstuvwxyz";

        for (int i = 0; i < letters.length(); i++) {
            char c = letters.charAt(i);
            keyParser.defineKey("\u001b" + c, META + c);
            keyParser.defineKey("\u001b" + Character.toUpperCase(c), META + SHIFT + c);
            keyParser.defineKey("\u001b" + (c - 96), META + c - 96);
        }
    }

    protected void checkBackspace(){
        String[] splitConfig = ttyConfig.split(":|=");

        if (splitConfig.length > 20 && "gfmt1".equals(splitConfig[0])) {
            // BSD style stty -g format
            backspaceDeleteSwitched = "7f".equals(splitConfig[20]);
        } else if (splitConfig.length > 6) {
            backspaceDeleteSwitched = "7f".equals(splitConfig[6]);
        }
    }

    /**
     *  Remove line-buffered input by invoking "stty -icanon min 1"
     *  against the current terminal.
     */
    public void initializeTerminal() throws IOException, InterruptedException {
        // save the initial tty configuration
        ttyConfig = stty("-g");

        // sanity check
        if ((ttyConfig.length() == 0) || ((ttyConfig.indexOf("=") == -1) && (ttyConfig.indexOf(":") == -1))) {
            throw new IOException("Unrecognized stty code: " + ttyConfig);
        }

        checkBackspace();

        // set the console to be character-buffered instead of line-buffered
        stty("-icanon min 1");

        // disable character echoing
        stty("-echo");
        echoEnabled = false;

        // at exit, restore the original tty configuration (for JDK 1.3+)
        try {
            Runtime.getRuntime().addShutdownHook(new Thread() {

                public void start() {
                    try {
                        restoreTerminal();
                    } catch (Exception e) {
                        consumeException(e);
                    }
                }
            });
        } catch (AbstractMethodError ame) {
            // JDK 1.3+ only method. Bummer.
            consumeException(ame);
        }
    }

    /** 
     * Restore the original terminal configuration, which can be used when
     * shutting down the console reader. The ConsoleReader cannot be
     * used after calling this method.
     */
    public void restoreTerminal() throws Exception {
        if (ttyConfig != null) {
            stty(ttyConfig);
            ttyConfig = null;
        }
        resetTerminal();
    }

    private static class EchoingInputStream extends InputStream {

        InputStream in;

        public EchoingInputStream(InputStream in) {
            this.in = in;
        }

        public int read() throws IOException {
            int c = in.read();
            System.out.println("Read " + c);
            return c;
        }
    }

    public int readVirtualKey(InputStream in) throws IOException {

        int c = keyParser.readKey(in);
        //int c = keyParser.readKey(new EchoingInputStream(in));
        return c;

        /*
        int c = readCharacter(in);

<<<<<<< HEAD:src/main/java/jline/UnixTerminal.java
        if (backspaceDeleteSwitched)
            if (c == DELETE)
                c = BACKSPACE;
            else if (c == BACKSPACE)
                c = DELETE;
=======
        if (backspaceDeleteSwitched) {
        if (c == DELETE) {
        c = '\b';
        } else if (c == '\b') {
        c = DELETE;
        }
        }
>>>>>>> 4bd213d4e8092bec887d6b68a44789d73b2fa321:src/main/java/jline/UnixTerminal.java

        // in Unix terminals, arrow keys are represented by
        // a sequence of 3 characters. E.g., the up arrow
        // key yields 27, 91, 68
        if (c == ARROW_START) {
        //also the escape key is 27
        //thats why we read until we
        //have something different than 27
        //this is a bugfix, because otherwise
        //pressing escape and than an arrow key
        //was an undefined state
        while (c == ARROW_START) {
        c = readCharacter(in);
        }
        if (c == ARROW_PREFIX || c == O_PREFIX) {
        c = readCharacter(in);
        switch (c) {
        case ARROW_UP:
        return CTRL_P;
        case ARROW_DOWN:
        return CTRL_N;
        case ARROW_LEFT:
        return CTRL_B;
        case ARROW_RIGHT:
        return CTRL_F;
        case HOME_CODE:
        return CTRL_A;
        case END_CODE:
        return CTRL_E;
        case DEL_THIRD:
        c = readCharacter(in);
        return DELETE;
        }
        }
        }
        // handle unicode characters, thanks for a patch from amyi@inf.ed.ac.uk
        if (c > 128) {
        // handle unicode characters longer than 2 bytes,
        // thanks to Marc.Herbert@continuent.com
        replayStream.setInput(c, in);
        //            replayReader = new InputStreamReader(replayStream, encoding);
        c = replayReader.read();

        }

        return c;
         */
    }

    /**
     *  No-op for exceptions we want to silently consume.
     */
    private void consumeException(Throwable e) {
    }

    public boolean isSupported() {
        return true;
    }

    public boolean getEcho() {
        return false;
    }

    /**
     *  Returns the value of "stty size" width param.
     *
     *  <strong>Note</strong>: this method caches the value from the
     *  first time it is called in order to increase speed, which means
     *  that changing to size of the terminal will not be reflected
     *  in the console.
     */
    public int getTerminalWidth() {
        int val = -1;

        try {
            val = getTerminalProperty("columns");
        } catch (Exception e) {
        }

        if (val == -1) {
            val = 80;
        }

        return val;
    }

    /**
     *  Returns the value of "stty size" height param.
     *
     *  <strong>Note</strong>: this method caches the value from the
     *  first time it is called in order to increase speed, which means
     *  that changing to size of the terminal will not be reflected
     *  in the console.
     */
    public int getTerminalHeight() {
        int val = -1;

        try {
            val = getTerminalProperty("rows");
        } catch (Exception e) {
        }

        if (val == -1) {
            val = 24;
        }

        return val;
    }

    private static int getTerminalProperty(String prop)
            throws IOException, InterruptedException {
        // need to be able handle both output formats:
        // speed 9600 baud; 24 rows; 140 columns;
        // and:
        // speed 38400 baud; rows = 49; columns = 111; ypixels = 0; xpixels = 0;
        String props = stty("-a");

        for (StringTokenizer tok = new StringTokenizer(props, ";\n");
                tok.hasMoreTokens();) {
            String str = tok.nextToken().trim();

            if (str.startsWith(prop)) {
                int index = str.lastIndexOf(" ");

                return Integer.parseInt(str.substring(index).trim());
            } else if (str.endsWith(prop)) {
                int index = str.indexOf(" ");

                return Integer.parseInt(str.substring(0, index).trim());
            }
        }

        return -1;
    }

    /**
     *  Execute the stty command with the specified arguments
     *  against the current active terminal.
     */
    protected static String stty(final String args)
            throws IOException, InterruptedException {
        return exec("stty " + args + " < /dev/tty").trim();
    }

    /**
     *  Execute the specified command and return the output
     *  (both stdout and stderr).
     */
    private static String exec(final String cmd)
            throws IOException, InterruptedException {
        return exec(new String[]{
                    "sh",
                    "-c",
                    cmd
                });
    }

    /**
     *  Execute the specified command and return the output
     *  (both stdout and stderr).
     */
    private static String exec(final String[] cmd)
            throws IOException, InterruptedException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        Process p = Runtime.getRuntime().exec(cmd);
        int c;
        InputStream in;

        in = p.getInputStream();

        while ((c = in.read()) != -1) {
            bout.write(c);
        }

        in = p.getErrorStream();

        while ((c = in.read()) != -1) {
            bout.write(c);
        }

        p.waitFor();

        String result = new String(bout.toByteArray());

        return result;
    }

    /**
     *  The command to use to set the terminal options. Defaults
     *  to "stty", or the value of the system property "jline.sttyCommand".
     */
    public static void setSttyCommand(String cmd) {
        sttyCommand = cmd;
    }

    /**
     *  The command to use to set the terminal options. Defaults
     *  to "stty", or the value of the system property "jline.sttyCommand".
     */
    public static String getSttyCommand() {
        return sttyCommand;
    }

    public synchronized boolean isEchoEnabled() {
        return echoEnabled;
    }

    public synchronized void enableEcho() {
        try {
            stty("echo");
            echoEnabled = true;
        } catch (Exception e) {
            consumeException(e);
        }
    }

    public synchronized void disableEcho() {
        try {
            stty("-echo");
            echoEnabled = false;
        } catch (Exception e) {
            consumeException(e);
        }
    }

    /**
     * This is awkward and inefficient, but probably the minimal way to add
     * UTF-8 support to JLine
     *
     * @author <a href="mailto:Marc.Herbert@continuent.com">Marc Herbert</a>
     */
    static class ReplayPrefixOneCharInputStream extends InputStream {

        byte firstByte;
        int byteLength;
        InputStream wrappedStream;
        int byteRead;
        final String encoding;

        public ReplayPrefixOneCharInputStream(String encoding) {
            this.encoding = encoding;
        }

        public void setInput(int recorded, InputStream wrapped) throws IOException {
            this.byteRead = 0;
            this.firstByte = (byte) recorded;
            this.wrappedStream = wrapped;

            byteLength = 1;
            if (encoding.equalsIgnoreCase("UTF-8")) {
                setInputUTF8(recorded, wrapped);
            } else if (encoding.equalsIgnoreCase("UTF-16")) {
                byteLength = 2;
            } else if (encoding.equalsIgnoreCase("UTF-32")) {
                byteLength = 4;
            }
        }

        public void setInputUTF8(int recorded, InputStream wrapped) throws IOException {
            // 110yyyyy 10zzzzzz
            if ((firstByte & (byte) 0xE0) == (byte) 0xC0) {
                this.byteLength = 2;
            } // 1110xxxx 10yyyyyy 10zzzzzz
            else if ((firstByte & (byte) 0xF0) == (byte) 0xE0) {
                this.byteLength = 3;
            } // 11110www 10xxxxxx 10yyyyyy 10zzzzzz
            else if ((firstByte & (byte) 0xF8) == (byte) 0xF0) {
                this.byteLength = 4;
            } else {
                throw new IOException("invalid UTF-8 first byte: " + firstByte);
            }
        }

        public int read() throws IOException {
            if (available() == 0) {
                return -1;
            }

            byteRead++;

            if (byteRead == 1) {
                return firstByte;
            }

            return wrappedStream.read();
        }

        /**
         * InputStreamReader is greedy and will try to read bytes in advance. We
         * do NOT want this to happen since we use a temporary/"losing bytes"
         * InputStreamReader above, that's why we hide the real
         * wrappedStream.available() here.
         */
        public int available() {
            return byteLength - byteRead;
        }
    }

    public String getKeyForVirtualKey(int virtualKey) {
        return keyParser.getBinding(virtualKey);
    }
}
