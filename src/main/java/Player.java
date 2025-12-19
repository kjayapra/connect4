package main.java;

import java.util.Objects;

public class Player {
    private long playerId;
    private TileColour colour;

    public Player(long playerId, TileColour colour) {
        this.playerId = playerId;
        this.colour = colour;
    }

    public TileColour getTileColour() {
        return this.colour;
    }

    public long getId() {
        return this.playerId;
    }

    public void setTile(TileColour tile) {
        this.colour = tile;
    }

    public void setId(long id) {
        this.playerId = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return playerId == player.playerId && colour == player.colour;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, colour);
    }

    

}
