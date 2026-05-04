package tictactoe;

public class GameLogic
{
    public boolean checkWin(Board board, char player)
    {
        char[][] grid = board.getGrid();
        int size = grid.length;

        // Rows
        for (int row = 0; row < size; row++)
        {
            boolean win = true;
            for (int col = 0; col < size; col++)
                if (grid[row][col] != player) { win = false; break; }
            if (win) return true;
        }

        // Cols
        for (int col = 0; col < size; col++)
        {
            boolean win = true;
            for (int row = 0; row < size; row++)
                if (grid[row][col] != player) { win = false; break; }
            if (win) return true;
        }

        // Main diagonal
        boolean win = true;
        for (int i = 0; i < size; i++)
            if (grid[i][i] != player) { win = false; break; }
        if (win) return true;

        // Anti-diagonal
        win = true;
        for (int i = 0; i < size; i++)
            if (grid[i][size - 1 - i] != player) { win = false; break; }
        return win;
    }

    // ── FIXED: was returning inside the row loop ──────────────────
    public boolean isDraw(Board board)
    {
        // Can't be a draw if someone has already won
        if (checkWin(board, 'X') || checkWin(board, 'O')) return false;

        // Must have no empty cells left
        char[][] grid = board.getGrid();
        for (int row = 0; row < grid.length; row++)
            for (int col = 0; col < grid[0].length; col++)
                if (grid[row][col] == 'E') return false;

        return true;
    }

    public boolean isGameOver(Board board)
    {
        return checkWin(board, 'X') || checkWin(board, 'O') || isDraw(board);
    }

    public char getCurrentPlayer(Board board)
    {
        char[][] grid = board.getGrid();
        int xCount = 0, oCount = 0;
        for (int row = 0; row < grid.length; row++)
            for (int col = 0; col < grid[0].length; col++)
            {
                if (grid[row][col] == 'X') xCount++;
                else if (grid[row][col] == 'O') oCount++;
            }
        return (xCount == oCount) ? 'X' : 'O';
    }

    public boolean makeMove(Board board, int row, int col)
    {
        if (row < 0 || row >= board.getGrid().length ||
            col < 0 || col >= board.getGrid()[0].length) return false;
        if (board.getCell(row, col) != 'E') return false;
        board.setCell(row, col, getCurrentPlayer(board));
        return true;
    }
}