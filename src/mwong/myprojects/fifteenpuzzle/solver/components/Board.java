/****************************************************************************
 *  @author   Meisze Wong
 *            www.linkedin.com/pub/macy-wong/46/550/37b/
 *
 *  Compilation: javac Board.java
 *  Dependencies : Direction.java
 *
 *  A data type for 15 Puzzle board
 *
 ****************************************************************************/

package mwong.myprojects.fifteenpuzzle.solver.components;

import java.util.ArrayList;
import java.util.Random;

public class Board {
    /**
     *  15 puzzle board difficult level that can be used.
     *  <li>{@link #RIGHT}</li>
     *  <li>{@link #DOWN}</li>
     *  <li>{@link #LEFT}</li>
     *  <li>{@link #UP}</li>
     *  <li>{@link #NONE}</li>
     */
    public enum Level {
        /**
         *  Difficult level easyt.
         */
        Easy,
        /**
         *  Difficult level moderate.
         */
        Moderate,
        /**
         *  Difficult level hard.
         */
        Hard,
        /**
         *  Difficult level random.
         */
        Random;
    }

    private static final byte rowSize = 4;
    private static final byte size = 16;
    private static final byte maxMoves = 80;
    // initializes the goal state to be use to generate random board
    private static final byte [] goalBoard =
        {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0};
    private static final int goalHash1 = 0x12345678;
    private static final int goalHash2 = 0x9ABCDEF0;

    // initializes hard boards to be use to generate random board
    private static final byte [][] presetHardZero0 = {
        {0, 11,  9, 13, 12, 15, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1},
        {0, 15,  9, 13, 11, 12, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1},
        {0, 12,  9, 13, 15, 11, 10, 14,  3,  7,  6,  2,  4,  8,  5,  1},
        {0, 12,  9, 13, 15, 11, 10, 14,  3,  7,  2,  5,  4,  8,  6,  1},
        {0, 12, 10, 13, 15, 11, 14,  9,  3,  7,  2,  5,  4,  8,  6,  1},
        {0, 12, 14, 13, 15, 11,  9, 10,  3,  7,  6,  2,  4,  8,  5,  1},
        {0, 12, 10, 13, 15, 11, 14,  9,  3,  7,  6,  2,  4,  8,  5,  1},
        {0, 12, 11, 13, 15, 14, 10,  9,  3,  7,  6,  2,  4,  8,  5,  1},
        {0, 12, 10, 13, 15, 11,  9, 14,  7,  3,  6,  2,  4,  8,  5,  1},
        {0, 12,  9, 13, 15, 11, 14, 10,  3,  8,  6,  2,  4,  7,  5,  1},
        {0, 12,  9, 13, 15, 11, 10, 14,  8,  3,  6,  2,  4,  7,  5,  1},
        {0, 12, 14, 13, 15, 11,  9, 10,  8,  3,  6,  2,  4,  7,  5,  1},
        {0, 12,  9, 13, 15, 11, 10, 14,  7,  8,  6,  2,  4,  3,  5,  1},
        {0, 12, 10, 13, 15, 11, 14,  9,  7,  8,  6,  2,  4,  3,  5,  1},
        {0, 12,  9, 13, 15,  8, 10, 14, 11,  7,  6,  2,  4,  3,  5,  1},
        {0, 12,  9, 13, 15, 11, 10, 14,  3,  7,  5,  6,  4,  8,  2,  1},
        {0, 12,  9, 13, 15, 11, 10, 14,  7,  8,  5,  6,  4,  3,  2,  1},
        {0, 15, 14, 13, 12, 11, 10,  9,  8,  7,  6,  5,  4,  3,  2,  1},
        {0, 15,  8,  3, 12, 11,  7,  4, 14, 10,  6,  5,  9, 13,  2,  1},
        {0, 12, 14,  4, 15, 11,  7,  3,  8, 10,  6,  5, 13,  9,  2,  1},
        {0, 12,  7,  3, 15, 11,  8,  4, 10, 14,  6,  2,  9, 13,  5,  1},
        {0, 12,  7,  4, 15, 11,  8,  3, 10, 14,  6,  2, 13,  9,  5,  1},
        {0, 12,  8,  3, 15, 11, 10,  4, 14,  7,  6,  5,  9, 13,  2,  1},
        {0, 12,  8,  3, 15, 11,  7,  4, 14, 10,  6,  2,  9, 13,  5,  1},
        {0, 12,  8,  4, 15, 11,  7,  3, 14, 10,  6,  2, 13,  9,  5,  1},
        {0, 12,  8,  7, 15, 11,  4,  3, 14, 13,  6,  2, 10,  9,  5,  1},
        {0, 15,  4, 10, 12, 11,  8,  3, 13, 14,  6,  2,  7,  9,  5,  1},
        {0, 15,  7,  4, 12, 11,  8,  5, 10, 14,  6,  3, 13,  2,  9,  1},
        {0, 15,  7,  8, 12, 11,  4,  3, 10, 13,  6,  5, 14,  9,  2,  1},
        {0, 15,  8, 10, 12, 11,  4,  3, 14, 13,  6,  2,  7,  9,  5,  1},
        {0, 15,  8,  3, 12, 11, 10,  4, 14,  7,  6,  2,  9, 13,  5,  1},
        {0, 15,  8,  4, 12, 11,  7,  3, 14, 10,  6,  5, 13,  9,  2,  1},
        {0, 15,  8,  4, 12, 11,  7,  5, 14, 10,  6,  3, 13,  2,  9,  1},
        {0, 15,  8,  7, 12, 11,  4,  3, 14, 13,  6,  5, 10,  9,  2,  1},
        {0,  2,  9, 13,  5,  1, 10, 14,  3,  7,  6, 15,  4,  8, 12, 11},
        {0,  5,  9, 13,  2,  1, 10, 14,  3,  7, 11, 15,  4,  8, 12,  6},
        {0,  5,  9, 13,  2,  6, 10, 14,  3,  7,  1, 15,  4,  8, 12, 11},
        {0,  5,  9, 14,  2,  6, 10, 13,  3,  7,  1, 15,  8,  4, 12, 11}
    };
    // initializes hard boards to be use to generate random board
    private static final byte [][] presetHardZero15 = {
        {1, 10, 14, 13,  7,  6,  5,  9,  8,  2, 11, 15,  4,  3, 12,  0},
        {1, 10,  9, 13,  7,  6,  5, 14,  3,  2, 11, 15,  4,  8, 12,  0},
        {1,  5, 14, 13,  2,  6, 10,  9,  8,  7, 11, 15,  4,  3, 12,  0},
        {1,  5,  9, 13,  2,  6, 10, 14,  3,  7, 11, 15,  4,  8, 12,  0},
        {6,  5, 13,  9,  2,  1, 10, 14,  4,  7, 11, 12,  3,  8, 15,  0},
        {6,  5, 14, 13,  2,  1, 10,  9,  8,  7, 11, 12,  4,  3, 15,  0},
        {6,  5,  9, 13,  2,  1, 10, 14,  3,  7, 11, 12,  4,  8, 15,  0},
        {6,  5,  9, 14,  2,  1, 10, 13,  3,  7, 11, 12,  8,  4, 15,  0}
    };

