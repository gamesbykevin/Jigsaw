package com.gamesbykevin.jigsaw.board;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.gamesbykevin.jigsaw.base.Entity;
import com.gamesbykevin.jigsaw.common.ICommon;
import com.gamesbykevin.jigsaw.game.GameHelper;
import com.gamesbykevin.jigsaw.opengl.Textures;
import com.gamesbykevin.jigsaw.util.UtilityHelper;

import static com.gamesbykevin.jigsaw.activity.GameActivity.getRandomObject;
import static com.gamesbykevin.jigsaw.board.BoardHelper.CALCULATE_INDICES;
import static com.gamesbykevin.jigsaw.board.BoardHelper.CALCULATE_UVS;
import static com.gamesbykevin.jigsaw.board.BoardHelper.CALCULATE_VERTICES;
import static com.gamesbykevin.jigsaw.board.BoardHelper.PUZZLE_TEXTURE_GENERATED;
import static com.gamesbykevin.jigsaw.board.BoardHelper.getGroupCount;
import static com.gamesbykevin.jigsaw.board.BoardHelper.getIndexPiece;
import static com.gamesbykevin.jigsaw.board.BoardHelper.getSquare;
import static com.gamesbykevin.jigsaw.board.BoardHelper.isGameOver;
import static com.gamesbykevin.jigsaw.board.BoardHelper.orderGroup;
import static com.gamesbykevin.jigsaw.board.BoardHelper.orderPlaced;
import static com.gamesbykevin.jigsaw.board.BoardHelper.updateCoordinates;
import static com.gamesbykevin.jigsaw.board.BoardHelper.updateGroup;
import static com.gamesbykevin.jigsaw.board.BoardHelper.updatePiece;
import static com.gamesbykevin.jigsaw.board.BoardHelper.updatePieces;
import static com.gamesbykevin.jigsaw.board.Piece.CONNECTOR_RATIO;
import static com.gamesbykevin.jigsaw.board.Piece.START_VELOCITY;
import static com.gamesbykevin.jigsaw.game.Game.INITIAL_RENDER;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.FPS;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.FRAME_DURATION;

/**
 * Created by Kevin on 9/4/2017.
 */
public class Board implements ICommon {

    private int cols, rows;

    //default size of the puzzle board
    private static final int DEFAULT_COLS = 4;
    private static final int DEFAULT_ROWS = 4;

    //desired size of the board
    public static int BOARD_COLS = DEFAULT_COLS;
    public static int BOARD_ROWS = DEFAULT_ROWS;

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
    private boolean update = false;

    //the coordinates to update
    private float updateX, updateY;

    //have we selected a piece?
    private boolean selection = false;

    //are we done with our selection?
    private boolean complete = false;

    //flag that we are starting the board
    private boolean starting = true;

    //do we check if the piece should be rotating
    private boolean checkRotate = false;

    //how much time has lapsed
    private long frames = 0;

    /**
     * Can we rotate the pieces?
     */
    public static boolean ROTATE = false;

    /**
     * How long can we view the puzzle before we start placing the pieces
     */
    private static final long VIEW_DELAY = (FPS * 1);

    /**
     * Default constructor
     */
    public Board() {

        //make sure minimum dimensions are set
        if (BOARD_COLS < 1)
            BOARD_COLS = DEFAULT_COLS;
        if (BOARD_ROWS < 1)
            BOARD_ROWS = DEFAULT_ROWS;

        setCols(BOARD_COLS);
        setRows(BOARD_ROWS);
        reset();
    }

    public boolean isStarting() {
        return this.starting;
    }

    public void setStarting(final boolean starting) {
        this.starting = starting;
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

        //if we are done and rotate is enabled, check if we should rotate the piece
        if (hasComplete() && ROTATE)
            checkRotate = true;
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

        //start checking the last index
        int index = (getCols() * getRows()) - 1;

        //keep going back till we find the piece
        while (index >= 0) {

            //get the piece at the index
            Piece piece = getIndexPiece(this, index);

            //we can only select a piece that is not placed
            if (!piece.isPlaced()) {

                //if we are within the bounds
                if (piece.contains(x, y, getDefaultWidth())) {

                    //assign the motion coordinates
                    piece.setMotionX(x - (piece.getWidth() / 2));
                    piece.setMotionY(y - (piece.getHeight() / 2));

                    //assign our selected piece
                    setSelected(piece);

                    //flag that we have a selection
                    setSelection(true);
                    setComplete(false);

                    //order the group to be on top
                    orderGroup(this);

                    //update the coordinates
                    updateCoordinates(this);

                    //no need to continue
                    return;
                }
            }

            //check the next index
            index--;
        }

        //we couldn't find anything
        removeSelected();
    }

    public void removeSelected() {

        //stop updating the piece
        this.update = false;

        //flag false
        setComplete(false);

        //flag false
        setSelection(false);

        //remove selection
        setSelected(null);
    }

    protected void setSelected(final Piece piece) {
        this.selected = piece;
    }

    public Piece getSelected() {
        return this.selected;
    }

    public void updatePlace(float x, float y) {

        //we can't update anything if the selected piece is rotating
        if (getSelected() != null && getSelected().hasRotate())
            return;

        //if we are trying to finish our move, don't update anything
        if (hasComplete())
            return;

        this.updateX = x;
        this.updateY = y;
        this.update = true;
    }

