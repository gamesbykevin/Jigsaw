package com.gamesbykevin.jigsaw.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.gamesbykevin.jigsaw.R;

public class OtherActivity extends BaseActivity {

    /**
     * After we initialize how long should we delay
     */
    public static final long DEFAULT_DELAY = 350L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //call parent
        super.onCreate(savedInstanceState);

        //inflate layout
        setContentView(R.layout.activity_other);
    }

    @Override
    public void onResume() {

        //call parent
        super.onResume();

        //delay a couple seconds before going to main page
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                //start the new activity
                startActivity(new Intent(OtherActivity.this, GameActivity.class));

                //close the activity
                finish();

            }

        }, DEFAULT_DELAY);
    }

    @Override
    public void onBackPressed() {

        //don't allow user to press back button
        return;
    }
}