    // Symmetry board tile value conversion
    private static final byte [] symmetryVal =
        {0, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15, 4, 8, 12};
    // Symmetry board tile position conversion
    private static final byte [] symmetryPos =
        {0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};

    private boolean isSolvable;
    private boolean isIdenticalSymmetry;
    private int zeroX;
    private int zeroY;
    private int hash1;
    private int hash2;
    private int hashcode;
    private byte[] tiles;
    private byte[] tilesSym;
    private int[] validMoves;

    /**
     * Initializes a Board object, generate a random board.
     */
    public Board() {
        this(Level.Random);
    }

    /**
     * Initializes a Board object, generate a random board
     * of given difficult level.
     *
     * @param level the given difficulty level
     */
    public Board(Level level) {
        if (level == Level.Random) {
            generateRandomBoard();
        } else {
            generateBoard(level);
        }
        setAdvancedProperties();
    }

    /**
     * Initializes a Board object of a given 16 tiles byte array.
     *
     * @param blocks the byte array of 16 tiles
     */
    public Board(byte [] blocks) {
        setBasicPriorities(blocks);
        setAdvancedProperties();
    }

    // use by shift() from a solvable object
    // initializes a Board object for internal use
    private Board(int zeroX, int zeroY, byte[] tiles) {
        isSolvable = true;
        this.zeroX = zeroX;
        this.zeroY = zeroY;
        this.tiles = tiles;
        setAdvancedProperties();
    }

