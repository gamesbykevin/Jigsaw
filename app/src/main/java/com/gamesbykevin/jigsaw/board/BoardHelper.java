package com.gamesbykevin.jigsaw.board;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.gamesbykevin.androidframeworkv2.base.Entity;
import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.opengl.Square;
import com.gamesbykevin.jigsaw.util.UtilityHelper;

import static com.gamesbykevin.jigsaw.activity.GameActivity.getGame;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.HEIGHT;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.WIDTH;

/**
 * Created by Kevin on 9/4/2017.
 */
public class BoardHelper {

    //used to render the puzzle pieces
    private static Square square = null;

    //only calculate when we need to
    protected static boolean CALCULATE_UVS = true, CALCULATE_INDICES = true, CALCULATE_VERTICES = true;

    //our texture containing the puzzle pieces
    public static Bitmap PUZZLE_TEXTURE = null;

    //has the puzzle texture been generated
    public static boolean PUZZLE_TEXTURE_GENERATED = false;

    /**
     * Cleanup resources
     */
    public static void dispose() {
        square = null;
        PUZZLE_TEXTURE = null;
    }

    protected static Square getSquare() {

        //create new if null
        if (square == null)
            square = new Square();

        return square;
    }

    protected static void cut(final Board board) {

        //bitmap mask to cut the pieces
        Bitmap east = BitmapFactory.decodeResource(getGame().getActivity().getResources(), R.drawable.cut_traditional_east);
        Bitmap west = BitmapFactory.decodeResource(getGame().getActivity().getResources(), R.drawable.cut_traditional_west);
        Bitmap north = BitmapFactory.decodeResource(getGame().getActivity().getResources(), R.drawable.cut_traditional_north);
        Bitmap south = BitmapFactory.decodeResource(getGame().getActivity().getResources(), R.drawable.cut_traditional_south);

        if (PUZZLE_TEXTURE != null) {
            PUZZLE_TEXTURE.recycle();
            PUZZLE_TEXTURE = null;
        }

        //temporary store all our created bitmaps
        Bitmap[][] tmpImages = new Bitmap[board.getRows()][board.getCols()];

        //the desired size of the image
        final int imageWidth;
        final int imageHeight;

        final float imgSrcHeight = (float)Board.IMAGE_SOURCE.getHeight();
        final float imgSrcWidth = (float)Board.IMAGE_SOURCE.getWidth();

        //get the size ratio
        final float ratio =  imgSrcHeight / imgSrcWidth;

        if (imgSrcHeight > HEIGHT || imgSrcWidth > WIDTH) {

            if (ratio >= 1) {

                imageWidth = (int)(HEIGHT * (imgSrcWidth / imgSrcHeight));
                imageHeight = HEIGHT;

            } else {

                imageWidth = HEIGHT;
                imageHeight = (int)(HEIGHT * (imgSrcHeight / imgSrcWidth));

            }

        } else {

            imageWidth = Board.IMAGE_SOURCE.getWidth();
            imageHeight = Board.IMAGE_SOURCE.getHeight();
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(Board.IMAGE_SOURCE, imageWidth, imageHeight, false);

        //typical size of piece, not including connectors
        final int w = imageWidth / board.getCols();
        final int h = imageHeight / board.getRows();

        //size of connector ends
        final int connectorW = (int)(w * Piece.CONNECTOR_RATIO);
        final int connectorH = (int)(h * Piece.CONNECTOR_RATIO);

        //the max size of a puzzle piece
        final int fullW = w + (connectorW * 2);
        final int fullH = h + (connectorH * 2);

        //where will the first piece be rendered
        final int startX = (WIDTH / 2) - ((board.getCols() * fullW) / 2);
        final int startY = (HEIGHT / 2) - ((board.getRows() * fullH) / 2);

        //now that all pieces are created, create the connectors
        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                //calculate the current location
                int x = col * w;
                int y = row * h;

                //coordinates to grab from the source image
                final int x1, y1, w1, h1;

                //x-coordinate and width will vary by location
                if (col == 0) {
                    x1 = 0;
                    w1 = w + connectorW;
                } else if (col == board.getCols() - 1) {
                    x1 = x - connectorW;
                    w1 = w + connectorW;
                } else {
                    x1 = x - connectorW;
                    w1 = fullW;
                }

                //y-coordinate and height will vary by location
                if (row == 0) {
                    y1 = 0;
                    h1 = h + connectorH;
                } else if (row == board.getRows() - 1) {
                    y1 = y - connectorH;
                    h1 = h + connectorH;
                } else {
                    y1 = y - connectorH;
                    h1 = fullH;
                }

                //create a bitmap of the specified area for our puzzle piece
                tmpImages[row][col] = Bitmap.createBitmap(resizedBitmap, x1, y1, w1, h1);

                //make bitmap mutable
                tmpImages[row][col] = tmpImages[row][col].copy(Bitmap.Config.ARGB_8888, true);

                //get the current piece
                Piece piece = board.getPieces()[row][col];

                //assign the coordinates
                piece.setX(startX + (col * fullW));
                piece.setY(startY + (row * fullH));

                //set the size of the piece
                piece.setWidth(tmpImages[row][col].getWidth());
                piece.setHeight(tmpImages[row][col].getHeight());

                //calculate the texture coordinates
                final float tmpCol = (float)col * (1.0f / (float)board.getCols());
                final float tmpRow = (float)row * (1.0f / (float)board.getRows());
                final float tmpW = 1.0f / (float)board.getCols();
                final float tmpH = 1.0f / (float)board.getRows();

                //make sure the texture coordinates are mapped
                piece.setTextureCoordinates(tmpCol, tmpRow, tmpW, tmpH);

                //cut the bitmap
                piece.cut(tmpImages[row][col], west, north, east, south, w, h);
            }
        }

