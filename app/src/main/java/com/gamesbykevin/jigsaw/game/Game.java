package com.gamesbykevin.jigsaw.game;

import android.view.MotionEvent;
import com.gamesbykevin.jigsaw.activity.GameActivity;
import com.gamesbykevin.jigsaw.activity.GameActivity.Screen;

import static com.gamesbykevin.jigsaw.game.GameHelper.FRAMES;
import static com.gamesbykevin.jigsaw.game.GameHelper.GAME_OVER;
import static com.gamesbykevin.jigsaw.game.GameHelper.GAME_OVER_DELAY_FRAMES;
import static com.gamesbykevin.jigsaw.opengl.OpenGLRenderer.LOADED;

/**
 * Created by Kevin on 7/19/2017.
 */
public class Game implements IGame {

    //store activity reference
    private final GameActivity activity;

    //do we reset the zoom factor
    public static boolean RESET_ZOOM = true;

    //store the zoom for motion events as well
    public static float ZOOM_SCALE_MOTION_X, ZOOM_SCALE_MOTION_Y;

    //are we pressing on the screen
    private boolean press = false;

    //did we perform the first render
    private boolean initialRender = false;

    /**
     * The list of steps in the game
     */
    public enum Step {
        Start,
        Reset,
        Loading,
        GameOver,
        Updating
    }

    //what is the current step that we are on
    public static Step STEP = Step.Loading;

    public Game(GameActivity activity) {

        //store activity reference
        this.activity = activity;

        //default to loading
        STEP = Step.Loading;

        //reset zoom
        RESET_ZOOM = true;
    }

    public void onPause() {
        //do we need to pause anything here?
        RESET_ZOOM = false;
    }

    public void onResume() {
        //do we need to resume anything
    }

    @Override
    public void reset() throws Exception {

        //flag game over false
        GAME_OVER = false;

        //keep track of how many games are played
        //activity.trackEvent(R.string.event_games_played);
    }

    @Override
    public void update() throws Exception {

        switch (STEP) {

            //we are loading
            case Loading:

                //if the textures have finished loading
                if (LOADED) {

                    //go to start step
                    STEP = Step.Reset;
                }
                break;

            //do nothing
            case Start:
                break;

            //we are resetting the board
            case Reset:

                //reset level
                reset();

                //after resetting, next step is updating
                STEP = Step.Updating;
                break;

            case Updating:

                //if the game is over, move to the next step
                if (GAME_OVER) {

                    //unlock any achievements we achieved
                    //AchievementHelper.completedGame(activity, getBoard());

                    //update the leader board as well (in milliseconds)
                    //LeaderboardHelper.updateLeaderboard(activity, getBoard(), activity.getSeconds() * 1000);

                    //keep track of how many games are completed
                    //activity.trackEvent(R.string.event_games_completed);

                    //reset frames count
                    FRAMES = 0;

                    //move to game over step
                    STEP = Step.GameOver;

                    //vibrate the phone
                    activity.vibrate();

                } else {

                    //update the board

                    //if we already rendered the board once, lets display it
                    if (initialRender && activity.getScreen() == Screen.Loading)
                        activity.setScreen(Screen.Ready);
                }
                break;

            case GameOver:

                //keep track of elapsed frames
                FRAMES++;

                //switch to game over screen if enough time passed and we haven't set yet
                if (FRAMES >= GAME_OVER_DELAY_FRAMES && activity.getScreen() != Screen.GameOver)
                    activity.setScreen(Screen.GameOver);
                break;
        }
    }

    /**
     * Recycle objects
     */
    @Override
    public void dispose() {

        GameHelper.dispose();
    }

    @Override
    public boolean onTouchEvent(final int action, float x, float y) {

        //don't continue if we aren't ready yet
        if (STEP != Step.Updating)
            return true;

        if (action == MotionEvent.ACTION_UP)
        {
            //check the board for rotations
            //if (this.press)
                //getBoard().touch(x, y);

            //un-flag press
            this.press = false;
        }
        else if (action == MotionEvent.ACTION_DOWN)
        {
            //flag that we pressed down
            this.press = true;
        }
        else if (action == MotionEvent.ACTION_MOVE)
        {
            //flag press
            this.press = true;
        }

        //return true to keep receiving events
        return true;
    }

    @Override
    public void render(float[] m) {

        //don't display if we aren't ready
        if (STEP != Step.Updating && STEP != Step.GameOver)
            return;

        //render everything on screen
        GameHelper.render(m);

        //we have performed the initial render
        initialRender = true;
    }
}