    // loop over the board and determine if the board is solvable
    // and initializes zero position
    private void setBasicPriorities(byte [] blocks) {
        isSolvable = true;
        tiles = new byte[size];
        System.arraycopy(blocks, 0, tiles, 0, size);

        // invert distance
        int invertH = 0;
        for (int i = 0; i < size; i++) {
            int value = tiles[i];
            if (value == 0) {
                zeroX = (byte) (i % rowSize);
                zeroY = (byte) (i / rowSize);
            } else {
                for (int j = i + 1; j < size; j++) {
                    if (blocks[j] > 0 && value > blocks[j])  {
                        invertH++;
                    }
                }
            }
        }

        if (zeroY % 2 == 1 && invertH % 2 == 1) {
            isSolvable = false;
        } else if (zeroY % 2 == 0 && invertH % 2 == 0) {
            isSolvable = false;
        }
    }

    // initializes the hashcode and valid moves with symmetry reduction
    private void setAdvancedProperties() {
        for (int i = 0; i < size / 2; i++) {
            hash1 <<= 4;
            hash1 |= tiles[i];
        }
        for (int i = size / 2; i < size; i++) {
            hash2 <<= 4;
            hash2 |= tiles[i];
        }
        hashcode = hash1 * (hash2 + 0x1111);

        tilesSym = new byte[size];
        // symmetry tiles of the original tiles
        for (int i = 0; i < size; i++) {
            tilesSym[symmetryPos[i]] = symmetryVal[tiles[i]];
        }

        validMoves = new int[4];
        // RIGHT
        if (zeroX < rowSize - 1) {
            validMoves[Direction.RIGHT.getValue()] = 1;
        }
        // DOWN
        if (zeroY < rowSize - 1) {
            validMoves[Direction.DOWN.getValue()] = 1;
        }
        // LEFT
        if (zeroX > 0) {
            validMoves[Direction.LEFT.getValue()] = 1;
        }
        // UP
        if (zeroY > 0) {
            validMoves[Direction.UP.getValue()] = 1;
        }

        isIdenticalSymmetry = true;
        for (int i = size - 1; i > -1; i--) {
            if (tiles[i] != tilesSym[i]) {
                isIdenticalSymmetry = false;
                break;
            }
        }
        if (isIdenticalSymmetry) {
            validMoves[Direction.DOWN.getValue()] = 0;
            validMoves[Direction.UP.getValue()] = 0;
        }
    }

    // generate a solvable random board with a given difficulty level
    private void generateBoard(Level level) {
        if (level == Level.Moderate) {
            int estimate;
            do {
                generateRandomBoard();
                estimate = heuristic();
            } while (estimate < 20 || estimate > 45);
        } else {
            byte [] blocks = new byte[size];
            int zero = 15;

            while (true) {
                System.arraycopy(goalBoard, 0, blocks, 0, size);
                zero = 15;
                if (level == Level.Hard) {
                    int rand = new Random().nextInt(5);
                    if (rand == 0) {
                        rand = new Random().nextInt(presetHardZero15.length);
                        System.arraycopy(presetHardZero15[rand], 0, blocks, 0, size);
                        zero = 15;
                    } else {
                        rand = new Random().nextInt(presetHardZero0.length);
                        System.arraycopy(presetHardZero0[rand], 0, blocks, 0, size);
                        zero = 0;
                    }
                }

                int shuffle = new Random().nextInt(100);
                int count = 0;
                while (count < shuffle) {
                    int dir = new Random().nextInt(4);
                    if (dir == 0 && zero % 4 < 3) {
                        blocks[zero] = blocks[zero + 1];
                        blocks[zero + 1] = 0;
                        zero = zero + 1;
                    } else if (dir == 1 && zero % 4 > 0) {
                        blocks[zero] = blocks[zero - 1];
                        blocks[zero - 1] = 0;
                        zero = zero - 1;
                    } else if (dir == 2 && zero > 3) {
                        blocks[zero] = blocks[zero - 4];
                        blocks[zero - 4] = 0;
                        zero = zero - 4;
                    } else if (dir == 3 && zero < 12) {
                        blocks[zero] = blocks[zero + 4];
                        blocks[zero + 4] = 0;
                        zero = zero + 4;
                    }
                    count++;
                }

                if (isGoal(blocks, zero)) {
                    continue;
                }

                tiles = new byte[size];
                System.arraycopy(blocks, 0, tiles, 0, size);
                if (level == Level.Hard && heuristic() > 40) {
                    break;
                }
                if (heuristic() < 25) {
                    break;
                }
            }

            isSolvable = true;
            zeroX = (byte) (zero % rowSize);
            zeroY = (byte) (zero / rowSize);
        }
    }

