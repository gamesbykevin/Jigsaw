package com.gamesbykevin.jigsaw.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import com.gamesbykevin.jigsaw.util.UtilityHelper;
import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.game.Game;
import com.gamesbykevin.jigsaw.services.BaseGameActivity;

public class MainActivity extends BaseGameActivity {

    //did we prompt the user before exiting the app
    private boolean promptExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //call parent
        super.onCreate(savedInstanceState);

        //assign main layout
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {

        //call parent
        super.onStart();
    }

    @Override
    protected void onStop() {

        //call parent
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        //call parent
        super.onDestroy();

        //recycle resources
        super.dispose();
    }

    @Override
    protected void onPause() {

        //call parent
        super.onPause();
    }

    @Override
    protected void onResume() {

        //call parent
        super.onResume();

        //resume audio
        super.playMenu();

        //flag prompt false
        promptExit = false;
    }

    @Override
    public void onBackPressed() {

        try {

            //if we already prompted the user, exit the app
            if (promptExit) {

                //no need to bypass login in the future
                BYPASS_LOGIN = false;

                //finish activity
                super.finish();

                //close all activities
                ActivityCompat.finishAffinity(this);

                //sign out of google play services
                super.signOut();

            } else {

                //prompt the user if they want to exit
                Toast.makeText(this, getString(R.string.exit_prompt), Toast.LENGTH_SHORT).show();

                //flag that we have prompted the user
                promptExit = true;
            }

        } catch (Exception e) {

            //handle exception
            UtilityHelper.handleException(e);

            //finish activity anyways
            super.finish();
        }
    }

    public void onClickStart(View view) {

        //reset the zoom, for every new game started
        Game.RESET_ZOOM = true;

        //store our shape selection
        //OptionsActivity.OPTION_BOARD_SHAPE = (Board.Shape)getObjectValue(R.string.game_shape_file_key, Board.Shape.class);

        //play the main theme
        super.playTheme();

        //reset to first page
        //LevelSelectActivity.CURRENT_PAGE = 0;

        //start game
        startActivity(new Intent(this, OtherActivity.class));
    }

    public void onClickTutorial(View view) {

        //start the tutorial
        startActivity(new Intent(this, TutorialActivity.class));
    }

    public void onClickOptions(View view) {

        //start options activity
        startActivity(new Intent(this, OptionsActivity.class));
    }
}