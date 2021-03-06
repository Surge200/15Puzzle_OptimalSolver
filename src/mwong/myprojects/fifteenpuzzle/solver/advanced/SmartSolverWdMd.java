package mwong.myprojects.fifteenpuzzle.solver.advanced;

import mwong.myprojects.fifteenpuzzle.solver.ai.ReferenceRemote;
import mwong.myprojects.fifteenpuzzle.solver.components.ApplicationMode;
import mwong.myprojects.fifteenpuzzle.solver.components.Board;
import mwong.myprojects.fifteenpuzzle.solver.components.Direction;
import mwong.myprojects.fifteenpuzzle.solver.standard.SolverWdMd;

/**
 * SmartSolverWdMd extends SolverWdMd.  The advanced version extend the standard solver
 * using the reference boards collection to boost the initial estimate.
 *
 * <p>Dependencies : Board.java, Direction.java, ReferenceAccumulator.java,
 *                   SmartSolverConstants.java, SmartSolverExtra.java, SolverWdMd.java
 *
 * @author Meisze Wong
 *         www.linkedin.com/pub/macy-wong/46/550/37b/
 */
public class SmartSolverWdMd extends SolverWdMd {
    /**
     * Initializes SmartSolverWdMd object.
     */
    public SmartSolverWdMd() {
        this(null);
    }

    /**
     * Initializes SmartSolverWdMd object.  If refAccumlator is null or empty,
     * it will act as standard version.
     *
     * @param refConnection the given ReferenceRemote connection object
     */
    public SmartSolverWdMd(ReferenceRemote refConnection) {
        super();
        setReferenceConnection(refConnection);
    }

    /**
     * Initializes SmartSolverWdMd object.  If refAccumlator is null or empty,
     * it will act as standard version.
     *
     * @param refConnection the given ReferenceRemote connection object
     * @param appMode the given applicationMode for GUI or CONSOLE
     */
    public SmartSolverWdMd(ReferenceRemote refConnection, ApplicationMode appMode) {
        super(appMode);
        setReferenceConnection(refConnection);
    }

    /**
     * Print solver description.
     */
    @Override
    public void printDescription() {
        extra.printDescription(flagAdvancedVersion, inUseHeuristic);
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
    private byte heuristic(Board board, boolean isAdvanced, boolean isSearch) {
        if (!board.isSolvable()) {
            return -1;
        }

        if (!board.equals(lastBoard) || isSearch) {
            // walking distance from parent/superclass
            priorityGoal = super.heuristic(board);
            priorityAdvanced = -1;
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

    // overload idaStar to solve the puzzle with the given max limit for advancedEstimate
    protected void idaStar(int limit, int maxLimit) {
        while (limit <= maxLimit) {
            dfsStartingOrder(zeroX, zeroY, 0, limit, wdIdxH, wdIdxV, wdValueH, wdValueV);
            if (solved) {
                return;
            }
            limit += 2;
        }
    }

    // solve the puzzle using interactive deepening A* algorithm
    protected void idaStar(int limit) {
        searchCountBase = 0;
        if (solutionMove[1] != null) {
            advancedSearch(limit);
            return;
        }
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
                dfsStartingOrder(zeroX, zeroY, initLimit, mdlcValue,
                        wdIdxH, wdIdxV, wdValueH, wdValueV);
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

    // skip the first 8 moves from stored record then solve the remaining puzzle
    // using depth first search with exact number of steps of optimal solution
    private void advancedSearch(int limit) {
        Direction[] dupSolution = new Direction[limit + 1];
        Board board = prepareAdvancedSearch(limit, dupSolution);
        heuristic(board, tagStandard, tagSearch);
        setLastDepthSummary(dupSolution[numPartialMoves]);

        idaCount = numPartialMoves;
        if (flagMessage) {
            System.out.print("ida limit " + limit);
        }
        dfsStartingOrder(zeroX, zeroY, limit - numPartialMoves + 1, mdlcValue,
                wdIdxH, wdIdxV, wdValueH, wdValueV);
        searchNodeCount = idaCount;
        afterAdvancedSearch(limit, dupSolution);
    }
}
