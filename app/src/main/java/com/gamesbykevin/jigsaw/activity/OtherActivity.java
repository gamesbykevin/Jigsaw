package com.gamesbykevin.jigsaw.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;

import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.board.Board;
import com.gamesbykevin.jigsaw.board.BoardHelper;
import com.gamesbykevin.jigsaw.util.UtilityHelper;

import static com.gamesbykevin.jigsaw.board.BoardHelper.PUZZLE_TEXTURE;

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

        //reset image(s)
        if (Board.IMAGE_SOURCE != null) {
            Board.IMAGE_SOURCE.recycle();
            Board.IMAGE_SOURCE = null;
        }

        if (PUZZLE_TEXTURE != null) {
            PUZZLE_TEXTURE.recycle();
            PUZZLE_TEXTURE = null;
        }

        //open up image picker to use a custom image
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 1);
    }

    @Override
    public void onResume() {

        //call parent
        super.onResume();

        /*
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
        */
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if (requestCode == SELECT_IMAGE)

        if (resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();

            try {
                Board.IMAGE_SOURCE = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (Exception e) {
                UtilityHelper.handleException(e);
            }

            // TODO Do something with the select image URI
        }

        //start the new activity
        startActivity(new Intent(OtherActivity.this, GameActivity.class));

        //close the activity
        finish();
    }

    @Override
    public void onBackPressed() {

        //don't allow user to press back button
        return;
    }
}