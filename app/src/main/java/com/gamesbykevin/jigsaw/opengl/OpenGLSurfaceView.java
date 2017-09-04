package com.gamesbykevin.jigsaw.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.gamesbykevin.jigsaw.base.Entity;
import com.gamesbykevin.jigsaw.util.UtilityHelper;
import com.gamesbykevin.jigsaw.game.Game;

import static com.gamesbykevin.jigsaw.util.UtilityHelper.DEBUG;
import static com.gamesbykevin.jigsaw.activity.GameActivity.getGame;
import static com.gamesbykevin.jigsaw.game.Game.ZOOM_SCALE_MOTION_X;
import static com.gamesbykevin.jigsaw.game.Game.ZOOM_SCALE_MOTION_Y;
import static com.gamesbykevin.jigsaw.opengl.OpenGLRenderer.LOADED;
import static com.gamesbykevin.jigsaw.opengl.OpenGLRenderer.ZOOM_RATIO_ADJUST;

/**
 * Created by Kevin on 6/1/2017.
 */
public class OpenGLSurfaceView extends GLSurfaceView implements Runnable {

    /**
     * Frames per second
     */
    public static final int FPS = 90;

    /**
     * Can we zoom in/out of the game?
     */
    public static final boolean ZOOM_ENABLED = true;

    /**
     * Default dimensions this game was designed for
     */
    public static final int WIDTH = 480;

    /**
     * Default dimensions this game was designed for
     */
    public static final int HEIGHT = 800;

    /**
     * The version of open GL we are using
     */
    public static final int OPEN_GL_VERSION = 2;

    //our object where we render our pixel data
    private OpenGLRenderer openGlRenderer;

    //our game mechanics will run on this thread
    private Thread thread;

    //keep our thread running
    private volatile boolean running = true;

    //track the time to keep a steady game speed
    private long previous;
    private long previousUpdate;
    private long previousDraw;
    private long postUpdate;
    private long postDraw;

    /**
     * How many milliseconds per second
     */
    public static final long MILLISECONDS_PER_SECOND = 1000L;

    /**
     * The duration of each frame (milliseconds)
     */
    public static final long FRAME_DURATION = (long)(MILLISECONDS_PER_SECOND / FPS);

    //count the number of frames for debugging purposes
    private int frames = 0;

    //keep track of time for debug purposes
    private long timestamp = System.currentTimeMillis();

    //how many fingers are touching the screen
    private int fingers = 0;

    /**
     * How many fingers do we need to zoom in/out
     */
    private static final int FINGERS_ZOOM = 2;

    //what is the distance between our finger coordinates
    private double pinchDistance = 0;

    /**
     * The minimum distance required to be considered a valid pinch
     */
    private static final float PINCH_THRESHOLD = 10;

    /**
     * The minimum pixel distance required to be considered a valid drag
     */
    private static final float DRAG_THRESHOLD = 15;

    //where are we moving our finger
    private float motionMoveX = 0.0f, motionMoveY = 0.0f;

    //do we offset the render for our game
    public static float OFFSET_X = 0.0f, OFFSET_Y = 0.0f;

    //do we offset the render for our game
    public static float OFFSET_ORIGINAL_X = 0.0f, OFFSET_ORIGINAL_Y = 0.0f;

    //are we zooming?
    private boolean zooming = false;

    //are we dragging
    private boolean dragging = false;

    public OpenGLSurfaceView(Context activity) {

        //call overloaded constructor
        this(activity, null);
    }

