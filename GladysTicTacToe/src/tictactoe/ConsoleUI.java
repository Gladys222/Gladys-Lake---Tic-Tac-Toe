package tictactoe;

import java.util.Scanner;

public class ConsoleUI
{
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String RED    = "\u001B[31m";
    private static final String BLUE   = "\u001B[34m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String DIM    = "\u001B[2m";
    private static final String WHITE  = "\u001B[97m";

    private Board board;
    private GameLogic logic;
    private Scanner scanner;

    private String player1Name;
    private String player2Name;
    private boolean continuousMode;
    private int score1 = 0, score2 = 0, draws = 0;

    public ConsoleUI(String filename)
    {
        board   = new Board(filename);
        logic   = new GameLogic();
        scanner = new Scanner(System.in);
    }

    // ── Main loop ────────────────────────────────────────────────
    public void startGame()
    {
        while (true)
        {
            showMainMenu();
            String choice = scanner.nextLine().trim();

            if (choice.equals("3"))
            {
                System.out.println(CYAN + "\n  Thanks for playing! Goodbye!\n" + RESET);
                break;
            }

            promptNames();

            if (choice.equals("1"))
            {
                continuousMode = false;
                playOneGame();
            }
            else if (choice.equals("2"))
            {
                continuousMode = true;
                score1 = 0; score2 = 0; draws = 0;
                playContinuous();
            }
        }
        scanner.close();
    }

    // ── Main menu ────────────────────────────────────────────────
    private void showMainMenu()
    {
        System.out.println(CYAN + BOLD);
        System.out.println("  ╔══════════════════════════════╗");
        System.out.println("  ║       TIC  TAC  TOE          ║");
        System.out.println("  ╚══════════════════════════════╝" + RESET);
        System.out.println();
        System.out.println(WHITE + "  Choose a mode:" + RESET);
        System.out.println(YELLOW + "  1" + RESET + "  →  Original Game  (one round)");
        System.out.println(YELLOW + "  2" + RESET + "  →  Continuous Game  (keep score)");
        System.out.println(YELLOW + "  3" + RESET + "  →  Quit");
        System.out.println();
        System.out.print(YELLOW + "  Your choice: " + RESET);
    }

    // ── Prompt names ─────────────────────────────────────────────
    private void promptNames()
    {
        System.out.println();
        System.out.print(CYAN + "  Player 1 name (plays X): " + RESET);
        String n1 = scanner.nextLine().trim();
        player1Name = n1.isEmpty() ? "Player 1" : n1;

        System.out.print(CYAN + "  Player 2 name (plays O): " + RESET);
        String n2 = scanner.nextLine().trim();
        player2Name = n2.isEmpty() ? "Player 2" : n2;

        System.out.println();
        System.out.println(GREEN + BOLD + "  Welcome, " + player1Name + " (X)  and  " + player2Name + " (O)!" + RESET);
        System.out.println(DIM + "  Type 'back' at any time to return to the main menu.\n" + RESET);
    }

    // ── One game ─────────────────────────────────────────────────
    private void playOneGame()
    {
        board.clearBoard();
        while (!logic.isGameOver(board))
        {
            printBoard();
            boolean wentBack = promptMove();
            if (wentBack) return;
        }
        printBoard();
        showResult();
    }

    // ── Continuous ───────────────────────────────────────────────
    private void playContinuous()
    {
        while (true)
        {
            board.clearBoard();
            while (!logic.isGameOver(board))
            {
                printBoard();
                boolean wentBack = promptMove();
                if (wentBack) return;
            }
            printBoard();
            showResult();
            printScores();

            System.out.println(YELLOW + "  Play again? (Enter = yes, 'back' = menu): " + RESET);
            String ans = scanner.nextLine().trim().toLowerCase();
            if (ans.equals("back") || ans.equals("b")) return;
        }
    }

    // ── Board display ────────────────────────────────────────────
    public void printBoard()
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

    // ── Move prompt — returns true if player typed 'back' ────────
    public boolean promptMove()
    {
        char player  = logic.getCurrentPlayer(board);
        String name  = (player == 'X') ? player1Name : player2Name;
        String color = (player == 'X') ? YELLOW : BLUE;

        System.out.println(color + BOLD + "  " + name + ", it's your turn!  (You are " + player + ")" + RESET);

        int row = -1, col = -1;
        while (true)
        {
            System.out.print(YELLOW + "  Enter row (0-2) or 'back': " + RESET);
            String rowStr = scanner.nextLine().trim();
            if (rowStr.equalsIgnoreCase("back")) return true;

            System.out.print(YELLOW + "  Enter col (0-2): " + RESET);
            String colStr = scanner.nextLine().trim();
            if (colStr.equalsIgnoreCase("back")) return true;

            try { row = Integer.parseInt(rowStr); col = Integer.parseInt(colStr); }
            catch (NumberFormatException e) { row = -1; col = -1; }

            if (row < 0 || row > 2 || col < 0 || col > 2)
                System.out.println(RED + "  Invalid position, try again." + RESET);
            else if (board.getCell(row, col) != 'E')
                System.out.println(RED + "  That cell is taken, try again." + RESET);
            else
                break;
        }

        logic.makeMove(board, row, col);

        try {
            System.out.print(GREEN + "  Placing move");
            for (int i = 0; i < 3; i++) { Thread.sleep(160); System.out.print("."); }
            System.out.println("  Done!" + RESET);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        return false;
    }

    // ── Result ───────────────────────────────────────────────────
    public void showResult()
    {
        System.out.println();
        if (logic.checkWin(board, 'X'))
        {
            score1++;
            System.out.println(YELLOW + BOLD + "  🏆  " + player1Name + " WINS!  Congratulations!" + RESET);
        }
        else if (logic.checkWin(board, 'O'))
        {
            score2++;
            System.out.println(BLUE + BOLD + "  🏆  " + player2Name + " WINS!  Congratulations!" + RESET);
        }
        else
        {
            draws++;
            System.out.println(CYAN + BOLD + "  🤝  It's a Draw,  " + player1Name + "  &  " + player2Name + "!  Great game!" + RESET);
        }
        System.out.println();
    }

    private void printScores()
    {
        System.out.println(DIM + "  ─────────────────────────────────" + RESET);
        System.out.println(WHITE + BOLD + "  Scores:" + RESET);
        System.out.println(YELLOW + "  " + player1Name + " (X): " + score1 + RESET);
        System.out.println(BLUE   + "  " + player2Name + " (O): " + score2 + RESET);
        System.out.println(CYAN   + "  Draws: " + draws + RESET);
        System.out.println(DIM + "  ─────────────────────────────────\n" + RESET);
    }

    public static void main(String[] args)
    {
        ConsoleUI ui = new ConsoleUI("board.csv");
        ui.startGame();
    }
}