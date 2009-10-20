/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jline;

/**
 *
 */
public abstract class ANSITerminal extends Terminal {

    public boolean supportsCommand(int cmdCode) {
        return true;
    }

    public String getCommand(int cmdCode) {
        final char ESC = (char) 27;
        switch (cmdCode) {
            case CMD_CLEAR_SCREEN:
                return ESC + "[2J";
            case CMD_CLEAR_EOL:
                return ESC + "[2K";
            case CMD_HOME:
                return ESC + "[1;1H";
        }
        return "";
    }
}
