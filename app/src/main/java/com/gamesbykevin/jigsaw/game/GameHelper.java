package com.gamesbykevin.jigsaw.game;

import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import com.gamesbykevin.androidframeworkv2.base.Entity;
import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.board.Board;
import com.gamesbykevin.jigsaw.opengl.Square;

import static com.gamesbykevin.jigsaw.activity.GameActivity.getGame;
import static com.gamesbykevin.jigsaw.opengl.OpenGLRenderer.RESET_ZOOM;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.FPS;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.HEIGHT;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.WIDTH;
import static com.gamesbykevin.jigsaw.opengl.Textures.TEXTURE_ID_PLACEMENT_BORDER;

public final class GameHelper {

    //used to render the background
	private static Entity entityBackground = null;
    private static Square squareBackground = null;

    //used to render the placement border
    private static Entity entityPlaceBorder = null;
    private static Square squarePlaceBorder = null;

    //did we flag the game over?
    public static boolean GAME_OVER = false;

    //do we want to display the timer
    public static boolean TIMER = true;

    //how long do we wait until displaying the game over overlay
    public static final int GAME_OVER_DELAY_FRAMES = (FPS * 3);

    /**
     * Is the game screen frozen? If yes then we prevent all motion events to the open gl surface view
     */
    public static boolean SCREEN_FROZEN = true;

    //keep track of elapsed frames
    public static int FRAMES = 0;

    private static Entity getEntityBackground() {

        //only need to setup once
        if (entityBackground == null) {
            entityBackground = new Entity();
            entityBackground.setX(0);
            entityBackground.setY(0);
            entityBackground.setAngle(0f);
            entityBackground.setWidth(WIDTH);
            entityBackground.setHeight(HEIGHT);
        }

        return entityBackground;
    }

    public static Square getSquareBackground() {

        //only need to setup once
        if (squareBackground == null) {
            squareBackground = new Square();
            squareBackground.setupImage();
            squareBackground.setupTriangle();
            squareBackground.setupVertices(getEntityBackground().getVertices());
        }

        return squareBackground;
    }

    public static Entity getEntityPlaceBorder() {

        //setup if null
        if (entityPlaceBorder == null) {
            entityPlaceBorder = new Entity();
            entityPlaceBorder.setX(0);
            entityPlaceBorder.setY(0);
            entityPlaceBorder.setAngle(0f);
            entityPlaceBorder.setWidth(WIDTH);
            entityPlaceBorder.setHeight(HEIGHT);
        }

        return entityPlaceBorder;
    }

    public static Square getSquarePlaceBorder() {

        //setup if null
        if (squarePlaceBorder == null) {
            squarePlaceBorder = new Square();
            squarePlaceBorder.setupImage();
            squarePlaceBorder.setupTriangle();
            squarePlaceBorder.setupVertices(getEntityPlaceBorder().getVertices());
        } else {

            //set the correct texture for rendering
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, TEXTURE_ID_PLACEMENT_BORDER);
        }

        return squarePlaceBorder;
    }

    public static void dispose() {
        squareBackground = null;
        entityBackground = null;

        squarePlaceBorder = null;
        entityPlaceBorder = null;
    }

    public static void reset(Game game) {

        //flag game over false
        GAME_OVER = false;

        //reset zoom
        RESET_ZOOM = true;

        //assign the image for the puzzle we need to cut
        if (Board.IMAGE_SOURCE == null)
            Board.IMAGE_SOURCE = BitmapFactory.decodeResource(game.getActivity().getResources(), R.drawable.picture1);

        //create new board
        game.setBoard(new Board());

        //keep track of how many games are played
        //game.getActivity().trackEvent(R.string.event_games_played);
    }

    /**
     * Render the game accordingly
     * @throws Exception
     */
    public static void render(float[] m) {

		//make sure we are supporting alpha for transparency
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //render the placeholder for the pieces
        getSquarePlaceBorder().render(m);

		//render the pieces on the board
        getGame().getBoard().render(m);

        //we can now disable alpha transparency
        GLES20.glDisable(GLES20.GL_BLEND);
    }
}