        //create our single texture containing all puzzle pieces
        Bitmap texture = Bitmap.createBitmap(fullW * board.getCols(), fullH * board.getRows(), Bitmap.Config.ARGB_8888);

        //convert bitmap to mutable object that we will convert to texture
        PUZZLE_TEXTURE = texture.copy(Bitmap.Config.ARGB_8888, true);

        //create a canvas to render to
        Canvas canvas = new Canvas(PUZZLE_TEXTURE);

        int x = 0;
        int y = 0;

        for (int col = 0; col < tmpImages[0].length; col++) {
            for (int row = 0; row < tmpImages.length; row++) {

                //calculate so the bitmap is rendered in the center
                x = (col * fullW) + (fullW / 2) - (tmpImages[row][col].getWidth() / 2);
                y = (row * fullH) + (fullH / 2) - (tmpImages[row][col].getHeight() / 2);

                //draw the puzzle piece on the large bitmap
                canvas.drawBitmap(tmpImages[row][col], x, y, null);
            }
        }

        //flag that the texture has been generated
        PUZZLE_TEXTURE_GENERATED = true;

        //recycle bitmap image since it is no longer needed
        Board.IMAGE_SOURCE.recycle();
        Board.IMAGE_SOURCE = null;

        resizedBitmap.recycle();
        resizedBitmap = null;
    }

    /**
     * Setup the coordinates for open gl rendering
     */
    protected static void updateCoordinates(Board board) {

        for (int col = 0; col < board.getPieces()[0].length; col++) {
            for (int row = 0; row < board.getPieces().length; row++) {

                try {

                    //get the current shape
                    Piece piece = board.getPieces()[row][col];

                    if (piece == null)
                        continue;

                    //update piece coordinates
                    updatePiece(board, piece);

                } catch (Exception e) {
                    UtilityHelper.handleException(e);
                }
            }
        }

        //make sure our indices are created
        board.getIndices();
    }

    protected static void updatePieceVertices(Board board, Piece piece) {

        //if rotating update vertices
        //if (piece.hasRotate())
        piece.updateVertices();

        //flag to recalculate
        CALCULATE_VERTICES = true;

        //assign vertices
        for (int i = 0; i < piece.getVertices().length; i++) {

            int index = (piece.getIndex() * 12) + i;

            if (index >= board.getVertices().length)
                return;

            board.getVertices()[index] = piece.getVertices()[i];
        }
    }

    protected static void updatePieceUvs(Board board, Piece piece) {

        //flag to recalculate
        CALCULATE_UVS = true;

        //which portion of the texture are we rendering
        for (int i = 0; i < piece.getTextureCoordinates().length; i++) {

            int index = (piece.getIndex() * 8) + i;

            if (index >= board.getUvs().length)
                return;

            board.getUvs()[index] = piece.getTextureCoordinates()[i];
        }
    }

    /**
     * Update the UVS and  Vertices coordinates
     * @param board The board containing the render coordinates
     * @param piece Current desired puzzle piece we want to update
     */
    protected static void updatePiece(Board board, Piece piece) {

        updatePieceVertices(board, piece);
        updatePieceUvs(board, piece);
    }
}