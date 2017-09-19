package com.gamesbykevin.jigsaw.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.board.Board;

public class LevelSelectActivity extends BaseActivity {

    /**
     * Our list of images to choose from
     */
    private final Integer[] RES_IDS = {
        R.drawable.picture0, R.drawable.picture1, R.drawable.picture2,
        R.drawable.picture3, R.drawable.picture4, R.drawable.picture5,
        R.drawable.picture6, R.drawable.picture7, R.drawable.picture8,
        R.drawable.picture9, R.drawable.picture10,
    };

    /**
     * What position is the seek bar at?
     */
    protected static int SEEK_BAR_PROGRESS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //call parent
        super.onCreate(savedInstanceState);

        //inflate the xml content
        setContentView(R.layout.activity_level_select);

        //obtain our list view container
        final ListView listView = findViewById(R.id.myListView);

        //obtain the text view
        final TextView textViewPieceCountDesc = findViewById(R.id.textViewPieceCountDesc);

        //obtain our seek bar
        final SeekBar seekBar = findViewById(R.id.mySeekBar);

        //set the adapter to build the list view
        listView.setAdapter(new MyArrayAdapter(this, RES_IDS));

        //assign our on click listener so we know what was selected
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //store the progress
                SEEK_BAR_PROGRESS = seekBar.getProgress();

                //if the user wants to use a custom image
                if (position == 0) {

                    //start the other activity which will let us choose the image
                    startActivity(new Intent(LevelSelectActivity.this, OtherActivity.class));

                } else {

                    //assign our image source
                    Board.IMAGE_SOURCE = BitmapFactory.decodeResource(getResources(), RES_IDS[position]);

                    //start the activity
                    startActivity(new Intent(LevelSelectActivity.this, ConfirmActivity.class));
                }
            }
        });

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
    }

    @Override
    public void onPause() {

        //call parent
        super.onPause();
    }

    @Override
    public void onBackPressed() {

        //if back pressed go to main activity
        startActivity(new Intent(this, MainActivity.class));

        //finish this activity
        finish();
    }

    private class MyArrayAdapter extends ArrayAdapter<Integer> {

        private final Context context;
        private final Integer[] values;

        public MyArrayAdapter(Context context, Integer[] values) {
            super(context, R.layout.list_level, values);

            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //obtain our view for the current position
            View tmpView = inflater.inflate(R.layout.list_level, parent, false);

            //get the image view container for this item
            ImageView imageView = tmpView.findViewById(R.id.myImageView);

            //obtain the image resource id based on the current position
            int resId = values[position];

            //set our image accordingly
            imageView.setImageResource(resId);

            //return the view
            return tmpView;
        }
    }
}