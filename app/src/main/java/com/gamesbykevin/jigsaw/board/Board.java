package com.gamesbykevin.jigsaw.board;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.gamesbykevin.jigsaw.board.Piece.Connector;
import com.gamesbykevin.jigsaw.common.ICommon;
import com.gamesbykevin.jigsaw.opengl.Textures;
import com.gamesbykevin.jigsaw.util.UtilityHelper;

import static com.gamesbykevin.jigsaw.activity.GameActivity.getRandomObject;
import static com.gamesbykevin.jigsaw.board.BoardHelper.CALCULATE_INDICES;
import static com.gamesbykevin.jigsaw.board.BoardHelper.CALCULATE_UVS;
import static com.gamesbykevin.jigsaw.board.BoardHelper.CALCULATE_VERTICES;
import static com.gamesbykevin.jigsaw.board.BoardHelper.PUZZLE_TEXTURE_GENERATED;
import static com.gamesbykevin.jigsaw.board.BoardHelper.getSquare;
import static com.gamesbykevin.jigsaw.board.BoardHelper.updateCoordinates;
import static com.gamesbykevin.jigsaw.board.BoardHelper.updatePiece;
import static com.gamesbykevin.jigsaw.game.Game.INITIAL_RENDER;

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

    //store these coordinates for rendering
    private static float[] VERTICES;
    private static short[] INDICES;
    private static float[] UVS;

    //have we selected a piece
    private Piece selected = null;

    /**
     * Default constructor
     */
    public Board() {
        setCols(DEFAULT_COLS);
        setRows(DEFAULT_ROWS);
        reset();
    }

    public Piece getSelected(final float x, final float y) {

        if (getPieces() == null)
            return null;

        for (int col = 0; col < getPieces()[0].length; col++) {
            for (int row = 0; row < getPieces().length; row++) {

                //if the coordinate is within the piece, return result
                if (getPieces()[row][col].contains(x, y))
                    return getPieces()[row][col];
            }
        }

        //we couldn't find anything
        return null;
    }

    public void setSelected(final Piece piece) {
        this.selected = piece;
    }

    public Piece getSelected() {
        return this.selected;
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

        VERTICES = null;
        UVS = null;
        INDICES = null;

        BoardHelper.dispose();
    }

    public float[] getVertices() {

        final int length = (getPieces()[0].length * getPieces().length) * 4 * 3;

        //if null or the size doesn't add up
        if (VERTICES == null || VERTICES.length != length) {
            VERTICES = new float[length];

            for (int i = 0; i < VERTICES.length; i++) {
                VERTICES[i] = 0;
            }
        }

        return VERTICES;
    }

    public short[] getIndices() {

        //expected length of array
        final int length = (getPieces()[0].length * getPieces().length) * 6;

        //if null or the size doesn't add up
        if (INDICES == null || INDICES.length != length) {
            INDICES = new short[length];

            int last = 0;

            for (int index = 0; index < getPieces()[0].length * getPieces().length; index++) {

                try {
                    //we need to set the new indices for the new quad
                    INDICES[(index * 6) + 0] = (short) (last + 0);
                    INDICES[(index * 6) + 1] = (short) (last + 1);
                    INDICES[(index * 6) + 2] = (short) (last + 2);
                    INDICES[(index * 6) + 3] = (short) (last + 0);
                    INDICES[(index * 6) + 4] = (short) (last + 2);
                    INDICES[(index * 6) + 5] = (short) (last + 3);

                    //normal quad = 0,1,2,0,2,3 so the next one will be 4,5,6,4,6,7
                    last = last + 4;

                } catch (Exception e) {
                    UtilityHelper.handleException(e);
                }
            }
        }

        return INDICES;
    }

    public float[] getUvs() {

        final int length = (getPieces()[0].length * getPieces().length) * 4 * 2;

        //if null or the size doesn't add up
        if (UVS == null || UVS.length != length) {
            UVS = new float[length];

            for (int i = 0; i < UVS.length; i++) {
                UVS[i] = 0;
            }
        }

        return UVS;
    }

    @Override
    public void reset() {

        //create new array if the size does not match
        if (getPieces().length != getRows() || getPieces()[0].length != getCols())
            setPieces(new Piece[getRows()][getCols()]);

        int index = 0;

        for (int col = 0; col < getCols(); col++) {
            for (int row = 0; row < getRows(); row++) {

                //create the piece and make sure location is correct
                if (getPieces()[row][col] == null) {
                    getPieces()[row][col] = new Piece(col, row);
                } else {
                    getPieces()[row][col].setCol(col);
                    getPieces()[row][col].setRow(row);
                }

                //keep track of index so we can map the open gl coordinates
                getPieces()[row][col].setIndex(index);

                //keep track of index
                index++;
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

        //update open gl coordinates
        updateCoordinates(this);

        //we need to recalculate coordinates
        CALCULATE_UVS = true;
        CALCULATE_INDICES = true;
        CALCULATE_VERTICES = true;
    }

    @Override
    public void update() {
        //do we update anything here?
    }

    @Override
    public void render(float[] m) {

        //make sure the texture has been generated first before rendering
        if (PUZZLE_TEXTURE_GENERATED) {

            //bind the correct texture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, Textures.TEXTURE_ID_IMAGE_SOURCE);

            //if null we need to setup the coordinates
            if (getUvs() == null || getIndices() == null || getVertices() == null) {

                //initialize all coordinates if null
                updateCoordinates(this);
            } else if (getSelected() != null) {

                //if a puzzle piece is selected we need to update the coordinates
                updatePiece(this, getSelected());
            }

            //only do these calculations when necessary
            if (CALCULATE_UVS) {
                getSquare().setupImage(getUvs());
                CALCULATE_UVS = false;
            }

            if (CALCULATE_INDICES) {
                getSquare().setupTriangle(getIndices());
                CALCULATE_INDICES = false;
            }

            if (CALCULATE_VERTICES) {
                getSquare().setupVertices(getVertices());
                CALCULATE_VERTICES = false;
            }

            //make a single render call to render everything
            getSquare().render(m);

            //flag that we have performed the initial render
            INITIAL_RENDER = true;
        }
    }
}