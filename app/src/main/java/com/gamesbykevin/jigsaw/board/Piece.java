package com.gamesbykevin.jigsaw.board;

import com.gamesbykevin.jigsaw.base.Entity;

/**
 * Created by Kevin on 9/4/2017.
 */

public class Piece extends Entity {

    /**
     * The connector options
     */
    public enum Connector {
        Male, Female, None
    }

    //connectors for each side
    private Connector west, east, north, south;

    /**
     * The size of the connector will be a fraction of the piece size
     */
    public static final float CONNECTOR_RATIO = .2f;

    public Piece(int col, int row) {

        //set the location
        super.setCol(col);
        super.setRow(row);
    }

    public void setWest(final Connector west) {
        this.west = west;
    }

    public void setEast(final Connector east) {
        this.east = east;
    }

    public void setNorth(final Connector north) {
        this.north = north;
    }

    public void setSouth(final Connector south) {
        this.south = south;
    }

    public Connector getWest() {
        return this.west;
    }

    public Connector getEast() {
        return this.east;
    }

    public Connector getNorth() {
        return this.north;
    }

    public Connector getSouth() {
        return this.south;
    }
}