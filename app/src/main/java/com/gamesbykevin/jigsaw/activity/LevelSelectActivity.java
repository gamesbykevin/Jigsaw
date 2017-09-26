package com.gamesbykevin.jigsaw.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.board.Board;
import com.gamesbykevin.jigsaw.services.BaseGameActivity;
import com.gamesbykevin.jigsaw.util.UtilityHelper;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.gamesbykevin.jigsaw.activity.GameActivity.getRandomObject;

public class LevelSelectActivity extends BaseGameActivity {

    /**
     * Our list of images to choose from
     */
    private enum ImageOption {

        ResumeSaved(R.drawable.picture0, R.string.save_image_resume),
        CustomImage(R.drawable.picture0, R.string.choose_image),
        Image1(R.drawable.picture1,   R.string.custom_image_desc_1),        Image2(R.drawable.picture2,   R.string.custom_image_desc_1),        Image3(R.drawable.picture3,   R.string.custom_image_desc_1),        Image4(R.drawable.picture4,   R.string.custom_image_desc_1),
        Image5(R.drawable.picture5,   R.string.custom_image_desc_1),        Image6(R.drawable.picture6,   R.string.custom_image_desc_1),        Image7(R.drawable.picture7,   R.string.custom_image_desc_1),        Image8(R.drawable.picture8,   R.string.custom_image_desc_1),
        Image9(R.drawable.picture9,   R.string.custom_image_desc_1),        Image10(R.drawable.picture10, R.string.custom_image_desc_1),        Image11(R.drawable.picture11, R.string.custom_image_desc_1),        Image12(R.drawable.picture12, R.string.custom_image_desc_1),
        Image13(R.drawable.picture13, R.string.custom_image_desc_1),        Image14(R.drawable.picture14, R.string.custom_image_desc_1),        Image15(R.drawable.picture15, R.string.custom_image_desc_1),        Image16(R.drawable.picture16, R.string.custom_image_desc_1),
        Image17(R.drawable.picture17, R.string.custom_image_desc_1),        Image18(R.drawable.picture18, R.string.custom_image_desc_1),        Image19(R.drawable.picture19, R.string.custom_image_desc_1),        Image20(R.drawable.picture20, R.string.custom_image_desc_1),
        Image21(R.drawable.picture21, R.string.custom_image_desc_1),        Image22(R.drawable.picture22, R.string.custom_image_desc_1),        Image23(R.drawable.picture23, R.string.custom_image_desc_1),        Image24(R.drawable.picture24, R.string.custom_image_desc_1),
        Image25(R.drawable.picture25, R.string.custom_image_desc_1),        Image26(R.drawable.picture26, R.string.custom_image_desc_1),        Image27(R.drawable.picture27, R.string.custom_image_desc_1),        Image28(R.drawable.picture28, R.string.custom_image_desc_1),
        Image29(R.drawable.picture29, R.string.custom_image_desc_1),        Image30(R.drawable.picture30, R.string.custom_image_desc_1),        Image31(R.drawable.picture31, R.string.custom_image_desc_1),        Image32(R.drawable.picture32, R.string.custom_image_desc_1),
        Image33(R.drawable.picture33, R.string.custom_image_desc_1),        Image34(R.drawable.picture34, R.string.custom_image_desc_1),        Image35(R.drawable.picture35, R.string.custom_image_desc_1),        Image36(R.drawable.picture36, R.string.custom_image_desc_1),
        Image37(R.drawable.picture37, R.string.custom_image_desc_1),        Image38(R.drawable.picture38, R.string.custom_image_desc_1),        Image39(R.drawable.picture39, R.string.custom_image_desc_1),        Image40(R.drawable.picture40, R.string.custom_image_desc_1),
        Image41(R.drawable.picture41, R.string.custom_image_desc_1),        Image42(R.drawable.picture42, R.string.custom_image_desc_1),        Image43(R.drawable.picture43, R.string.custom_image_desc_1),        Image44(R.drawable.picture44, R.string.custom_image_desc_1),
        Image45(R.drawable.picture45, R.string.custom_image_desc_1),        Image46(R.drawable.picture46, R.string.custom_image_desc_1),        Image47(R.drawable.picture47, R.string.custom_image_desc_1),        Image48(R.drawable.picture48, R.string.custom_image_desc_1),
        Image49(R.drawable.picture49, R.string.custom_image_desc_1),        Image50(R.drawable.picture50, R.string.custom_image_desc_1),        Image51(R.drawable.picture51, R.string.custom_image_desc_1),        Image52(R.drawable.picture52, R.string.custom_image_desc_1),
        Image53(R.drawable.picture53, R.string.custom_image_desc_1),        Image54(R.drawable.picture54, R.string.custom_image_desc_1),        Image55(R.drawable.picture55, R.string.custom_image_desc_1),        Image56(R.drawable.picture56, R.string.custom_image_desc_1),
        Image57(R.drawable.picture57, R.string.custom_image_desc_1),        Image58(R.drawable.picture58, R.string.custom_image_desc_1),        Image59(R.drawable.picture59, R.string.custom_image_desc_1),        Image60(R.drawable.picture60, R.string.custom_image_desc_1),
        Image61(R.drawable.picture61, R.string.custom_image_desc_1),        Image62(R.drawable.picture62, R.string.custom_image_desc_1),        Image63(R.drawable.picture63, R.string.custom_image_desc_1),        Image64(R.drawable.picture64, R.string.custom_image_desc_1),
        Image65(R.drawable.picture65, R.string.custom_image_desc_1),        Image66(R.drawable.picture66, R.string.custom_image_desc_1),        Image67(R.drawable.picture67, R.string.custom_image_desc_1),        Image68(R.drawable.picture68, R.string.custom_image_desc_1),
        Image69(R.drawable.picture69, R.string.custom_image_desc_1),        Image70(R.drawable.picture70, R.string.custom_image_desc_1),        Image71(R.drawable.picture71, R.string.custom_image_desc_1),        Image72(R.drawable.picture72, R.string.custom_image_desc_1),
        Image73(R.drawable.picture73, R.string.custom_image_desc_1),        Image74(R.drawable.picture74, R.string.custom_image_desc_1),        Image75(R.drawable.picture75, R.string.custom_image_desc_1);

