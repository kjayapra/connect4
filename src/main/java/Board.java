package main.java;

public class Board {
    private TileColour[][] grid;
    private int filled;
    

    public Board() {
        this.grid = new TileColour[6][7];
        this.filled = 0;
    }

    public Board(TileColour[][] grid) {
        this.grid = grid;

        int count = 0;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] != null) {
                    count++;
                }
            }
        }
        this.filled = count;
    }


    public Move makeMove(TileColour tile, int column) {
        
        int row = getLastAvailable(column);
        if (row == -1) {
            return new Move(tile, column, row, false);
        }

        grid[row][column] = tile;
        filled++;
        return new Move(tile, column, row, true);
    }

    public int getLastAvailable(int column) {
        if (column >= grid[0].length || column < 0) {
            return -1;
        }
        for (int i = grid.length - 1; i >= 0; i--) {
            if (grid[i][column] == null) {
                return i;
            }
        }

        return -1;
    }

    public boolean checkWin(Move move) {
        if (!move.isSuccess()) {
            return false;
        }

        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {-1, 1}};

        
        for (int[] direction : directions) {
            int count = 1;
            count += getTileCount(direction, move.getTile(), true, move.getMove());
            count += getTileCount(direction, move.getTile(), false, move.getMove());
            if (count == 4) {
                return true;
            }
        }

        return false;
    }

    public int getTileCount(int[] direction, TileColour tile, boolean positive, int[] position) {
        int row = position[0];
        int col = position[1];
        int rowDir = direction[0];
        int colDir = direction[1];

        if (!positive) {
            rowDir *= -1;
            colDir *= -1;
        }

        int count = 0;

        for (int i = 0; i < 4; i++) {
            row += rowDir;
            col += colDir;

            if (row < 0 || row >= grid.length || col < 0 || col >= grid[0].length || grid[row][col] != tile) {
                break;
            }
            
            count++;
        }

        return count;
    }

    public boolean checkDraw() {
        return grid.length * grid[0].length == filled;
    }

    public TileColour[][] getGrid() {
        return this.grid;
    }

    public int getFilledCount() {
        return this.filled;
    }

    public int getRowSize() {
        if (grid == null) {
            return 0;
        }
        return grid.length;
    }

    public int getColSize() {
        if (grid == null) {
            return 0;
        }
        return grid[0].length;
    }


}