    // generate a solvable random board using Knuth Shuffle
    private void generateRandomBoard() {
        Random random = new Random();
        byte [] blocks = new byte[size];
        int count = 1;
        while (count < size) {
            int rand = random.nextInt(count + 1);
            blocks[count] = blocks[rand];
            blocks[rand] = (byte) (count++);
        }

        setBasicPriorities(blocks);
        // if the random board is not solvable, swap a pair of tiles.
        if (!isSolvable) {
            if (zeroY == 0)  {
                byte temp = tiles[4];
                tiles[4] = tiles[5];
                tiles[5] = temp;
            } else {
                byte temp = tiles[0];
                tiles[0] = tiles[1];
                tiles[1] = temp;
            }
            isSolvable = true;
        }
    }

    /**
     * Returns a Board object after it shift one move with the given direction.
     *
     * @param dir the given Direction of move
     * @return Board object after it shift one move with the given direction
     */
    public Board shift(Direction dir) {
        if (!isSolvable) {
            return null;
        }

        int orgX = zeroX;
        int orgY = zeroY;
        byte [] movedTiles = tiles.clone();
        int zeroPos = orgY * rowSize + orgX;
        switch (dir) {
            // space RIGHT, tile LEFT
            case RIGHT:
                if (orgX == rowSize - 1) {
                    return null;
                }
                orgX++;
                movedTiles[zeroPos] = tiles[zeroPos + 1];
                movedTiles[zeroPos + 1] = 0;
                return new Board(orgX, orgY, movedTiles);
            // space DOWN, tile UP
            case DOWN:
                if (orgY == rowSize - 1) {
                    return null;
                }
                orgY++;
                movedTiles[zeroPos] = tiles[zeroPos + rowSize];
                movedTiles[zeroPos + rowSize] = 0;
                return new Board(orgX, orgY, movedTiles);
            // space LEFT, tile RIGHT
            case LEFT:
                if (orgX == 0) {
                    return null;
                }
                orgX--;
                movedTiles[zeroPos] = tiles[zeroPos - 1];
                movedTiles[zeroPos - 1] = 0;
                return new Board(orgX, orgY, movedTiles);
            // space UP, tile DOWN
            case UP:
                if (orgY == 0) {
                    return null;
                }
                orgY--;
                movedTiles[zeroPos] = tiles[zeroPos - rowSize];
                movedTiles[zeroPos - rowSize] = 0;
                return new Board(orgX, orgY, movedTiles);
            default:
                return null;
        }
    }

    /**
     * Returns a ArrayList of all neighbor boards with symmetry reduction.
     *
     * @return a ArrayList of all neighbor boards with symmetry reduction
     */
    public ArrayList<Board> neighbors() {
        ArrayList<Board> list = new ArrayList<Board>();
        Board nextShift;
        nextShift = shift(Direction.RIGHT);
        if (nextShift != null) {
            list.add(nextShift);
        }
        if (!isIdenticalSymmetry) {
            nextShift = shift(Direction.DOWN);
            if (nextShift != null) {
                list.add(nextShift);
            }
        }
        nextShift = shift(Direction.LEFT);
        if (nextShift != null) {
            list.add(nextShift);
        }
        if (!isIdenticalSymmetry) {
            nextShift = shift(Direction.UP);
            if (nextShift != null) {
                list.add(nextShift);
            }
        }
        return list;
    }

    // returns the heuristic using manhattan distance
    private int heuristic() {
        int manhattan = 0;
        int value;
        int base = 0;
        for (int row = 0; row < rowSize; row++) {
            for (int col = 0; col < rowSize; col++) {
                value = tiles[base + col];
                if (value != 0) {
                    manhattan += Math.abs((value - 1) % rowSize - col);
                    manhattan += Math.abs((((value - 1)
                            - (value - 1) % rowSize) / rowSize) - row);
                }
            }
            base += rowSize;
        }
        return manhattan;
    }

    /**
     * Returns the boolean represent this board is the goal board.
     *
     * @return boolean represent this board is the goal board
     */
    public boolean isGoal() {
        if (hash1 == goalHash1 && hash2 == goalHash2) {
            return true;
        }
        return false;
    }

