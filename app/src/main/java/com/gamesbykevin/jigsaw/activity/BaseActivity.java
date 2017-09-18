package com.gamesbykevin.jigsaw.activity;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.gamesbykevin.jigsaw.util.UtilityHelper;
import com.gamesbykevin.jigsaw.R;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.gamesbykevin.jigsaw.util.UtilityHelper.AMAZON;
import static com.gamesbykevin.jigsaw.util.UtilityHelper.displayDensity;

/**
 * Created by Kevin on 5/22/2017.
 */
public abstract class BaseActivity extends com.gamesbykevin.androidframeworkv2.activity.BaseActivity {

    //our social media urls etc....
    private static final String URL_YOUTUBE = "https://youtube.com/gamesbykevin";
    private static final String URL_FACEBOOK = "https://facebook.com/gamesbykevin";
    private static final String URL_TWITTER = "https://twitter.com/gamesbykevin";
    private static final String URL_INSTAGRAM = "https://www.instagram.com/gamesbykevin";
    private static final String URL_WEBSITE = "http://gamesbykevin.com";
    private static final String URL_RATE = "https://play.google.com/store/apps/details?id=com.gamesbykevin.jigsaw";

    //collection of music
    private static SparseArray<MediaPlayer> SOUND;

    /**
     * Is sound enabled in the game?
     */
    public static boolean SOUND_ENABLED = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //call parent
        super.onCreate(savedInstanceState);

        //if null
        if (SOUND == null) {

            /*
            //create new list
            SOUND = new SparseArray<>();
            loadSound(R.raw.menu);
            loadSound(R.raw.theme_square);
            loadSound(R.raw.theme_hexagon);
            loadSound(R.raw.theme_diamond);
            loadSound(R.raw.rotate);
            loadSound(R.raw.rotate_connect);
            */
        }

        //make sure all options are entered
        setupDefaultOptions();

        //display the screen density of the phone
        displayDensity(this);
    }

    /**
     * Setup default options if they don't exist yet
     */
    private void setupDefaultOptions() {

        //get the editor so we can access the shared preferences
        SharedPreferences.Editor editor = getSharedPreferences().edit();

        //was any changes made
        boolean dirty = false;

        //check every button to make sure a default setting is assigned
        for (OptionsActivity.Buttons button : OptionsActivity.Buttons.values()) {

            //if the setting does not exist
            if (!getSharedPreferences().contains(getString(button.settingId))) {

                //default to true
                editor.putBoolean(getString(button.settingId), true);

                //if this is an amazon app we can't perform google play login
                if (AMAZON && button.buttonId == R.id.toggleButtonGoogleLogin)
                    editor.putBoolean(getString(button.settingId), false);

                //flag dirty since change was made
                dirty = true;
            }
        }

        //if any changes were made, make it final
        if (dirty)
            editor.commit();
    }

    private void loadSound(final int resId) {
        SOUND.put(resId, MediaPlayer.create(this, resId));
    }

    protected void playMenu() {

        //stop all audio
        stopSound();

        //start playing menu song
        //playSong(R.raw.menu);
    }

    protected void playTheme() {

        /*
        //stop all audio
        stopSound();

        //start playing main song
        switch (OptionsActivity.OPTION_BOARD_SHAPE) {

            case Diamond:
                playSong(R.raw.theme_diamond);
                break;

            case Square:
                playSong(R.raw.theme_square);
                break;

            case Hexagon:
                playSong(R.raw.theme_hexagon);
                break;
        }
        */
    }

    protected void playSong(final int resId) {
        playSound(resId, false, true);
    }

    public void playSoundEffect(final int resId) {
        playSound(resId, false, false);
    }

    private void playSound(final int resId, boolean restart, boolean loop) {

        try {

            //we can't play if the sound isn't enabled
            if (!SOUND_ENABLED)
                return;

            //if there is no sound, we can't play it
            if (SOUND == null || SOUND.size() < 1)
                return;

            //if restarting go to beginning of sound
            if (restart)
                SOUND.get(resId).seekTo(0);

            //do we want our sound to loop
            SOUND.get(resId).setLooping(loop);

            //resume playing
            SOUND.get(resId).start();

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    /**
     * Release all resources in BaseActivity
     */
    public void dispose() {

        try {

            //recycle parent
            super.dispose();

            //stop, kill all sound
            destroySound();

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    private void destroySound() {

        if (SOUND != null) {
            for (int i = 0; i < SOUND.size(); i++) {
                final int key = SOUND.keyAt(i);
                stopSound(key);
                SOUND.get(key).release();
            }

            SOUND.clear();
            SOUND = null;
        }
    }

    protected void stopSound() {

        if (SOUND != null) {
            for (int i = 0; i < SOUND.size(); i++) {
                final int key = SOUND.keyAt(i);
                stopSound(key);
            }
        }
    }

    private void stopSound(final int resId) {
        try {
            if (SOUND != null && SOUND.size() > 0 && SOUND.get(resId) != null) {

                //get the song and stop if playing
                if (SOUND.get(resId).isPlaying() || SOUND.get(resId).isLooping())
                    SOUND.get(resId).pause();
            }
        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }

    /**
     * Open the youtube web page
     * @param view Current view
     */
    public void onClickYoutube(View view) {
        openUrl(URL_YOUTUBE);
    }

    /**
     * Open the facebook web page
     * @param view Current view
     */
    public void onClickFacebook(View view) {
        openUrl(URL_FACEBOOK);
    }

    /**
     * Open the twitter web page
     * @param view Current view
     */
    public void onClickTwitter(View view) {
        openUrl(URL_TWITTER);
    }

    /**
     * Open the twitter web page
     * @param view Current view
     */
    public void onClickInstagram(View view) {
        openUrl(URL_INSTAGRAM);
    }

    public void onClickRate(View view) {
        openUrl(URL_RATE);
    }

    public void onClickMore(View view) {
        openUrl(URL_WEBSITE);
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
    }

    @Override
    protected void onPause() {

        //call parent
        super.onPause();

        //stop all sound
        stopSound();
    }

    @Override
    protected void onResume() {

        //call parent
        super.onResume();
    }

    protected void setLayoutVisibility(final ViewGroup layoutView, final boolean visible) {
        setLayoutVisibility(layoutView, (visible) ? VISIBLE : INVISIBLE);
    }

    protected void setLayoutVisibility(final ViewGroup layoutView, final int visibility) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //assign visibility accordingly
                layoutView.setVisibility(visibility);

                //if the layout is visible, make sure it is displayed
                if (visibility == VISIBLE) {
                    layoutView.invalidate();
                    layoutView.bringToFront();
                }
            }
        });
    }
}