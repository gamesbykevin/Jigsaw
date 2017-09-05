package com.gamesbykevin.jigsaw.board;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.gamesbykevin.androidframeworkv2.base.Entity;
import com.gamesbykevin.jigsaw.board.Piece.Connector;
import com.gamesbykevin.jigsaw.common.ICommon;
import com.gamesbykevin.jigsaw.opengl.Square;
import com.gamesbykevin.jigsaw.opengl.Textures;

import static com.gamesbykevin.jigsaw.activity.GameActivity.getRandomObject;
import static com.gamesbykevin.jigsaw.board.BoardHelper.square;

/**
 * Created by Kevin on 9/4/2017.
 */

public class Board implements ICommon {

    private int cols, rows;

    //default size of the puzzle board
    private static final int DEFAULT_COLS = 4;
    private static final int DEFAULT_ROWS = 4;

    //the pieces on our board
    private Piece[][] pieces;

    //the overall image of the puzzle
    public static Bitmap IMAGE_SOURCE;

    /**
     * Default constructor
     */
    public Board() {
        setCols(DEFAULT_COLS);
        setRows(DEFAULT_ROWS);
        reset();
    }

    public void setPieces(final Piece[][] pieces) {
        this.pieces = pieces;
    }

    public Piece[][] getPieces() {

        if (this.pieces == null)
            this.pieces = new Piece[getRows()][getCols()];

        return this.pieces;
    }

    public void setCols(final int cols) {
        this.cols = cols;
    }

    public int getCols() {
        return this.cols;
    }

    public void setRows(final int rows) {
        this.rows = rows;
    }

    public int getRows() {
        return this.rows;
    }

    @Override
    public void dispose() {

        if (this.pieces != null) {
            for (int col = 0; col < this.pieces[0].length; col++) {
                for (int row = 0; row < this.pieces.length; row++) {
                    this.pieces[row][col] = null;
                }
            }
        }

        this.pieces = null;
    }

    @Override
    public void reset() {

        //create new array if the size does not match
        if (getPieces().length != getRows() || getPieces()[0].length != getCols())
            setPieces(new Piece[getRows()][getCols()]);

        for (int col = 0; col < getCols(); col++) {
            for (int row = 0; row < getRows(); row++) {

                //create the piece and make sure location is correct
                if (getPieces()[row][col] == null) {
                    getPieces()[row][col] = new Piece(col, row);
                } else {
                    getPieces()[row][col].setCol(col);
                    getPieces()[row][col].setRow(row);
                }
            }
        }

        //now that all pieces are created, create the connectors
        for (int col = 0; col < getCols(); col++) {
            for (int row = 0; row < getRows(); row++) {

                //get the current piece
                Piece piece = getPieces()[row][col];

                //our neighbor piece
                Piece neighbor;

                //certain sides won't have any connectors depending on the puzzle position
                if (row == 0)
                    piece.setNorth(Connector.None);
                if (row == getRows() - 1)
                    piece.setSouth(Connector.None);
                if (col == 0)
                    piece.setWest(Connector.None);
                if (col == getCols() - 1)
                    piece.setEast(Connector.None);

                //if we aren't on the end set the connector with our neighbor
                if (col < getCols() - 1) {

                    //make random decision
                    boolean result = getRandomObject().nextBoolean();

                    //east neighbor
                    neighbor = getPieces()[row][col + 1];

                    //make sure we can connect to our neighbor
                    piece.setEast(result ? Connector.Male : Connector.Female);
                    neighbor.setWest(result ? Connector.Female : Connector.Male);
                }

                //if we aren't on the end set the connector with our neighbor
                if (row < getRows() - 1) {

                    //make random decision
                    boolean result = getRandomObject().nextBoolean();

                    //south neighbor
                    neighbor = getPieces()[row + 1][col];

                    //make sure we can connect to our neighbor
                    piece.setSouth(result ? Connector.Male : Connector.Female);
                    neighbor.setNorth(result ? Connector.Female : Connector.Male);
                }
            }
        }

        //cut the pieces
        BoardHelper.cut(this);
    }

    @Override
    public void update() {

    }

    @Override
    public void render(float[] m) {

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, Textures.TEXTURE_ID_IMAGE_SOURCE);

        square.render(m);
    }
}