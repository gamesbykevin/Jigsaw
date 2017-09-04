package com.gamesbykevin.jigsaw.game;

import android.opengl.GLES20;

import com.gamesbykevin.jigsaw.base.Entity;
import com.gamesbykevin.jigsaw.opengl.Square;

import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.FPS;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.HEIGHT;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.WIDTH;

public final class GameHelper {

	private static Entity entityBackground = null;

    private static Square square = null;

    private static Square squareBackground = null;

    public static Entity getEntityBackground() {

        if (entityBackground == null) {
            entityBackground = new Entity();
            entityBackground.setX(0);
            entityBackground.setY(0);
            entityBackground.setAngle(0f);
            entityBackground.setWidth(WIDTH);
            entityBackground.setHeight(HEIGHT);

            //only needed to do one time
            getSquareBackground().setupImage();
            getSquareBackground().setupTriangle();
            getSquareBackground().setupVertices(entityBackground.getVertices());
        }

        return entityBackground;
    }


    public static Square getSquare() {
        if (square == null)
            square = new Square();

        return square;
    }

    public static Square getSquareBackground() {
        if (squareBackground == null)
            squareBackground = new Square();

        return squareBackground;
    }

    //did we flag the game over?
    public static boolean GAME_OVER = false;

    //how long do we wait until displaying the game over overlay
    public static final int GAME_OVER_DELAY_FRAMES = (FPS * 3);

    //keep track of elapsed frames
    public static int FRAMES = 0;

    public static void dispose() {
        squareBackground = null;
        square = null;
        entityBackground = null;
    }

    /**
     * Render the game accordingly
     * @throws Exception
     */
    public static void render(float[] m) {

		//make sure we are supporting alpha for transparency
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		//render the pieces on the board
        //getGame().getBoard().render(m);

        //we can now disable alpha transparency
        GLES20.glDisable(GLES20.GL_BLEND);
    }
}