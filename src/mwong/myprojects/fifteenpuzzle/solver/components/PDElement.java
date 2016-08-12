/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation: javac PDElement.java
 *  Dependencies: Stopwatch.java, Board.java, Direction.java
 *
 *  A immutable data type PDElement of detected universal key set and format set
 *  with change after each move for 15puzzle pattern database generator
 *  and puzzle solver
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver.components;

import mwong.myprojects.utilities.Stopwatch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

public class PDElement {
    // Additive pattern element mode can be use
    public enum Mode {
        // For pattern database generator
        Generator,
        // For puzzle solver
        PuzzleSolver
    }

    // Remarks - key size : group of 3 = 3! = 6, group of 6 = 6! = 120
    private static final int[] keySize = {0, 1, 2, 6, 24, 120, 720, 5040, 40320};
    // Remarks - format size : group of 3 = 16C3 = 560, group of 6 = 16C6 = 8008
    private static final int[] formatSize = {0, 16, 120, 560, 1820, 4368, 8008, 11440, 12870};
    // 16 bits represent the position of the tile
    private static final int[] bitPos16 = {1 << 15, 1 << 14, 1 << 13, 1 << 12, 1 << 11, 1 << 10,
        1 << 9, 1 << 8, 1 << 7, 1 << 6, 1 << 5, 1 << 4, 1 << 3, 1 << 2, 1 << 1, 1};

    private static final String directory = "database";
    private static final String seperator = System.getProperty("file.separator");
    private static final String filePath = directory + seperator + "pattern_element_";
    // Standard group are 3, 5, 6, 7, 8 for patterns (663, 555 oar 78)
    private static final boolean[] standardGroups
            = {false, false, false, true, false, true, true, true, true};
    private static final int[] maxShiftX2 = {0, 0, 2, 4, 6, 6, 6, 6, 6};
    private static final int maxGroupSize = 8;
    private static final int puzzleSize = Board.getSize();
    // partial key - last # of keys (4 bits each)
    private static final int[] partialBits
            = {0, 0x000F, 0x00FF, 0x0FFF, 0x0000FFFF, 0x000FFFFF, 0x00FFFFFF, 0x0FFFFFFF};

    private HashMap<Integer, Integer> keys;
    private HashMap<Integer, Integer> formats;
    private int [][] keys2combo;
    private int [][] formats2combo;
    // next key after rotate
    // for each group : key size x number of keys x 6 shift max (3 left, 3 right)
    private int [][] rotateKeyByPos;
    // store 2 values ( rotate | next format index)
    //  for each group : format size x number of keys x 4 direction of moves
    private int [][][] linkFormatCombo;
    private int [][] linkFormatMove;

    /**
     * Initializes the PDElement with standard groups and generator mode.
     */
    public PDElement() {
        this(standardGroups, Mode.Generator);
    }

    /**
     * Initializes the PDElement with given pattern groups and generator mode.
     *
     * @param patternGroups boolean array of pattern groups in use
     * @param mode Enum Mode of Generator or PuzzleSolver
     */
    public PDElement(boolean [] patternGroups, Mode mode) {
        if (patternGroups.length != maxGroupSize + 1) {
            System.err.println("Invalid input - require boolean array of size 9");
            throw new IllegalArgumentException();
        }
        loadData(patternGroups, mode);
    }

