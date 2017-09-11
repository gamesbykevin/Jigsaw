package com.gamesbykevin.jigsaw.activity;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

import com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceViewHelper;
import com.gamesbykevin.jigsaw.util.UtilityHelper;
import com.gamesbykevin.jigsaw.R;

public class OptionsActivity extends BaseActivity {

    //has the activity been paused
    private boolean paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_options);

        //retrieve our buttons so we can update based on current setting (shared preferences)
        ToggleButton buttonSound = (ToggleButton)findViewById(R.id.toggleButtonSound);
        ToggleButton buttonVibrate = (ToggleButton)findViewById(R.id.toggleButtonVibrate);

        /*
        //populate shape options
        this.buttonShape = (MultiStateToggleButton)findViewById(R.id.ToggleButtonShape);
        this.buttonShape.setOptions(Board.Shape.values());
        this.buttonShape.setHeader(getString(R.string.text_header_shape));
        */

        //update our buttons accordingly
        buttonSound.setChecked(getBooleanValue(R.string.sound_file_key));
        buttonVibrate.setChecked(getBooleanValue(R.string.vibrate_file_key));
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop sound
        super.stopSound();

        //flag paused
        paused = true;
    }

    @Override
    public void onResume() {

        //call parent
        super.onResume();

        //play menu
        super.playMenu();

        //flag false
        paused = false;
    }

    /**
     * Override the back pressed so we save the shared preferences
     */
    @Override
    public void onBackPressed() {

        try {

            //get the editor so we can change the shared preferences
            Editor editor = getSharedPreferences().edit();

            //store the sound setting based on the toggle button
            editor.putBoolean(getString(R.string.sound_file_key), ((ToggleButton)findViewById(R.id.toggleButtonSound)).isChecked());

            //store the vibrate setting based on the toggle button
            editor.putBoolean(getString(R.string.vibrate_file_key), ((ToggleButton)findViewById(R.id.toggleButtonVibrate)).isChecked());

            //store the zoom setting based on the toggle button
            editor.putBoolean(getString(R.string.open_gl_zoom_file_key), ((ToggleButton)findViewById(R.id.toggleButtonZoom)).isChecked());

            //store the shape setting as well
            //editor.putString(getString(R.string.game_shape_file_key), GSON.toJson(buttonShape.getValue()));

            //make it final by committing the change
            editor.commit();

        } catch (Exception ex) {

            //handle exception
            UtilityHelper.handleException(ex);
        }

        //call parent function
        super.onBackPressed();
    }

    public void onClickVibrate(View view) {

        //get the button
        ToggleButton button = (ToggleButton)view.findViewById(R.id.toggleButtonVibrate);

        //if the button is checked we will vibrate the phone
        if (button.isChecked()) {
            super.vibrate(true);
        }
    }

    public void onClickSound(View view) {

        //get the button
        ToggleButton button = (ToggleButton)view.findViewById(R.id.toggleButtonSound);

        if (!button.isChecked()) {
            //if not enabled stop all sound
            super.stopSound();
        } else {
            //ifi enabled play menu theme
            super.playMenu();
        }
    }

    public void onClickZoom(View view) {

        //get the button
        ToggleButton button = (ToggleButton)view.findViewById(R.id.toggleButtonZoom);

        //update the view  options
        OpenGLSurfaceViewHelper.ZOOM_ENABLED = button.isChecked();
        OpenGLSurfaceViewHelper.DRAG_ENABLED = button.isChecked();
    }
}