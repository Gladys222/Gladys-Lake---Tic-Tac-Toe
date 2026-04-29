package tictactoe;

import java.util.Scanner;

public class ConsoleUI
{
    // ANSI color codes for terminal output
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String RED    = "\u001B[31m";
    private static final String BLUE   = "\u001B[34m";
    private static final String CYAN   = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String DIM    = "\u001B[2m";

    private Board board;
    private GameLogic logic;
    private Scanner scanner;

    public ConsoleUI(String filename)
    {
        board   = new Board(filename);
        logic   = new GameLogic();
        scanner = new Scanner(System.in);
    }

    // Prints a styled, color-coded board
    public void printBoard()
    {
        char[][] grid = board.getGrid();

        System.out.println();
        System.out.println(CYAN + BOLD + "    0     1     2  " + RESET);
        System.out.println(DIM  + "  +-----+-----+-----+" + RESET);

        for (int row = 0; row < 3; row++)
        {
            System.out.print(CYAN + BOLD + row + " " + RESET);
            System.out.print(DIM + "|" + RESET);
            for (int col = 0; col < 3; col++)
            {
                char cell = grid[row][col];
                if (cell == 'X')
                    System.out.print(BLUE + BOLD + "  X  " + RESET);
                else if (cell == 'O')
                    System.out.print(RED + BOLD + "  O  " + RESET);
                else
                    System.out.print("     ");
                System.out.print(DIM + "|" + RESET);
            }
            System.out.println();
            System.out.println(DIM + "  +-----+-----+-----+" + RESET);
        }
        System.out.println();
    }

    // Prompts the current player for a valid row and col
    public void promptMove()
    {
        char player = logic.getCurrentPlayer(board);
        String color = (player == 'X') ? BLUE : RED;
        System.out.println(color + BOLD + "Player " + player + "'s turn." + RESET);

        int row = -1;
        int col = -1;

        while (true)
        {
            System.out.print(YELLOW + "  Enter row (0-2): " + RESET);
            row = scanner.nextInt();
            System.out.print(YELLOW + "  Enter col (0-2): " + RESET);
            col = scanner.nextInt();

            if (row < 0 || row > 2 || col < 0 || col > 2)
            {
                System.out.println(RED + "  X Invalid position. Try again." + RESET);
            }
            else if (board.getCell(row, col) != 'E')
            {
                System.out.println(RED + "  X Cell already taken. Try again." + RESET);
            }
            else
            {
                break;
            }
        }

        logic.makeMove(board, row, col);
        animateMove();
    }

    // Small pause to simulate a move-placed animation in the terminal
    private void animateMove()
    {
        try
        {
            System.out.print(GREEN + "  Placing move");
            for (int i = 0; i < 3; i++)
            {
                Thread.sleep(180);
                System.out.print(".");
            }
            Thread.sleep(180);
            System.out.println(" Done!" + RESET);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    // Shows the end-of-game message with color
    public void showResult()
    {
        System.out.println();
        if (logic.checkWin(board, 'X'))
            System.out.println(BLUE + BOLD + "  Player X wins! Congratulations!" + RESET);
        else if (logic.checkWin(board, 'O'))
            System.out.println(RED + BOLD + "  Player O wins! Congratulations!" + RESET);
        else if (logic.isDraw(board))
            System.out.println(YELLOW + BOLD + "  It's a draw!" + RESET);
        System.out.println();
    }

    // Main game loop
    public void startGame()
    {
        System.out.println(CYAN + BOLD + "\n  ==========================");
        System.out.println(        "       TIC  TAC  TOE        ");
        System.out.println(        "  ==========================" + RESET);

        board.clearBoard();

        while (!logic.isGameOver(board))
        {
            printBoard();
            promptMove();
        }

        printBoard();
        showResult();
        scanner.close();
    }

    public static void main(String[] args)
    {
        ConsoleUI ui = new ConsoleUI("board.csv");
        ui.startGame();
    }
}