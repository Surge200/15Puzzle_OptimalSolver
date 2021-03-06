package mwong.myprojects.fifteenpuzzle.solver.advanced;

import mwong.myprojects.fifteenpuzzle.solver.SmartSolverExtra;
import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceRemote;
import mwong.myprojects.fifteenpuzzle.solver.components.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.PatternOptions;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverPdb;
import mwong.myprojects.fifteenpuzzle.utilities.Stopwatch;

import java.rmi.RemoteException;

/**
 * SmartSolverPdbBase extends SolverPdb.  It extends the standard solver using the reference
 * boards collection to boost the initial estimate only, without using partial preset solution.
 *
 * <p>Dependencies : Board.java, Direction.java, HeuristicOptions.java,
 *                   PatternOptions.java, ReferenceRemote.java, SmartSolverExtra.java,
 *                   SolverPdb.java, Stopwatch.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SmartSolverPdbBase extends SolverPdb {
    protected Board lastSearchBoard;
    protected boolean addedReference;

    /**
     * Default constructor.
     */
    SmartSolverPdbBase() {}

    // Initializes SmartSolverPdbBase object with choice of given preset pattern.
    SmartSolverPdbBase(PatternOptions presetPattern, int choice) {
        super(presetPattern, choice);
    }

    // Initializes SmartSolverPdbBase object with preset pattern type and application mode.
    SmartSolverPdbBase(PatternOptions presetPattern, ApplicationMode appMode) {
        super(presetPattern, appMode);
    }

    // Initializes SmartSolverPdbBase object with user defined custom pattern.
    SmartSolverPdbBase(byte[] customPattern, boolean[] elementGroups) {
        super(customPattern, elementGroups);
    }

    /**
     * Initializes SolverPdb object with a given concrete class.
     *
     * @param copySolver an instance of SolverPdb
     * @param refConnection the given ReferenceRemote connection object
     */
    public SmartSolverPdbBase(SolverPdb copySolver, ReferenceRemote refConnection) {
        super(copySolver);
        try {
            if (refConnection == null || refConnection.getActiveMap() == null) {
                System.out.println("Attention: Referece board collection unavailable."
                        + " Advanced estimate will use standard estimate.");
            } else {
                activeSmartSolver = true;
                extra = new SmartSolverExtra();
                this.refConnection = refConnection;
            }
        } catch (RemoteException ex) {
            System.err.println(this.getClass().getSimpleName()
                    + " - Attention: Server connection failed. Resume to standard version.\n");
            flagAdvancedVersion = tagStandard;
            activeSmartSolver = false;
        }
    }

    /**
     * Print solver description with in use pattern.
     */
    @Override
    public void printDescription() {
        extra.printDescription(flagAdvancedVersion, inUseHeuristic);
        printInUsePattern();
    }

    /**
     * Find the optimal path to goal state if the given board is solvable.
     * Overload findOptimalPath with given heuristic value (for AdvancedAccumulator)
     *
     * @param board the initial puzzle Board object to solve
     * @param estimate the given initial limit to solve the puzzle
     */
    public void findOptimalPath(Board board, byte estimate) throws RemoteException {
        if (board.isSolvable()) {
            clearHistory();
            stopwatch = new Stopwatch();
            setLastDepthSummary(board);
            // initializes the board by calling heuristic function using original priority
            // then solve the puzzle with given estimate instead
            heuristic(board, tagStandard, tagSearch);
            idaStar(estimate);
            stopwatch = null;
        }
    }

    /**
     * Returns the heuristic value of the given board based on the solver setting.
     *
     * @param board the initial puzzle Board object to solve
     * @return byte value of the heuristic value of the given board
     */
    @Override
    public byte heuristic(Board board) {
        return heuristic(board, flagAdvancedVersion, tagSearch);
    }

    // overload method to calculate the heuristic value of the given board and conditions
    protected byte heuristic(Board board, boolean isAdvanced, boolean isSearch) {
        if (!board.isSolvable()) {
            return -1;
        }

        if (!board.equals(lastBoard) || isSearch) {
            priorityGoal = super.heuristic(board);
        }

        if (!isAdvanced) {
            return priorityGoal;
        } else if (!isSearch && priorityAdvanced != -1) {
            return priorityAdvanced;
        }

        setPriorityAdvanced(board, isSearch);
        return priorityAdvanced;
    }

    /**
     * Returns the original heuristic value of the given board.
     *
     * @return byte value of the original heuristic value of the given board
     */
    @Override
    public byte heuristicStandard(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }

        if (!board.isSolvable()) {
            return -1;
        }
        return heuristic(board, tagStandard, tagReview);
    }

    /**
     * Returns the advanced heuristic value of the given board.
     *
     * @return byte value of the advanced heuristic value of the given board
     */
    @Override
    public byte heuristicAdvanced(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board is null");
        }

        if (!board.isSolvable()) {
            return -1;
        }

        if (!activeSmartSolver) {
            heuristic(board, tagStandard, tagReview);
        }
        return heuristic(board, tagAdvanced, tagReview);
    }

    // solve the puzzle using interactive deepening A* algorithm
    protected void idaStar(int limit) {
        if (inUsePattern == PatternOptions.Pattern_78) {
            lastSearchBoard = new Board(tiles);
        }
        addedReference = false;

        searchCountBase = 0;
        int countDir = 0;
        for (int i = 0; i < rowSize; i++) {
            if (lastDepthSummary[i + rowSize] > 0) {
                countDir++;
            }
        }

        // quick scan for advanced priority, determine the start order for optimization
        if (flagAdvancedVersion && countDir > 1) {
            int initLimit = priorityGoal;
            while (initLimit < limit) {
                idaCount = 0;
                dfsStartingOrder(zeroX, zeroY, initLimit, pdValReg, pdValSym);
                initLimit += 2;

                boolean overload = false;
                for (int i = rowSize; i < rowSize * 2; i++) {
                    if (lastDepthSummary[i] > 10000) {
                        overload = true;
                        break;
                    }
                }
                if (overload) {
                    break;
                }
            }
        }

        super.idaStar(limit);
    }

    /**
     * Returns the boolean represents the advanced priority in use.
     *
     * @return boolean represents the advanced priority in use
     */
    public final boolean getTimeoutFlag() {
        return flagTimeout;
    }

    /**
     * Returns the boolean represents the advanced priority in use.
     *
     * @return boolean represents the advanced priority in use
     */
    public final boolean getMessageFlag() {
        return flagMessage;
    }

    /**
     * Returns the boolean represents the advanced priority in use.
     *
     * @return boolean represents the advanced priority in use
     */
    public final boolean getInUseVersionFlag() {
        return flagAdvancedVersion;
    }

    /**
     * Returns the board object of last search.
     *
     * @return board object of last search
     */
    public final Board lastSearchBoard() {
        if (inUsePattern != PatternOptions.Pattern_78) {
            throw new UnsupportedOperationException();
        }
        return lastSearchBoard;
    }

    /**
     * Returns the boolean value of last search has added the board in reference collection.
     *
     * @return board object of last search has added the board in reference collection
     */
    public final boolean isAddedReference() {
        return addedReference;
    }
}
