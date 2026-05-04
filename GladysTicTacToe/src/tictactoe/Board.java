package tictactoe;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Board
{
    private char[][] grid;
    private String filename;

    public Board(String filename)
    {
        this.filename = filename;
        this.grid = new char[3][3];
        if (isValidBoardFile())
            loadBoardFromFile();
        else
            clearBoard();
    }

    public char getCell(int row, int col)   { return grid[row][col]; }
    public char[][] getGrid()               { return grid; }

    public void setCell(int row, int col, char player)
    {
        grid[row][col] = player;
        saveBoardToFile();
    }

    public void setGrid(char[][] newGrid)
    {
        this.grid = newGrid;
        saveBoardToFile();
    }

    // ── Fixed: actually populates grid ───────────────────────────
    public void loadBoardFromFile()
    {
        try
        {
            File file    = new File("src/tictactoe/" + this.filename);
            Scanner scan = new Scanner(file);
            int row = 0;
            while (scan.hasNextLine() && row < 3)
            {
                String line = scan.nextLine().trim();
                // Support both "E,X,O" and "E, X, O"
                String[] parts = line.split(",\\s*");
                if (parts.length == 3)
                    for (int col = 0; col < 3; col++)
                        grid[row][col] = parts[col].trim().charAt(0);
                row++;
            }
            scan.close();
        }
        catch (Exception e)
        {
            clearBoard();
        }
    }

    public boolean isValidBoardFile()
    {
        try
        {
            File file    = new File("src/tictactoe/" + this.filename);
            Scanner scan = new Scanner(file);
            int rows = 0;
            int xCount = 0, oCount = 0;
            while (scan.hasNextLine())
            {
                String line = scan.nextLine().trim();
                if (!line.matches("[EXO],\\s*[EXO],\\s*[EXO]"))
                { scan.close(); return false; }
                String[] parts = line.split(",\\s*");
                for (String p : parts)
                {
                    if (p.trim().equals("X")) xCount++;
                    if (p.trim().equals("O")) oCount++;
                }
                rows++;
            }
            scan.close();
            return rows == 3 && (xCount == oCount || xCount == oCount + 1);
        }
        catch (Exception e) { return false; }
    }

    public void saveBoardToFile()
    {
        try
        {
            File file       = new File("src/tictactoe/" + this.filename);
            FileWriter writer = new FileWriter(file);
            StringBuilder sb  = new StringBuilder();
            for (int row = 0; row < grid.length; row++)
            {
                for (int col = 0; col < grid[0].length; col++)
                {
                    sb.append(grid[row][col]);
                    if (col < 2) sb.append(',');
                }
                if (row < 2) sb.append('\n');
            }
            writer.write(sb.toString());
            writer.close();
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    public void printGrid()
    {
        for (int row = 0; row < grid.length; row++)
        {
            for (int col = 0; col < grid[0].length; col++)
                System.out.print(grid[row][col] + " ");
            System.out.println();
        }
    }

    public void createRandomBoard()
    {
        char[] options = {'E', 'X', 'O'};
        for (int row = 0; row < grid.length; row++)
            for (int col = 0; col < grid[0].length; col++)
                grid[row][col] = options[(int)(Math.random() * options.length)];
        saveBoardToFile();
    }

    public void clearBoard()
    {
        grid = new char[][]{{'E','E','E'},{'E','E','E'},{'E','E','E'}};
        saveBoardToFile();
    }

    public static void main(String[] args)
    {
        Board b = new Board("board.csv");
        System.out.println(b.isValidBoardFile());
        b.createRandomBoard();
        b.printGrid();
        b.saveBoardToFile();
        b.loadBoardFromFile();
        System.out.println();
        b.printGrid();
    }
}