    public OpenGLSurfaceView(Context activity, AttributeSet attrs) {

        //call parent constructor
        super(activity, attrs);

        //create an OpenGL ES 1.0 context.
        setEGLContextClientVersion(OPEN_GL_VERSION);

        //preserve the context on pause (not guaranteed to work)
        setPreserveEGLContextOnPause(true);

        //create a new instance of our renderer
        this.openGlRenderer = new OpenGLRenderer(activity);

        //set the renderer for drawing on the gl surface view
        setRenderer(getOpenGlRenderer());

        //set render mode to only draw when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    /**
     * Get our OpenGL Renderer
     * @return Object used for all texture mapping
     */
    private OpenGLRenderer getOpenGlRenderer() {
        return this.openGlRenderer;
    }

    /**
     * If the game is paused stop our thread
     */
    @Override
    public void onPause() {

        //call parent function
        super.onPause();

        //pause the open gl renderer
        getOpenGlRenderer().onPause();

        //flag that we don't want our thread to continue running
        this.running = false;

        try {

            //wait for thread to finish
            this.thread.join();

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    /**
     * Once we resume start a new thread again
     */
    @Override
    public void onResume() {

        //call parent function
        super.onResume();

        //resume the open gl renderer
        getOpenGlRenderer().onResume();

        //flag running true
        this.running = true;

        //create the thread
        this.thread = new Thread(this);

        //start the thread
        this.thread.start();
    }

    @Override
    public void run() {

        //do we continue to loop
        while (running) {

            try {
                //get the current time
                this.previous = System.currentTimeMillis();

                //update the game state
                update();

                //render the image
                draw();

                //control the game speed
                control();

            } catch (Exception e) {
                UtilityHelper.handleException(e);
            }
        }
    }

    /**
     * Make sure we maintain game speed
     * @throws InterruptedException
     */
    private void control() throws InterruptedException {

        //calculate how much time had passed
        final long duration = System.currentTimeMillis() - this.previous;

        //we want each loop to have the same duration to maintain fps
        long remaining = FRAME_DURATION - duration;

        //log event id this loop is running slow
        if (DEBUG && remaining <= 0) {
            UtilityHelper.logEvent("Slow: " + remaining);
            UtilityHelper.logEvent("Update duration: " + (this.postUpdate - this.previousUpdate));
            UtilityHelper.logEvent("Draw   duration: " + (this.postDraw - this.previousDraw));
        }

        //make sure we sleep at least 1 millisecond
        if (remaining < 1)
            remaining = 1;

        //make sure time remaining is a valid number
        remaining = (remaining <= 0) ? 1 : remaining;

        //sleep the thread to maintain a steady game speed
        thread.sleep(remaining);

        //if debugging track performance
        if (DEBUG)
            trackProgress();
    }

    /**
     * Track progress for debugging purposes
     */
    private void trackProgress() {

        //keep track of the frames
        frames++;

        //if 1 second has passed, print fps counter
        if (System.currentTimeMillis() - timestamp >= MILLISECONDS_PER_SECOND) {

            //print progress
            UtilityHelper.logEvent("FPS: " + frames);

            //reset timer for next update
            timestamp = System.currentTimeMillis();

            //reset frame count
            frames = 0;
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        try
        {
            //if we aren't updating the game
            if (Game.STEP != Game.Step.Updating)
                return true;

            //we can't continue if the textures have not yet loaded
            if (!LOADED)
                return true;

            final float x = event.getX();
            final float y = event.getY();

            //if zoom functionality is enabled, check for it
            if (ZOOM_ENABLED) {

                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    //keep track of how many fingers are on the screen
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:

                        //keep track of how many fingers we have on screen
                        fingers++;

                        //reset distance
                        pinchDistance = 0;

                        //store the coordinates
                        motionMoveX = x;
                        motionMoveY = y;
                        break;

                    //keep track of how many fingers are on the screen
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:

                        //keep track of how many fingers we have on screen
                        fingers--;

                        //reset distance
                        pinchDistance = 0;

                        //if we are zooming
                        if (zooming) {

                            //if no more fingers are touching the screen, flag zoom over
                            if (fingers < 1)
                                zooming = false;

                            //don't continue because we don't want to update the game at this point
                            return true;
                        }

                        //if we were dragging
                        if (dragging) {

                            //flag false
                            dragging = false;

                            //don't continue because we don't want to update the game at this point
                            return true;
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:

                        //if there are 2 coordinates and we recorded 2 fingers
                        if (fingers == 2 && event.getPointerCount() == 2) {

                            //flag that we are zooming
                            zooming = true;

                            //get the distance between the two points
                            double distance = Entity.getDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));

                            if (pinchDistance == 0) {

                                //store the previous distance for our zoom
                                pinchDistance = distance;

                            } else {

                                //calculate the difference
                                double diff = (distance > pinchDistance) ? distance - pinchDistance : pinchDistance - distance;

                                //make sure the finger distance is great enough to be valid
                                if (diff > PINCH_THRESHOLD) {

                                    if (pinchDistance > distance) {

                                        //if the distance is greater we are zooming in
                                        OpenGLRenderer.adjustZoom(ZOOM_RATIO_ADJUST);

                                    } else {

                                        //if the distance is shorter we are zooming out
                                        OpenGLRenderer.adjustZoom(-ZOOM_RATIO_ADJUST);
                                    }

                                    //reset pinch distance so if doesn't zoom too fast
                                    pinchDistance = 0;
                                }
                            }

                            //don't interact with the game since that is not the intention
                            return true;

                        } else if (fingers == 1 && event.getPointerCount() == 1) {

                            //if we are zooming we can't offset the board
                            if (zooming)
                                return true;

                            //adjust the coordinates where touch event occurred
                            final float x1 = x;
                            final float y1 = y;
                            final float x2 = motionMoveX;
                            final float y2 = motionMoveY;

                            //get our move difference
                            float xDiff = ((x2 > x1) ? -(x2 - x1) : (x1 - x2));
                            float yDiff = ((y2 > y1) ? -(y2 - y1) : (y1 - y2));

                            //don't continue if we didn't move enough to register
                            if (Math.abs(xDiff) < DRAG_THRESHOLD && Math.abs(yDiff) < DRAG_THRESHOLD)
                                return true;

                            //flag that we are dragging
                            dragging = true;

                            //keep track of original coordinates
                            OFFSET_ORIGINAL_X += xDiff;
                            OFFSET_ORIGINAL_Y += yDiff;

                            //if one finger is moved, offset the x,y coordinates
                            OFFSET_X += (xDiff * ZOOM_SCALE_MOTION_X);
                            OFFSET_Y += (yDiff * ZOOM_SCALE_MOTION_Y);

                            //update the coordinates
                            motionMoveX = x1;
                            motionMoveY = y1;

                            //don't interact with the game since that is not the intention
                            return true;
                        }
                        break;
                }
            }

            //make sure we aren't using too many fingers
            if (fingers < 2) {

                //adjust the coordinates where touch event occurred
                float adjustX = (x * ZOOM_SCALE_MOTION_X) - OFFSET_X;
                float adjustY = (y * ZOOM_SCALE_MOTION_Y) - OFFSET_Y;

                //adjust the coordinates based on the window displayed
                if (getOpenGlRenderer() != null) {
                    adjustX += OpenGLRenderer.LEFT;
                    adjustY += OpenGLRenderer.TOP;
                }

                //update game accordingly
                getGame().onTouchEvent(event.getAction(), adjustX, adjustY);
            }
        }
        catch (Exception e)
        {
            UtilityHelper.handleException(e);
        }

        //return true to keep receiving touch events
        return true;
    }

    /**
     * Update the game state
     */
    private void update() throws Exception {

        //track time before update
        this.previousUpdate = System.currentTimeMillis();

        //update game logic here
        getGame().update();

        //track time after update
        this.postUpdate = System.currentTimeMillis();
    }

    /**
     * Render the game image to screen surface
     */
    private void draw() {

        //track time before draw
        this.previousDraw = System.currentTimeMillis();

        try {

            //render game objects
            requestRender();

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }

        //track time after draw
        this.postDraw = System.currentTimeMillis();
    }
}