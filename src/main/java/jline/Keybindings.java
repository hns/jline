/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jline;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public class Keybindings implements ConsoleOperations {
    private Map actions;
    private Map keys;

    /** Construct a new Keybindings object to store and translate keybindings. */
    public Keybindings() {
        actions = new TreeMap();
        keys = new TreeMap();
    }

    /** Add a new key binding. */
    public void bindKey(int virtualKeyCode, int action) {
        Integer key = new Integer(virtualKeyCode);
        Integer act = new Integer(action);
        actions.put(key, act);
        keys.put(act, key);
    }

    /**
     * Resolve a key to its action.
     *
     * Unrecodgnized control keys are mapped to UNKNOWN, printable characters
     * are passed back as given.
     */
    public int resolveKey(int virtualKeyCode) {
        Integer key = new Integer(virtualKeyCode);
        if (actions.containsKey(key)) {
            return ((Integer) actions.get(key)).intValue();
        } else if (virtualKeyCode >= SPECIAL || Character.isISOControl(virtualKeyCode)) {
            return UNKNOWN;
        } else {
            return virtualKeyCode;
        }
    }

    /**
     * Reverse look up the key for a given action.
     */
    public int getKeyForAction(int action) {
        Integer act = new Integer(action);
        if (keys.containsKey(act)) {
            return ((Integer) keys.get(act)).intValue();
        } else {
            return -1;
        }
    }

    /**
     * Parses key names like "^a" or
     *
     * @param key
     * @return the (virtual) key code, or -1 if no key was found.
     */
    public static int parseKey(String key) {
        if (key.length() == 0) {
            return -1;
        }

        // Deal with digits first
        if (Character.isDigit(key.charAt(0))) {
            if (key.charAt(0) == '0') {
                if (key.length() >= 2 && key.charAt(1) == 'x') {
                    return Integer.valueOf(key.substring(2), 16).intValue();
                } else {
                    return Integer.valueOf(key.substring(1), 8).intValue();
                }
            } else {
                return Integer.valueOf(key).intValue();
            }
        } else if (isControlKeySequence(key)) {
            return key.codePointAt(1) - 96;
        } else if (isEscapeKeySequence(key)) {
            return META + key.codePointAt(2);
        } else if (isEscapedKeySequence(key)) {
            switch (key.charAt(1)) {
                case 'e':
                    return 0x1b;
                case '\\':
                    return '\\';
                case '\n':
                    return 10;
                default:
                    return -1;
            }
        } else if (key.equals("left")) {
            return VK_LEFT;
        } else if (key.equals("right")) {
            return VK_RIGHT;
        } else if (key.equals("up")) {
            return VK_UP;
        } else if (key.equals("down")) {
            return VK_DOWN;
        } else if (key.equals("delete")) {
            return VK_DELETE;
        } else if (key.equals("page-up")) {
            return VK_PAGE_UP;
        } else if (key.equals("page-down")) {
            return VK_PAGE_DOWN;
        } else if (key.equals("home")) {
            return VK_HOME;
        } else if (key.equals("end")) {
            return VK_END;
        } else if (key.equals("backspace")) {
            return VK_BACKSPACE;
        } else {
            return -1;
        }
    }

    /** Checks for "^a", "^d", etc. */
    private static boolean isControlKeySequence(String key) {
        return key.length() == 2 && key.charAt(0) == '^' && Character.isLowerCase(key.charAt(1));
    }

    /** Checks for "^[a", "^[D", etc. */
    private static boolean isEscapeKeySequence(String key) {
        return key.length() == 3 && key.charAt(0) == '^' && key.charAt(1) == '[';
    }

    /** Checks for sequences starting with "\\" */
    private static boolean isEscapedKeySequence(String key) {
        return key.length() == 2 && key.charAt(0) == '\\';
    }

}
