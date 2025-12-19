package test.java;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import main.java.GameEngine;
import main.java.GameState;
import main.java.Move;
import main.java.Player;
import main.java.TileColour;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;


public class GameEngineTest {
    private GameEngine engine;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        player1 = new Player(1, TileColour.GOLD);
        player2 = new Player(2, TileColour.RED);
        engine = new GameEngine(player1, player2);
    }

    @Test
    void testSetup() {
        assertEquals(player1, engine.getCurrentPlayer());
        assertEquals(GameState.IN_PROGRESS, engine.getState());
        assertEquals(player1, engine.getPlayer1());
        assertEquals(player2, engine.getPlayer2());
        assertEquals(Optional.empty(), engine.getWinner());
    }

    @Test
    void testValidMove() {

        for (int i = 0; i < 2; i++) {
            assertEquals("Move was valid", engine.makeMove(player1, 0));
            assertEquals("Move was valid", engine.makeMove(player2, 1));
        }
        
    }

    @Test
    void testInValidPlayerMove() {

        assertEquals("Move was valid", engine.makeMove(player1, 0));
        assertEquals("Player cannot make a move.", engine.makeMove(player1, 0));
        
    }

    @Test
    void testInValidColumnMove() {

        assertEquals("Cannot place tile in column.", engine.makeMove(player1, -1));
        assertEquals("Cannot place tile in column.", engine.makeMove(player1, 8));
        
    }

    @Test
    void testWinMove() {

        for (int i = 0; i < 3; i++) {
            assertEquals("Move was valid", engine.makeMove(player1, 0));
            assertEquals("Move was valid", engine.makeMove(player2, 1));
        }

        assertEquals("Game Ended. Winner is " + player1.getId(), engine.makeMove(player1, 0));
        assertEquals(GameState.WIN, engine.getState());
        assertEquals(player1, engine.getWinner().get());
        
    }

    

}
