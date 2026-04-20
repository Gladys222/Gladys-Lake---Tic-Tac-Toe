package tictactoe;

public class GameLogic 

{
	public boolean checkWin(Board board, char player)
	{
		{
	        char[][] grid = board.getGrid();

	        for(int row = 0; row < 3; row++)
	        {
	            if(grid[row][0] == player &&
	               grid[row][1] == player &&
	               grid[row][2] == player)
	            {
	                return true;
	            }
	        }
	        
	        for(int col = 0; col < 3; col++)
	        {
	            if(grid[0][col] == player &&
	               grid[1][col] == player &&
	               grid[2][col] == player)
	            {
	                return true;
	            }
	        }
	        	 if(grid[0][0] == player &&
	                grid[1][1] == player &&
	                grid[2][2] == player)
	             {
	                 return true;
	             }

	             if(grid[0][2] == player &&
	                grid[1][1] == player &&
	                grid[2][0] == player)
	             {
	                 return true;
	             }

	             return false;
	         }
	     }
	
	 public boolean isDraw(Board board)
	 {
		 
		 {
	 
	        char[][] grid = board.getGrid(); 

	        for(int row = 0; row < 3; row++)
	        {
	            for(int col = 0; col < 3; col++)
	            {
	                if(grid[row][col] == 'E')
	                {
	                    return false;
	                }
	            }
	            
	            if(checkWin(board, 'X') || checkWin(board, 'O'))
	            {
	                return false;
	            }

	            return true;
	        }
	        
	        }
		 return false;
	 }
	        
	 public boolean isGameOver(Board board)
	 {
	    return checkWin(board, 'X') || checkWin(board, 'O') || isDraw(board);
	 }     
	 
	 public char getCurrentPlayer(Board board)
	 {
	     char[][] grid = board.getGrid();
	     int xCount = 0;
	     int oCount = 0;

	     for(int row = 0; row < 3; row++)
	     {
	         for(int col = 0; col < 3; col++)
	         {
	             if(grid[row][col] == 'X')
	             {
	                 xCount++;
	             }
	             else if(grid[row][col] == 'O')
	             {
	                 oCount++;
	             }
	         }
	     }

	     if(xCount == oCount)
	     {
	         return 'X';
	     }
	     else
	     {
	         return 'O';
	     }
	 }
	 
	 public boolean makeMove(Board board, int row, int col)
	 {
		 char player = getCurrentPlayer(board);
		 
	     board.setCell(row, col, getCurrentPlayer(board));

	     if(board.isValidBoardFile() && row >= 0 && row <= 2 && col >= 0 && col <= 2)
	         return true;

	     return false;
	 }
	 
	 
	 
	 
	 
	 
	 
	 
}