        private int resIdImage;
        private int resIdDesc;

        private ImageOption(final int resIdImage, final int resIdDesc) {
            this.resIdImage = resIdImage;
            this.resIdDesc = resIdDesc;
        }

        protected int getResIdDesc() {
            return this.resIdDesc;
        }

        protected int getResIdImage() {
            return this.resIdImage;
        }
    }

    /**
     * Do we resume a saved puzzle
     */
    public static boolean RESUME_SAVED = false;

    /**
     * What position is the seek bar at?
     */
    protected static int SEEK_BAR_PROGRESS = 0;

    //keep track of index list
    private ArrayList<Integer> indexes;

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

        //the rotate setting description
        final TextView textViewRotateDesc = findViewById(R.id.textViewRotateDesc);

        //obtain our seek bar
        final SeekBar seekBar = findViewById(R.id.mySeekBar);

        //toggle for rotate
        final Switch switchRotate = findViewById(R.id.switchRotate);

        //set the adapter to build the list view
        listView.setAdapter(new MyArrayAdapter(this, ImageOption.values()));

        //assign our on click listener so we know what was selected
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //store the progress
                SEEK_BAR_PROGRESS = seekBar.getProgress();

                //update board rotate setting
                Board.ROTATE = (switchRotate.isChecked());

                if (position == 0) {

                    //if first selection check if the user has a saved puzzle
                    if (hasSavedGame()) {

                        //flag that we want to resume our saved puzzle
                        RESUME_SAVED = true;

                        //get the location of the image
                        String bitmapLocation = (String)getObjectValue(R.string.saved_puzzle_custom_image_key, String.class);;

                        //check if local location, or in .apk
                        if (bitmapLocation != null && bitmapLocation.length() > 0 && TextUtils.isDigitsOnly(bitmapLocation)) {

                            //if only numeric then the image is part of our enum array
                            Board.IMAGE_SOURCE = BitmapFactory.decodeResource(getResources(), ImageOption.values()[Integer.parseInt(bitmapLocation)].getResIdImage());

                        } else {

                            try {

                                //see if we can get the image from the user's storage
                                Board.IMAGE_SOURCE = OtherActivity.getBitmapImage(bitmapLocation, OtherActivity.RESUME_IMAGE_FILE_NAME);

                            } catch (Exception e) {

                                //handle exception
                                UtilityHelper.handleException(e);

                                //since there was an issue, pick a random existing image
                                int index = getRandomObject().nextInt(ImageOption.values().length - 2) + 2;

                                //if there was an issue finding the image, use a default
                                Board.IMAGE_SOURCE = BitmapFactory.decodeResource(getResources(), ImageOption.values()[index].getResIdImage());
                            }
                        }

                        //assign rotate value
                        Board.ROTATE = getBooleanValue(R.string.saved_puzzle_rotate_key);

                        //go straight to the game
                        startActivity(new Intent(LevelSelectActivity.this, GameActivity.class));

                    } else {

                        //start the other activity which will let us choose the image
                        startActivity(new Intent(LevelSelectActivity.this, OtherActivity.class));
                    }

                } else if (position == 1) {

                    //start the other activity which will let us choose the image
                    startActivity(new Intent(LevelSelectActivity.this, OtherActivity.class));

                } else {

                    //store the image location
                    Board.IMAGE_LOCATION = position + "";

                    //assign our image source
                    Board.IMAGE_SOURCE = BitmapFactory.decodeResource(getResources(), ImageOption.values()[position].getResIdImage());

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

        //play song
        super.playMenu();

        //change value so onProgress changed actually fires
        ((SeekBar)findViewById(R.id.mySeekBar)).setProgress((SEEK_BAR_PROGRESS == 0) ? 1 : 0);

        //set progress to user selection
        ((SeekBar)findViewById(R.id.mySeekBar)).setProgress(SEEK_BAR_PROGRESS);

        //restore switch setting
        ((Switch)findViewById(R.id.switchRotate)).setChecked(Board.ROTATE);

        //flag false for now
        RESUME_SAVED = false;

        if (Board.IMAGE_SOURCE != null) {
            Board.IMAGE_SOURCE.recycle();
            Board.IMAGE_SOURCE = null;
        }

        //get the level completed index array from the shared preferences
        this.indexes = (ArrayList<Integer>)getObjectValue(R.string.completed_puzzle_index_key, new TypeToken<ArrayList<Integer>>(){}.getType());

        //create new if not existing
        if (this.indexes == null)
            this.indexes = new ArrayList<>();
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

    /**
     * Have we previously solved the given puzzle image
     * @param position The index of the puzzle we want to check
     * @return true if the user completed the puzzle previously, false otherwise
     */
    public boolean hasCompleted(final int position) {

        //never show for resume, or select images
        if (position < 2)
            return false;

        for (int i = 0; i < this.indexes.size(); i++) {

            //if existing, return true
            if (this.indexes.get(i) == position)
                return true;
        }

        //index not found, return false
        return false;
    }

    private class MyArrayAdapter extends ArrayAdapter<ImageOption> {

        private final Context context;
        private final ImageOption[] values;

        public MyArrayAdapter(Context context, ImageOption[] values) {
            super(context, R.layout.list_level, values);

            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            //obtain our view for the current position
            View tmpView = inflater.inflate(R.layout.list_level, parent, false);

            //get our ui containers
            ImageView myImageView = tmpView.findViewById(R.id.myImageView);
            TextView myTextView = tmpView.findViewById(R.id.myImageText);
            ImageView myImageViewOverlay = tmpView.findViewById(R.id.myImageViewOverlay);

            //if we completed the puzzle, display overlay
            myImageViewOverlay.setVisibility(hasCompleted(position) ? VISIBLE : GONE);

            //set our image accordingly
            myImageView.setImageResource(values[position].getResIdImage());

            //update image text
            myTextView.setText(values[position].getResIdDesc());

            //if the first position
            if (position == 0) {

                //if we don't have a saved puzzle, hide this
                if (!hasSavedGame()) {

                    myImageView.setVisibility(GONE);
                    myTextView.setVisibility(GONE);
                    tmpView.setVisibility(GONE);

                } else {

                    //get the location of the image
                    String bitmapLocation = (String)getObjectValue(R.string.saved_puzzle_custom_image_key, String.class);

                    if (bitmapLocation != null) {

                        //check if local location, or in .apk
                        if (TextUtils.isDigitsOnly(bitmapLocation)) {

                            //update the image like so
                            myImageView.setImageResource(ImageOption.values()[Integer.parseInt(bitmapLocation)].getResIdImage());

                        } else {

                            try {

                                //update from user storage
                                myImageView.setImageBitmap(OtherActivity.getBitmapImage(bitmapLocation, OtherActivity.RESUME_IMAGE_FILE_NAME));

                            } catch (Exception e) {

                                //handle exception
                                UtilityHelper.handleException(e);

                                //since there was an issue, pick a random existing image to use in our puzzle
                                int index = getRandomObject().nextInt(ImageOption.values().length - 2) + 2;

                                //store the new location since the custom image is unavailable
                                Board.IMAGE_LOCATION = index + "";

                                //update the image like so
                                myImageView.setImageResource(ImageOption.values()[index].getResIdImage());
                            }
                        }
                    }
                }
            }

            //return the view
            return tmpView;
        }
    }
}