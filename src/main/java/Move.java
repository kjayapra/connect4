package main.java;
public class Move {
    private TileColour tile;
    private int column;
    private int row;
    private boolean success;


    public Move(TileColour tile, int column, int row, boolean success) {
        this.tile = tile;
        this.column = column;
        this.row = row;
        this.success = success;
    }

    public TileColour getTile() {
        return this.tile;
    }

    public int[] getMove() {
        return new int[]{row, column};
    }

    public boolean isSuccess() {
        return this.success;
    }
}
