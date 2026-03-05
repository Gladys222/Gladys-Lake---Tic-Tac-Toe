package tictactoe8;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Board
{
	//holds game play data in an instance variable
	private char[][] grid;

	//holds game play data in a CSV file
	private String filename;


	//non-default constructor - [5 points]
	public Board(String filename)
	{
		//set the file name
		//if the board is valid then create the 3x3 grid
		//and load the board from the file
	}

	//loads the grid with the file contents - [5 points]
	public void loadBoardFromFile()
	{

		//Use a scanner to read the board file
		//and populate the grid with the board values
		//remember to close the scanner afterwards
		//use isValidBoard method as a guide

	}


	//valid if it resembles a 3x3 board that contains only E, X, O
	public boolean isValidBoardFile()
	{


	}


	//saves the grid to the file in the proper format (CSV)
	public void saveBoardToFile()
	{


	}


	/***These are the methods used to test those above***/
	//prints the current grid
	public void printGrid()
	{

	}

	//create a random board
	public void createRandomBoard()
	{

	}

	//clears the grid by placing E in every cell
	public void clearBoard()
	{

	}

	public static void main(String args[])
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
