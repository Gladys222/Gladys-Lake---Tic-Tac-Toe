package tictactoe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ComputerPlayer — picks moves for both the classic Board and DynamicBoard.
 *
 * Strategy (in order):
 *   1. Win immediately if possible
 *   2. Block opponent's winning move
 *   3. Take centre (or near-centre for bigger boards)
 *   4. Random empty cell
 *
 * For Challenge C2 (Piece Stealer), the computer may choose to steal
 * an opponent piece rather than place a new one.
 */
public class ComputerPlayer
{
    private final GameLogic logic;
    private final char mySymbol;
    private final char opponentSymbol;
    private final Random rng = new Random();

    public ComputerPlayer(GameLogic logic, char mySymbol)
    {
        this.logic          = logic;
        this.mySymbol       = mySymbol;
        this.opponentSymbol = (mySymbol == 'X') ? 'O' : 'X';
    }

    // ── Classic 3×3 board ─────────────────────────────────────────
    /** Returns int[]{row, col} for the chosen move. */
    public int[] chooseMove(Board board)
    {
        int[] win   = findWinningMove(board, mySymbol);
        if (win   != null) return win;

        int[] block = findWinningMove(board, opponentSymbol);
        if (block != null) return block;

        if (board.getCell(1, 1) == 'E') return new int[]{1, 1};

        return randomMove(board);
    }

    private int[] findWinningMove(Board board, char symbol)
    {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (board.getCell(r, c) == 'E')
                {
                    board.setCell(r, c, symbol);
                    boolean wins = logic.checkWin(board, symbol);
                    board.setCell(r, c, 'E');
                    if (wins) return new int[]{r, c};
                }
        return null;
    }

    private int[] randomMove(Board board)
    {
        ArrayList<int[]> empty = new ArrayList<>();
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (board.getCell(r, c) == 'E')
                    empty.add(new int[]{r, c});
        return empty.get(rng.nextInt(empty.size()));
    }

    // ── DynamicBoard (challenge mode) ─────────────────────────────
    /**
     * Returns int[]{row, col} for normal placement on a DynamicBoard.
     * winLen is how many in a row are needed.
     */
    public int[] chooseMoveOnDynamic(DynamicBoard db, int winLen)
    {
        int size = db.size();

        // 1. Win
        int[] win = findWinningMoveDynamic(db, mySymbol, winLen);
        if (win != null) return win;

        // 2. Block
        int[] block = findWinningMoveDynamic(db, opponentSymbol, winLen);
        if (block != null) return block;

        // 3. Centre or near-centre
        int mid = size / 2;
        if (db.getCell(mid, mid) == 'E') return new int[]{mid, mid};

        // 4. Random
        List<int[]> empty = db.emptyCells();
        if (empty.isEmpty()) return new int[]{0, 0}; // fallback (shouldn't happen)
        return empty.get(rng.nextInt(empty.size()));
    }

    private int[] findWinningMoveDynamic(DynamicBoard db, char symbol, int winLen)
    {
        int size = db.size();
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (db.getCell(r, c) == 'E')
                {
                    db.setCell(r, c, symbol);
                    boolean wins = db.checkWin(symbol, winLen);
                    db.setCell(r, c, 'E');
                    if (wins) return new int[]{r, c};
                }
        return null;
    }

    /**
     * C2 Piece Stealer: computer decides whether to steal or place.
     * Returns int[]{row, col, mode}  where mode=0 = place, mode=1 = steal.
     */
    public int[] chooseChallengeMove(DynamicBoard db, int winLen, boolean canSteal)
    {
        // First try to win by placing
        int[] winMove = findWinningMoveDynamic(db, mySymbol, winLen);
        if (winMove != null) return new int[]{winMove[0], winMove[1], 0};

        // Then try to block by placing
        int[] blockMove = findWinningMoveDynamic(db, opponentSymbol, winLen);
        if (blockMove != null) return new int[]{blockMove[0], blockMove[1], 0};

        // If canSteal, consider stealing a key opponent piece (random choice for now)
        if (canSteal && rng.nextBoolean())
        {
            List<int[]> opCells = db.getOpponentCells(opponentSymbol);
            if (!opCells.isEmpty())
            {
                int[] stolen = opCells.get(rng.nextInt(opCells.size()));
                return new int[]{stolen[0], stolen[1], 1}; // mode=1 = steal
            }
        }

        // Normal placement
        int[] place = chooseMoveOnDynamic(db, winLen);
        return new int[]{place[0], place[1], 0};
    }

    public char getSymbol() { return mySymbol; }
}