    public boolean hasUpdate() {
        return this.update;
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

        //if we are starting
        if (isStarting()) {

            //if we have not yet viewed the image long enough
            if (frames < VIEW_DELAY) {

                //keep track of time lapsed
                frames++;

                //don't continue until enough time has passed
                return;
            }

            //how many pieces have we moved
            int count = 0;

            //how many pieces can we move at once
            int max = 4;

            for (int row = 0; row < getRows(); row++) {
                for (int col = 0; col < getCols(); col++) {

                    //don't continue
                    if (count >= max)
                        return;

                    Piece piece = getPieces()[row][col];

                    //if this piece is not at the start, move it
                    if (!piece.hasStart()) {

                        if (piece.getX() < piece.getStartX()) {
                            piece.setX(piece.getX() + START_VELOCITY);

                            if (piece.getX() >= piece.getStartX())
                                piece.setX(piece.getStartX());
                        } else if (piece.getX() > piece.getStartX()) {
                            piece.setX(piece.getX() - START_VELOCITY);

                            if (piece.getX() <= piece.getStartX())
                                piece.setX(piece.getStartX());
                        }

                        if (piece.getY() < piece.getStartY()) {
                            piece.setY(piece.getY() + START_VELOCITY);

                            if (piece.getY() >= piece.getStartY())
                                piece.setY(piece.getStartY());
                        } else if (piece.getY() > piece.getStartY()) {
                            piece.setY(piece.getY() - START_VELOCITY);

                            if (piece.getY() <= piece.getStartY())
                                piece.setY(piece.getStartY());
                        }

                        //if rotate is enabled, and we are at the start, pick a random rotation
                        if (ROTATE && piece.hasStart()) {

                            //pick random rotation angle
                            float angle = (getRandomObject().nextInt(3) + 1) * Piece.ANGLE_INCREMENT;

                            //update the piece to reflect
                            piece.setAngle(angle);
                            piece.setDestination(angle);
                        }

                        //update render coordinates
                        updatePiece(this, piece);

                        //keep track of how many pieces we moved
                        count++;
                    }
                }
            }

            //if we moved a piece, don't continue yet
            if (count != 0)
                return;

            //set the timer back to 0 since we are done
            frames = 0;

            //all pieces are at the beginning, we are done starting
            setStarting(false);

            //don't continue yet
            return;
        }

        //move the piece and others in the group
        if (hasUpdate() && getSelected() != null) {

            //flag false
            this.update = false;

            getSelected().setX(updateX - (getSelected().getWidth() / 2));
            getSelected().setY(updateY - (getSelected().getHeight() / 2));

            //update every other piece that is part of the same group
            updateGroup(this, -1, getSelected());

            //update the render coordinates
            updatePieces(this, getSelected().getGroup());

        } else if (hasSelection()) {

            //update the render coordinates
            updatePieces(this, getSelected().getGroup());

            //do we want to stop our selection?
            if (hasComplete()) {

                //if we have rotation continue to update
                if (getSelected().hasRotate()) {
                    getSelected().update();

                    //if the rotate is done
                    if (!getSelected().hasRotate()) {

                        //update the render coordinates
                        updatePieces(this, getSelected().getGroup());

                        //place the piece on the board accordingly
                        BoardHelper.placeSelected(this);

                        //remove the selected piece
                        removeSelected();
                    }

                } else {

                    //check how far we are from our final destination
                    final double distance = Entity.getDistance(getSelected().getX(), getSelected().getY(), getSelected().getDestinationX(), getSelected().getDestinationY());

                    //if the piece is the correct angle and we are close enough place the piece at its destination
                    if (getSelected().getAngle() == 0 && distance <= getSelected().getWidth() * CONNECTOR_RATIO) {

                        //place at the destination
                        getSelected().setX(getSelected().getDestinationX());
                        getSelected().setY(getSelected().getDestinationY());

                        //flag placed true
                        getSelected().setPlaced(true);

                        //update the group
                        updateGroup(this, -1, getSelected());

                        //update the location as well
                        updatePieces(this, getSelected().getGroup());

                        //game over?
                        GameHelper.GAME_OVER = isGameOver(this);

                        //order the pieces placed so they won't appear over the other pieces
                        BoardHelper.orderPlaced(this);

                        //place the piece on the board accordingly
                        BoardHelper.placeSelected(this);

                        //remove the selected piece
                        removeSelected();

                    } else {

                        //do we check to rotate the piece?
                        if (checkRotate) {

                            //flag false
                            checkRotate = false;

                            //we will only rotate if the selected piece is by itself
                            if (getGroupCount(this, getSelected()) == 1) {

                                //see how far we are from our origin
                                final double tmp = getSelected().getDistance(getSelected().getMotionX(), getSelected().getMotionY());

                                //if we are close enough to the start motion coordinates, start rotating
                                if (tmp < getSelected().getWidth() / 4)
                                    getSelected().setDestination(getSelected().getAngle() + Piece.ANGLE_INCREMENT);
                            }

                        } else {

                            //place the piece on the board accordingly
                            BoardHelper.placeSelected(this);

                            //remove the selected piece
                            removeSelected();
                        }
                    }
                }
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