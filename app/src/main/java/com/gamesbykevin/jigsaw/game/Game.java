package com.gamesbykevin.jigsaw.game;

import android.view.MotionEvent;

import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.activity.GameActivity;
import com.gamesbykevin.jigsaw.activity.GameActivity.Screen;
import com.gamesbykevin.jigsaw.board.Board;
import com.gamesbykevin.jigsaw.services.AchievementHelper;
import com.gamesbykevin.jigsaw.services.AnalyticsHelper;
import com.gamesbykevin.jigsaw.services.LeaderboardHelper;

import static com.gamesbykevin.jigsaw.board.BoardHelper.CONNECTED;
import static com.gamesbykevin.jigsaw.board.BoardHelper.PLACED;
import static com.gamesbykevin.jigsaw.game.GameHelper.FRAMES;
import static com.gamesbykevin.jigsaw.game.GameHelper.GAME_OVER;
import static com.gamesbykevin.jigsaw.game.GameHelper.GAME_OVER_DELAY_FRAMES;
import static com.gamesbykevin.jigsaw.game.GameHelper.SAVE_EXIT;
import static com.gamesbykevin.jigsaw.opengl.OpenGLRenderer.LOADED;

/**
 * Created by Kevin on 7/19/2017.
 */
public class Game implements IGame {

    //store activity reference
    private final GameActivity activity;

    //are we pressing on the screen
    private boolean press = false;

    //did we perform the first render
    public static boolean INITIAL_RENDER = false;

    //puzzle board
    private Board board;

    /**
     * The list of steps in the game
     */
    public enum Step {
        Start, Reset, Loading, GameOver, Running
    }

    //what is the current step that we are on
    public static Step STEP = Step.Loading;

    public Game(GameActivity activity) {

        //store activity reference
        this.activity = activity;

        //default to loading
        STEP = Step.Loading;
    }

    protected void setBoard(final Board board) {
        this.board = board;
    }

    public Board getBoard() {
        return this.board;
    }

    public GameActivity getActivity() {
        return this.activity;
    }

    @Override
    public void onPause() {
        //do we need to do anything here
    }

    @Override
    public void onResume() {
        //do we need to resume anything
    }

    @Override
    public void reset() {

        //reset initial render flag
        INITIAL_RENDER = false;

        //reset game components
        GameHelper.reset(this);

        //track game started in analytics
        AnalyticsHelper.trackPuzzleStarted(getActivity(), this);
    }

    @Override
    public void update() {

        switch (STEP) {

            //we are loading
            case Loading:

                //if the textures have finished loading
                if (LOADED)
                    STEP = Step.Reset;
                break;

            //do nothing
            case Start:
                break;

            //we are resetting the game
            case Reset:

                //reset level
                reset();

                //after resetting, next step is updating
                STEP = Step.Running;
                break;

            //the main game occurs here
            case Running:

                //if the game is over, move to the next step
                if (GAME_OVER) {

                    //remove the saved game since it has been beaten
                    getActivity().clearSave();

                    //save completed puzzle index
                    getActivity().savePuzzleIndex();

                    //track game completed in analytics
                    AnalyticsHelper.trackPuzzleCompleted(getActivity(), this);

                    //unlock any achievements we achieved
                    AchievementHelper.completedGame(getActivity(), this);

                    //update the leader board as well (in milliseconds)
                    LeaderboardHelper.updateLeaderboard(getActivity(), this);

                    //reset frames count
                    FRAMES = 0;

                    //move to game over step
                    STEP = Step.GameOver;

                    //vibrate the phone
                    activity.vibrate();

                } else {

                    //if saving and exiting do this here
                    if (SAVE_EXIT) {
                        SAVE_EXIT = false;
                        STEP = Step.Start;
                        getActivity().savePuzzle();
                        getActivity().exit();
                        return;
                    }

                    boolean starting = getBoard().isStarting();

                    //update the board
                    getBoard().update();

                    //if we just started all the pieces, start the main theme
                    if (starting && !getBoard().isStarting()) {

                        //stop any sound
                        getActivity().stopSound();

                        //loop the main theme and start from the beginning
                        getActivity().playSound(R.raw.theme, true, true);
                    }

                    //play any sound effects?
                    if (PLACED) {
                        getActivity().playSoundEffect(R.raw.place);
                        PLACED = false;
                        CONNECTED = false;
                    } else if (CONNECTED) {
                        getActivity().playSoundEffect(R.raw.connect);
                        CONNECTED = false;
                    }

                    //if we already rendered the board once, lets display it
                    if (INITIAL_RENDER) {

                        if (getActivity().getScreen() == Screen.Loading) {

                            //if still loading switch to ready
                            getActivity().setScreen(Screen.Ready);

                        } else {

                            //if we are playing the game and the pieces have been placed, update the timer
                            if (getActivity().getScreen() == Screen.Ready && !getBoard().isStarting())
                                getActivity().getTimer().update(getActivity());
                        }
                    }
                }
                break;

            //the game has ended
            case GameOver:

                //switch to game over screen if enough time passed and we haven't set yet
                if (FRAMES >= GAME_OVER_DELAY_FRAMES && getActivity().getScreen() != Screen.GameOver) {
                    //if enough time passed go to game over screen
                    getActivity().setScreen(Screen.GameOver);
                } else if (FRAMES <= GAME_OVER_DELAY_FRAMES) {
                    //keep track of time
                    FRAMES++;
                }
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

    public void setSelected(final float x, final float y) {
        if (getActivity().getScreen() == Screen.Ready && !getBoard().isStarting() && getBoard().getSelected() == null)
            getBoard().setSelected(x, y);
    }

    public void removeSelected() {
        if (getActivity().getScreen() == Screen.Ready && !getBoard().isStarting())
            getBoard().removeSelected();
    }

    public boolean hasSelection() {
        if (getActivity().getScreen() == Screen.Ready && !getBoard().isStarting()) {
            return getBoard().hasSelection();
        } else {
            return false;
        }
    }

    public void setComplete(final boolean complete) {
        if (getActivity().getScreen() == Screen.Ready && !getBoard().isStarting() && getBoard().getSelected() != null)
            getBoard().setComplete(complete);
    }

    public void updatePlace(final float x, final float y) {
        if (getActivity().getScreen() == Screen.Ready && !getBoard().isStarting() && !getBoard().getSelected().hasRotate())
            getBoard().updatePlace(x, y);
    }

    @Override
    public boolean onTouchEvent(final int action, float x, float y) {

        //don't continue if we aren't ready yet
        if (STEP != Step.Running)
            return true;

        if (action == MotionEvent.ACTION_UP) {
            //check the board for rotations
            //if (this.press)
                //getBoard().touch(x, y);

            //un-flag press
            this.press = false;
        } else if (action == MotionEvent.ACTION_DOWN) {
            //flag that we pressed down
            this.press = true;
        } else if (action == MotionEvent.ACTION_MOVE) {
            //flag press
            this.press = true;
        }

        //return true to keep receiving events
        return true;
    }

    @Override
    public void render(float[] m) {

        //don't display if we aren't ready
        if (STEP != Step.Running && STEP != Step.GameOver)
            return;

        //render everything on screen
        GameHelper.render(m);

        //flag that we have performed the initial render
        INITIAL_RENDER = true;
    }
}