package com.gamesbykevin.jigsaw.activity;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.gamesbykevin.androidframeworkv2.base.Disposable;
import com.gamesbykevin.jigsaw.opengl.OpenGLRenderer;
import com.gamesbykevin.jigsaw.opengl.Textures;
import com.gamesbykevin.jigsaw.util.UtilityHelper;
import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.game.Game;
import com.gamesbykevin.jigsaw.game.Game.Step;
import com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView;
import com.gamesbykevin.jigsaw.services.BaseGameActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.gamesbykevin.jigsaw.game.Game.STEP;
import static com.gamesbykevin.jigsaw.util.UtilityHelper.DEBUG;

public class GameActivity extends BaseActivity implements Disposable {

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
        GameOver
    }

    //current screen we are on
    private Screen screen = Screen.Loading;

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
        this.layouts.add((ViewGroup)findViewById(R.id.gameOverLayoutDefault));
        this.layouts.add((ViewGroup)findViewById(R.id.loadingScreenLayout));
        this.layouts.add((ViewGroup)findViewById(R.id.layoutGameControls));
    }

    public static Game getGame() {
        return GAME;
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

        //resume the game object
        getGame().onResume();

        //play the main theme
        playTheme();

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

        //default all layouts to hidden
        for (int i = 0; i < layouts.size(); i++) {
            setLayoutVisibility(layouts.get(i), false);
        }

        //only display the correct screens
        switch (screen) {

            //show loading screen
            case Loading:
                setLayoutVisibility((ViewGroup)findViewById(R.id.loadingScreenLayout), true);
                break;

            //decide which game over screen is displayed
            case GameOver:
                setLayoutVisibility((ViewGroup)findViewById(R.id.gameOverLayoutDefault), true);
                break;

            //don't re-enable anything
            case Ready:
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

        //show loading screen while we reset
        setScreen(Screen.Loading);

        //move step to do nothing
        STEP = Step.Start;

        //call parent
        super.onBackPressed();
    }

    public void onClickMenu(View view) {

        //go back to the main game menu
        startActivity(new Intent(this, MainActivity.class));
    }

    public void onClickBackgroundWhite(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_WHITE;
    }

    public void onClickBackgroundBlack(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_BLACK;
    }

    public void onClickBackgroundGray(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_GRAY;
    }

    public void onClickBackgroundYellow(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_YELLOW;
    }

    public void onClickBackgroundRed(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_RED;
    }

    public void onClickBackgroundBlue(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_BLUE;
    }

    public void onClickBackgroundBrown(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_BROWN;
    }

    public void onClickBackgroundPink(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_PINK;
    }

    public void onClickBackgroundPurple(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_PURPLE;
    }

    public void onClickBackgroundOrange(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_ORANGE;
    }

    public void onClickBackgroundGreen(View view) {
        OpenGLRenderer.BACKGROUND_TEXTURE_ID_CURRENT = Textures.TEXTURE_ID_BACKGROUND_GREEN;
    }
}