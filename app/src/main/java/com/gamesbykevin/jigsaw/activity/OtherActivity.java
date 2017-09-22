package com.gamesbykevin.jigsaw.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.gamesbykevin.jigsaw.R;
import com.gamesbykevin.jigsaw.board.Board;
import com.gamesbykevin.jigsaw.util.UtilityHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static com.gamesbykevin.jigsaw.board.BoardHelper.PUZZLE_TEXTURE;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.HEIGHT;
import static com.gamesbykevin.jigsaw.opengl.OpenGLSurfaceView.WIDTH;

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //call parent
        super.onActivityResult(requestCode, resultCode, data);

        //if everything is ok
        if (resultCode == Activity.RESULT_OK) {

            try {

                //get the location of the image
                Uri selectedImage = data.getData();

                //store the image location
                Board.IMAGE_LOCATION = selectedImage.toString();

                //retrieve the bitmap from the uri location
                Board.IMAGE_SOURCE = getBitmapImage(this, selectedImage);

                //start the new activity
                startActivity(new Intent(OtherActivity.this, ConfirmActivity.class));

            } catch (Exception e) {

                UtilityHelper.handleException(e);

                //any issue we go back to the previous page
                super.onBackPressed();
            }

        } else {

            //any issue we can go back to the previous page
            super.onBackPressed();
        }

        //close this activity
        finish();
    }

    public static Bitmap getBitmapImage(Activity activity, String location) throws Exception {
        return getBitmapImage(activity, Uri.parse(location));
    }

    public static Bitmap getBitmapImage(Activity activity, Uri selectedImage) throws Exception {

        //load the image
        Bitmap tmp = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), selectedImage);

        float imgSrcHeight = tmp.getHeight();
        float imgSrcWidth = tmp.getWidth();

        //if too large, resize the image
        if (imgSrcHeight > HEIGHT || imgSrcWidth > WIDTH) {

            //get the size ratio
            final float ratio = imgSrcHeight / imgSrcWidth;

            //new image dimensions
            int imageWidth;
            int imageHeight;

            if (ratio >= 1) {

                imageWidth = (int) (HEIGHT * (imgSrcWidth / imgSrcHeight));
                imageHeight = HEIGHT;

            } else {

                imageWidth = HEIGHT;
                imageHeight = (int) (HEIGHT * (imgSrcHeight / imgSrcWidth));

            }

            //resize the image
            tmp = Bitmap.createScaledBitmap(tmp, imageWidth, imageHeight, false);
        }

        //output stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        //compress the file so it won't cause a performance issue and write to output stream
        tmp.compress(Bitmap.CompressFormat.PNG, 25, out);

        //clean up resources
        tmp.recycle();
        tmp = null;

        //return our result
        return BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
    }

    @Override
    public void onBackPressed() {

        //call parent
        super.onBackPressed();
    }
}