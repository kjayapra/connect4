package main.java;

import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");

        Player player1 = new Player(1, TileColour.GOLD);
        Player player2 = new Player(2, TileColour.RED);

        GameEngine game = new GameEngine(player1, player2);

        Scanner scanner = new Scanner(System.in);

        while (game.getState().equals(GameState.IN_PROGRESS)) {
            Player current = game.getCurrentPlayer();
            System.out.print("Player " + current.getId() + ", enter column (0-6): ");
            int col = scanner.nextInt();
            String result = game.makeMove(current, col);
            System.out.println(result);
        }

    }
}
