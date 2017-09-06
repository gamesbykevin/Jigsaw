package com.gamesbykevin.jigsaw.board;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.gamesbykevin.androidframeworkv2.base.Entity;
import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.opengl.Square;
import com.gamesbykevin.jigsaw.opengl.Textures;

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

        final Bitmap bitmapCut = BitmapFactory.decodeResource(getGame().getActivity().getResources(), R.drawable.cut_east);

        if (PUZZLE_TEXTURE != null) {
            PUZZLE_TEXTURE.recycle();
            PUZZLE_TEXTURE = null;
        }

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
                    tmpImages[row][col] = Bitmap.createBitmap(Board.IMAGE_SOURCE, x1, y1, w1, h1);

                    //make the image mutable
                    tmpImages[row][col] = tmpImages[row][col].copy(Bitmap.Config.ARGB_8888, true);

                    //canvas object to make changes to bitmap
                    Canvas canvas = new Canvas(tmpImages[row][col]);

                    Paint paint = new Paint();
                    paint.setARGB(255, 255, 255, 0);
                    paint.setStrokeWidth(20);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

                    Rect rectSrc = new Rect(0, 0, bitmapCut.getWidth(), bitmapCut.getHeight());
                    Rect rectDest = new Rect(w1 - (w1/3), 0, w1, h1);

                    canvas.drawBitmap(bitmapCut, rectSrc, rectDest, paint);

                    //remove circle in bitmap
                    //canvas.drawCircle((w1 / 3), (y1 / 3), (w1 / 3), paint);

                    //assign image reference
                    tmp = tmpImages[row][col];
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
    }
}