package com.gamesbykevin.jigsaw.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import com.gamesbykevin.androidframeworkv2.base.Disposable;
import com.gamesbykevin.jigsaw.board.Board;
import com.gamesbykevin.jigsaw.game.GameHelper;
import com.gamesbykevin.jigsaw.opengl.OpenGLRenderer;
import com.gamesbykevin.jigsaw.opengl.Textures;
import com.gamesbykevin.jigsaw.services.BaseGameActivity;
import com.gamesbykevin.jigsaw.services.LeaderboardHelper;
import com.gamesbykevin.jigsaw.util.GameTimer;
import com.gamesbykevin.jigsaw.util.UtilityHelper;
import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.game.Game;
import com.gamesbykevin.jigsaw.game.Game.Step;
import com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.gamesbykevin.jigsaw.activity.LevelSelectActivity.RESUME_SAVED;
import static com.gamesbykevin.jigsaw.game.Game.STEP;
import static com.gamesbykevin.jigsaw.game.GameHelper.GAME_OVER;
import static com.gamesbykevin.jigsaw.util.UtilityHelper.DEBUG;

public class GameActivity extends BaseGameActivity implements Disposable {

    //our open GL surface view
    private GLSurfaceView glSurfaceView;

    /**
     * Create a random object which the seed as the current time stamp
     */
    private static Random RANDOM;

    //Our game manager class
    private static Game GAME;

    //has the activity been paused
    private boolean paused = false;

    //our layout parameters
    private LinearLayout.LayoutParams layoutParams;

    //a list of layouts on the game screen, separate from open gl layout
    private List<ViewGroup> layouts;

    /**
     * Different steps in the game
     */
    public enum Screen {
        Loading,
        Ready,
        GameOver,
        Settings,
        Prompt
    }

    //current screen we are on
    private Screen screen = Screen.Loading;

    //keep track of game time
    private GameTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //call parent
        super.onCreate(savedInstanceState);

        //create our game manager
        GAME = new Game(this);

        //set the content view
        setContentView(R.layout.activity_game);

        //obtain our open gl surface view object for reference
        this.glSurfaceView = (OpenGLSurfaceView)findViewById(R.id.openGLView);