    // check if given values is goal state
    private boolean isGoal(byte[] blocks, int zero) {
        if (zero % rowSize != rowSize - 1 || zero / rowSize != rowSize - 1) {
            return false;
        } else {
            int idx = 0;
            while (idx < size - 1) {
                if (blocks[idx++] != idx) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the number of board dimension N.
     *
     * @return number of board dimension N
     */
    public static byte getRowSize() {
        return rowSize;
    }

    /**
     * Returns the number of board size.
     *
     * @return number of board size
     */
    public static int getSize() {
        return size;
    }

    /**
     * Returns the number of maximum moves.
     *
     * @return number of maximum moves
     */
    public static int getMaxMoves() {
        return maxMoves;
    }

    /**
     * Returns the byte array of goal state.
     *
     * @return byte array of goal state
     */
    public static byte[] getBoardgoal() {
        return goalBoard;
    }

    /**
     * Returns the byte array of symmetry value conversion.
     *
     * @return byte array of symmetry value conversion
     */
    public static byte[] getSymValConversion() {
        return symmetryVal;
    }

    /**
     * Returns the byte array of symmetry position conversion.
     *
     * @return byte array of symmetry position conversion
     */
    public static byte[] getSymPosConversion() {
        return symmetryPos;
    }

    /**
     * Returns the column index of zero position.
     *
     * @return integer represent the column index of zero space
     */
    public int getZeroX() {
        return zeroX;
    }

    /**
     * Returns the row index of zero position.
     *
     * @return integer represent the row index of zero space
     */
    public int getZeroY() {
        return zeroY;
    }

    /**
     * Returns the index of zero position as 1d array.
     *
     * @return integer represent the index of zero space as 1d array
     */
    public byte getZero1d() {
        return (byte) (zeroY * rowSize + zeroX);
    }

    /**
     * Returns the byte array of board tiles.
     *
     * @return byte array of board tiles
     */
    public final byte [] getTiles() {
        return tiles;
    }

    /**
     * Returns the byte array of board's symmetry tiles.
     *
     * @return byte array of board's symmetry tiles
     */
    public final byte[] getTilesSym() {
        return tilesSym;
    }

    /**
     * Returns the integer array of represent the valid moves.
     *
     * @return integer array of represent the valid moves
     */
    public final int[] getValidMoves() {
        return validMoves;
    }

    /**
     * Returns the boolean represent this board is solvable.
     *
     * @return boolean represent this board is solvable
     */
    public boolean isSolvable() {
        return isSolvable;
    }

    /**
     * Returns the boolean represent this board is a symmtry board.
     *
     * @return boolean represent this board is a symmtry board
     */
    public boolean isIdenticalSymmetry() {
        return isIdenticalSymmetry;
    }

    /**
     * Returns a string representation of the board, 4 rows with 4 numbers.
     *
     * @return a string representation of the board, 4 rows with 4 numbers
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < size; i++) {
            str.append(String.format("%2d ", tiles[i]));
            if (i % rowSize == rowSize - 1) {
                str.append("\n");
            }
        }
        return str.toString();
    }

    /**
     * Returns an integer of hashcode.
     *
     * @return an integer of hashcode
     */
    @Override
    public int hashCode() {
        return hashcode;
    }

    /**
     * Returns an boolean represents the given object is equal.
     *
     * @param obj the given Object
     * @return an boolean represents the given object is equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Board that = (Board) obj;
        if (this.hashcode != that.hashcode) {
            return false;
        }
        if (this.hash1 == that.hash1 && this.hash2 == that.hash2) {
            return true;
        }
        return false;
    }

    /**
     * Unit test.
     *
     * @param args Standard argument main function
     */
    public static void main(String[] args) {
        byte[] arr1 = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,0};
        byte[] arr2 = {1,2,3,4,9,10,11,12,5,6,7,8,13,14,15,0};
        byte[] arr3 = {1,4,2,3,9,10,11,12,5,6,7,8,13,14,15,0};
        Board board = new Board(arr1);
        System.out.println(board.isGoal());
        System.out.println(board.isIdenticalSymmetry());

        Board b2 = new Board(arr2);
        Board b3 = new Board(arr3);
        System.out.println(b2.equals(b3));
        System.out.println(b2.equals(b2));
        System.out.println(b2.isGoal());
        System.out.println(b2.isIdenticalSymmetry());
        System.out.println(b3.isGoal());
        System.out.println(b3.isIdenticalSymmetry());
    }
}
