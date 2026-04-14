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


