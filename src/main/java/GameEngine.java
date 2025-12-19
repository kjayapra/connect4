package main.java;
import java.util.Optional;
import java.util.logging.Logger;

public class GameEngine {
    private Board board;
    private Player player1;
    private Player player2;
    private GameState state;
    private Player winner;
    private Player current;


    public GameEngine(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.current = player1;
        this.state = GameState.IN_PROGRESS;
        board = new Board();
    }

    public String makeMove(Player player, int column) {
        

        if (!state.equals(GameState.IN_PROGRESS)) {
            String msg = "Game is no longer ACTIVE";
            return msg;
        }

        if (!player.equals(current)) {
            String msg = "Player cannot make a move.";
            return msg;
        }

        Move move = board.makeMove(player.getTileColour(), column);

        if (move.isSuccess()) {
            if (board.checkWin(move)) {
                state = GameState.WIN;
                winner = current;
                String msg = "Game Ended. Winner is " + current.getId();
                return msg;
            }
            else if (board.checkDraw()) {
                state = GameState.DRAW;
                String msg = "Game Ended in Draw.";
                return msg;
            }
            else {
                if (current.equals(player1)) {
                    current = player2;
                }
                else {
                    current = player1;
                }
                String msg = "Move was valid";
                return msg;
            }
        }
        else {
            String msg = "Cannot place tile in column.";
            return msg;
        }
    }


    public Optional<Player> getWinner() {
        if (state.equals(GameState.WIN)) {
            return Optional.of(winner);
        }

        return Optional.empty();

    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public GameState getState() {
        return state;
    }

    public Player getCurrentPlayer() {
        return current;
    }

}