        //add the layouts to our list
        this.layouts = new ArrayList<>();
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGameOver));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutLoadingScreen));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGameControls));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGameSettings));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGamePrompt));

        //update the timer appropriately
        updateTimerUI(getBooleanValue(R.string.timer_file_key) ? VISIBLE : GONE);

        //update the ui for the sound setting
        updateSoundUI();

        //if we are resuming a saved game, restore that timer
        if (RESUME_SAVED)
            getTimer().restoreTime((String)getObjectValue(R.string.saved_puzzle_timer_key, String.class));
    }

    public static Game getGame() {
        return GAME;
    }

    public GameTimer getTimer() {

        //create timer if null
        if (this.timer == null)
            this.timer = new GameTimer(this);

        //return our timer object
        return this.timer;
    }

    /**
     * Get our random object.<br>
     * If object is null a new instance will be instantiated
     * @return Random object used to generate random events
     */
    public static Random getRandomObject() {

        //create the object if null
        if (RANDOM == null) {

            //get the current timestamp
            final long time = System.nanoTime();

            //create our Random object
            RANDOM = new Random(time);

            if (DEBUG)
                UtilityHelper.logEvent("Random seed: " + time);
        }

        return RANDOM;
    }

    @Override
    protected void onStart() {

        //call parent
        super.onStart();
    }

    @Override
    protected void onDestroy() {

        //call parent
        super.onDestroy();

        //cleanup resources
        if (GAME != null) {
            try {
                GAME.dispose();
            } catch (Exception e) {
                UtilityHelper.handleException(e);
            }
        }

        if (layouts != null) {

            for (ViewGroup view : layouts) {
                if (view != null) {
                    try {
                        view.removeAllViews();
                        view = null;
                    } catch (Exception e) {
                        UtilityHelper.handleException(e);
                    }
                }
            }

            layouts.clear();
            layouts = null;
        }

        if (timer != null) {
            timer.dispose();
            timer = null;
        }

        glSurfaceView = null;
        layoutParams = null;
    }

    @Override
    protected void onPause() {

        //call parent
        super.onPause();

        //pause the game
        getGame().onPause();

        //flag paused true
        this.paused = true;

        //pause the game view
        glSurfaceView.onPause();

        //flag for recycling
        glSurfaceView = null;

        //stop all sound
        stopSound();
    }

    @Override
    protected void onResume() {

        //call parent
        super.onResume();

        //resume theme if the game has started
        if (getGame() != null && getGame().getBoard() != null && !getGame().getBoard().isStarting())
            super.playTheme();

        //resume the game object
        getGame().onResume();

        //if the game was previously paused we need to re-initialize the views
        if (this.paused) {

            //flag paused false
            this.paused = false;

            //create a new OpenGL surface view
            glSurfaceView = new OpenGLSurfaceView(this);

            //resume the game view
            glSurfaceView.onResume();

            //remove layouts from the parent view
            for (int i = 0; i < layouts.size(); i++) {
                ((ViewGroup)layouts.get(i).getParent()).removeView(layouts.get(i));
            }

            //set the content view for our open gl surface view
            setContentView(glSurfaceView);

            //add the layouts to the current content view
            for (int i = 0; i < layouts.size(); i++) {
                super.addContentView(layouts.get(i), getLayoutParams());
            }

        } else {

            //resume the game view
            glSurfaceView.onResume();
        }

        //determine what screen(s) are displayed
        setScreen(getScreen());
    }

    public Screen getScreen() {
        return this.screen;
    }

    public void setScreen(final Screen screen) {

        //flag the screen frozen
        GameHelper.SCREEN_FROZEN = true;

        //default all layouts to hidden
        for (int i = 0; i < layouts.size(); i++) {
            setLayoutVisibility(layouts.get(i), false);
        }

        //only display the correct screens
        switch (screen) {

            //show loading screen
            case Loading:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutLoadingScreen), true);
                break;

            //show the game settings
            case Settings:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutGameSettings), true);
                break;

            //decide which game over screen is displayed
            case GameOver:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutGameOver), true);
                break;

            //show the save prompt
            case Prompt:
                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutGamePrompt), true);
                break;

            //don't re-enable anything
            case Ready:

                //allow screen interaction
                GameHelper.SCREEN_FROZEN = false;

                setLayoutVisibility((ViewGroup)findViewById(R.id.layoutGameControls), true);
                break;
        }

        //assign screen to view
        this.screen = screen;
    }

    private LinearLayout.LayoutParams getLayoutParams() {

        if (this.layoutParams == null)
            this.layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.MATCH_PARENT);

        return this.layoutParams;
    }

    @Override
    public void onBackPressed() {

        //we will do different things depening which screen we are on
        switch (getScreen()) {

            //if viewing the game settings and back pressed, go back to game
            case Settings:
                setScreen(Screen.Ready);
                break;

            //if playing the game and back pressed, prompt user to save
            case Ready:

                //if the game is still starting, do nothing
                if (getGame().getBoard().isStarting())
                    return;

                //do we show overwrite text
                findViewById(R.id.textViewPromptExtra).setVisibility(hasSavedGame() ? VISIBLE : View.INVISIBLE);

                //display prompt window
                setScreen(Screen.Prompt);
                break;

            //if the user is being prompted, don't allow back pressed
            case Prompt:
                return;

            //if the loading screen is displayed, don't allow the user to go back
            case Loading:
                return;

            default:

                //show loading screen while we reset
                setScreen(Screen.Loading);

                //move step to do nothing
                STEP = Step.Start;

                //go back to level select activity
                startActivity(new Intent(this, LevelSelectActivity.class));

                //finish the activity
                finish();

                //done with statement
                break;
        }
    }

    public void onClickMenu(View view) {

        //go back to the main game menu
        startActivity(new Intent(this, MainActivity.class));
    }

    public void onClickBackgroundWhite(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_WHITE;
        resetBackgroundSelector();
        ((ImageView)findViewById(R.id.backgroundWhite)).setImageResource(R.drawable.background_selector_white_selected);
    }

    public void onClickBackgroundBlack(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_BLACK;
        resetBackgroundSelector();
        ((ImageView)findViewById(R.id.backgroundBlack)).setImageResource(R.drawable.background_selector_black_selected);
    }

    public void onClickBackgroundGray(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_GRAY;
        resetBackgroundSelector();
        ((ImageView)findViewById(R.id.backgroundGray)).setImageResource(R.drawable.background_selector_gray_selected);
    }

    public void onClickBackgroundYellow(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_YELLOW;
        resetBackgroundSelector();
        ((ImageView)findViewById(R.id.backgroundYellow)).setImageResource(R.drawable.background_selector_yellow_selected);
    }

    public void onClickBackgroundRed(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_RED;
        resetBackgroundSelector();
        ((ImageView)findViewById(R.id.backgroundRed)).setImageResource(R.drawable.background_selector_red_selected);
    }

    public void onClickBackgroundBlue(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_BLUE;
        resetBackgroundSelector();
        ((ImageView)findViewById(R.id.backgroundBlue)).setImageResource(R.drawable.background_selector_blue_selected);
    }

    public void onClickBackgroundBrown(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_BROWN;
        resetBackgroundSelector();
        ((ImageView)findViewById(R.id.backgroundBrown)).setImageResource(R.drawable.background_selector_brown_selected);
    }

    public void onClickBackgroundPink(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_PINK;
        resetBackgroundSelector();
        ((ImageView)findViewById(R.id.backgroundPink)).setImageResource(R.drawable.background_selector_pink_selected);
    }

    public void onClickBackgroundPurple(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_PURPLE;
        resetBackgroundSelector();
        ((ImageView)findViewById(R.id.backgroundPurple)).setImageResource(R.drawable.background_selector_purple_selected);
    }

    public void onClickBackgroundOrange(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_ORANGE;
        resetBackgroundSelector();
        ((ImageView)findViewById(R.id.backgroundOrange)).setImageResource(R.drawable.background_selector_orange_selected);
    }

    public void onClickBackgroundGreen(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_GREEN;
        resetBackgroundSelector();
        ((ImageView)findViewById(R.id.backgroundGreen)).setImageResource(R.drawable.background_selector_green_selected);
    }

    public void onClickBackgroundTurquoise(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_TURQUOISE;
        resetBackgroundSelector();
        ((ImageView)findViewById(R.id.backgroundTurquoise)).setImageResource(R.drawable.background_selector_turquoise_selected);
    }

    private void resetBackgroundSelector() {
        ((ImageView)findViewById(R.id.backgroundWhite)).setImageResource(R.drawable.background_selector_white);
        ((ImageView)findViewById(R.id.backgroundBlack)).setImageResource(R.drawable.background_selector_black);
        ((ImageView)findViewById(R.id.backgroundGray)).setImageResource(R.drawable.background_selector_gray);
        ((ImageView)findViewById(R.id.backgroundRed)).setImageResource(R.drawable.background_selector_red);
        ((ImageView)findViewById(R.id.backgroundBrown)).setImageResource(R.drawable.background_selector_brown);
        ((ImageView)findViewById(R.id.backgroundBlue)).setImageResource(R.drawable.background_selector_blue);
        ((ImageView)findViewById(R.id.backgroundYellow)).setImageResource(R.drawable.background_selector_yellow);
        ((ImageView)findViewById(R.id.backgroundOrange)).setImageResource(R.drawable.background_selector_orange);
        ((ImageView)findViewById(R.id.backgroundPurple)).setImageResource(R.drawable.background_selector_purple);
        ((ImageView)findViewById(R.id.backgroundGreen)).setImageResource(R.drawable.background_selector_green);
        ((ImageView)findViewById(R.id.backgroundPink)).setImageResource(R.drawable.background_selector_pink);
        ((ImageView)findViewById(R.id.backgroundTurquoise)).setImageResource(R.drawable.background_selector_turquoise);
    }

    public void onClickSettings(View view) {

        if (findViewById(R.id.layoutGameSettings).getVisibility() != VISIBLE) {

            //if the settings are not showing, display it
            setScreen(Screen.Settings);
        } else {

            //if the settings are showing, go back to ready
            setScreen(Screen.Ready);
        }
    }

    public void onClickConfirm(View view) {

        //display loading screen
        setScreen(Screen.Loading);

        //flag save
        GameHelper.SAVE_EXIT = true;
    }

    public void onClickExit(View view) {
        exit();
    }

    public void onClickNext(View view) {
        exit();
    }

    public void onClickLeaderboard(View view) {

        displayLeaderboardUI(getString(LeaderboardHelper.getResId(getGame().getBoard())));
    }

    public void onClickHome(View view) {

        //start the activity
        startActivity(new Intent(GameActivity.this, MainActivity.class));

        //remove this activity from the back stack
        finish();
    }

    public void exit() {

        //start the activity
        startActivity(new Intent(GameActivity.this, LevelSelectActivity.class));

        //remove this activity from the back stack
        finish();
    }

    public void onClickChangeSound(View view) {

        //flip the sound setting
        SOUND_ENABLED = !SOUND_ENABLED;

        //either stop the sound, or play the theme
        if (SOUND_ENABLED) {

            //if the game started, we can continue playing the theme
            if (getGame() != null && getGame().getBoard() != null && !getGame().getBoard().isStarting())
                playTheme();
        } else {
            stopSound();
        }

        //update the ui
        updateSoundUI();
    }

    public void onClickTweet(View view) {

        //this should never happen
        if (!GAME_OVER || getGame() == null || getGame().getBoard() == null)
            return;

        //create our tweet message
        String message = "I completed a " + (getGame().getBoard().getCols() * getGame().getBoard().getRows()) + " piece jigsaw puzzle in " + this.timer.getTimeDesc() + " @gamesbykevin";

        //put the message in a url
        String tweetUrl = "https://twitter.com/intent/tweet?text=" + message;

        //start the activity
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl)));
    }

    public void onClickShowTimer(View view) {

        //update the timer ui
        updateTimerUI(findViewById(R.id.tableGameTimer).getVisibility() != VISIBLE ? VISIBLE : View.GONE);
    }

    private void updateSoundUI() {

        //show sound enabled / disabled based on our global sound flag
        ((ImageView)findViewById(R.id.buttonSound)).setImageResource(SOUND_ENABLED ? R.drawable.sound_on : R.drawable.sound_off);
    }

    private void updateTimerUI(final int visibility) {

        //obtain our ui objects
        TableLayout tmp = findViewById(R.id.tableGameTimer);
        ImageView buttonTimer = findViewById(R.id.buttonTimer);

        if (visibility == VISIBLE) {

            //if showing, display it
            tmp.setVisibility(VISIBLE);
            buttonTimer.setImageResource(R.drawable.timer_on);
        } else {

            //if not showing, hide it
            tmp.setVisibility(View.GONE);
            buttonTimer.setImageResource(R.drawable.timer_off);
        }
    }

    /**
     * Save the puzzle for future play
     */
    public void savePuzzle() {

        try {

            //if the location is not a number, then the location is a path to a file and we need to move/save
            if (!TextUtils.isDigitsOnly(Board.IMAGE_LOCATION)) {

                //rename our temp file
                OtherActivity.saveToInternalStorage(
                    this,
                    OtherActivity.getBitmapImage(Board.IMAGE_LOCATION, OtherActivity.RESUME_IMAGE_TMP_FILE_NAME),
                    OtherActivity.RESUME_IMAGE_FILE_NAME
                );
            }

            //get the editor so we can change the shared preferences
            SharedPreferences.Editor editor = getSharedPreferences().edit();

            //convert array to json string when saving in shared preferences
            editor.putString(getString(R.string.saved_puzzle_key), GSON.toJson(getGame().getBoard().getPieces()));

            //store the rotation setting in the shared preferences
            editor.putBoolean(getString(R.string.saved_puzzle_rotate_key), Board.ROTATE);

            //store the selected image in shared preferences as well
            editor.putString(getString(R.string.saved_puzzle_custom_image_key), GSON.toJson(Board.IMAGE_LOCATION));

            //store the current timer
            editor.putString(getString(R.string.saved_puzzle_timer_key), GSON.toJson(getTimer().getTime()));

            //save changes
            editor.commit();

        } catch (Exception e) {

            UtilityHelper.handleException(e);

        }
    }

    public void clearSave() {

        //get the editor so we can change the shared preferences
        SharedPreferences.Editor editor = getSharedPreferences().edit();

        //remove all saved puzzle information
        editor.remove(getString(R.string.saved_puzzle_key));
        editor.remove(getString(R.string.saved_puzzle_rotate_key));
        editor.remove(getString(R.string.saved_puzzle_timer_key));
        editor.remove(getString(R.string.saved_puzzle_custom_image_key));

        //save changes
        editor.commit();
    }

    public void savePuzzleIndex() {

        //make sure level location is a number
        if (Board.IMAGE_LOCATION == null)
            return;
        if (Board.IMAGE_LOCATION.length() < 1)
            return;
        if (!TextUtils.isDigitsOnly(Board.IMAGE_LOCATION))
            return;

        //get the current level index
        final int index = Integer.parseInt(Board.IMAGE_LOCATION);

        //get the level completed index array from the shared preferences
        ArrayList<Integer> indexes = (ArrayList<Integer>)getObjectValue(R.string.completed_puzzle_index_key, new TypeToken<ArrayList<Integer>>(){}.getType());

        //create new if not existing
        if (indexes == null)
            indexes = new ArrayList<>();

        //check if we already have completed the level
        for (int i = 0; i < indexes.size(); i++) {

            //if we found match, no need to save
            if (indexes.get(i) == index)
                return;
        }

        //add index to the list
        indexes.add(index);

        //get the editor so we can change the shared preferences
        SharedPreferences.Editor editor = getSharedPreferences().edit();

        //convert array to json string when saving in shared preferences
        editor.putString(getString(R.string.completed_puzzle_index_key), GSON.toJson(indexes));

        //make changes final
        editor.commit();
    }
}