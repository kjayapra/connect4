import org.junit.jupiter.api.Test;

import main.java.Board;
import main.java.Move;
import main.java.TileColour;

import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    private Board board1;
    private Board board2;

    @BeforeEach
    void setUp() {
        board1 = new Board();
        TileColour[][] grid = new TileColour[5][5];
        grid[4][0] = TileColour.GOLD;
        board2 = new Board(grid);
    }

    @Test
    void testDefaultConstructor() {
        assertEquals(6, board1.getRowSize());
        assertEquals(7, board1.getColSize());
        assertEquals(0, board1.getFilledCount());
    }

    @Test
    void testParameterizedConstructor() {
        assertEquals(5, board2.getRowSize());
        assertEquals(5, board2.getColSize());
        assertEquals(1, board2.getFilledCount());
    }

    @Test
    void testLastAvailableEmptyRow() {
        assertEquals(5, board1.getLastAvailable(0));
    }

    @Test
    void testLastAvailableNonEmptyRow() {
        
        assertEquals(3, board2.getLastAvailable(0));
    }

    @Test
    void testLastAvailableFullRow() {

        for (int i = 0; i <= 3; i++) {
            board2.makeMove(TileColour.GOLD, 0);
        }
        
        assertEquals(-1, board2.getLastAvailable(0));
    }


    @Test
    void testMakeValidMove() {
        Move move = board1.makeMove(TileColour.GOLD, 0);
        assertEquals(5, move.getMove()[0]);
        assertEquals(0, move.getMove()[1]);
        assertEquals(TileColour.GOLD, move.getTile());
        assertEquals(true, move.isSuccess());
        assertEquals(1, board1.getFilledCount());
    }

    @Test
    void testMakeInvalidMove() {
        Move move = board1.makeMove(TileColour.GOLD, -1);
        assertEquals(-1, move.getMove()[0]);
        assertEquals(-1, move.getMove()[1]);
        assertEquals(TileColour.GOLD, move.getTile());
        assertEquals(false, move.isSuccess());
        assertEquals(0, board1.getFilledCount());
    }



}
