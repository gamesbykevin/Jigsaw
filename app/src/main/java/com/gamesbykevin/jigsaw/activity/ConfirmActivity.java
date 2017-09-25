package com.gamesbykevin.jigsaw.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.board.Board;
import com.gamesbykevin.jigsaw.services.BaseGameActivity;

import static com.gamesbykevin.jigsaw.activity.LevelSelectActivity.SEEK_BAR_PROGRESS;
import static com.gamesbykevin.jigsaw.board.BoardHelper.PUZZLE_TEXTURE;

public class ConfirmActivity extends BaseGameActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //call parent
        super.onCreate(savedInstanceState);

        //set content view
        setContentView(R.layout.activity_confirm);

        //update with the image source displayed to the user
        ((ImageView)findViewById(R.id.previewImageView)).setImageBitmap(Board.IMAGE_SOURCE);

        //obtain the text view
        final TextView textViewPieceCountDesc = findViewById(R.id.textViewPieceCountDesc);

        //obtain our seek bar
        final SeekBar seekBar = findViewById(R.id.mySeekBar);

        //add listener when the user changes the puzzle piece count
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                //we start at 9 piece puzzle (3 * 3)
                int count = (progress + 3) * (progress + 3);

                //update the display text
                textViewPieceCountDesc.setText(count + " " + getString(R.string.count_description));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do we need to do anything here?
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //do we need to do anything here?
            }
        });
    }

    @Override
    public void onResume() {

        //call parent
        super.onResume();

        //change value so onProgress changed actually fires
        ((SeekBar)findViewById(R.id.mySeekBar)).setProgress((SEEK_BAR_PROGRESS == 0) ? 1 : 0);

        //set progress to user selection
        ((SeekBar)findViewById(R.id.mySeekBar)).setProgress(SEEK_BAR_PROGRESS);

        //restore switch setting
        ((Switch)findViewById(R.id.switchRotate)).setChecked(Board.ROTATE);
    }

    public void onClickConfirmPuzzle(View view) {

        //update board rotate setting
        Board.ROTATE = ((Switch)findViewById(R.id.switchRotate)).isChecked();

        //the position will determine the size of the puzzle board
        final int size = ((SeekBar)findViewById(R.id.mySeekBar)).getProgress() + 3;

        //assign the size of our board
        Board.BOARD_COLS = size;
        Board.BOARD_ROWS = size;

        //start the game activity
        startActivity(new Intent(ConfirmActivity.this, GameActivity.class));

        //finish this activity
        finish();
    }
}