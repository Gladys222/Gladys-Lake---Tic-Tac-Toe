package tictactoe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * DynamicBoard — flexible in-memory board for Challenge Mode (3x3, 4x4, 5x5).
 *
 * Turn tracking uses a simple integer counter (turnCount) so that steals
 * and vanishes can never corrupt whose turn it is.
 *   turnCount even  → X's turn
 *   turnCount odd   → O's turn
 *
 * Calling advanceTurn() is the ONE place that moves the game forward.
 * Both normal placements and steals call advanceTurn() exactly once.
 *
 * Steal limit: each player may steal at most MAX_STEALS times per round.
 */
public class DynamicBoard
{
    public static final int MAX_STEALS = 2;

    private char[][] grid;
    private final int size;

    // ── Turn counter (the only source of truth for whose turn it is) ──
    private int  turnCount = 0;   // even = X, odd = O
    private int  stealsX   = 0;
    private int  stealsO   = 0;

    public DynamicBoard(int size)
    {
        this.size = size;
        grid = new char[size][size];
        clear();
    }

    // ── Accessors ────────────────────────────────────────────────
    public int     size()                   { return size; }
    public char    getCell(int r, int c)    { return grid[r][c]; }
    public char[][] getGrid()              { return grid; }
    public void    setCell(int r, int c, char v) { grid[r][c] = v; }

    // ── Turn management ──────────────────────────────────────────
    /** Whose turn is it right now? */
    public char currentPlayer() { return (turnCount % 2 == 0) ? 'X' : 'O'; }

    /** Call once after any action (place OR steal) to move to next player. */
    public void advanceTurn() { turnCount++; }

    // ── Steal tracking ───────────────────────────────────────────
    public int  stealsUsed(char player) { return (player == 'X') ? stealsX : stealsO; }
    public boolean canSteal(char player) { return stealsUsed(player) < MAX_STEALS; }
    public void recordSteal(char player)
    {
        if (player == 'X') stealsX++;
        else               stealsO++;
    }

    // ── Blocked cell (stolen cell cannot be placed on immediately after) ──
    private int blockedRow = -1;
    private int blockedCol = -1;

    /** Steal (blank out) a cell and mark it as blocked for one turn. */
    public void stealCell(int r, int c)
    {
        grid[r][c] = 'E';
        blockedRow = r;
        blockedCol = c;
    }

    /** Returns true if this cell was just stolen and cannot be placed on yet. */
    public boolean isBlocked(int r, int c)
    {
        return r == blockedRow && c == blockedCol;
    }

    /** Clear the block — call this after the next player completes their turn. */
    public void clearBlock()
    {
        blockedRow = -1;
        blockedCol = -1;
    }

    // ── Clear ────────────────────────────────────────────────────
    public void clear()
    {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                grid[r][c] = 'E';
        turnCount  = 0;
        stealsX    = 0;
        stealsO    = 0;
        blockedRow = -1;
        blockedCol = -1;
    }

    // ── Win / draw ───────────────────────────────────────────────
    public boolean checkWin(char player, int winLen)
    {
        // Rows
        for (int r = 0; r < size; r++)
        {
            int run = 0;
            for (int c = 0; c < size; c++)
            {
                run = (grid[r][c] == player) ? run + 1 : 0;
                if (run >= winLen) return true;
            }
        }
        // Cols
        for (int c = 0; c < size; c++)
        {
            int run = 0;
            for (int r = 0; r < size; r++)
            {
                run = (grid[r][c] == player) ? run + 1 : 0;
                if (run >= winLen) return true;
            }
        }
        // Diagonals top-left → bottom-right
        for (int r = 0; r <= size - winLen; r++)
            for (int c = 0; c <= size - winLen; c++)
            {
                boolean ok = true;
                for (int k = 0; k < winLen; k++)
                    if (grid[r + k][c + k] != player) { ok = false; break; }
                if (ok) return true;
            }
        // Anti-diagonals
        for (int r = 0; r <= size - winLen; r++)
            for (int c = winLen - 1; c < size; c++)
            {
                boolean ok = true;
                for (int k = 0; k < winLen; k++)
                    if (grid[r + k][c - k] != player) { ok = false; break; }
                if (ok) return true;
            }
        return false;
    }

    public boolean isDraw(int winLen)
    {
        if (checkWin('X', winLen) || checkWin('O', winLen)) return false;
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] == 'E') return false;
        return true;
    }

    public boolean isGameOver(int winLen)
    {
        return checkWin('X', winLen) || checkWin('O', winLen) || isDraw(winLen);
    }

    // ── C3 Vanishing Act ─────────────────────────────────────────
    /**
     * Removes 1 or 2 random pieces, but NEVER if removal would immediately
     * hand a win to either player, and NEVER changes whose turn it is
     * (turnCount is not modified).
     */
    public int vanish(int winLen)
    {
        Random rng = new Random();
        List<int[]> filled = new ArrayList<int[]>();
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] != 'E') filled.add(new int[]{r, c});

        if (filled.isEmpty()) return 0;

        Collections.shuffle(filled, rng);
        int count   = Math.min(1 + rng.nextInt(2), filled.size());
        int removed = 0;

        for (int[] cell : filled)
        {
            if (removed >= count) break;
            char backup = grid[cell[0]][cell[1]];
            grid[cell[0]][cell[1]] = 'E';
            if (!checkWin('X', winLen) && !checkWin('O', winLen))
                removed++;
            else
                grid[cell[0]][cell[1]] = backup;
        }
        return removed;
        // turnCount intentionally NOT changed — vanish doesn't affect whose turn it is
    }

    // ── Helpers ──────────────────────────────────────────────────
    public List<int[]> emptyCells()
    {
        List<int[]> list = new ArrayList<int[]>();
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] == 'E' && !isBlocked(r, c)) list.add(new int[]{r, c});
        return list;
    }

    public List<int[]> getOpponentCells(char opponent)
    {
        List<int[]> list = new ArrayList<int[]>();
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] == opponent) list.add(new int[]{r, c});
        return list;
    }
}