    // load the database pattern components from file
    private void loadData(boolean [] patternGroups, Mode mode) {
        keys = new HashMap<Integer, Integer>();
        formats = new HashMap<Integer, Integer>();
        linkFormatCombo = new int[maxGroupSize + 1][0][0];
        linkFormatMove = new int[maxGroupSize + 1][0];
        rotateKeyByPos = new int[maxGroupSize + 1][0];
        keys2combo = new int [maxGroupSize + 1][0];
        formats2combo = new int [maxGroupSize + 1][0];

        boolean printMsg = true;
        if (mode == Mode.PuzzleSolver) {
            printMsg = false;
        }
        Stopwatch stopwatch = new Stopwatch();

        for (int group = 2; group <= maxGroupSize; group++) {
            if (patternGroups[group]) {
                try (FileInputStream fin = new FileInputStream(filePath + group + ".db");
                        FileChannel inChannel = fin.getChannel();) {
                    ByteBuffer buffer =
                            inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());

                    keys2combo[group] = new int[keySize[group]];
                    for (int i = 0; i < keySize[group]; i++) {
                        keys2combo[group][i] = buffer.getInt();
                        keys.put(keys2combo[group][i], i);
                    }

                    rotateKeyByPos[group] = new int[keySize[group] * group * maxShiftX2[group]];
                    for (int i = 0; i < rotateKeyByPos[group].length; i++) {
                        rotateKeyByPos[group][i] = buffer.getInt();
                    }

                    formats2combo[group] = new int[formatSize[group]];
                    for (int i = 0; i < formatSize[group]; i++) {
                        formats2combo[group][i] = buffer.getInt();
                        formats.put(formats2combo[group][i], i);
                    }

                    if (mode == Mode.PuzzleSolver) {
                        linkFormatMove[group] = new int[formatSize[group] * 64];
                        for (int i = 0; i < linkFormatMove[group].length; i++) {
                            linkFormatMove[group][i] = buffer.getInt();
                        }
                        // skip remaining linkFormatCombo set for puzzle solver
                    } else {
                        // skip following linkFormatMove set for generator
                        for (int i = 0; i < formatSize[group] * 64; i++) {
                            buffer.getInt();
                        }

                        linkFormatCombo[group] = new int[formatSize[group]][group * 4];
                        for (int f = 0; f < formatSize[group]; f++) {
                            for (int i = 0; i < group * 4; i++) {
                                linkFormatCombo[group][f][i] = buffer.getInt();
                            }
                        }
                    }
                } catch (BufferUnderflowException | IOException ex) {
                    build();
                    saveData(patternGroups, printMsg);
                    wrapup(patternGroups, mode);
                    return;
                }
            }
        }
        if (printMsg) {
            System.out.println("PD15Element - load data from file succeeded : "
                    + stopwatch.currentTime() + "s");
        }
    }

    // save the database pattern components in file
    private void saveData(boolean [] patternGroups, boolean printMsg)  {
        if (!(new File(directory)).exists()) {
            (new File(directory)).mkdir();
        }

        Stopwatch stopwatch = new Stopwatch();
        // store key components from group 2 to 8 in data file
        for (int group = 2; group <= maxGroupSize; group++) {
            if (patternGroups[group]) {
                if ((new File(filePath + group + ".db")).exists()) {
                    (new File(filePath + group + ".db")).delete();
                }

                try (FileOutputStream fout = new FileOutputStream(filePath + group + ".db");
                        FileChannel outChannel = fout.getChannel();) {
                    ByteBuffer buffer = ByteBuffer.allocateDirect(keySize[group] * 4);
                    for (int combo : keys2combo[group]) {
                        buffer.putInt(combo);
                    }
                    buffer.flip();
                    outChannel.write(buffer);

                    buffer = ByteBuffer.allocateDirect(rotateKeyByPos[group].length * 4);
                    for (int i = 0; i < rotateKeyByPos[group].length; i++) {
                        buffer.putInt(rotateKeyByPos[group][i]);
                    }
                    buffer.flip();
                    outChannel.write(buffer);

                    buffer = ByteBuffer.allocateDirect(formatSize[group] * 4);
                    for (int combo : formats2combo[group]) {
                        buffer.putInt(combo);
                    }
                    buffer.flip();
                    outChannel.write(buffer);

                    buffer = ByteBuffer.allocateDirect(formatSize[group] * 64 * 4);
                    for (int combo : linkFormatMove[group]) {
                        buffer.putInt(combo);
                    }
                    buffer.flip();
                    outChannel.write(buffer);

                    buffer = ByteBuffer.allocateDirect(formatSize[group] * group * 4 * 4);
                    for (int f = 0; f < formatSize[group]; f++) {
                        for (int combo : linkFormatCombo[group][f]) {
                            buffer.putInt(combo);
                        }
                    }
                    buffer.flip();
                    outChannel.write(buffer);
                } catch (BufferUnderflowException | IOException ex) {
                    if (printMsg) {
                        System.out.println("PD15Element - save data in file failed.");
                    }
                    for (int group2 = 2; group2 <= maxGroupSize; group2++) {
                        if ((new File(filePath + group2 + ".db")).exists()) {
                            (new File(filePath + group2 + ".db")).delete();
                        }
                    }
                    return;
                }
            }
        }
        if (printMsg) {
            System.out.println("PD15Element - save data in file succeeded : "
                    + stopwatch.currentTime() + "s");
        }
    }

    // clear all unused components
    private void wrapup(boolean [] groups, Mode mode) {
        for (int group = 1; group < groups.length; group++) {
            if (!groups[group]) {
                if (group > 1) {
                    for (int key : keys2combo[group]) {
                        keys.remove(key);
                    }
                }
                for (int format : formats2combo[group]) {
                    formats.remove(format);
                }
                keys2combo[group] = null;
                formats2combo[group] = null;
                linkFormatCombo[group] = null;
                rotateKeyByPos[group] = null;
                linkFormatMove[group] = null;
            }
        }

        if (mode == Mode.Generator) {
            linkFormatMove = null;
        } else if (mode == Mode.PuzzleSolver) {
            keys2combo = null;
            formats2combo = null;
            linkFormatCombo = null;
        }
    }

    // initializes all storages then generate keys and format components
    private void build() {
        keys = new HashMap<Integer, Integer>();
        formats = new HashMap<Integer, Integer>();
        keys2combo = new int [maxGroupSize + 1][];
        formats2combo = new int [maxGroupSize + 1][];
        linkFormatCombo = new int[maxGroupSize + 1][][];
        linkFormatMove = new int[maxGroupSize + 1][];
        rotateKeyByPos = new int[maxGroupSize + 1][];
        Stopwatch stopwatch = new Stopwatch();
        genKeys();
        genFormats();
        System.out.println("PD15Element - generate data set completed : "
                + stopwatch.currentTime() + "s");
    }

    // generate the key components from group 2 to 8
    private void genKeys() {
        int [] initKeys = new int[maxGroupSize + 1];
        int basedGroup = 1;
        int counter = 0;
        HashSet<int[]> set = new HashSet<int[]>();
        int [] basedKey = {0};
        set.add(basedKey);

        // expand the based group (1 - 7) for key set of size 2 - 8
        while (basedGroup < maxGroupSize) {
            keys2combo[basedGroup + 1] = new int[keySize[basedGroup + 1]];

            HashSet<int[]> expend = new HashSet<int[]>();
            counter = 0;
            for (int[] previousKey : set) {
                for (int pos = 0; pos < basedGroup; pos++) {
                    basedKey = new int[basedGroup + 1];
                    System.arraycopy(previousKey, 0, basedKey, 0, pos);
                    basedKey[pos] = basedGroup;
                    System.arraycopy(previousKey, pos, basedKey, pos + 1, basedGroup - pos);
                    expend.add(basedKey);
                }

                basedKey = new int[basedGroup + 1];
                System.arraycopy(previousKey, 0, basedKey, 0, basedGroup);
                basedKey[basedGroup] = basedGroup;
                expend.add(basedKey);
            }

            basedGroup++;

            // keep in sorted order, easy for track. (unnecessary)
            TreeSet<Integer> sorted = new TreeSet<Integer>();
            for (int [] combo : expend) {
                int compressKey = 0;
                for (int key : combo) {
                    compressKey = compressKey << 4 | key;
                }
                sorted.add(compressKey);
            }
            for (int compressKey : sorted) {
                keys2combo[basedGroup][counter] = compressKey;
                keys.put(compressKey, counter++);
            }

            set = expend;
            initKeys[basedGroup] = sorted.first();
            genRotateKeys(basedGroup, initKeys);
        }
    }

    // generate the key links of move UP or DOWN which impact the changes of the order set of keys
    // odd:  tile DOWN, tile 1 shift to right 012 => 002
    //                                        000    010
    // even: tile UP,   tile 2 shift to left  000 => 002
    //                                        012    010
    private void genRotateKeys(int group, int [] initKeys) {
        if (group < 2) {
            return;
        }
        int shiftCount = maxShiftX2[group] / 2;

        int [][][] temp = new int[keySize[group]][][];
        HashSet<Integer> set = new HashSet<Integer>();
        HashSet<Integer> visited;
        set.add(initKeys[group]);

        while (!set.isEmpty()) {
            visited = set;
            set = new HashSet<Integer>();
            for (int val : visited) {
                int keyIdx = keys.get(val);
                temp [keyIdx] = new int [group][shiftCount * 2];

                for (int pos = 0; pos < group; pos++) {

                    int shifter = (group - pos - 1) * 4;
                    int self = (val & (partialBits[1] << shifter)) >> shifter;

                    //right shift use odd numbers 1, 3, 5 for 1 to 3 shifts
                    int base = val >> (shifter + 4);
                    for (int shift = 1; shift <= shiftCount; ++shift) {
                        if (pos + shift < group) {
                            int rightShift = shifter - (shift * 4);
                            int portion = (val & (partialBits[shift] << rightShift)) >> rightShift;
                            int unshift = val & partialBits[group - pos - shift - 1];
                            int val2 = ((((base << (shift * 4)) | portion) << 4) | self)
                                    << ((group - pos - shift - 1) * 4) | unshift;
                            temp[keyIdx][pos][shift * 2 - 1] = keys.get(val2);
                            if (temp[keys.get(val2)] == null) {
                                set.add(val2);
                            }
                        } else {
                            break;
                        }
                    }

                    //left shift use even numbers 0, 2, 4 for 1 to 3 shifts
                    base = val & partialBits[group - pos - 1];
                    for (int shift = 1; shift <= shiftCount; ++shift) {
                        if (pos - shift >= 0) {
                            int unshift = val >> ((group - pos + shift) * 4);
                            int leftShift = shifter + 4;
                            int portion = (val & (partialBits[shift] << leftShift)) >> leftShift;
                            int val2 = ((((unshift << 4) | self) << (shift * 4) | portion)
                                    << (4 * (group - pos - 1))) | base;
                            temp[keyIdx][pos][(shift - 1) * 2] = keys.get(val2);
                            if (temp[keys.get(val2)] == null) {
                                set.add(val2);
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        rotateKeyByPos[group] = new int[keySize[group] * group * (shiftCount * 2)];
        int idx = 0;
        for (int k = 0; k < keySize[group]; k++) {
            for (int o = 0; o < group; o++) {
                for (int s = 0; s < shiftCount * 2; s++) {
                    rotateKeyByPos[group][idx++] = temp[k][o][s];
                }
            }
        }
    }

    // generate the format components from group 1 to 8
    private void genFormats() {
        int [] initFormat = new int[maxGroupSize + 1];
        int basedGroup = 0;
        int counter = 0;
        HashSet<int[]> set = new HashSet<int[]>();
        int [] basedFormat = { 0, 0, 0, 0,
                               0, 0, 0, 0,
                               0, 0, 0, 0,
                               0, 0, 0, 0};
        set.add(basedFormat);

        // expand the based group (1 - 7) for format set with key size 2 - 8
        while (basedGroup < maxGroupSize) {
            formats2combo[basedGroup + 1] = new int[formatSize[basedGroup + 1]];
            HashSet<int[]> expend = new HashSet<int[]>();
            counter = 0;

            // keep in sorted order, easy to track (unnecessary)
            TreeSet<Integer> sorted = new TreeSet<Integer>();
            for (int[] previousFormat : set) {
                for (int pos = 0; pos < puzzleSize; pos++) {
                    if (previousFormat[pos] == 0) {
                        basedFormat = new int [puzzleSize];
                        System.arraycopy(previousFormat, 0, basedFormat, 0, puzzleSize);
                        basedFormat[pos] = 1;

                        int compressFormat = 0;
                        for (int key : basedFormat) {
                            compressFormat = compressFormat << 1 | key;
                        }

                        if (!sorted.contains(compressFormat)) {
                            sorted.add(compressFormat);
                            expend.add(basedFormat);
                        }
                    }
                }
            }

            basedGroup++;
            for (int compressFormat : sorted) {
                formats2combo[basedGroup][counter] = compressFormat;
                formats.put(compressFormat, counter++);
            }

            set = expend;
            initFormat[basedGroup] = sorted.first();
            if (basedGroup > 1) {
                genLinkFormats(basedGroup, initFormat);
            }
        }
    }

    // generate the format link of 4 direction of moves of each bit represents a tile location
    // and the key shift reference code
    private void genLinkFormats(int group, int [] initFormat) {
        linkFormatCombo[group] = new int[formatSize[group]][0];
        linkFormatMove[group] = new int[formatSize[group] * 64];
        HashSet<Integer> set = new HashSet<Integer>();
        HashSet<Integer> visited;
        set.add(initFormat[group]);
        while (!set.isEmpty()) {
            visited = set;
            set = new HashSet<Integer>();
            for (int fmt : visited) {
                int fmtIdx = formats.get(fmt);
                int key = 0;
                linkFormatCombo[group][fmtIdx] = new int [group * 4];

                for (int pos = 0; pos < puzzleSize; pos++) {
                    if ((fmt & bitPos16[pos]) > 0) {
                        int [] next = {-1, -1, -1, -1};
                        int [] shift = {0, 0, 0, 0};
                        int base = fmt ^ bitPos16[pos];
                        // space right, tile left
                        if (pos % 4 > 0) {
                            if ((fmt & bitPos16[pos - 1]) == 0) {
                                next[Direction.RIGHT.getValue()] = base | bitPos16[pos - 1];
                            }
                        }
                        // space down, tile up
                        if (pos > 3) {
                            if ((fmt & bitPos16[pos - 4]) == 0) {
                                next[Direction.DOWN.getValue()] = base | bitPos16[pos - 4];
                                int numShift = 0;
                                for (int keyShift = 1; keyShift < 4; keyShift++) {
                                    if ((fmt & bitPos16[pos - keyShift]) > 0) {
                                        numShift++;
                                    }
                                }
                                if (numShift > 0) {
                                    shift[Direction.DOWN.getValue()] = numShift * 2 - 1;
                                }
                            }
                        }
                        // space left, tile right
                        if (pos % 4 < 3) {
                            if ((fmt & bitPos16[pos + 1]) == 0) {
                                next[Direction.LEFT.getValue()] = base | bitPos16[pos + 1];
                            }
                        }
                        // space up, tile down
                        if (pos < 12) {
                            if ((fmt & bitPos16[pos + 4]) == 0) {
                                next[Direction.UP.getValue()] = base | bitPos16[pos + 4];
                                int numShift = 0;
                                for (int keyShift = 1; keyShift < 4; keyShift++) {
                                    if ((fmt & bitPos16[pos + keyShift]) > 0) {
                                        numShift++;
                                    }
                                }
                                if (numShift > 0) {
                                    shift[Direction.UP.getValue()] = numShift * 2;
                                }
                            }
                        }

                        for (int move = 0; move < 4; move++) {
                            if (next[move] > -1) {
                                int zeroPos = pos;
                                if (move == Direction.RIGHT.getValue()) {
                                    zeroPos--;
                                } else if (move == Direction.DOWN.getValue()) {
                                    zeroPos -= 4;
                                } else if (move == Direction.LEFT.getValue()) {
                                    zeroPos++;
                                } else if (move == Direction.UP.getValue()) {
                                    zeroPos += 4;
                                }
                                linkFormatCombo[group][fmtIdx][key * 4 + move]
                                        = shift[move] | (next[move] << 4);
                                linkFormatMove[group][fmtIdx * 64 + zeroPos * 4 + move]
                                        = (formats.get(next[move]) << 8) | (key << 4) | shift[move];
                                if (linkFormatCombo[group][formats.get(next[move])].length == 0) {
                                    set.add(next[move]);
                                }
                            }
                        }
                        key++;
                    }
                }
            }
        }
    }

    /**
     * Returns the integer array that the '1' bit represent the position of the board.
     *
     * @return integer array that the '1' bit represent the position of the board
     */
    public static int[] getBitPos16() {
        return bitPos16;
    }

    /**
     * Returns the integer of maximum group size of pattern.
     *
     * @return integer of maximum group size of pattern
     */
    public static int getMaxGroupSize() {
        return maxGroupSize;
    }

    /**
     * Returns the integer of the key size of the given group.
     *
     * @param group the given group size
     * @return integer of the key size of the given group
     */
    public static int getKeySize(int group) {
        return keySize[group];
    }

    /**
     * Returns the integer of the format size of the given group.
     *
     * @param group the given group size
     * @return integer of the format size of the given group
     */
    public static int getFormatSize(int group) {
        return formatSize[group];
    }

    /**
     * Returns the integer of the double value of maximum key shift of the given group.
     *
     * @param group the given group size
     * @return integer of the double value of maximum key shift of the given group
     */
    public static int getShiftMaxX2(int group) {
        return maxShiftX2[group];
    }

    /**
     * Returns the string of the file path of the given group.
     *
     * @param group the given group size
     * @return string of the file path of the given group
     */
    public static String getFilePath(int group) {
        return filePath + group + ".db";
    }

    /**
     * Returns HashMap of compress key combo to key index.
     *
     * @return HashMap of compress key combo to key index
     */
    public final HashMap<Integer, Integer> getKeys() {
        return keys;
    }

    /**
     * Returns HashMap of 16 bits format pattern to format index.
     *
     * @return HashMap of 16 bits format pattern to format index
     */
    public final HashMap<Integer, Integer> getFormats() {
        return formats;
    }

    /**
     * Returns integer array of 16 bits format pattern with the given group.
     *
     * @param group the given group size
     * @return integer array of 16 bits format pattern with the given group
     */
    protected final int [] getFormatCombo(int group) {
        return formats2combo[group];
    }

    /**
     * Returns integer array of 16 bits format pattern with the given group.
     *
     * @param group the given group size
     * @return integer array of 16 bits format pattern with the given group
     */
    public final int [] getKeyCombo(int group) {
        return keys2combo[group];
    }

    /**
     * Returns integer array of key shifting set with the given group.
     *
     * @param group the given group size
     * @return integer array of key shifting set with the given group
     */
    public final int [] getKeyShiftSet(int group) {
        return rotateKeyByPos[group];
    }

    /**
     * Returns integer array of format change set with the given group.
     *
     * @param group the given group size
     * @return integer array of format change set with the given group
     */
    protected final int[][] getLinkFormatComboSet(int group) {
        return linkFormatCombo[group];
    }

    /**
     * Returns integer array of key shift count from format change set with the given group.
     *
     * @param group the given group size
     * @return integer array of key shift count from format change set with the given group
     */
    public final int[] getLinkFormatMoveSet(int group) {
        return linkFormatMove[group];
    }
}
