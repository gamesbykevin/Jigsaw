package com.gamesbykevin.jigsaw.board;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.gamesbykevin.androidframeworkv2.base.Entity;
import com.gamesbykevin.jigsaw.opengl.Square;
import com.gamesbykevin.jigsaw.opengl.Textures;

import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.HEIGHT;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.WIDTH;

/**
 * Created by Kevin on 9/4/2017.
 */

public class BoardHelper {

    //used to render the background
    protected static Entity entity = null;
    protected static Square square = null;

    protected static void cut(final Board board) {

        //temporary store all our created bitmaps
        Bitmap[][] tmpImages = new Bitmap[board.getRows()][board.getCols()];

        //typical size of piece, not including connectors
        final int w = Board.IMAGE_SOURCE.getWidth() / board.getCols();
        final int h = Board.IMAGE_SOURCE.getHeight() / board.getRows();

        //size of connector ends
        final int connectorW = (int)(w * Piece.CONNECTOR_RATIO);
        final int connectorH = (int)(h * Piece.CONNECTOR_RATIO);

        //now that all pieces are created, create the connectors
        for (int col = 0; col < board.getCols(); col++) {
            for (int row = 0; row < board.getRows(); row++) {

                //calculate the current location
                int x = col * w;
                int y = row * h;

                //get the current bitmap
                Bitmap tmp = tmpImages[row][col];

                //if image doesn't exist yet we will need to create it
                if (tmp == null) {

                    //coordinates to grab from the source image
                    int x1, y1, w1, h1;

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
                    tmp = Bitmap.createBitmap(Board.IMAGE_SOURCE, x1, y1, w1, h1);
                }

                //get the current piece
                Piece piece = board.getPieces()[row][col];

                //set the size of the piece
                piece.setWidth(tmp.getWidth());
                piece.setHeight(tmp.getHeight());

                //check the east connector
                if (col < board.getCols() - 1) {

                    //get neighbor
                    Piece neighbor = board.getPieces()[row][col + 1];

                    switch (piece.getEast()) {
                        case Male:
                            break;

                        case Female:
                            break;

                        //this should never happen
                        case None:
                            throw new RuntimeException("Piece has no east connector: (" + col + "," + row + ")");
                    }
                }

                //check the south connector
                if (row < board.getRows() - 1) {

                    //get neighbor
                    Piece neighbor = board.getPieces()[row + 1][col];

                    switch (piece.getSouth()) {
                        case Male:
                            break;

                        case Female:
                            break;

                        //this should never happen
                        case None:
                            throw new RuntimeException("Piece has no east connector: (" + col + "," + row + ")");
                    }
                }
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

        Bitmap texture = Bitmap.createBitmap(width, height, null);

        //convert bitmap to mutable object
        texture = texture.copy(android.graphics.Bitmap.Config.ARGB_8888, true);

        //create a canvas to render to
        Canvas canvas = new Canvas(texture);

        int x = 0;
        int y = 0;

        for (int col = 0; col < tmpImages[0].length; col++) {

            x += tmpImages[0][col].getWidth();
            y = 0;

            for (int row = 0; row < tmpImages.length; row++) {

                canvas.drawBitmap(tmpImages[row][col], x, y, null);

                y += tmpImages[row][col].getHeight();
            }
        }

        //only need to setup once
        entity = new Entity();
        entity.setX(0);
        entity.setY(0);
        entity.setAngle(0f);
        entity.setWidth(WIDTH);
        entity.setHeight(HEIGHT);
        square = new Square();
        square.setupImage();
        square.setupTriangle();
        square.setupVertices(entity.getVertices());

        //load the texture into open gl
        Textures.TEXTURE_ID_IMAGE_SOURCE = Textures.loadTexture(texture, Textures.INDEX_TEXTURE_ID_IMAGE_SOURCE);
    }

}
