package tictactoe;

import java.util.Scanner;

/**
 * Main — the only class that was added/changed for this feature set.
 *
 * What this class does:
 *   • Mode selection  : Player vs Player  OR  Player vs Computer
 *   • Score tracking  : X wins, O wins, Draws — updated with logic.checkWin()
 *   • Replay          : loops without restarting the program
 *   • Computer moves  : delegates entirely to ComputerPlayer
 *
 * Board and GameLogic are used exactly as-is — zero modifications.
 */
public class Main
{
    // ── ANSI colours (console only) ──────────────────────────────
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String DIM    = "\u001B[2m";
    private static final String RED    = "\u001B[31m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE   = "\u001B[34m";
    private static final String CYAN   = "\u001B[36m";
    private static final String WHITE  = "\u001B[97m";

    // ── Shared objects (reused across replays) ───────────────────
    private static final Board     board  = new Board("board.csv");
    private static final GameLogic logic  = new GameLogic();
    private static final Scanner   input  = new Scanner(System.in);

    // ── Score tracking variables (in main, not in Board/Logic) ───
    private static int xWins  = 0;
    private static int oWins  = 0;
    private static int ties   = 0;

    // ── Player names ─────────────────────────────────────────────
    private static String xName = "Player 1";
    private static String oName = "Player 2";

    // ── Mode flag ────────────────────────────────────────────────
    private static boolean vsComputer = false;

    // ════════════════════════════════════════════════════════════
    public static void main(String[] args)
    {
        printBanner();
        setupPlayers();         // names + mode selection

        // ── Replay loop — no program restart needed ───────────────
        boolean keepPlaying = true;
        while (keepPlaying)
        {
            board.clearBoard();  // reuse the same Board object
            playOneGame();
            updateScores();      // uses logic.checkWin() — no new logic
            printScores();
            keepPlaying = askReplay();
        }

        System.out.println(CYAN + BOLD + "\n  Thanks for playing! Final scores:\n" + RESET);
        printScores();
        System.out.println(CYAN + "  Goodbye!\n" + RESET);
        input.close();
    }

    // ════════════════════════════════════════════════════════════
    //  Setup
    // ════════════════════════════════════════════════════════════

    private static void printBanner()
    {
        System.out.println(CYAN + BOLD);
        System.out.println("  ╔══════════════════════════════╗");
        System.out.println("  ║       TIC  TAC  TOE          ║");
        System.out.println("  ╚══════════════════════════════╝" + RESET);
        System.out.println();
    }

    /** Prompt for names and game mode. */
    private static void setupPlayers()
    {
        System.out.println(WHITE + BOLD + "  Game Mode:" + RESET);
        System.out.println(YELLOW + "  1" + RESET + "  →  Player vs Player");
        System.out.println(YELLOW + "  2" + RESET + "  →  Player vs Computer");
        System.out.println();
        System.out.print(YELLOW + "  Choose (1 or 2): " + RESET);

        String modeChoice = input.nextLine().trim();
        vsComputer = modeChoice.equals("2");

        System.out.println();
        System.out.print(CYAN + "  Enter X player name: " + RESET);
        String n1 = input.nextLine().trim();
        if (!n1.isEmpty()) xName = n1;

        if (vsComputer)
        {
            oName = "Computer";
            System.out.println(GREEN + "  " + oName + " will play as O." + RESET);
        }
        else
        {
            System.out.print(CYAN + "  Enter O player name: " + RESET);
            String n2 = input.nextLine().trim();
            if (!n2.isEmpty()) oName = n2;
        }

        System.out.println();
        System.out.println(GREEN + BOLD + "  Welcome, " + xName + " (X)  vs  " + oName + " (O)!\n" + RESET);
    }

    // ════════════════════════════════════════════════════════════
    //  One game
    // ════════════════════════════════════════════════════════════

    /**
     * Runs a single game to completion.
     * Only changes WHO provides the move — not how moves are processed.
     * board.setCell() and logic.makeMove() are called the same way regardless.
     */
    private static void playOneGame()
    {
        // Computer player is created fresh each game but uses the same logic object
        ComputerPlayer computer = vsComputer ? new ComputerPlayer(logic, 'O') : null;

        while (!logic.isGameOver(board))
        {
            printBoard();

            char currentPlayer = logic.getCurrentPlayer(board);
            String currentName = (currentPlayer == 'X') ? xName : oName;

            if (vsComputer && currentPlayer == 'O')
            {
                // Computer chooses the move
                computerTurn(computer, currentName);
            }
            else
            {
                // Human chooses the move
                humanTurn(currentPlayer, currentName);
            }
        }

        printBoard();
    }

    /** Human move: prompt row + col, validate, call logic.makeMove(). */
    private static void humanTurn(char player, String name)
    {
        String color = (player == 'X') ? YELLOW : BLUE;
        System.out.println(color + BOLD + "  " + name + ", it's your turn! (You are " + player + ")" + RESET);

        int row = -1, col = -1;
        while (true)
        {
            System.out.print(YELLOW + "  Enter row (0-2): " + RESET);
            try { row = Integer.parseInt(input.nextLine().trim()); }
            catch (NumberFormatException e) { row = -1; }

            System.out.print(YELLOW + "  Enter col (0-2): " + RESET);
            try { col = Integer.parseInt(input.nextLine().trim()); }
            catch (NumberFormatException e) { col = -1; }

            if (row < 0 || row > 2 || col < 0 || col > 2)
                System.out.println(RED + "  Invalid position. Try again." + RESET);
            else if (board.getCell(row, col) != 'E')
                System.out.println(RED + "  That cell is taken. Try again." + RESET);
            else
                break;
        }

        // Move is processed exactly as before — only the input source changed
        logic.makeMove(board, row, col);
        animateDots("  Placing move");
    }

    /** Computer move: ComputerPlayer picks row+col, logic.makeMove() does the rest. */
    private static void computerTurn(ComputerPlayer computer, String name)
    {
        System.out.println(BLUE + BOLD + "  " + name + " is thinking..." + RESET);
        animateDots("  Computing move");

        // ComputerPlayer returns the chosen cell — logic.makeMove() processes it
        int[] move = computer.chooseMove(board);
        logic.makeMove(board, move[0], move[1]);

        System.out.println(BLUE + "  " + name + " played row " + move[0] + ", col " + move[1] + RESET);
        System.out.println();
    }

    // ════════════════════════════════════════════════════════════
    //  Score tracking
    // ════════════════════════════════════════════════════════════

    /**
     * Updates xWins, oWins, ties using logic.checkWin() — the same method
     * the game already uses. No new win-checking logic introduced.
     */
    private static void updateScores()
    {
        if (logic.checkWin(board, 'X'))
            xWins++;
        else if (logic.checkWin(board, 'O'))
            oWins++;
        else
            ties++;

        // Print result message
        System.out.println();
        if (logic.checkWin(board, 'X'))
            System.out.println(YELLOW + BOLD + "  🏆  " + xName + " WINS! Congratulations!" + RESET);
        else if (logic.checkWin(board, 'O'))
            System.out.println(BLUE + BOLD + "  🏆  " + oName + " WINS! Congratulations!" + RESET);
        else
            System.out.println(CYAN + BOLD + "  🤝  It's a draw!  Well played, both of you!" + RESET);

        System.out.println();
    }

    /** Prints current score totals — all stored in Main, not in Board/Logic. */
    private static void printScores()
    {
        System.out.println(DIM + "  ─────────────────────────────────" + RESET);
        System.out.println(WHITE + BOLD + "  Scoreboard:" + RESET);
        System.out.println(YELLOW + "    " + xName + " (X) wins : " + xWins + RESET);
        System.out.println(BLUE   + "    " + oName  + " (O) wins : " + oWins + RESET);
        System.out.println(CYAN   + "    Draws            : " + ties  + RESET);
        System.out.println(DIM + "  ─────────────────────────────────\n" + RESET);
    }

    // ════════════════════════════════════════════════════════════
    //  Replay
    // ════════════════════════════════════════════════════════════

    /**
     * Asks if the players want another game.
     * Returns true → replay (board.clearBoard() is called at top of loop).
     * Returns false → exit.
     * No program restart needed — same Board and GameLogic objects reused.
     */
    private static boolean askReplay()
    {
        System.out.print(YELLOW + "  Play again? (y = yes, n = quit, c = change names/mode): " + RESET);
        String ans = input.nextLine().trim().toLowerCase();

        if (ans.equals("c"))
        {
            System.out.println();
            setupPlayers();   // re-run setup, resets names and mode
            return true;
        }

        return ans.equals("y") || ans.equals("yes") || ans.isEmpty();
    }

    // ════════════════════════════════════════════════════════════
    //  Board display
    // ════════════════════════════════════════════════════════════

    private static void printBoard()
    {
        char[][] grid = board.getGrid();
        System.out.println();
        System.out.println(CYAN + BOLD + "    0     1     2" + RESET);
        System.out.println(DIM  + "  +-----+-----+-----+" + RESET);
        for (int row = 0; row < 3; row++)
        {
            System.out.print(CYAN + BOLD + row + " " + RESET + DIM + "|" + RESET);
            for (int col = 0; col < 3; col++)
            {
                char cell = grid[row][col];
                if      (cell == 'X') System.out.print(YELLOW + BOLD + "  X  " + RESET);
                else if (cell == 'O') System.out.print(BLUE   + BOLD + "  O  " + RESET);
                else                  System.out.print("     ");
                System.out.print(DIM + "|" + RESET);
            }
            System.out.println();
            System.out.println(DIM + "  +-----+-----+-----+" + RESET);
        }
        System.out.println();
    }

    // ════════════════════════════════════════════════════════════
    //  Utility
    // ════════════════════════════════════════════════════════════

    private static void animateDots(String message)
    {
        try
        {
            System.out.print(GREEN + message);
            for (int i = 0; i < 3; i++) { Thread.sleep(200); System.out.print("."); }
            System.out.println("  Done!" + RESET);
        }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}