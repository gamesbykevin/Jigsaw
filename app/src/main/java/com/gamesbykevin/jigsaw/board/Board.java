package com.gamesbykevin.jigsaw.board;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.gamesbykevin.jigsaw.common.ICommon;
import com.gamesbykevin.jigsaw.opengl.Textures;
import com.gamesbykevin.jigsaw.util.UtilityHelper;

import static com.gamesbykevin.jigsaw.board.BoardHelper.CALCULATE_INDICES;
import static com.gamesbykevin.jigsaw.board.BoardHelper.CALCULATE_UVS;
import static com.gamesbykevin.jigsaw.board.BoardHelper.CALCULATE_VERTICES;
import static com.gamesbykevin.jigsaw.board.BoardHelper.PUZZLE_TEXTURE_GENERATED;
import static com.gamesbykevin.jigsaw.board.BoardHelper.getSquare;
import static com.gamesbykevin.jigsaw.board.BoardHelper.updateCoordinates;
import static com.gamesbykevin.jigsaw.board.BoardHelper.updateGroup;
import static com.gamesbykevin.jigsaw.board.BoardHelper.updatePieces;
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

    //the default size of a puzzle piece without the end connectors
    private int defaultWidth, defaultHeight;

    //do we update the current selected piece?
    private boolean place = false;

    //the coordinates to update
    private float placeX, placeY;

    //have we selected a piece?
    private boolean selection = false;

    //are we done with our selection?
    private boolean complete = false;

    /**
     * Default constructor
     */
    public Board() {
        setCols(DEFAULT_COLS);
        setRows(DEFAULT_ROWS);
        reset();
    }

    public boolean hasSelection() {
        return this.selection;
    }

    public void setSelection(final boolean selection) {
        this.selection = selection;
    }

    public boolean hasComplete() {
        return this.complete;
    }

    public void setComplete(final boolean complete) {
        this.complete = complete;
    }

    public void setDefaultWidth(final int defaultWidth) {
        this.defaultWidth = defaultWidth;
    }

    public void setDefaultHeight(final int defaultHeight) {
        this.defaultHeight = defaultHeight;
    }

    public int getDefaultWidth() {
        return this.defaultWidth;
    }

    public int getDefaultHeight() {
        return this.defaultHeight;
    }

    public void setSelected(final float x, final float y) {

        if (getPieces() == null)
            return;

        for (int col = 0; col < getPieces()[0].length; col++) {
            for (int row = 0; row < getPieces().length; row++) {

                //if the coordinate is within the piece, return result
                if (getPieces()[row][col].contains(x, y)) {

                    //assign our selected piece
                    setSelected(getPieces()[row][col]);

                    //flag that we have a selection
                    setSelection(true);
                    setComplete(false);

                    //no need to continue
                    return;
                }
            }
        }

        //we couldn't find anything
        removeSelected();
    }

    public void removeSelected() {

        //we don't want to update the place since we are removing our selection
        this.place = false;

        //flag false
        setComplete(false);

        //flag false
        setSelection(false);

        //remove selection
        setSelected(null);
    }

    private void setSelected(final Piece piece) {
        this.selected = piece;
    }

    public Piece getSelected() {
        return this.selected;
    }

    public void updatePlace(float x, float y) {

        this.placeX = x;
        this.placeY = y;
        this.place = true;
    }

    public boolean hasUpdatePlace() {
        return this.place;
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
        BoardHelper.reset(this);
    }

    @Override
    public void update() {

        //move the piece and others in the group
        if (hasUpdatePlace() && getSelected() != null) {

            //flag false
            this.place = false;

            getSelected().setX(placeX - (getSelected().getWidth() / 2));
            getSelected().setY(placeY - (getSelected().getHeight() / 2));

            //update every other piece that is part of the same group
            updateGroup(this, -1, getSelected());

            //update the render coordinates
            updatePieces(this, getSelected().getGroup());

        } else if (hasSelection()) {

            //update the render coordinates
            updatePieces(this, getSelected().getGroup());

            //do we want to stop our selection?
            if (hasComplete()) {

                //place the piece on the board accordingly
                BoardHelper.placeSelected(this);

                //remove the selected piece
                removeSelected();
            }
        }

    }

    @Override
    public void render(float[] m) {

        //make sure the texture has been generated first before rendering
        if (PUZZLE_TEXTURE_GENERATED) {

            //bind the correct texture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, Textures.TEXTURE_ID_IMAGE_SOURCE);

            //if null we need to setup the coordinates
            if (getUvs() == null || getIndices() == null || getVertices() == null)
                updateCoordinates(this);

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