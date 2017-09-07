package com.gamesbykevin.jigsaw.board;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.gamesbykevin.androidframeworkv2.base.Entity;
import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.opengl.Square;

import static com.gamesbykevin.jigsaw.activity.GameActivity.getGame;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.HEIGHT;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.WIDTH;

/**
 * Created by Kevin on 9/4/2017.
 */

public class BoardHelper {

    //used to render the background
    protected static Entity entity = null;
    protected static Square square = null;

    //our texture containing the puzzle pieces
    public static Bitmap PUZZLE_TEXTURE = null;

    //has the puzzle texture been generated
    public static boolean PUZZLE_TEXTURE_GENERATED = false;

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
                    w1 = w + (connectorW * 2);
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
                    h1 = h + (connectorH * 2);
                }

                //create a bitmap of the specified area for our puzzle piece
                tmpImages[row][col] = Bitmap.createBitmap(resizedBitmap, x1, y1, w1, h1);

                //make bitmap mutable
                tmpImages[row][col] = tmpImages[row][col].copy(Bitmap.Config.ARGB_8888, true);

                //get the current piece
                Piece piece = board.getPieces()[row][col];

                //set the size of the piece
                piece.setWidth(tmpImages[row][col].getWidth());
                piece.setHeight(tmpImages[row][col].getHeight());

                //cut the bitmap
                piece.cut(tmpImages[row][col], west, north, east, south);
            }
        }

        int width = 0;
        int height = 0;

        for (int col = 0; col < tmpImages[0].length; col++) {
            width += tmpImages[0][col].getWidth();
        }

        for (int row = 0; row < tmpImages.length; row++) {
            height += tmpImages[row][0].getHeight();
        }

        Bitmap texture = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        //convert bitmap to mutable object that we will convert to texture
        PUZZLE_TEXTURE = texture.copy(Bitmap.Config.ARGB_8888, true);

        //create a canvas to render to
        Canvas canvas = new Canvas(PUZZLE_TEXTURE);

        int x = 0;
        int y = 0;

        for (int col = 0; col < tmpImages[0].length; col++) {

            y = 0;

            for (int row = 0; row < tmpImages.length; row++) {

                canvas.drawBitmap(tmpImages[row][col], x, y, null);

                y += tmpImages[row][col].getHeight();
            }

            x += tmpImages[0][col].getWidth();
        }

        //only need to setup once
        entity = new Entity();
        entity.setX((WIDTH / 2) - (width / 2));
        entity.setY((HEIGHT / 2) - (height / 2));
        entity.setAngle(0f);
        entity.setWidth(width);
        entity.setHeight(height);
        square = new Square();
        square.setupImage();
        square.setupTriangle();
        square.setupVertices(entity.getVertices());

        //flag that the texture has been generated
        PUZZLE_TEXTURE_GENERATED = true;

        //recycle bitmap image since it is no longer needed
        Board.IMAGE_SOURCE.recycle();
        Board.IMAGE_SOURCE = null;

        resizedBitmap.recycle();
        resizedBitmap = null;
    }
}